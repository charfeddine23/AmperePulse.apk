package com.example.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.example.BuildConfig
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

data class InAppUpdateUiState(
    val isChecking: Boolean = false,
    val isDialogVisible: Boolean = false,
    val updateAvailable: Boolean = false,
    val isDownloading: Boolean = false,
    val isDownloadedReadyToInstall: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadedMb: Float = 0f,
    val totalMb: Float = 0f,
    val currentVersionName: String = BuildConfig.VERSION_NAME,
    val currentVersionCode: Int = BuildConfig.VERSION_CODE,
    val availableVersionName: String? = null,
    val availableVersionCode: Int? = null,
    val downloadUrl: String = InAppUpdateEngine.GITHUB_LATEST_RELEASE_URL,
    val apkFileName: String? = null,
    val apkSizeMb: Float? = null,
    val statusBadgeText: String = "تحديث جديد",
    val headlineText: String = "تحديث جديد متاح",
    val descriptionText: String = "يتوفر تحديث جديد للتطبيق جاهز للتنزيل والتثبيت المباشر.",
    val lastCheckSummary: String = "الإصدار الحالي v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
    val hasPlayUpdatePayload: Boolean = false
)

/**
 * Automatic In-App Update Engine wired to GitHub Releases
 * (https://github.com/charfeddine23/Ampere) with Google Play AppUpdateManager support.
 * Opens the signed release APK URL directly via Intent.ACTION_VIEW for compliant in-place updates.
 */
