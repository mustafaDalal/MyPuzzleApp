package com.md.mypuzzleapp.manager

import android.util.Log
import com.md.mypuzzleapp.domain.model.Puzzle
import com.md.mypuzzleapp.domain.repository.PuzzleRepository
import com.md.mypuzzleapp.domain.usecase.PreparePuzzlePiecesUseCase
import com.md.mypuzzleapp.presentation.puzzle.PuzzlePiece
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.fold
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PuzzleManager"

data class PuzzleWithPieces(
    val puzzle: Puzzle,
    val pieces: List<PuzzlePiece>
)

@Singleton
class PuzzleManager @Inject constructor(
    private val puzzleRepository: PuzzleRepository,
    private val preparePuzzlePiecesUseCase: PreparePuzzlePiecesUseCase
) {
    private val _placedPieces = MutableStateFlow<Map<Int, PuzzlePiece>>(emptyMap())
    val placedPieces = _placedPieces.asStateFlow()

    private val _unplacedPieces = MutableStateFlow<List<PuzzlePiece>>(emptyList())
    val unplacedPieces = _unplacedPieces.asStateFlow()

    private val _correctlyPlacedPieces = MutableStateFlow<Set<Int>>(emptySet())
    val correctlyPlacedPieces = _correctlyPlacedPieces.asStateFlow()

    private var currentlyDraggedPiece: PuzzlePiece? = null
    private var dragStartTime: Long = 0

    suspend fun loadPuzzle(id: String): Result<PuzzleWithPieces> {
        return try {
            val puzzle = puzzleRepository.getPuzzleById(id).first()

            puzzle?.let {
                val pieces = preparePuzzlePiecesUseCase(it)
                _unplacedPieces.value = pieces
                _placedPieces.value = emptyMap()
                Result.success(PuzzleWithPieces(it, pieces))
            } ?: Result.failure(Exception("Puzzle not found"))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun handlePiecePlacement(pieceId: Int, position: Int): Boolean {
        val piece = _unplacedPieces.value.find { it.id == pieceId } ?: return false

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
        
        // Check if the piece is placed correctly
        if (position == piece.correctPosition) {
            _correctlyPlacedPieces.value = _correctlyPlacedPieces.value + piece.id
            Log.d(TAG, "Piece $pieceId placed correctly at position $position")
            return true
        } else {
            _correctlyPlacedPieces.value = _correctlyPlacedPieces.value - piece.id
            Log.d(TAG, "Piece $pieceId placed incorrectly at position $position (should be at ${piece.correctPosition})")
            return false
        }
    }

    fun handlePieceReturn(piece: PuzzlePiece, position: Int) {
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

    fun checkPuzzleCompletion(): Boolean {
        return _placedPieces.value.values.all { it.currentPosition == it.correctPosition } 
            && _unplacedPieces.value.isEmpty()
    }

    suspend fun restartPuzzle(puzzle: Puzzle): Result<List<PuzzlePiece>> {
        return try {
            val pieces = preparePuzzlePiecesUseCase(puzzle)
            _unplacedPieces.value = pieces
            _placedPieces.value = emptyMap()
            _correctlyPlacedPieces.value = emptySet()
            Result.success(pieces)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun setDraggedPiece(piece: PuzzlePiece?) {
        currentlyDraggedPiece = piece
        dragStartTime = System.currentTimeMillis()
    }

    fun resetDragState() {
        currentlyDraggedPiece = null
        dragStartTime = 0
    }

    fun getDraggedPiece(): PuzzlePiece? = currentlyDraggedPiece

    fun getDragStartTime(): Long = dragStartTime
} 