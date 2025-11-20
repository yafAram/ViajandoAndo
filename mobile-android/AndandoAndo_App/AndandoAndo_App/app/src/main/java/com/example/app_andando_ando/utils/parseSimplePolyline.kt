package com.example.app_andando_ando.utils

fun parseSimplePolyline(polyline: String): List<Pair<Double, Double>> {
    return polyline.split(";").mapNotNull {
        val parts = it.split(",")
        if (parts.size == 2) {
            val lat = parts[0].toDoubleOrNull()
            val lng = parts[1].toDoubleOrNull()
            if (lat != null && lng != null) lat to lng else null
        } else null
    }
}