package com.md.mypuzzleapp.presentation.home

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.md.mypuzzleapp.domain.model.PuzzleDifficulty
import com.md.mypuzzleapp.domain.repository.PuzzleRepository
import com.md.mypuzzleapp.manager.HomeManager
import com.md.mypuzzleapp.presentation.common.Screen
import com.md.mypuzzleapp.presentation.common.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.graphics.BitmapFactory

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeManager: HomeManager,
    private val context: Context
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
                // This part of the logic needs to be adapted to use the repository
                // For now, it's kept as is, but it will cause a compilation error
                // as HomeManager is removed.
                // The original code had puzzleRepository.getAllPuzzlesForDevice(context).collectLatest { puzzles ->
                // This line is removed as per the new_code, as the repository is no longer used.
                // The HomeManager will now handle fetching puzzles.
                homeManager.getAllPuzzles().collectLatest { puzzles ->
                    state = state.copy(
                        puzzles = puzzles,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                // Handle error (e.g., show snackbar)
            }
        }
    }
    
    private fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                // Load bitmap from URI
                val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
                val result = homeManager.createPuzzleWithImage(
                    name = state.uploadImageName,
                    difficulty = state.selectedDifficulty.name, // Store as String
                    bitmap = bitmap,
                    context = context
                )
                if (result.isSuccess) {
                    val puzzle = result.getOrNull()
                    state = state.copy(
                        isLoading = false,
                        isUploadDialogVisible = false,
                        uploadImageName = "",
                        selectedDifficulty = PuzzleDifficulty.EASY
                    )
                    if (puzzle != null) {
                        _uiEvent.send(UiEvent.Navigate("puzzle/${puzzle.id}"))
                        _uiEvent.send(UiEvent.ShowSnackbar("Puzzle created successfully"))
                    }
                } else {
                    state = state.copy(isLoading = false)
                    _uiEvent.send(UiEvent.ShowSnackbar(result.exceptionOrNull()?.message ?: "Error creating puzzle"))
                }
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
                homeManager.fetchRandomImage(
                    name = "Random Puzzle ${System.currentTimeMillis()}",
                    difficulty = state.selectedDifficulty.name,
                    context = context
                ).fold(
                    onSuccess = { puzzle ->
                        state = state.copy(
                            isLoading = false,
                            isUploadDialogVisible = false,
                            uploadImageName = "",
                            selectedDifficulty = PuzzleDifficulty.EASY
                        )
                        _uiEvent.send(UiEvent.ShowSnackbar("Random puzzle created successfully"))
                    },
                    onFailure = { error ->
                        state = state.copy(isLoading = false)
                        _uiEvent.send(UiEvent.ShowSnackbar(error.message ?: "Error fetching random image"))
                    }
                )
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _uiEvent.send(UiEvent.ShowSnackbar(e.message ?: "Error fetching random image"))
            }
        }
    }
} 