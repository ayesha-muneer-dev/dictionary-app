package com.example.dictionary.feature_dictionary.data.network

sealed class RequestType (
    val link:String
){
    data class Get(val url: String) : RequestType(link = url)
}