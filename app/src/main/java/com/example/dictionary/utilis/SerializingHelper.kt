package com.example.dictionary.utilis

import android.util.Log
import kotlinx.serialization.json.Json

object SerializingHelper {

    fun getJson(): Json {
        return Json {
            isLenient = true
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    inline fun <reified T> modelToText(data: T): String? {
        return try {
            getJson().encodeToString(data)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    inline fun <reified T> textToModel(data: String): T? {
        return try {
            getJson().decodeFromString<T>(data)
        } catch (e: Exception) {
            Log.e("SerializingHelper", "Error decoding JSON: ${e.message}", e)
            null
        }
    }
}
