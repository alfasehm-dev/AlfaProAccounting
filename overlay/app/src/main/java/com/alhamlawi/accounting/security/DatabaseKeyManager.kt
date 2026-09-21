package com.alhamlawi.accounting.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class DatabaseKeyManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("crypto_keys", Context.MODE_PRIVATE)
    private val alias = "alfa_pro_master_key"
    fun databasePassphrase(): ByteArray {
        val stored = prefs.getString("db_key", null)
        if (stored != null) return decrypt(stored)
        val raw = ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }
        prefs.edit().putString("db_key", encrypt(raw)).apply()
        return raw
    }
    fun encryptBytes(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        return cipher.iv + cipher.doFinal(data)
    }
    fun decryptBytes(data: ByteArray): ByteArray {
        require(data.size > 12) { "بيانات مشفرة غير صالحة" }
        val iv = data.copyOfRange(0, 12)
        val payload = data.copyOfRange(12, data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(payload)
    }
    private fun encrypt(data: ByteArray): String = Base64.encodeToString(encryptBytes(data), Base64.NO_WRAP)
    private fun decrypt(value: String): ByteArray = decryptBytes(Base64.decode(value, Base64.NO_WRAP))
    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build())
        return generator.generateKey()
    }
}
