package com.example.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.FileProvider
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

data class InAppUpdateUiState(
    val isChecking: Boolean = false,
    val isDialogVisible: Boolean = false,
    val updateAvailable: Boolean = false,
    val isDownloading: Boolean = false,
    val isDownloadedReadyToInstall: Boolean = false,
    val needsInstallPermission: Boolean = false,
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
    val downloadedApkPath: String? = null,
    val statusBadgeText: String = "تحديث جديد",
    val headlineText: String = "تحديث جديد متاح",
    val descriptionText: String = "يتوفر تحديث جديد للتطبيق جاهز للتنزيل والتثبيت المباشر من الداخل.",
    val lastCheckSummary: String = "الإصدار الحالي v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
    val hasPlayUpdatePayload: Boolean = false
)

/**
 * Fully In-App Update Engine wired to GitHub Releases
 * (https://github.com/charfeddine23/Ampere) with native OkHttp streaming download,
 * FileProvider local PackageInstaller launch (no external browser), and Play Core support.
 */
class InAppUpdateEngine(private val context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeDownloadJob: Job? = null

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
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
                    descriptionText = "يتم تنزيل التحديث الآن داخل التطبيق...",
                    lastCheckSummary = "جاري التنزيل داخلياً (${(fraction * 100).toInt()}%)"
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
                    descriptionText = "التحديث جاهز للتثبيت المباشر فوق النسخة الحالية بدون حذف التطبيق.",
                    lastCheckSummary = "اكتمل التنزيل • جاهز للتثبيت المباشر"
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

    init {
        cleanupOldCachedApks()
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
     * Checks GitHub Latest Release API for newer signed APK builds.
     * When [autoDownloadIfFound] is true and a newer direct .apk asset exists,
     * begins downloading inside the app automatically with no external apps.
     */
    fun checkForUpdates(
        autoShowOnAvailable: Boolean = true,
        openDialogImmediatelyOnManual: Boolean = false,
        autoDownloadIfFound: Boolean = false
    ) {
        if (_uiState.value.isChecking || _uiState.value.isDownloading) return

        // If an APK was already downloaded and is awaiting permission or install, check permission state
        val existingApkPath = _uiState.value.downloadedApkPath
        if (_uiState.value.isDownloadedReadyToInstall && existingApkPath != null && File(existingApkPath).exists()) {
            val canInstall = canInstallPackages()
            _uiState.value = _uiState.value.copy(
                needsInstallPermission = !canInstall,
                lastCheckSummary = if (canInstall) "جاهز للتثبيت المباشر" else "فعّل إذن التثبيت لإكمال التحديث"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isChecking = true,
            lastCheckSummary = "جاري البحث عن تحديثات..."
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
                        descriptionText = "الإصدار v${githubResult.versionName}$sizeLabel متاح للتنزيل والتثبيت المباشر داخل التطبيق.",
                        lastCheckSummary = "يتوفر تحديث: v${githubResult.versionName}$sizeLabel"
                    )

                    if (autoDownloadIfFound && githubResult.downloadUrl.contains(".apk", ignoreCase = true)) {
                        startInAppApkDownload(
                            url = githubResult.downloadUrl,
                            fileName = githubResult.apkFileName ?: "Ampere.Pulse.v${githubResult.versionName}.apk",
                            expectedSizeMb = githubResult.apkSizeMb,
                            hostContext = appContext
                        )
                    }
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
                            "أنت تستخدم أحدث إصدار متاح من Ampere Pulse (v${BuildConfig.VERSION_NAME})."
                        } else {
                            "تحديث جديد متاح"
                        },
                        lastCheckSummary = "أنت على أحدث إصدار (v${githubResult.versionName})"
                    )
                    return@launch
                }
            }

            // Secondary fallback: Google Play In-App Updates API
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
                                descriptionText = "يتوفر تحديث جديد متاح للتنزيل من داخل التطبيق.",
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
                                lastCheckSummary = "الإصدار الحالي v${BuildConfig.VERSION_NAME} • محدّث"
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
                        headlineText = "التحديث التلقائي",
                        descriptionText = "تعذر الاتصال بخادم التحديثات الآن. تأكد من الاتصال بالإنترنت وأعد المحاولة.",
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
            statusBadgeText = when {
                current.isDownloadedReadyToInstall -> "جاهز للتثبيت"
                current.isDownloading -> "جاري التنزيل • ${(current.downloadProgress * 100).toInt()}%"
                current.updateAvailable -> "تحديث جديد • v$targetLabel"
                else -> "تحديث جديد • v$targetLabel"
            },
            headlineText = when {
                current.isDownloadedReadyToInstall -> "اكتمل تنزيل التحديث"
                current.updateAvailable -> "تحديث جديد متاح"
                else -> "تحديث جديد متاح"
            },
            descriptionText = when {
                current.isDownloadedReadyToInstall -> "تم التنزيل داخل التطبيق بنجاح وجاهز للتثبيت المباشر."
                current.updateAvailable -> "الإصدار v$targetLabel جاهز للتنزيل والتثبيت مباشرة داخل التطبيق."
                else -> "اضغط تحديث لفحص وتحميل أحدث نسخة مباشرة داخل التطبيق دون مغادرته."
            }
        )
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(isDialogVisible = false)
    }

    fun startOrCompleteUpdate(hostContext: Context) {
        val current = _uiState.value
        if (current.isDownloading) return

        // 1. Ready to install locally downloaded APK
        val apkPath = current.downloadedApkPath
        if (current.isDownloadedReadyToInstall && apkPath != null) {
            val apkFile = File(apkPath)
            if (apkFile.exists()) {
                launchLocalApkInstaller(hostContext, apkFile)
                return
            }
        }

        // 2. Ready to complete Play Core flexible update
        if (current.isDownloadedReadyToInstall && current.hasPlayUpdatePayload) {
            try {
                appUpdateManager.completeUpdate()
            } catch (_: Exception) {}
            return
        }

        // 3. Play Store Update flow
        val info = cachedAppUpdateInfo
        if (current.hasPlayUpdatePayload && info != null && info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            val updateType = when {
                info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> AppUpdateType.FLEXIBLE
                info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
                else -> AppUpdateType.FLEXIBLE
            }
            if (launchPlayUpdateFlow(info, updateType)) return
        }

        // 4. Direct In-App APK download from GitHub Releases (no external apps/browser!)
        if (current.updateAvailable && current.downloadUrl.contains(".apk", ignoreCase = true)) {
            val targetName = current.apkFileName ?: "Ampere.Pulse.v${current.availableVersionName ?: "latest"}.apk"
            startInAppApkDownload(
                url = current.downloadUrl,
                fileName = targetName,
                expectedSizeMb = current.apkSizeMb,
                hostContext = hostContext
            )
            return
        }

        // 5. Otherwise run a live check & auto-download inside the app if found
        checkForUpdates(
            autoShowOnAvailable = true,
            openDialogImmediatelyOnManual = true,
            autoDownloadIfFound = true
        )
    }

    private fun startInAppApkDownload(
        url: String,
        fileName: String,
        expectedSizeMb: Float?,
        hostContext: Context
    ) {
        activeDownloadJob?.cancel()
        activeDownloadJob = scope.launch {
            val updatesDir = File(appContext.cacheDir, "updates").apply { mkdirs() }
            val safeFileName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val targetFile = File(updatesDir, safeFileName)
            val tempFile = File(updatesDir, "$safeFileName.part")

            _uiState.value = _uiState.value.copy(
                isDownloading = true,
                isDownloadedReadyToInstall = false,
                needsInstallPermission = false,
                downloadProgress = 0f,
                downloadedMb = 0f,
                totalMb = expectedSizeMb ?: 0f,
                headlineText = "جاري تحميل التحديث...",
                statusBadgeText = "تنزيل داخلي • 0%",
                descriptionText = "يتم تحميل التحديث داخل التطبيق مباشرة بدون تطبيقات خارجية...",
                lastCheckSummary = "جاري التنزيل داخل التطبيق (0%)"
            )

            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "AmperePulse-Android/${BuildConfig.VERSION_NAME}")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IllegalStateException("HTTP ${response.code}")
                    }
                    val body = response.body ?: throw IllegalStateException("Empty body")
                    val totalBytes = body.contentLength().let { len ->
                        if (len > 0L) len else ((expectedSizeMb ?: 0f) * 1024f * 1024f).toLong()
                    }
                    val totalMbVal = if (totalBytes > 0L) totalBytes / (1024f * 1024f) else (expectedSizeMb ?: 0f)

                    body.byteStream().use { input ->
                        FileOutputStream(tempFile).use { output ->
                            val buffer = ByteArray(16 * 1024)
                            var bytesReadTotal = 0L
                            var lastUiEmitTime = 0L

                            while (isActive) {
                                val read = input.read(buffer)
                                if (read == -1) break
                                output.write(buffer, 0, read)
                                bytesReadTotal += read

                                val now = System.currentTimeMillis()
                                if (now - lastUiEmitTime >= 80L) {
                                    lastUiEmitTime = now
                                    val fraction = if (totalBytes > 0L) {
                                        (bytesReadTotal.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                                    } else 0.5f
                                    val downloadedMbVal = bytesReadTotal / (1024f * 1024f)
                                    val pct = (fraction * 100).toInt()
                                    _uiState.value = _uiState.value.copy(
                                        downloadProgress = fraction,
                                        downloadedMb = downloadedMbVal,
                                        totalMb = totalMbVal,
                                        statusBadgeText = "تنزيل داخلي • $pct%",
                                        lastCheckSummary = "جاري التحميل داخلياً: $pct% (${String.format(Locale.US, "%.1f", downloadedMbVal)} MB)"
                                    )
                                }
                            }
                            output.flush()
                        }
                    }
                }

                if (targetFile.exists()) targetFile.delete()
                if (!tempFile.renameTo(targetFile)) {
                    tempFile.copyTo(targetFile, overwrite = true)
                    tempFile.delete()
                }

                val canInstall = canInstallPackages()
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    isDownloadedReadyToInstall = true,
                    needsInstallPermission = !canInstall,
                    downloadProgress = 1f,
                    downloadedApkPath = targetFile.absolutePath,
                    headlineText = "اكتمل تنزيل التحديث",
                    statusBadgeText = if (canInstall) "جاهز للتثبيت المباشر" else "يتطلب إذن التثبيت",
                    descriptionText = if (canInstall) {
                        "تم تحميل التحديث داخل التطبيق بنجاح وجاهز للتثبيت مباشرة فوق النسخة الحالية."
                    } else {
                        "تم التحميل بنجاح. فعّل خيار السماح بالتثبيت من هذا المصدر لإكمال التحديث."
                    },
                    lastCheckSummary = if (canInstall) "اكتمل التنزيل • جاهز للتثبيت المباشر" else "تم التنزيل • فعّل إذن التثبيت"
                )

                withContext(Dispatchers.Main) {
                    launchLocalApkInstaller(hostContext, targetFile)
                }
            } catch (ce: kotlinx.coroutines.CancellationException) {
                runCatching { tempFile.delete() }
                throw ce
            } catch (_: Exception) {
                runCatching { tempFile.delete() }
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    downloadProgress = 0f,
                    headlineText = "تعذر تحميل التحديث",
                    statusBadgeText = "فشل التنزيل",
                    descriptionText = "تعذر تنزيل الحزمة داخل التطبيق الآن. تأكد من استقرار الشبكة وحاول مرة أخرى.",
                    lastCheckSummary = "تعذر تنزيل التحديث • أعد المحاولة"
                )
            }
        }
    }

    fun cancelInAppDownload() {
        activeDownloadJob?.cancel()
        activeDownloadJob = null
        _uiState.value = _uiState.value.copy(
            isDownloading = false,
            downloadProgress = 0f,
            headlineText = "تحديث جديد متاح",
            statusBadgeText = "تحديث جديد",
            descriptionText = "تم إيقاف التنزيل مؤقتاً. يمكنك استئنافه في أي وقت.",
            lastCheckSummary = "تم إلغاء التنزيل"
        )
    }

    private fun canInstallPackages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                appContext.packageManager.canRequestPackageInstalls()
            } catch (_: Exception) {
                true
            }
        } else {
            true
        }
    }

    private fun launchLocalApkInstaller(hostContext: Context, apkFile: File) {
        if (!apkFile.exists()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !canInstallPackages()) {
            _uiState.value = _uiState.value.copy(needsInstallPermission = true)
            try {
                val permIntent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${appContext.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                hostContext.startActivity(permIntent)
            } catch (_: Exception) {}
            return
        }

        try {
            val contentUri = FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                apkFile
            )
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            hostContext.startActivity(installIntent)
        } catch (_: Exception) {}
    }

    private fun cleanupOldCachedApks() {
        scope.launch {
            try {
                val updatesDir = File(appContext.cacheDir, "updates")
                updatesDir.listFiles()?.forEach { file ->
                    if (file.isFile && (file.name.endsWith(".part") || System.currentTimeMillis() - file.lastModified() > 24 * 3600_000L)) {
                        file.delete()
                    }
                }
            } catch (_: Exception) {}
        }
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
