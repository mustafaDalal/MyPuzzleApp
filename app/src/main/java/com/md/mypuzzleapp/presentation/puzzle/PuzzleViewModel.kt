package com.md.mypuzzleapp.presentation.puzzle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.md.mypuzzleapp.manager.PuzzleManager
import com.md.mypuzzleapp.presentation.common.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PuzzleViewModel @Inject constructor(
    private val puzzleManager: PuzzleManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(PuzzleState())
        private set

    val placedPieces = puzzleManager.placedPieces
    val unplacedPieces = puzzleManager.unplacedPieces
    val correctlyPlacedPieces = puzzleManager.correctlyPlacedPieces

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val puzzleId: String = checkNotNull(savedStateHandle["puzzleId"])

    init {
        loadPuzzle(puzzleId)
    }

    fun onEvent(event: PuzzleEvent) {
        when (event) {
            is PuzzleEvent.MovePiece -> {
                movePiece(event.fromIndex, event.toIndex)
            }
            is PuzzleEvent.DragPiece -> {
                if (!state.isDragging) {
                    state = state.copy(isDragging = true)
                    puzzleManager.setDraggedPiece(unplacedPieces.value.find { it.id == event.pieceId })
                }
            }
            is PuzzleEvent.DropPiece -> {
                val isCorrectlyPlaced = puzzleManager.handlePiecePlacement(event.pieceId, event.toPosition)
                if (isCorrectlyPlaced) {
                    viewModelScope.launch {
                        _uiEvent.send(UiEvent.ShowSnackbar("Piece placed correctly!"))
                    }
                }
                state = state.copy(
                    moves = state.moves + 1,
                    isDragging = false
                )
                puzzleManager.resetDragState()
                checkPuzzleCompletion()
            }
            is PuzzleEvent.ReturnPiece -> {
                puzzleManager.handlePieceReturn(event.piece, event.position)
            }
            PuzzleEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.NavigateUp)
                }
            }
            PuzzleEvent.RestartPuzzle -> {
                restartPuzzle()
            }
            PuzzleEvent.CheckCompletion -> {
                checkPuzzleCompletion()
            }
            PuzzleEvent.RevealImage -> {
                revealImage()
            }
            PuzzleEvent.HideImage -> {
                state = state.copy(isRevealingImage = false)
            }
            PuzzleEvent.StopDragging -> {
                // Only process stop dragging if we've been dragging for more than 100ms
                // This prevents accidental drag ends from quick touches
                if (System.currentTimeMillis() - puzzleManager.getDragStartTime() > 100) {
                    state = state.copy(isDragging = false)
                    // If we have a dragged piece that wasn't placed, return it to unplaced pieces
                    puzzleManager.getDraggedPiece()?.let { piece ->
                        if (!placedPieces.value.values.any { it.id == piece.id }) {
                            if (!unplacedPieces.value.any { it.id == piece.id }) {
                                puzzleManager.handlePieceReturn(piece, -1)
                            }
                        }
                    }
                    puzzleManager.resetDragState()
                }
            }
        }
    }

    private fun loadPuzzle(id: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                puzzleManager.loadPuzzle(id).let { result ->
                    result.fold(
                        onSuccess = { puzzleWithPieces ->
                            state = state.copy(
                                puzzle = puzzleWithPieces.puzzle,
                                puzzlePieces = puzzleWithPieces.pieces,
                                isLoading = false
                            )
                        },
                        onFailure = { error ->
                            state = state.copy(isLoading = false)
                            _uiEvent.send(UiEvent.ShowSnackbar(error.message ?: "Error loading puzzle"))
                            _uiEvent.send(UiEvent.NavigateUp)
                        }
                    )
                }
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _uiEvent.send(UiEvent.ShowSnackbar(e.message ?: "Error loading puzzle"))
                _uiEvent.send(UiEvent.NavigateUp)
            }
        }
    }

    private fun movePiece(fromIndex: Int, toIndex: Int) {
        val currentPieces = state.puzzlePieces.toMutableList()
        val fromPiece = currentPieces[fromIndex]
        val toPiece = currentPieces[toIndex]

        currentPieces[fromIndex] = toPiece.copy(currentPosition = fromIndex)
        currentPieces[toIndex] = fromPiece.copy(currentPosition = toIndex)

        state = state.copy(
            puzzlePieces = currentPieces,
            moves = state.moves + 1
        )

        checkPuzzleCompletion()
    }

    private fun checkPuzzleCompletion() {
        if (puzzleManager.checkPuzzleCompletion() && !state.isComplete) {
            state = state.copy(isComplete = true)
            viewModelScope.launch {
                _uiEvent.send(UiEvent.ShowSnackbar("Congratulations! Puzzle completed in ${state.moves} moves!"))
            }
        }
    }

    private fun revealImage() {
        viewModelScope.launch {
            state = state.copy(isRevealingImage = true)
            delay(2000) // Show image for 2 seconds
            state = state.copy(isRevealingImage = false)
        }
    }

    private fun restartPuzzle() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                val puzzle = state.puzzle
                if (puzzle != null) {
                    puzzleManager.restartPuzzle(puzzle).fold(
                        onSuccess = { pieces ->
                            state = state.copy(
                                puzzlePieces = pieces,
                                isLoading = false,
                                isComplete = false,
                                moves = 0
                            )
                        },
                        onFailure = { error ->
                            state = state.copy(isLoading = false)
                            _uiEvent.send(UiEvent.ShowSnackbar(error.message ?: "Error restarting puzzle"))
                        }
                    )
                }
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _uiEvent.send(UiEvent.ShowSnackbar(e.message ?: "Error restarting puzzle"))
            }
        }
    }
} 