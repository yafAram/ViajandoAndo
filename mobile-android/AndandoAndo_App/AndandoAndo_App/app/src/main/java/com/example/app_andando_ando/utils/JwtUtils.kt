package com.example.app_andando_ando.utils

import android.util.Base64
import org.json.JSONObject

object JwtUtils {
    fun getExpirationEpochSeconds(jwt: String?): Long? {
        if (jwt.isNullOrBlank()) return null
        return try {
            val parts = jwt.split(".")
            if (parts.size < 2) return null
            val payload = parts[1]
            val decoded = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP))
            val obj = JSONObject(decoded)
            if (obj.has("exp")) obj.getLong("exp") else null
        } catch (ex: Exception) {
            null
        }
    }

    fun isTokenValidNow(jwt: String?): Boolean {
        val exp = getExpirationEpochSeconds(jwt) ?: return false
        val nowSec = System.currentTimeMillis() / 1000L
        return exp > nowSec
    }
}
