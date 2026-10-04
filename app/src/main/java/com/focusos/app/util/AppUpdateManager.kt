package com.focusos.app.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersionCode: Int,
    val latestVersionName: String,
    val changelog: String,
    val downloadUrl: String
)

object AppUpdateManager {

    const val CURRENT_VERSION_CODE = 3
    const val CURRENT_VERSION_NAME = "1.0.2"

    // GitHub raw version endpoint
    private const val VERSION_CHECK_URL =
        "https://raw.githubusercontent.com/rg-ui/focus-os-/main/version.json"

    private val client = OkHttpClient()

    suspend fun checkForUpdates(): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(VERSION_CHECK_URL)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val remoteCode = json.optInt("versionCode", CURRENT_VERSION_CODE)
                    val remoteName = json.optString("versionName", CURRENT_VERSION_NAME)
                    val changelog = json.optString("changelog", "Bug fixes and performance improvements.")
                    val downloadUrl = json.optString(
                        "downloadUrl",
                        "https://raw.githubusercontent.com/rg-ui/focus-os-/main/FocusOS-v1.0.apk"
                    )

                    val hasUpdate = remoteCode > CURRENT_VERSION_CODE
                    Result.success(
                        UpdateInfo(
                            hasUpdate = hasUpdate,
                            latestVersionCode = remoteCode,
                            latestVersionName = remoteName,
                            changelog = changelog,
                            downloadUrl = downloadUrl
                        )
                    )
                } else {
                    Result.failure(Exception("Failed to check version: HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun startDownloadAndInstall(context: Context, downloadUrl: String, onProgress: (String) -> Unit) {
        onProgress("Downloading update in background...")

        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle("Focus OS Update (v1.0.2)")
            .setDescription("Downloading latest version with updated AI Mentor...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "FocusOS-update.apk")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    installApk(ctxt)
                    try {
                        ctxt.unregisterReceiver(this)
                    } catch (e: Exception) {}
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED
            )
        } else {
            context.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }

    fun installApk(context: Context) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "FocusOS-update.apk"
        )
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        }
    }
}
