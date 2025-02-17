package com.example.novenaappstore

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

object ApkInstaller {
    fun installApk(context: Context, fileName: String) {
        //requestInstallPermission(context)

        try {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )

            if (!file.exists()) {
                Log.e("ApkInstaller", "APK file not found!")
                return
            }

            val packageInstaller = context.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)

            val inputStream = file.inputStream()
            val outputStream = session.openWrite("package_install", 0, -1)

            inputStream.copyTo(outputStream)
            session.fsync(outputStream)
            outputStream.close()
            inputStream.close()

            // Commit the session to install the APK
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                Intent(Intent.ACTION_INSTALL_PACKAGE),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            session.commit(pendingIntent.intentSender)

//            val apkUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
//            } else {
//                Uri.fromFile(file)
//            }
//
//            val intent = Intent(Intent.ACTION_VIEW).apply {
//                setDataAndType(apkUri, "application/vnd.android.package-archive")
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
//            }
//
//            context.startActivity(intent)

        } catch (e: Exception) {
            Log.e("ApkInstaller", "Installation failed: ${e.message}")
        }

    }

//    private fun requestInstallPermission(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            if (!context.packageManager.canRequestPackageInstalls()) {
//                val intent = Intent(
//                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
//                    Uri.parse("package:${context.packageName}")
//                )
//                context.startActivity(intent)
//            }
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (!context.packageManager.canRequestPackageInstalls()) {
//                val intent = Intent(
//                    Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
//                )
//                context.startActivity(intent)
//            }
//        }
//    }
}