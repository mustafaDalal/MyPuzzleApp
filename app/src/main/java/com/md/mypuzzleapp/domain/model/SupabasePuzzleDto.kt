package com.md.mypuzzleapp.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabasePuzzleDto(
    val id: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("difficulty")
    val difficulty: Int? = null, // nullable: fallback handled in mapper (default EASY=3)
    @SerialName("image_url")
    val imageUrl: String? = "",
    @SerialName("piece_count")
    val pieceCount: Int = 9,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("device_id")
    val deviceId: String? = null,


)

@Serializable
data class SupabasePuzzleProgressDto(
    val id: String = "",
    @SerialName("puzzle_id")
    val puzzleId: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("completed_pieces")
    val completedPieces: Int = 0,
    @SerialName("total_pieces")
    val totalPieces: Int = 0,
    @SerialName("is_completed")
    val isCompleted: Boolean = false,
    @SerialName("last_played")
    val lastPlayed: String = "",
    @SerialName("piece_placements")
    val piecePlacements: String = "" // JSON string for piece placements
)

@Serializable
data class SupabaseUserDto(
    val id: String = "",
    @SerialName("device_id")
    val deviceId: String = "",
    @SerialName("created_at")
    val createdAt: String = "",
)