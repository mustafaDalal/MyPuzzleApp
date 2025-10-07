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
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeManager: HomeManager,
    private val context: Context
) : ViewModel() {
    
    var state by mutableStateOf(HomeState())
        private set
    
    private var loadJob: Job? = null

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
        loadJob?.cancel()
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            Log.d("HomeViewModel", "loadPuzzles: start")
            withContext(Dispatchers.Main) {
                state = state.copy(isLoading = true)
            }
            try {
                homeManager.getAllPuzzles().collectLatest { puzzles ->
                    Log.d("HomeViewModel", "loadPuzzles: received ${puzzles.size} puzzles")
                    val display = puzzles.filter { it.originalImage != null || it.localImageUri != null }
                    val filteredOut = puzzles.size - display.size
                    if (filteredOut > 0) {
                        Log.d("HomeViewModel", "loadPuzzles: filtered out ${filteredOut} puzzles without image")
                    }
                    display.take(3).forEach { p ->
                        Log.d(
                            "HomeViewModel",
                            "showing id=${p.id} name=${p.name} hasBitmap=${p.originalImage != null} uri=${p.localImageUri}"
                        )
                    }
                    withContext(Dispatchers.Main){state = state.copy(
                        puzzles = display,
                        isLoading = false
                    )

                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "loadPuzzles: failed", e)
                withContext(Dispatchers.Main) {
                    state = state.copy(isLoading = false)
                }
                // Handle error (e.g., show snackbar)
            }
            Log.d("HomeViewModel", "loadPuzzles: end isLoading=${state.isLoading} shown=${state.puzzles.size}")
        }
    }
    
    private fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            // Dismiss dialog immediately to avoid overlay issues and show loading state in main UI
            state = state.copy(isLoading = true, isUploadDialogVisible = false)
            try {
                // Load bitmap from URI off the main thread
                val bitmap = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
                }
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
                        uploadImageName = ""
                        // Don't reset selectedDifficulty here as it should be preserved for future operations
                    )
                    if (puzzle != null) {
                        // Optimistically add to grid so it appears instantly
                        state = state.copy(puzzles = listOf(puzzle) + state.puzzles)
                        // Refresh list so Home reflects the newly created puzzle when user returns
                        loadPuzzles()
                        _uiEvent.send(UiEvent.Navigate("puzzle/${puzzle.id}"))
                        _uiEvent.send(UiEvent.ShowSnackbar("Puzzle created successfully"))
                    }
                } else {
                    // Dismiss the dialog on failure as well so the snackbar is visible
                    state = state.copy(isLoading = false, isUploadDialogVisible = false)
                    _uiEvent.send(UiEvent.ShowSnackbar(result.exceptionOrNull()?.message ?: "Error creating puzzle"))
                }
            } catch (e: Exception) {
                // Dismiss the dialog on unexpected errors
                _uiEvent.send(UiEvent.ShowSnackbar(e.message ?: "Error creating puzzle"))
            }
        }
    }
    
    private fun fetchRandomImage() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                // Use user-provided name if available, otherwise generate a default one
                val puzzleName = if (state.uploadImageName.isNotBlank()) {
                    state.uploadImageName
                } else {
                    "Random Puzzle ${System.currentTimeMillis()}"
                }
                
                // Use the currently selected difficulty
                val difficulty = state.selectedDifficulty
                Log.d("HomeViewModel", "fetchRandomImage: using difficulty = ${difficulty.name} (${difficulty.gridSize}x${difficulty.gridSize})")
                
                homeManager.fetchRandomImage(
                    name = puzzleName,
                    difficulty = difficulty.name,
                    context
                ).fold(
                    onSuccess = { puzzle ->
                        state = state.copy(
                            isLoading = false,
                            isUploadDialogVisible = false,
                            uploadImageName = ""
                            // Don't reset selectedDifficulty here to preserve user's choice
                        )
                        // Refresh list so Home reflects the newly fetched puzzle
                        loadPuzzles()
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