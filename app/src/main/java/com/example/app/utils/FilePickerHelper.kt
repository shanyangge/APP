package com.example.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object FilePickerHelper {
    fun createImagePickerIntent(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
    }

    fun createAudioPickerIntent(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
    }

    fun createVideoPickerIntent(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
    }

    fun copyFileToInternalStorage(context: Context, uri: Uri, fileName: String): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val outputFile = context.getFileStreamPath(fileName)

        inputStream?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return outputFile.absolutePath
    }
}