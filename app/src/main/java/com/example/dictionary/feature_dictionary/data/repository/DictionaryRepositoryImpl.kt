package com.example.dictionary.feature_dictionary.data.repository

import android.util.Log
import com.example.dictionary.feature_dictionary.data.local.DictionaryDao
import com.example.dictionary.feature_dictionary.domain.entity.DictionaryEntity
import com.example.dictionary.feature_dictionary.data.network.NetworkClient
import com.example.dictionary.feature_dictionary.data.network.NetworkUtil
import com.example.dictionary.feature_dictionary.domain.repository.DictionaryRepository
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

class DictionaryRepositoryImpl(
    private val dao: DictionaryDao
) : DictionaryRepository {

    override suspend fun getAllWords(): List<DictionaryEntity> {
        return dao.getAllWords()
    }

    override suspend fun getSuggestions(query: String): List<String> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return emptyList()

        val suggestions = mutableListOf<String>()

        try {
            val local = dao.getLocalSuggestions(q)
            suggestions.addAll(local)
        } catch (_: Exception) {}

        try {
            val url = "https://api.datamuse.com/sug?s=$q"
            val responseObj = NetworkClient.httpClient.get(url)
            if (responseObj.status.value == 200) {
                val json = responseObj.bodyAsText()
                val element = Json.parseToJsonElement(json)
                if (element is JsonArray) {
                    element.forEach { item ->
                        val obj = item as? JsonObject
                        val word = obj?.get("word").asString()
                        if (word.isNotBlank() && !suggestions.contains(word)) {
                            suggestions.add(word)
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        return suggestions.distinct().take(5)
    }

    override suspend fun searchWord(word: String): DictionaryEntity? = supervisorScope {
        val query = word.trim().lowercase()
        if (query.isBlank()) return@supervisorScope null

        val localWord = dao.searchWord(query)
        if (localWord != null && localWord.meaning.isNotBlank()) {
            return@supervisorScope localWord
        }

        var englishMeaning = ""
        var phonetic = ""
        var partOfSpeech = ""
        var example = ""
        var synonyms = ""
        var urduMeaning = ""

        try {
            val url = "${NetworkUtil.BASE_URL}entries/en/$query"
            Log.d("DictionaryRepo", "Fetching FreeDictionary URL: $url")
            val responseObj = NetworkClient.httpClient.get(url)
            Log.d("DictionaryRepo", "FreeDictionary status: ${responseObj.status}")
            if (responseObj.status.value == 200) {
                val json: String = responseObj.bodyAsText()
                val element = Json.parseToJsonElement(json)
                if (element is JsonArray && element.isNotEmpty()) {
                    val firstEntry = element[0] as? JsonObject
                    
                    phonetic = firstEntry?.get("phonetic").asString()
                    if (phonetic.isBlank()) {
                        val phoneticsArray = firstEntry?.get("phonetics") as? JsonArray
                        phoneticsArray?.forEach { p ->
                            val text = (p as? JsonObject)?.get("text").asString()
                            if (text.isNotBlank()) {
                                phonetic = text
                                return@forEach
                            }
                        }
                    }

                    val meaningsArray = firstEntry?.get("meanings") as? JsonArray
                    val synonymsList = mutableListOf<String>()

                    meaningsArray?.forEach { meaningElement ->
                        val meaningObj = meaningElement as? JsonObject
                        val pos = meaningObj?.get("partOfSpeech").asString()
                        
                        val definitionsArray = meaningObj?.get("definitions") as? JsonArray
                        definitionsArray?.forEach { defElement ->
                            val defObj = defElement as? JsonObject
                            val def = defObj?.get("definition").asString()
                            val ex = defObj?.get("example").asString()

                            if (def.isNotBlank() && englishMeaning.isBlank()) {
                                englishMeaning = def
                                partOfSpeech = pos
                                example = ex
                            } else if (example.isBlank() && ex.isNotBlank()) {
                                example = ex
                            }
                        }

                        val meaningSynonyms = meaningObj?.get("synonyms") as? JsonArray
                        meaningSynonyms?.forEach { syn ->
                            val synText = syn.asString()
                            if (synText.isNotBlank()) synonymsList.add(synText)
                        }
                    }

                    if (synonymsList.isNotEmpty()) {
                        synonyms = synonymsList.distinct().take(5).joinToString(", ")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DictionaryRepo", "Error in FreeDictionary API", e)
        }

        if (englishMeaning.isBlank()) {
            try {
                val datamuseUrl = "https://api.datamuse.com/words?sp=$query&md=d&max=1"
                Log.d("DictionaryRepo", "Fetching Datamuse URL: $datamuseUrl")
                val responseObj = NetworkClient.httpClient.get(datamuseUrl)
                if (responseObj.status.value == 200) {
                    val json = responseObj.bodyAsText()
                    val element = Json.parseToJsonElement(json)
                    if (element is JsonArray && element.isNotEmpty()) {
                        val wordObj = element[0] as? JsonObject
                        val defsArray = wordObj?.get("defs") as? JsonArray
                        val firstDefRaw = defsArray?.firstOrNull().asString()
                        if (firstDefRaw.contains("\t")) {
                            val parts = firstDefRaw.split("\t")
                            if (parts.size >= 2) {
                                val rawPos = parts[0].trim()
                                englishMeaning = parts[1].trim().replaceFirstChar { it.uppercase() }
                                if (partOfSpeech.isBlank()) {
                                    partOfSpeech = when (rawPos) {
                                        "n" -> "noun"
                                        "v" -> "verb"
                                        "adj" -> "adjective"
                                        "adv" -> "adverb"
                                        else -> rawPos
                                    }
                                }
                            }
                        } else if (firstDefRaw.isNotBlank()) {
                            englishMeaning = firstDefRaw.replaceFirstChar { it.uppercase() }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DictionaryRepo", "Error in Datamuse API", e)
            }
        }

        try {
            val url = "https://api.mymemory.translated.net/get?q=$query&langpair=en|ur"
            Log.d("DictionaryRepo", "Fetching MyMemory URL: $url")
            val responseObj = NetworkClient.httpClient.get(url)
            if (responseObj.status.value == 200) {
                val json: String = responseObj.bodyAsText()
                val element = Json.parseToJsonElement(json) as? JsonObject
                val responseData = element?.get("responseData") as? JsonObject
                urduMeaning = responseData?.get("translatedText").asString()
            }
        } catch (e: Exception) {
            Log.e("DictionaryRepo", "Error in MyMemory API", e)
        }

        if (phonetic.isBlank() && (englishMeaning.isNotBlank() || urduMeaning.isNotBlank())) {
            phonetic = "/$query/"
        }

        if (example.isBlank() && englishMeaning.isNotBlank()) {
            example = "Word '$query' means: $englishMeaning."
        }

        if (englishMeaning.isBlank() && urduMeaning.isBlank()) {
            return@supervisorScope null
        }

        val dictionaryWord = DictionaryEntity(
            word = query,
            phonetic = phonetic,
            partOfSpeech = partOfSpeech,
            meaning = englishMeaning,
            urduMeaning = urduMeaning,
            example = example,
            synonyms = synonyms,
            isFavorite = false
        )

        try {
            insertWord(dictionaryWord)
        } catch (_: Exception) {
        }

        dictionaryWord
    }

    override suspend fun insertWord(word: DictionaryEntity) {
        dao.insertWord(
            word = word.word,
            phonetic = word.phonetic,
            partOfSpeech = word.partOfSpeech,
            meaning = word.meaning,
            urduMeaning = word.urduMeaning,
            example = word.example,
            synonyms = word.synonyms,
            isFavorite = word.isFavorite
        )
    }

    private fun JsonElement?.asString(): String {
        if (this == null) return ""
        return try {
            (this as? JsonPrimitive)?.contentOrNull ?: ""
        } catch (_: Exception) {
            ""
        }
    }
}
