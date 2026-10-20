package com.example.dictionary.feature_dictionary.data.local

import androidx.room.Dao
import androidx.room.Query
import com.example.dictionary.feature_dictionary.domain.entity.DictionaryEntity

@Dao
interface DictionaryDao {

    @Query("SELECT * FROM dictionary_table")
    suspend fun getAllWords(): List<DictionaryEntity>

    @Query("""
    INSERT OR REPLACE INTO dictionary_table
    (word, phonetic, partOfSpeech, meaning, urduMeaning, example, synonyms, isFavorite)
    VALUES (:word, :phonetic, :partOfSpeech, :meaning, :urduMeaning, :example, :synonyms, :isFavorite)
""")
    suspend fun insertWord(
        word: String,
        phonetic: String,
        partOfSpeech: String,
        meaning: String,
        urduMeaning: String,
        example: String,
        synonyms: String,
        isFavorite: Boolean
    )

    @Query("SELECT * FROM dictionary_table WHERE word = :word COLLATE NOCASE LIMIT 1")
    suspend fun searchWord(word: String): DictionaryEntity?

    @Query("SELECT word FROM dictionary_table WHERE word LIKE :query || '%' LIMIT 5")
    suspend fun getLocalSuggestions(query: String): List<String>
}
