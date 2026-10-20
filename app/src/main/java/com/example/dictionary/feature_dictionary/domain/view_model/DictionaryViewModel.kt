package com.example.dictionary.feature_dictionary.domain.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dictionary.feature_dictionary.domain.entity.DictionaryEntity
import com.example.dictionary.feature_dictionary.data.network.NetworkUtil
import com.example.dictionary.feature_dictionary.domain.repository.DictionaryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DictionaryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val toastMessage: String? = null,
    val dictionaryWord: DictionaryEntity? = null,
    val topSuggestion: String = ""
)

class DictionaryViewModel(
    private val repository: DictionaryRepository,
    private val application: Application
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(DictionaryUiState())
    val state = _state.asStateFlow()

    fun searchWord(word: String) {
        viewModelScope.launch {

            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    toastMessage = null,
                    dictionaryWord = null,
                    topSuggestion = ""
                )
            }

            try {
                val isOnline = NetworkUtil.isNetworkAvailable(application)
                val result = repository.searchWord(word)

                if (result != null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            dictionaryWord = result,
                            error = null,
                            topSuggestion = ""
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            dictionaryWord = null,
                            error = if (!isOnline) {
                                "No internet connection. Word not saved offline."
                            } else {
                                "Word not found"
                            },
                            topSuggestion = ""
                        )
                    }
                }

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.update {
                    it.copy(
                        isLoading = false,
                        dictionaryWord = null,
                        error = e.localizedMessage?.ifBlank { null } ?: "Network error",
                        topSuggestion = ""
                    )
                }
            }
        }
    }

    fun onSearchTextChanged(text: String) {
        viewModelScope.launch {
            if (text.isBlank()) {
                clearSearch()
            } else {
                val list = repository.getSuggestions(text)
                val bestSuggestion = list.firstOrNull { it.lowercase().startsWith(text.lowercase()) } ?: list.firstOrNull() ?: ""
                _state.update {
                    it.copy(topSuggestion = bestSuggestion)
                }
            }
        }
    }

    fun clearSearch() {
        _state.update {
            it.copy(
                isLoading = false,
                error = null,
                toastMessage = null,
                dictionaryWord = null,
                topSuggestion = ""
            )
        }
    }
}
