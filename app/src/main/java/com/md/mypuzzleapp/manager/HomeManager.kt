package com.md.mypuzzleapp.manager

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.md.mypuzzleapp.domain.model.Puzzle
import com.md.mypuzzleapp.domain.model.PuzzleDifficulty
import com.md.mypuzzleapp.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.md.mypuzzleapp.domain.model.PuzzleProgress
import com.md.mypuzzleapp.domain.model.PiecePlacement
import android.content.Context
import com.md.mypuzzleapp.domain.repository.PuzzleRepository
import android.util.Log
import com.md.mypuzzleapp.util.DeviceIdUtil

@Singleton
class HomeManager @Inject constructor(
    private val addPuzzlesUseCase: AddPuzzleUseCase,
    private val getAllPuzzlesUseCase: GetAllPuzzlesUseCase,
    private val createPuzzleUseCase: CreatePuzzleUseCase,
    private val createPuzzleFromBitmapUseCase: CreatePuzzleFromBitmapUseCase,
    private val getRandomImageUseCase: GetRandomImageUseCase,
    private val puzzleRepository: PuzzleRepository
) {
    fun getAllPuzzles(): Flow<List<Puzzle>> {
        return getAllPuzzlesUseCase()
    }

    suspend fun createPuzzleWithImage(
        name: String,
        difficulty: String,
        bitmap: Bitmap,
        context: Context
    ): Result<Puzzle> {
        val userId = DeviceIdUtil.getDeviceId(context)
        
        // TODO: Check for existing puzzle with same name and userId using Supabase
        // For now, we'll skip this check and implement it later with Supabase queries
        
        val result = puzzleRepository.addPuzzleWithImage(name, difficulty, bitmap, context)
        if (result.isSuccess) {
            val puzzle = result.getOrNull()
            if (puzzle != null) {
                // Save initial progress (all pieces in starting position, moves = 0)
                val piecePlacements = puzzle.pieces.associate { piece ->
                    piece.id.toString() to PiecePlacement(
                        pieceId = piece.id,
                        currentX = piece.currentPosition,
                        currentY = 0,
                        isPlaced = false
                    )
                }
                val progress = PuzzleProgress(
                    puzzleId = puzzle.id,
                    piecePlacements = piecePlacements,
                    startTime = System.currentTimeMillis()
                )
                Log.d("PuzzleProgress", "Saving initial progress: $progress")
                
                // TODO: Save progress using Supabase instead of Firebase
                // This will be implemented when we add progress persistence
            }
        }
        return result
    }

    suspend fun fetchRandomImage(name: String, difficulty: String, context: Context): Result<Puzzle> {
        return try {
            val response = getRandomImageUseCase()
            if (!response.isSuccessful) {
                return Result.failure(Exception("Failed to fetch image: ${response.code()}"))
            }
            val bitmap = withContext(Dispatchers.IO) {
                response.body()?.byteStream()?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                } ?: throw Exception("Failed to decode image")
            }
            // Use the unified remote creation logic

            createPuzzleWithImage(
                name = name,
                difficulty = difficulty,
                bitmap = bitmap,
                context = context
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 