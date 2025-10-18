package com.md.mypuzzleapp.data.source.remote

import android.content.Context
import com.md.mypuzzleapp.data.local.UserPreferences
import com.md.mypuzzleapp.data.source.PuzzleProgressDataSource
import com.md.mypuzzleapp.domain.model.PuzzleProgress
import com.md.mypuzzleapp.domain.model.SupabasePuzzleProgressDto
import com.md.mypuzzleapp.domain.model.toSupabaseDto
import com.md.mypuzzleapp.domain.model.toDomain
import com.md.mypuzzleapp.di.SupabaseModule
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class SupabasePuzzleProgressDataSource(private val context: Context, private val userPreferences: UserPreferences) : PuzzleProgressDataSource {

    override fun getPuzzleProgress(puzzleId: String): Flow<PuzzleProgress?> = flow {
        try {
            val userId = userPreferences.getEffectiveUserId()
            val latest = withContext(Dispatchers.IO) {
                val rows = SupabaseModule.database
                    .from("puzzle_progress")
                    .select(columns = Columns.list("id", "puzzle_id", "user_id", "moves", "completed_pieces", "total_pieces", "is_completed", "last_played", "piece_placements")) {
                        filter {
                            eq("puzzle_id", puzzleId)
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<SupabasePuzzleProgressDto>()

                // Pick most recent by last_played (safe parse)
                rows.maxByOrNull { dto -> dto.lastPlayed.toLongOrNull() ?: 0L }
            }
            val progress = latest?.toDomain()
            emit(progress)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            emit(null)
        }
    }

    override suspend fun savePuzzleProgress(puzzleProgress: PuzzleProgress) = withContext(Dispatchers.IO) {
        try {
            val userId = userPreferences.getEffectiveUserId()
            val progressDto = puzzleProgress.toSupabaseDto().copy(userId = userId)
            SupabaseModule.database
                .from("puzzle_progress")
                .upsert(
                    progressDto,
                    onConflict = "puzzle_id,user_id"
                )
            Unit
        } catch (e: Exception) {
            throw Exception("Failed to save progress: ${e.message}")
        }
    }

    override suspend fun deletePuzzleProgress(puzzleId: String) = withContext(Dispatchers.IO) {
        try {
            val userId = userPreferences.getEffectiveUserId()
            SupabaseModule.database
                .from("puzzle_progress")
                .delete {
                    filter {
                        eq("puzzle_id", puzzleId)
                        eq("user_id", userId)
                    }
                }
            Unit
        } catch (e: Exception) {
            throw Exception("Failed to delete progress: ${e.message}")
        }
    }
}
