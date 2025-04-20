package com.md.mypuzzleapp.presentation.puzzle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.md.mypuzzleapp.domain.model.Puzzle
import com.md.mypuzzleapp.domain.repository.PuzzleRepository
import com.md.mypuzzleapp.domain.usecase.PreparePuzzlePiecesUseCase
import com.md.mypuzzleapp.presentation.common.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PuzzleViewModel @Inject constructor(
    private val puzzleRepository: PuzzleRepository,
    private val preparePuzzlePiecesUseCase: PreparePuzzlePiecesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(PuzzleState())
        private set

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
                // Handle drag start
            }
            is PuzzleEvent.DropPiece -> {
                handlePieceDrop(event.pieceId, event.toPosition)
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
        }
    }

    private fun loadPuzzle(id: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                val puzzle = puzzleRepository.getPuzzleById(id)

                puzzle.collect{
                    it?.let { puzzleData ->
                        val pieces = preparePuzzlePiecesUseCase(puzzleData)
                        val gridSize = puzzleData.difficulty.gridSize
                        val totalPieces = gridSize * gridSize

                        state = state.copy(
                            puzzle = puzzleData,
                            puzzlePieces = pieces,
                            boardPieces = List(totalPieces) { null },
                            unplacedPieces = pieces,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _uiEvent.send(UiEvent.ShowSnackbar(e.message ?: "Error loading puzzle"))
                _uiEvent.send(UiEvent.NavigateUp)
            }
        }
    }

    private fun handlePieceDrop(pieceId: Int, toPosition: Int) {
        val piece = state.unplacedPieces.find { it.id == pieceId }
            ?: state.boardPieces.filterNotNull().find { it.id == pieceId }
            ?: return

        val currentBoardPieces = state.boardPieces.toMutableList()
        val currentUnplacedPieces = state.unplacedPieces.toMutableList()

        // Remove piece from its current location
        if (piece in state.unplacedPieces) {
            currentUnplacedPieces.remove(piece)
        } else {
            val currentPosition = state.boardPieces.indexOfFirst { it?.id == piece.id }
            if (currentPosition != -1) {
                currentBoardPieces[currentPosition] = null
            }
        }

        // Place piece in new location
        if (toPosition >= 0 && toPosition < currentBoardPieces.size) {
            // If there's a piece in the target position, swap it to unplaced
            currentBoardPieces[toPosition]?.let {
                currentUnplacedPieces.add(it)
            }
            currentBoardPieces[toPosition] = piece.copy(currentPosition = toPosition)
        } else {
            currentUnplacedPieces.add(piece)
        }

        state = state.copy(
            boardPieces = currentBoardPieces,
            unplacedPieces = currentUnplacedPieces,
            moves = state.moves + 1
        )

        checkPuzzleCompletion()
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
        val isComplete = state.boardPieces.filterNotNull()
            .all { it.currentPosition == it.correctPosition }
        
        if (isComplete && !state.isComplete && state.unplacedPieces.isEmpty()) {
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
//                if (puzzle != null) {
//                    val pieces = preparePuzzlePiecesUseCase(puzzle)
//                    val gridSize = puzzle.difficulty.gridSize
//                    val totalPieces = gridSize * gridSize
//
//                    state = state.copy(
//                        puzzlePieces = pieces,
//                        boardPieces = List(totalPieces) { null },
//                        unplacedPieces = pieces,
//                        isLoading = false,
//                        isComplete = false,
//                        moves = 0
//                    )
//                }
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _uiEvent.send(UiEvent.ShowSnackbar(e.message ?: "Error restarting puzzle"))
            }
        }
    }
} 