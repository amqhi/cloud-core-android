/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.amqhi.cloud.core

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class SecureStorageProvider(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val lock = Any()

    @Volatile
    private var secretKey: SecretKey? = null

    private fun getOrCreateKey(): SecretKey {
        secretKey?.let { return it }
        synchronized(lock) {
            secretKey?.let { return it }
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val existing = (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
            if (existing != null) {
                secretKey = existing
                return existing
            }
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            generator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
            return generator.generateKey().also { secretKey = it }
        }
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val payload = ByteArray(FORMAT_MAGIC.size + iv.size + ciphertext.size)
        FORMAT_MAGIC.copyInto(payload, destinationOffset = 0)
        iv.copyInto(payload, destinationOffset = FORMAT_MAGIC.size)
        ciphertext.copyInto(payload, destinationOffset = FORMAT_MAGIC.size + iv.size)
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun decrypt(encryptedValue: String): String {
        val decoded = Base64.decode(encryptedValue, Base64.NO_WRAP)
        if (!decoded.hasCurrentFormat()) {
            throw IllegalArgumentException("Unrecognized secure storage format")
        }
        val ivOffset = FORMAT_MAGIC.size
        val ciphertextOffset = ivOffset + GCM_IV_LENGTH
        val iv = decoded.copyOfRange(ivOffset, ciphertextOffset)
        val ciphertext = decoded.copyOfRange(ciphertextOffset, decoded.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(GCM_TAG_BITS, iv)
        )
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    private fun decryptLegacy(encryptedValue: String): String {
        val cipher = Cipher.getInstance(LEGACY_TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(LEGACY_SECRET_KEY_BYTES, "AES"),
            IvParameterSpec(LEGACY_IV_BYTES)
        )
        val decoded = Base64.decode(encryptedValue, Base64.NO_WRAP)
        return String(cipher.doFinal(decoded), Charsets.UTF_8)
    }

    private fun writeSecureString(key: String, value: String): Boolean {
        return prefs.edit().putString(key, encrypt(value)).commit()
    }

    fun setSecureString(key: String, value: String): Boolean {
        return try {
            synchronized(lock) {
                writeSecureString(key, value)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store secure string", e)
            false
        }
    }

    fun getSecureString(key: String, defaultValue: String): String {
        synchronized(lock) {
            val encrypted = prefs.getString(key, null) ?: return defaultValue
            return try {
                val decoded = Base64.decode(encrypted, Base64.NO_WRAP)
                if (decoded.hasCurrentFormat()) {
                    decrypt(encrypted)
                } else {
                    val legacy = decryptLegacy(encrypted)
                    try {
                        if (!writeSecureString(key, legacy)) {
                            Log.e(TAG, "Failed to migrate secure string")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to migrate secure string", e)
                    }
                    legacy
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read secure string", e)
                defaultValue
            }
        }
    }

    fun removeSecureString(key: String) {
        synchronized(lock) {
            if (!prefs.edit().remove(key).commit()) {
                Log.e(TAG, "Failed to remove secure string")
            }
        }
    }

    private fun ByteArray.hasCurrentFormat(): Boolean {
        if (size < FORMAT_MAGIC.size + GCM_IV_LENGTH + GCM_TAG_BYTES) return false
        for (index in FORMAT_MAGIC.indices) {
            if (this[index] != FORMAT_MAGIC[index]) return false
        }
        return true
    }

    private companion object {
        const val TAG = "SecureStorageProvider"
        const val PREFS_NAME = "amqhi_secure_prefs"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "amqhi_secure_storage_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_IV_LENGTH = 12
        const val GCM_TAG_BITS = 128
        const val GCM_TAG_BYTES = GCM_TAG_BITS / 8
        val FORMAT_MAGIC = byteArrayOf(0x41, 0x51, 0x47, 0x31)

        const val LEGACY_TRANSFORMATION = "AES/CBC/PKCS5Padding"
        val LEGACY_SECRET_KEY_BYTES = "AmqhiCloudKey123".toByteArray()
        val LEGACY_IV_BYTES = "AmqhiCloudIV_123".toByteArray()
    }
}
