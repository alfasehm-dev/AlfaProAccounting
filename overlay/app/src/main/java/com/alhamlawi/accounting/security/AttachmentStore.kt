package com.alhamlawi.accounting.security

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

class AttachmentStore(private val context: Context, private val keyManager: DatabaseKeyManager) {
    private val dir = File(context.filesDir, "encrypted_attachments").apply { mkdirs() }
    fun importUri(uriString: String): StoredAttachment {
        val uri = Uri.parse(uriString)
        val resolver = context.contentResolver
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: error("تعذر قراءة المرفق")
        val mime = resolver.getType(uri) ?: "application/octet-stream"
        val name = queryName(uri) ?: "attachment"
        return importBytes(name, mime, bytes)
    }
    fun importBytes(fileName: String, mimeType: String, bytes: ByteArray): StoredAttachment {
        val file = File(dir, UUID.randomUUID().toString() + ".enc")
        file.writeBytes(keyManager.encryptBytes(bytes))
        return StoredAttachment(fileName, mimeType, file.absolutePath)
    }
    fun read(path: String): ByteArray = keyManager.decryptBytes(File(path).readBytes())
    fun delete(path: String) { runCatching { File(path).delete() } }
    fun clearAll() { dir.listFiles()?.forEach { runCatching { it.delete() } } }
    private fun queryName(uri: Uri): String? = runCatching {
        context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    }.getOrNull()
}
data class StoredAttachment(val fileName: String, val mimeType: String, val encryptedPath: String)
