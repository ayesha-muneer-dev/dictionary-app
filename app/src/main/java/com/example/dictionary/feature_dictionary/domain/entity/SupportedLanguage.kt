package com.example.dictionary.feature_dictionary.domain.entity

data class SupportedLanguage(
    val name: String,
    val code: String,
    val flag: String
)

val supportedLanguages = listOf(
    SupportedLanguage("Urdu", "ur", "🇵🇰"),
    SupportedLanguage("Hindi", "hi", "🇮🇳"),
    SupportedLanguage("Arabic", "ar", "🇸🇦"),
    SupportedLanguage("Spanish", "es", "🇪🇸"),
    SupportedLanguage("French", "fr", "🇫🇷"),
    SupportedLanguage("German", "de", "🇩🇪"),
    SupportedLanguage("Turkish", "tr", "🇹🇷"),
    SupportedLanguage("Pashto", "ps", "🇵🇰"),
    SupportedLanguage("Sindhi", "sd", "🇵🇰")
)