class InAppUpdateEngine(private val context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(appContext)
    }

    private var cachedAppUpdateInfo: AppUpdateInfo? = null
    private var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    private val _uiState = MutableStateFlow(InAppUpdateUiState())
    val uiState: StateFlow<InAppUpdateUiState> = _uiState.asStateFlow()

    private val installStateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val downloaded = state.bytesDownloaded().coerceAtLeast(0L)
                val total = state.totalBytesToDownload().coerceAtLeast(1L)
                val fraction = (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                _uiState.value = _uiState.value.copy(
                    isDownloading = true,
                    isDownloadedReadyToInstall = false,
                    downloadProgress = fraction,
                    downloadedMb = downloaded / (1024f * 1024f),
                    totalMb = total / (1024f * 1024f),
                    statusBadgeText = "جاري التنزيل • ${(fraction * 100).toInt()}%",
                    descriptionText = "يتم تنزيل التحديث التلقائي الآن..."
                )
            }
            InstallStatus.DOWNLOADED -> {
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    isDownloadedReadyToInstall = true,
                    isDialogVisible = true,
                    downloadProgress = 1f,
                    statusBadgeText = "جاهز للتثبيت",
                    headlineText = "اكتمل تنزيل التحديث",
                    descriptionText = "التحديث جاهز للتثبيت المباشر فوق النسخة الحالية."
                )
            }
            InstallStatus.INSTALLED -> {
                unregisterListener()
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    isDownloadedReadyToInstall = false,
                    isDialogVisible = false,
                    updateAvailable = false
                )
            }
            InstallStatus.FAILED,
            InstallStatus.CANCELED -> {
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    downloadProgress = 0f,
                    statusBadgeText = "تحديث جديد",
                    descriptionText = "تعذر إكمال التنزيل. يمكنك المحاولة مرة أخرى."
                )
            }
            else -> Unit
        }
    }

    fun attachLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        activityResultLauncher = launcher
    }

    fun registerListener() {
        try {
            appUpdateManager.registerListener(installStateListener)
        } catch (_: Exception) {}
    }

    fun unregisterListener() {
        try {
            appUpdateManager.unregisterListener(installStateListener)
        } catch (_: Exception) {}
    }

    /**
     * Checks GitHub Latest Release API (https://api.github.com/repos/charfeddine23/Ampere/releases/latest)
     * and Play Core AppUpdateManager for any newer build.
     */
    fun checkForUpdates(
        autoShowOnAvailable: Boolean = true,
        openDialogImmediatelyOnManual: Boolean = false
    ) {
        if (_uiState.value.isChecking) return
        _uiState.value = _uiState.value.copy(
            isChecking = true,
            lastCheckSummary = "جاري الفحص عبر GitHub Releases..."
        )

        scope.launch {
            val githubResult = fetchLatestGithubRelease()
            if (githubResult != null) {
                val isNewer = compareSemanticVersions(githubResult.versionName, BuildConfig.VERSION_NAME) > 0
                if (isNewer) {
                    val sizeLabel = githubResult.apkSizeMb?.let {
                        String.format(Locale.US, " (%.1f MB)", it)
                    } ?: ""
                    _uiState.value = _uiState.value.copy(
                        isChecking = false,
                        updateAvailable = true,
                        isDialogVisible = autoShowOnAvailable || openDialogImmediatelyOnManual,
                        availableVersionName = githubResult.versionName,
                        downloadUrl = githubResult.downloadUrl,
                        apkFileName = githubResult.apkFileName,
                        apkSizeMb = githubResult.apkSizeMb,
                        hasPlayUpdatePayload = false,
                        statusBadgeText = "تحديث جديد • v${githubResult.versionName}",
                        headlineText = "تحديث جديد متاح",
                        descriptionText = "الإصدار v${githubResult.versionName}$sizeLabel جاهز للتثبيت المباشر فوق النسخة الحالية بدون حذف التطبيق.",
                        lastCheckSummary = "تحديث متاح: v${githubResult.versionName} (${githubResult.apkFileName ?: "GitHub Release"})"
                    )
                    return@launch
                } else {
                    _uiState.value = _uiState.value.copy(
                        isChecking = false,
                        updateAvailable = false,
                        availableVersionName = githubResult.versionName,
                        downloadUrl = githubResult.downloadUrl,
                        apkFileName = githubResult.apkFileName,
                        apkSizeMb = githubResult.apkSizeMb,
                        hasPlayUpdatePayload = false,
                        isDialogVisible = openDialogImmediatelyOnManual,
                        statusBadgeText = "محدّث • v${BuildConfig.VERSION_NAME}",
                        headlineText = if (openDialogImmediatelyOnManual) "أنت على أحدث إصدار" else "تحديث جديد متاح",
                        descriptionText = if (openDialogImmediatelyOnManual) {
                            "أنت تستخدم أحدث إصدار من Ampere Pulse (v${BuildConfig.VERSION_NAME})."
                        } else {
                            "تحديث جديد متاح"
                        },
                        lastCheckSummary = "محدّث لأحدث إصدار (v${githubResult.versionName})"
                    )
                    return@launch
                }
            }

            // Fallback to Google Play In-App Updates if GitHub API is unreachable/rate-limited
            checkPlayInAppUpdateFallback(autoShowOnAvailable, openDialogImmediatelyOnManual)
        }
    }

    private data class GithubReleaseInfo(
        val versionName: String,
        val downloadUrl: String,
        val apkFileName: String?,
        val apkSizeMb: Float?
    )

    private fun fetchLatestGithubRelease(): GithubReleaseInfo? {
        return try {
            val request = Request.Builder()
                .url(GITHUB_LATEST_RELEASE_API)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "AmperePulse-Android/${BuildConfig.VERSION_NAME}")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val json = JSONObject(body)

                val rawTag = json.optString("tag_name", "").trim()
                val versionName = rawTag.removePrefix("v").removePrefix("V")
                    .substringBefore("-")
                    .ifBlank { return null }

                val htmlUrl = json.optString("html_url", GITHUB_LATEST_RELEASE_URL)
                var apkDownloadUrl: String? = null
                var apkName: String? = null
                var apkSizeMb: Float? = null

                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.optJSONObject(i) ?: continue
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkName = name
                            apkDownloadUrl = asset.optString("browser_download_url", htmlUrl)
                            val bytes = asset.optLong("size", 0L)
                            if (bytes > 0L) {
                                apkSizeMb = bytes / (1024f * 1024f)
                            }
                            break
                        }
                    }
                }

                GithubReleaseInfo(
                    versionName = versionName,
                    downloadUrl = apkDownloadUrl ?: htmlUrl,
                    apkFileName = apkName,
                    apkSizeMb = apkSizeMb
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun checkPlayInAppUpdateFallback(
        autoShowOnAvailable: Boolean,
        openDialogImmediatelyOnManual: Boolean
    ) {
        try {
            appUpdateManager.appUpdateInfo
                .addOnSuccessListener { info ->
                    cachedAppUpdateInfo = info
                    val availability = info.updateAvailability()
                    val installStatus = info.installStatus()

                    when {
                        installStatus == InstallStatus.DOWNLOADED -> {
                            _uiState.value = _uiState.value.copy(
                                isChecking = false,
                                updateAvailable = true,
                                isDownloadedReadyToInstall = true,
                                isDialogVisible = true,
                                availableVersionCode = info.availableVersionCode(),
                                hasPlayUpdatePayload = true,
                                statusBadgeText = "جاهز للتثبيت",
                                headlineText = "اكتمل تنزيل التحديث",
                                descriptionText = "اضغط على تحديث لإكمال التثبيت وإعادة تشغيل التطبيق.",
                                lastCheckSummary = "تحديث جاهز للتثبيت (Build ${info.availableVersionCode()})"
                            )
                        }
                        availability == UpdateAvailability.UPDATE_AVAILABLE -> {
                            val verCode = info.availableVersionCode()
                            _uiState.value = _uiState.value.copy(
                                isChecking = false,
                                updateAvailable = true,
                                isDialogVisible = autoShowOnAvailable || openDialogImmediatelyOnManual,
                                availableVersionCode = verCode,
                                hasPlayUpdatePayload = true,
                                statusBadgeText = "تحديث جديد • Build $verCode",
                                headlineText = "تحديث جديد متاح",
                                descriptionText = "يتوفر تحديث جديد لتحسين دقة القياس واستقرار النظام.",
                                lastCheckSummary = "يتوفر تحديث جديد (Build $verCode)"
                            )
                        }
                        else -> {
                            _uiState.value = _uiState.value.copy(
                                isChecking = false,
                                updateAvailable = false,
                                hasPlayUpdatePayload = false,
                                downloadUrl = GITHUB_LATEST_RELEASE_URL,
                                isDialogVisible = openDialogImmediatelyOnManual,
                                statusBadgeText = "محدّث • v${BuildConfig.VERSION_NAME}",
                                headlineText = if (openDialogImmediatelyOnManual) "أنت على أحدث إصدار" else "تحديث جديد متاح",
                                descriptionText = "الإصدار الحالي v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE}).",
                                lastCheckSummary = "الإصدار الحالي v${BuildConfig.VERSION_NAME}"
                            )
                        }
                    }
                }
                .addOnFailureListener {
                    cachedAppUpdateInfo = null
                    _uiState.value = _uiState.value.copy(
                        isChecking = false,
                        hasPlayUpdatePayload = false,
                        downloadUrl = GITHUB_LATEST_RELEASE_URL,
                        isDialogVisible = openDialogImmediatelyOnManual,
                        statusBadgeText = "v${BuildConfig.VERSION_NAME} • GitHub Releases",
                        headlineText = "تحديث جديد متاح",
                        descriptionText = "يمكنك تنزيل أحدث إصدار مباشرة من GitHub Releases وتثبيته فوق النسخة الحالية.",
                        lastCheckSummary = "تعذر الاتصال • الإصدار الحالي v${BuildConfig.VERSION_NAME}"
                    )
                }
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(
                isChecking = false,
                downloadUrl = GITHUB_LATEST_RELEASE_URL,
                lastCheckSummary = "الإصدار الحالي v${BuildConfig.VERSION_NAME}"
            )
        }
    }

    fun openUpdateDialogPreview() {
        val current = _uiState.value
        val targetLabel = current.availableVersionName ?: current.currentVersionName
        _uiState.value = current.copy(
            isDialogVisible = true,
            statusBadgeText = if (current.updateAvailable) {
                "تحديث جديد • v$targetLabel"
            } else {
                "تحديث جديد • GitHub Releases"
            },
            headlineText = "تحديث جديد متاح",
            descriptionText = if (current.updateAvailable) {
                "الإصدار v$targetLabel جاهز للتنزيل والتثبيت المباشر فوق النسخة الحالية."
            } else {
                "تحقق وحمّل أحدث إصدار من صفحة الإصدارات الرسمية لتثبيته مباشرة فوق التطبيق."
            }
        )
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(isDialogVisible = false)
    }

    fun startOrCompleteUpdate(hostContext: Context) {
        val current = _uiState.value
        if (current.isDownloadedReadyToInstall) {
            try {
                appUpdateManager.completeUpdate()
            } catch (_: Exception) {
                openDownloadUrl(hostContext, current.downloadUrl)
            }
            return
        }

        val info = cachedAppUpdateInfo
        if (current.hasPlayUpdatePayload && info != null && info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            val updateType = when {
                info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> AppUpdateType.FLEXIBLE
                info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
                else -> AppUpdateType.FLEXIBLE
            }
            val started = launchPlayUpdateFlow(info, updateType)
            if (started) return
        }

        // Open direct GitHub Releases APK download URL via Intent.ACTION_VIEW
        openDownloadUrl(hostContext, current.downloadUrl.ifBlank { GITHUB_LATEST_RELEASE_URL })
        dismissDialog()
    }

    private fun launchPlayUpdateFlow(info: AppUpdateInfo, updateType: Int): Boolean {
        val launcher = activityResultLauncher ?: return false
        return try {
            if (updateType == AppUpdateType.FLEXIBLE) {
                registerListener()
            }
            appUpdateManager.startUpdateFlowForResult(
                info,
                launcher,
                AppUpdateOptions.newBuilder(updateType).build()
            )
        } catch (_: Exception) {
            false
        }
    }

    fun onUpdateFlowResult(resultCode: Int) {
        if (resultCode != android.app.Activity.RESULT_OK) {
            _uiState.value = _uiState.value.copy(
                isDownloading = false,
                lastCheckSummary = "تم تأجيل التحديث — يمكنك التحديث في أي وقت من الإعدادات"
            )
        }
    }

    private fun openDownloadUrl(hostContext: Context, url: String) {
        val primaryIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_LATEST_RELEASE_URL)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            hostContext.startActivity(primaryIntent)
        } catch (_: Exception) {
            try {
                hostContext.startActivity(fallbackIntent)
            } catch (_: Exception) {}
        }
    }

    companion object {
        const val GITHUB_OWNER = "charfeddine23"
        const val GITHUB_REPO = "Ampere"
        const val GITHUB_LATEST_RELEASE_API =
            "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
        const val GITHUB_LATEST_RELEASE_URL =
            "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

        fun compareSemanticVersions(remote: String, current: String): Int {
            val remoteParts = remote.split(".").map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
            val currentParts = current.split(".").map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
            val maxLen = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r != c) return r.compareTo(c)
            }
            return 0
        }
    }
}
