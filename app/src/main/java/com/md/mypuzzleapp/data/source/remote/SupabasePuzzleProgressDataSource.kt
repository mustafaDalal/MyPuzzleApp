package com.md.mypuzzleapp.data.source.remote

import com.md.mypuzzleapp.data.source.PuzzleProgressDataSource
import com.md.mypuzzleapp.domain.model.PiecePlacement
import com.md.mypuzzleapp.domain.model.PuzzleProgress
import com.md.mypuzzleapp.domain.model.SupabasePuzzleProgressDto
import com.md.mypuzzleapp.domain.model.toSupabaseDto
import com.md.mypuzzleapp.domain.model.toDomain
import com.md.mypuzzleapp.di.SupabaseModule
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class SupabasePuzzleProgressDataSource : PuzzleProgressDataSource {
    override fun getPuzzleProgress(puzzleId: String): Flow<PuzzleProgress?> = flow {
        try {
            val response = withContext(Dispatchers.IO) {
                SupabaseModule.database
                    .from("puzzle_progress")
                    .select(columns = Columns.list("id", "puzzle_id", "user_id", "completed_pieces", "total_pieces", "is_completed", "last_played", "piece_placements")) {
                        filter { eq("puzzle_id", puzzleId) }
                    }
                    .decodeSingle<SupabasePuzzleProgressDto>()
            }
            val progress = response.toDomain()
            emit(progress)
        } catch (e: Exception) {
            emit(null)
        }
    }

    override suspend fun savePuzzleProgress(puzzleProgress: PuzzleProgress) = withContext(Dispatchers.IO) {
        try {
            val progressDto = puzzleProgress.toSupabaseDto()
            SupabaseModule.database
                .from("puzzle_progress")
                .upsert(progressDto)
            Unit
        } catch (e: Exception) {
            throw Exception("Failed to save progress: ${e.message}")
        }
    }

    override suspend fun updatePiecePlacement(puzzleId: String, piecePlacement: PiecePlacement) = withContext(Dispatchers.IO) {
        try {
            // First, get existing progress or create new one
            val existingProgress = try {
                SupabaseModule.database
                    .from("puzzle_progress")
                    .select(columns = Columns.list("id", "puzzle_id", "user_id", "completed_pieces", "total_pieces", "is_completed", "last_played", "piece_placements")) {
                        filter { eq("puzzle_id", puzzleId) }
                    }
                    .decodeSingle<SupabasePuzzleProgressDto>()
            } catch (e: Exception) {
                null
            }

            if (existingProgress != null) {
                // Update existing progress
                val updatedProgress = existingProgress.copy(
                    completedPieces = existingProgress.completedPieces + (if (piecePlacement.isPlaced) 1 else -1),
                    lastPlayed = System.currentTimeMillis().toString()
                )

                SupabaseModule.database
                    .from("puzzle_progress")
                    .update(updatedProgress) {
                        filter { eq("id", existingProgress.id) }
                    }
                Unit
            } else {
                // Create new progress
                val newProgress = PuzzleProgress(
                    puzzleId = puzzleId,
                    piecePlacements = mapOf(piecePlacement.pieceId.toString() to piecePlacement),
                    startTime = System.currentTimeMillis(),
                    lastUpdated = System.currentTimeMillis()
                )

                savePuzzleProgress(newProgress)
            }
        } catch (e: Exception) {
            throw Exception("Failed to update piece placement: ${e.message}")
        }
    }

    override suspend fun deletePuzzleProgress(puzzleId: String) = withContext(Dispatchers.IO) {
        try {
            SupabaseModule.database
                .from("puzzle_progress")
                .delete {
                    filter { eq("puzzle_id", puzzleId) }
                }
            Unit
        } catch (e: Exception) {
            throw Exception("Failed to delete progress: ${e.message}")
        }
    }
}
