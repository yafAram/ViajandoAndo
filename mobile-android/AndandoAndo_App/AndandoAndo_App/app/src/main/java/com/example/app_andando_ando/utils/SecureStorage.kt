package com.example.app_andando_ando.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SecureStorage(context: Context) {

    private val prefs: SharedPreferences by lazy {
        try {
            val prefsName = "secure_prefs"
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                prefsName,
                masterKeyAlias,
                context.applicationContext, // ✅ importante para evitar memory leaks
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // ⚠️ fallback si falla el cifrado (dispositivos antiguos o sin soporte)
            context.getSharedPreferences("secure_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    companion object {
        private const val KEY_TOKEN = "KEY_TOKEN"
        private const val KEY_USER = "KEY_USER"
    }

    fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun clearToken() = prefs.edit().remove(KEY_TOKEN).apply()

    fun saveUser(json: String) = prefs.edit().putString(KEY_USER, json).apply()
    fun getUserJson(): String? = prefs.getString(KEY_USER, null)
    fun clearUser() = prefs.edit().remove(KEY_USER).apply()
}

