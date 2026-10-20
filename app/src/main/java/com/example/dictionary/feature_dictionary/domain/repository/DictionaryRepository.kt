package com.example.dictionary.feature_dictionary.domain.repository

import com.example.dictionary.feature_dictionary.domain.entity.DictionaryEntity

interface DictionaryRepository {

    suspend fun getAllWords(): List<DictionaryEntity>

    suspend fun searchWord(word: String): DictionaryEntity?

    suspend fun insertWord(word: DictionaryEntity)

    suspend fun getSuggestions(query: String): List<String>
}
