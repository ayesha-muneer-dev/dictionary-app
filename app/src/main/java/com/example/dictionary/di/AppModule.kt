package com.example.dictionary.di

import android.content.Context
import androidx.room.Room
import com.example.dictionary.feature_dictionary.data.local.DictionaryDatabase
import com.example.dictionary.feature_dictionary.data.repository.DictionaryRepositoryImpl
import com.example.dictionary.feature_dictionary.domain.repository.DictionaryRepository
import com.example.dictionary.feature_dictionary.domain.view_model.DictionaryViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        Room.databaseBuilder(
            get<Context>(), DictionaryDatabase::class.java, "dictionary_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    single {
        get<DictionaryDatabase>().dictionaryDao()
    }

    single<DictionaryRepository> {
        DictionaryRepositoryImpl(
            dao = get()
        )
    }

    viewModel {
        DictionaryViewModel(
            repository = get(),
            application = androidApplication()
        )
    }

}
