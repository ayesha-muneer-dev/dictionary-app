package com.example.dictionary.feature_dictionary.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header

object NetworkClient {

    val httpClient = HttpClient(OkHttp) {

        defaultRequest {
            header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        }

        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                }
            )
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
        }
    }


    suspend fun makeNetworkRequest(
        requestType: RequestType
    ): String {
        return when (requestType) {
            is RequestType.Get -> {
                httpClient.get(requestType.url).body()
            }
        }
    }

}
