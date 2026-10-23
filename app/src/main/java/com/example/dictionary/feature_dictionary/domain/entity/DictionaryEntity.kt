package com.example.dictionary.feature_dictionary.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dictionary_table")
data class DictionaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val word: String,
    val phonetic: String = "",
    val partOfSpeech: String = "",
    val meaning: String = "",
    val urduMeaning: String = "",
    val example: String = "",
    val synonyms: String = "",
    val isFavorite: Boolean = false,
    val rhymingWords: String = ""
)
