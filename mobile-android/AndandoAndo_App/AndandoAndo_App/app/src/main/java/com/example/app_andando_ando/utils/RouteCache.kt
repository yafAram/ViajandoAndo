package com.example.app_andando_ando.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken



class RouteCache(
    private val context: Context,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "RouteCache"
        private const val PREFS_NAME = "route_cache_prefs"
        private const val KEY_WAYPOINTS = "KEY_WAYPOINTS"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun readList(): MutableList<WaypointSimple> {
        val json = prefs.getString(KEY_WAYPOINTS, null)
        Log.d(TAG, "🔹 Leyendo caché JSON: $json")
        if (json.isNullOrBlank()) return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<WaypointSimple>>() {}.type
            gson.fromJson<MutableList<WaypointSimple>>(json, type) ?: mutableListOf()
        } catch (t: Throwable) {
            Log.e(TAG, "Error parseando JSON del cache", t)
            mutableListOf()
        }
    }

    private fun saveList(list: List<WaypointSimple>) {
        val json = try { gson.toJson(list) } catch (t: Throwable) { "[]" }
        prefs.edit().putString(KEY_WAYPOINTS, json).apply()
        Log.d(TAG, "🔸 Guardado caché JSON: $json")
    }

    fun getAll(): List<WaypointSimple> {
        val list = readList().toList()
        Log.d(TAG, "📤 getAll() devolvió ${list.size} elementos")
        return list
    }

    fun add(w: WaypointSimple) {
        val list = readList()
        if (list.any { it.poiId == w.poiId }) {
            Log.d(TAG, "➕ add() ignorado: poiId ya existe=${w.poiId}")
            return
        }
        list.add(w)
        saveList(list)
        Log.d(TAG, "➕ add() agregado: ${w.poiId}")
    }

    fun removeAt(index: Int) {
        val list = readList()
        if (index in list.indices) {
            val removed = list.removeAt(index)
            saveList(list)
            Log.d(TAG, "➖ removeAt($index) removido: ${removed.poiId}")
        } else {
            Log.w(TAG, "removeAt index fuera de rango: $index")
        }
    }

    fun removeByPoiId(poiId: String) {
        val list = readList().filterNot { it.poiId == poiId }
        saveList(list)
        Log.d(TAG, "➖ removeByPoiId: $poiId")
    }

    fun move(fromIndex: Int, toIndex: Int) {
        val list = readList()
        if (fromIndex in list.indices && toIndex in 0..list.size) {
            val item = list.removeAt(fromIndex)
            val insertIndex = if (toIndex > list.size) list.size else toIndex
            list.add(insertIndex, item)
            saveList(list)
            Log.d(TAG, "🔀 move() from=$fromIndex to=$toIndex poi=${item.poiId}")
        } else {
            Log.w(TAG, "move() índices inválidos from=$fromIndex to=$toIndex size=${list.size}")
        }
    }

    fun clear() {
        prefs.edit().remove(KEY_WAYPOINTS).apply()
        Log.d(TAG, "🧹 Caché de rutas limpiado")
    }

    /**
     * Devuelve el JSON crudo almacenado en SharedPreferences (útil para debugging desde UI).
     */
    fun dumpJson(): String? {
        val json = prefs.getString(KEY_WAYPOINTS, null)
        Log.d(TAG, "🚨 dumpJson -> $json")
        return json
    }
}




