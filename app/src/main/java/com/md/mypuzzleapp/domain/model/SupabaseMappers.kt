package com.md.mypuzzleapp.domain.model

import android.graphics.Bitmap
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.Serializable
import java.util.UUID

fun Puzzle.toSupabaseDto(): SupabasePuzzleDto = SupabasePuzzleDto(
    id = id,
    name = name,
    difficulty = when (difficulty) {
        PuzzleDifficulty.EASY -> 3
        PuzzleDifficulty.MEDIUM -> 4
        PuzzleDifficulty.HARD -> 5
    },
    imageUrl = "", // Will be set when image is uploaded
    pieceCount = pieces.size,
    createdAt = createdAt.toString(),
    userId = "" // TODO: Set when user authentication is implemented
)

fun SupabasePuzzleDto.toDomain(): Puzzle = Puzzle(
    id = id,
    name = name,
    difficulty = when (difficulty ?: 3) {
        3 -> PuzzleDifficulty.EASY
        4 -> PuzzleDifficulty.MEDIUM
        5 -> PuzzleDifficulty.HARD
        else -> PuzzleDifficulty.EASY
    },
    pieces = emptyList(), // Will be generated when puzzle is loaded
    originalImage = null, // Will be loaded from imageUrl
    localImageUri = imageUrl?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) },
    createdAt = createdAt.toLongOrNull() ?: System.currentTimeMillis(),
    isCompleted = false,
    moves = 0
)

fun PuzzleProgress.toSupabaseDto(): SupabasePuzzleProgressDto = SupabasePuzzleProgressDto(
    id = UUID.randomUUID().toString(),
    puzzleId = puzzleId,
    moves = moves,
    completedPieces = piecePlacements.values.count { it.isPlaced },
    totalPieces = piecePlacements.size,
    isCompleted = piecePlacements.values.all { it.isPlaced },
    lastPlayed = lastUpdated.toString(),
    piecePlacements = Gson().toJson(piecePlacements)
)

fun SupabasePuzzleProgressDto.toDomain(): PuzzleProgress = PuzzleProgress(
    puzzleId = puzzleId,
    piecePlacements = try {
        val type = object : TypeToken<Map<String, PiecePlacement>>() {}.type
        Gson().fromJson<Map<String, PiecePlacement>>(piecePlacements, type) ?: emptyMap()
    } catch (e: Exception) {
        emptyMap()
    },
    moves = moves,
    startTime = lastPlayed.toLongOrNull() ?: System.currentTimeMillis(),
    lastUpdated = lastPlayed.toLongOrNull() ?: System.currentTimeMillis()
)

@Serializable
data class InsertedPuzzleId(val id: String = "")
