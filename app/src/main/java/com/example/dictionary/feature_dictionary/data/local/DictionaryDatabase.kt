package com.example.dictionary.feature_dictionary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dictionary.feature_dictionary.domain.entity.DictionaryEntity

@Database(
    entities = [
        DictionaryEntity::class
    ],
    version = 2
)
abstract class DictionaryDatabase : RoomDatabase() {

    abstract fun dictionaryDao(): DictionaryDao

}