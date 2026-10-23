package com.example.dictionary.feature_dictionary.presentation

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dictionary.feature_dictionary.domain.entity.supportedLanguages
import com.example.dictionary.feature_dictionary.domain.view_model.DictionaryViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun DictionaryScreen(
    viewModel: DictionaryViewModel = koinViewModel()
) {
    var searchText by remember {
        mutableStateOf("")
    }

    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                searchText = spokenText.trim()
                viewModel.searchWord(spokenText.trim())
            }
        }
    }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var textToSpeech: TextToSpeech? = null
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
            }
        }
        tts = textToSpeech
        onDispose {
            textToSpeech.shutdown()
        }
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "My Dictionary",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Search an English word",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Multi-Language Selection Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(supportedLanguages) { lang ->
                    val isSelected = lang.code == state.selectedLanguage.code
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.selectLanguage(lang)
                        },
                        label = {
                            Text("${lang.flag} ${lang.name}")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    viewModel.onSearchTextChanged(it)
                },
                label = {
                    Text("Enter English word")
                },
                placeholder = {
                    Text("e.g. hello")
                },
                supportingText = {
                    if (state.topSuggestion.isNotBlank() && !searchText.equals(state.topSuggestion, ignoreCase = true)) {
                        Text(
                            text = "Suggestion: ${state.topSuggestion}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.clickable {
                                searchText = state.topSuggestion
                                viewModel.searchWord(state.topSuggestion)
                            }
                        )
                    }
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = {
                                searchText = ""
                                viewModel.clearSearch()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear text"
                                )
                            }
                        }

                        IconButton(onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a word to search...")
                            }
                            speechRecognizerLauncher.launch(intent)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Search"
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.searchWord(searchText.trim())
                },
                enabled = searchText.isNotBlank() && !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (state.isLoading) {
                        "Searching..."
                    } else {
                        "Search"
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
            }

            state.error?.let { errorMessage ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            state.dictionaryWord?.let { word ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = word.word,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.weight(1f)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                IconButton(
                                    onClick = {
                                        tts?.speak(word.word, TextToSpeech.QUEUE_FLUSH, null, null)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Speak word"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val textToCopy = "Word: ${word.word}\nMeaning: ${word.meaning}\n${state.selectedLanguage.name}: ${word.urduMeaning}"
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Dictionary Word", textToCopy)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val shareText = "Word: ${word.word}\nMeaning: ${word.meaning}\n${state.selectedLanguage.name}: ${word.urduMeaning}"
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Pronunciation",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = word.phonetic.ifBlank {
                                "Not available"
                            }
                        )

                        if (word.partOfSpeech.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Part of Speech",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(text = word.partOfSpeech)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Meaning",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = word.meaning.ifBlank {
                                "Meaning not available"
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "${state.selectedLanguage.name} Meaning (${state.selectedLanguage.flag})",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = word.urduMeaning.ifBlank {
                                "${state.selectedLanguage.name} meaning not available"
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Example",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = word.example.ifBlank {
                                "Example not available"
                            }
                        )

                        if (word.synonyms.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Synonyms",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(text = word.synonyms)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
