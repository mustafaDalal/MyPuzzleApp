package com.md.mypuzzleapp.presentation.puzzle

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.md.mypuzzleapp.domain.repository.PuzzleRepository
import com.md.mypuzzleapp.domain.usecase.PreparePuzzlePiecesUseCase
import com.md.mypuzzleapp.presentation.common.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PuzzleViewModel"

@HiltViewModel
class PuzzleViewModel @Inject constructor(
    private val puzzleRepository: PuzzleRepository,
    private val preparePuzzlePiecesUseCase: PreparePuzzlePiecesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(PuzzleState())
        private set

    private val _placedPieces = MutableStateFlow<Map<Int, PuzzlePiece>>(emptyMap())
    val placedPieces = _placedPieces.asStateFlow()

    private val _unplacedPieces = MutableStateFlow<List<PuzzlePiece>>(emptyList())
    val unplacedPieces = _unplacedPieces.asStateFlow()

    private val _correctlyPlacedPieces = MutableStateFlow<Set<Int>>(emptySet())
    val correctlyPlacedPieces = _correctlyPlacedPieces.asStateFlow()

    private var currentlyDraggedPiece: PuzzlePiece? = null
    private var dragStartTime: Long = 0

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
                    currentlyDraggedPiece = _unplacedPieces.value.find { it.id == event.pieceId }
                    dragStartTime = System.currentTimeMillis()
                }
            }
            is PuzzleEvent.DropPiece -> {
                handlePiecePlacement(event.pieceId, event.toPosition)
                resetDragState()
            }
            is PuzzleEvent.ReturnPiece -> {
                handlePieceReturn(event.piece, event.position)
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
                if (System.currentTimeMillis() - dragStartTime > 100) {
                    state = state.copy(isDragging = false)
                    // If we have a dragged piece that wasn't placed, return it to unplaced pieces
                    currentlyDraggedPiece?.let { piece ->
                        if (!_placedPieces.value.values.any { it.id == piece.id }) {
                            if (!_unplacedPieces.value.any { it.id == piece.id }) {
                                _unplacedPieces.value = _unplacedPieces.value + piece
                            }
                        }
                    }
                    resetDragState()
                }
            }
        }
    }

    private fun resetDragState() {
        currentlyDraggedPiece = null
        dragStartTime = 0
    }

    private fun handlePiecePlacement(pieceId: Int, position: Int) {
        val piece = _unplacedPieces.value.find { it.id == pieceId } ?: return

        Log.d(TAG, """
            Piece Placement Details:
            - Piece ID: $pieceId
            - Current Position: $position
            - Correct Position: ${piece.correctPosition}
            - Is Correctly Placed: ${position == piece.correctPosition}
            - Total Placed Pieces: ${_placedPieces.value.size + 1}
            - Remaining Unplaced Pieces: ${_unplacedPieces.value.size - 1}
        """.trimIndent())

        _placedPieces.value += (position to piece)
        _unplacedPieces.value = _unplacedPieces.value.filter { it.id != pieceId }
        state = state.copy(
            moves = state.moves + 1,
            isDragging = false
        )
        
        // Check if the piece is placed correctly
        if (position == piece.correctPosition) {
            _correctlyPlacedPieces.value = _correctlyPlacedPieces.value + piece.id
            Log.d(TAG, "Piece $pieceId placed correctly at position $position")
            viewModelScope.launch {
                _uiEvent.send(UiEvent.ShowSnackbar("Piece placed correctly!"))
            }
        } else {
            _correctlyPlacedPieces.value = _correctlyPlacedPieces.value - piece.id
            Log.d(TAG, "Piece $pieceId placed incorrectly at position $position (should be at ${piece.correctPosition})")
        }
        
        checkPuzzleCompletion()
    }

    private fun handlePieceReturn(piece: PuzzlePiece, position: Int) {
        if (_placedPieces.value[position]?.id == piece.id) {
            Log.d(TAG, """
                Piece Return Details:
                - Piece ID: ${piece.id}
                - Returned From Position: $position
                - Was Correctly Placed: ${_correctlyPlacedPieces.value.contains(piece.id)}
                - Total Placed Pieces: ${_placedPieces.value.size - 1}
                - Remaining Unplaced Pieces: ${_unplacedPieces.value.size + 1}
            """.trimIndent())

            _placedPieces.value = _placedPieces.value - position
            _correctlyPlacedPieces.value = _correctlyPlacedPieces.value - piece.id
            if (!_unplacedPieces.value.any { it.id == piece.id }) {
                _unplacedPieces.value = _unplacedPieces.value + piece
            }
        }
    }

    private fun loadPuzzle(id: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                val puzzle = puzzleRepository.getPuzzleById(id)

                puzzle.collect { puzzleData ->
                    puzzleData?.let { puzzle ->
                        val pieces = preparePuzzlePiecesUseCase(puzzle)
                        state = state.copy(
                            puzzle = puzzle,
                            puzzlePieces = pieces,
                            isLoading = false
                        )
                        _unplacedPieces.value = pieces
                        _placedPieces.value = emptyMap()
                    }
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
        val isComplete = _placedPieces.value.values
            .all { it.currentPosition == it.correctPosition }
        
        if (isComplete && !state.isComplete && _unplacedPieces.value.isEmpty()) {
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
                    val pieces = preparePuzzlePiecesUseCase(puzzle)
                    state = state.copy(
                        puzzlePieces = pieces,
                        isLoading = false,
                        isComplete = false,
                        moves = 0
                    )
                    _unplacedPieces.value = pieces
                    _placedPieces.value = emptyMap()
                    _correctlyPlacedPieces.value = emptySet()
                }
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _uiEvent.send(UiEvent.ShowSnackbar(e.message ?: "Error restarting puzzle"))
            }
        }
    }
} 