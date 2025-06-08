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

@Singleton
class HomeManager @Inject constructor(
    private val addPuzzlesUseCase: AddPuzzleUseCase,
    private val getAllPuzzlesUseCase: GetAllPuzzlesUseCase,
    private val createPuzzleUseCase: CreatePuzzleUseCase,
    private val createPuzzleFromBitmapUseCase: CreatePuzzleFromBitmapUseCase,
    private val getRandomImageUseCase: GetRandomImageUseCase
) {
    fun getAllPuzzles(): Flow<List<Puzzle>> {
        return getAllPuzzlesUseCase()
    }

    suspend fun uploadImage(uri: Uri, name: String, difficulty: PuzzleDifficulty): Result<Puzzle> {
        return try {
            val bitmap = createPuzzleUseCase(uri)
            val puzzle = createPuzzleFromBitmapUseCase(
                name = name,
                bitmap = bitmap,
                difficulty = difficulty
            )
            addPuzzlesUseCase(puzzle)
            Result.success(puzzle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchRandomImage(name: String, difficulty: PuzzleDifficulty): Result<Puzzle> {
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

            val puzzle = createPuzzleFromBitmapUseCase(
                name = name,
                bitmap = bitmap,
                difficulty = difficulty
            )
            addPuzzlesUseCase(puzzle)
            Result.success(puzzle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 