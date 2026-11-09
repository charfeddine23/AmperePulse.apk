package com.example.data.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Real Production Firebase Auth & Firestore Synchronization Repository.
 * Authenticates users exclusively via Google Sign-In with Jetpack CredentialManager,
 * and maintains 24/7 cloud sync with the designated Firestore database.
 */
class FirebaseAuthRepository(private val context: Context) {

    private val auth: FirebaseAuth = Firebase.auth
    private val databaseId = try {
        context.getString(R.string.firestore_database_id)
    } catch (e: Exception) {
        "ai-studio-android-amperepu-21743d81-61e9-4757-9e7a-f60b24b9b903"
    }

    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(databaseId)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        auth.addAuthStateListener { fbAuth ->
            _currentUser.value = fbAuth.currentUser
            fbAuth.currentUser?.let { user ->
                syncUserProfile(user)
            }
        }
    }

    fun getWebClientId(): String {
        return try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            "696252497383-ih22s0p4c73096sdt5cdopdj1epco79g.apps.googleusercontent.com"
        }
    }

    /**
     * Interactive Google Sign-In using CredentialManager and GetSignInWithGoogleOption.
     */
    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        _authError.value = null
        val clientId = getWebClientId()
        val credentialManager = CredentialManager.create(activity)

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        syncUserProfile(user)
                        onSuccess(user)
                    } else {
                        onError("Firebase user is null after Google authentication.")
                    }
                } else {
                    val msg = "Unexpected credential format received from Google Play Services."
                    _authError.value = msg
                    onError(msg)
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("FirebaseAuth", "Google Sign-In dismissed by user: ${e.message}")
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Google Sign-In failed"
                Log.e("FirebaseAuth", "Google Sign-In error", e)
                _authError.value = errorMsg
                onError(errorMsg)
            }
        }
    }

    /**
     * Silent Auto-Sign-In on App Launch using GetGoogleIdOption.
     */
    fun attemptSilentSignIn(scope: CoroutineScope) {
        if (auth.currentUser != null) return

        val clientId = getWebClientId()
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch(Dispatchers.IO) {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                }
            } catch (e: Exception) {
                // Silent sign-in gracefully ignored if no cached account
            }
        }
    }

    /**
     * Sign out and clear CredentialManager state.
     */
    fun signOut(scope: CoroutineScope, onComplete: () -> Unit = {}) {
        val credentialManager = CredentialManager.create(context)
        scope.launch(Dispatchers.IO) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("FirebaseAuth", "Failed to clear credential manager state: ${e.message}")
            } finally {
                auth.signOut()
                _currentUser.value = null
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    /**
     * Synchronize user profile into Firestore under users/{userId}.
     */
    fun syncUserProfile(user: FirebaseUser) {
        val uid = user.uid
        val docRef = firestore.collection("users").document(uid)
        val profileData = hashMapOf(
            "userId" to uid,
            "displayName" to (user.displayName ?: "Pulse Explorer"),
            "email" to (user.email ?: ""),
            "photoUrl" to (user.photoUrl?.toString() ?: ""),
            "isCloudSyncActive" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        docRef.set(profileData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FirebaseAuth", "User profile synchronized successfully in Firestore: $uid")
            }
            .addOnFailureListener { e ->
                Log.w("FirebaseAuth", "Failed to write user profile to Firestore: ${e.message}")
            }
    }

    /**
     * Push real-time telemetry log to Firestore under users/{userId}/telemetry_logs/{logId}.
     */
    suspend fun pushTelemetrySnapshot(
        voltageMv: Int,
        currentMa: Int,
        temperatureC: Float,
        batteryPct: Int,
        state: String
    ) {
        val user = auth.currentUser ?: return
        val uid = user.uid

        val logDoc = firestore.collection("users")
            .document(uid)
            .collection("telemetry_logs")
            .document()

        val logData = hashMapOf(
            "userId" to uid,
            "timestamp" to FieldValue.serverTimestamp(),
            "voltageMv" to voltageMv,
            "currentMa" to currentMa,
            "temperatureC" to temperatureC,
            "batteryPct" to batteryPct,
            "state" to state,
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            _isSyncing.value = true
            logDoc.set(logData).await()
        } catch (e: Exception) {
            Log.w("FirebaseAuth", "Failed to sync telemetry snapshot: ${e.message}")
        } finally {
            _isSyncing.value = false
        }
    }
}
