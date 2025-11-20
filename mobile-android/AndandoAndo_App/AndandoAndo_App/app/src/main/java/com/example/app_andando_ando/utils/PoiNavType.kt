package com.example.app_andando_ando.utils

import android.os.Bundle
import androidx.navigation.NavType
import com.example.app_andando_ando.domain.model.mapUser.Poi
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

object PoiNavType : NavType<Poi>(isNullableAllowed = false) {

    override fun get(bundle: Bundle, key: String): Poi? {
        return bundle.getString(key)?.let { Json.decodeFromString<Poi>(it) }
    }

    override fun parseValue(value: String): Poi {
        return Json.decodeFromString(value)
    }

    override fun put(bundle: Bundle, key: String, value: Poi) {
        bundle.putString(key, Json.encodeToString(value))
    }
}
