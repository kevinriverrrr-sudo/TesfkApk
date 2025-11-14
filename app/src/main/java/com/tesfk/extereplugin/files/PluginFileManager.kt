package com.tesfk.extereplugin.files

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.tesfk.extereplugin.data.PluginBlueprint
import com.tesfk.extereplugin.data.PluginSerializer
import java.io.File
import java.io.IOException

class PluginFileManager(private val context: Context) {
    private val pluginsDir: File by lazy {
        File(context.filesDir, "plugins").apply { mkdirs() }
    }

    fun writePluginFile(blueprint: PluginBlueprint): File {
        val safeName = blueprint.safeFileName()
        val file = File(pluginsDir, "${safeName}-${System.currentTimeMillis()}.plugin")
        try {
            file.writeText(PluginSerializer.serialize(blueprint))
        } catch (error: IOException) {
            if (file.exists()) file.delete()
            throw error
        }
        return file
    }

    fun shareIntent(file: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun listRecentFiles(): List<File> =
        pluginsDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
}
