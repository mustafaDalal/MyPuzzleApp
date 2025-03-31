package com.md.mypuzzleapp.presentation.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.md.mypuzzleapp.domain.model.PuzzleDifficulty
import com.md.mypuzzleapp.domain.usecase.CreatePuzzleFromBitmapUseCase
import com.md.mypuzzleapp.domain.usecase.CreatePuzzleUseCase
import com.md.mypuzzleapp.domain.usecase.GetAllPuzzlesUseCase
import com.md.mypuzzleapp.domain.usecase.GetRandomImageUseCase
import com.md.mypuzzleapp.presentation.common.Screen
import com.md.mypuzzleapp.presentation.common.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPuzzlesUseCase: GetAllPuzzlesUseCase,
    private val createPuzzleUseCase: CreatePuzzleUseCase,
    private val createPuzzleFromBitmapUseCase: CreatePuzzleFromBitmapUseCase,
    private val getRandomImageUseCase: GetRandomImageUseCase
) : ViewModel() {
    
    var state by mutableStateOf(HomeState())
        private set
    
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()
    
    init {
        loadPuzzles()
    }
    
    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SelectPuzzle -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.Navigate("puzzle/${event.puzzleId}"))
                }
            }
            is HomeEvent.ShowUploadDialog -> {
                state = state.copy(isUploadDialogVisible = true)
            }
            is HomeEvent.DismissUploadDialog -> {
                state = state.copy(
                    isUploadDialogVisible = false,
                    uploadImageName = "",
                    selectedDifficulty = PuzzleDifficulty.EASY
                )
            }
            is HomeEvent.UploadImageNameChanged -> {
                state = state.copy(uploadImageName = event.name)
            }
            is HomeEvent.DifficultyChanged -> {
                state = state.copy(selectedDifficulty = event.difficulty)
            }
            is HomeEvent.UploadImage -> {
                uploadImage(event.uri)
            }
            is HomeEvent.FetchRandomImage -> {
                fetchRandomImage()
            }
            HomeEvent.LoadPuzzles -> {
                loadPuzzles()
            }
        }
    }
    
    private fun loadPuzzles() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                getAllPuzzlesUseCase().collect { puzzles ->
                    state = state.copy(
                        puzzles = puzzles,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _uiEvent.send(UiEvent.ShowSnackbar(e.message ?: "Error loading puzzles"))
            }
        }
    }
    
    private fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                createPuzzleUseCase(
                    name = state.uploadImageName,
                    imageUri = uri,
                    difficulty = state.selectedDifficulty
                )
                state = state.copy(
                    isLoading = false,
                    isUploadDialogVisible = false,
                    uploadImageName = "",
                    selectedDifficulty = PuzzleDifficulty.EASY
                )
                _uiEvent.send(UiEvent.ShowSnackbar("Puzzle created successfully"))
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _uiEvent.send(UiEvent.ShowSnackbar(e.message ?: "Error creating puzzle"))
            }
        }
    }
    
    private fun fetchRandomImage() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                val response = getRandomImageUseCase()
                if (!response.isSuccessful) {
                    throw Exception("Failed to fetch image: ${response.code()}")
                }

                // Show upload dialog with default name

                Log.d("Mustafa Debug", "response body - ${response.body()}")
                state = state.copy(
                    isUploadDialogVisible = true,
                    uploadImageName = "Random Puzzle ${System.currentTimeMillis()}",
                    selectedDifficulty = PuzzleDifficulty.EASY
                )

                // Convert response to bitmap and create puzzle
                val bitmap = withContext(Dispatchers.IO) {
                    response.body()?.byteStream()?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    } ?: throw Exception("Failed to decode image")
                }

                // Create puzzle with the bitmap
                createPuzzleFromBitmapUseCase(
                    name = state.uploadImageName,
                    bitmap = bitmap,
                    difficulty = state.selectedDifficulty
                )

                state = state.copy(
                    isLoading = false,
                    isUploadDialogVisible = false,
                    uploadImageName = "",
                    selectedDifficulty = PuzzleDifficulty.EASY
                )
                _uiEvent.send(UiEvent.ShowSnackbar("Random puzzle created successfully"))
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _uiEvent.send(UiEvent.ShowSnackbar(e.message ?: "Error fetching random image"))
            }
        }
    }
} 