package com.md.mypuzzleapp.data.source.remote

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.ByteArrayOutputStream
import com.md.mypuzzleapp.data.source.PuzzleDataSource
import com.md.mypuzzleapp.di.SupabaseModule
import com.md.mypuzzleapp.domain.model.*
import com.md.mypuzzleapp.util.DeviceIdUtil
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

class SupabasePuzzleDataSource(private val context: Context) : PuzzleDataSource {
    private val userId: String
        get() = DeviceIdUtil.getDeviceId(context)

    private val BUCKET = "puzzle-images"

    override fun getAllPuzzles(): Flow<List<Puzzle>> = flow {
        try {
            val response = withContext(Dispatchers.IO) {
                SupabaseModule.database
                    .from("puzzles")
                    .select(columns = Columns.list("id", "name", "difficulty", "image_url", "piece_count", "created_at", "user_id")) {
                        filter { eq("user_id", userId) }
                    }
                    .decodeList<SupabasePuzzleDto>()
            }
            emit(response.map { it.toDomain() })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun getPuzzleById(id: String): Flow<Puzzle?> = flow {
        try {
            val response = withContext(Dispatchers.IO) {
                SupabaseModule.database
                    .from("puzzles")
                    .select(columns = Columns.list("id", "name", "difficulty", "image_url", "piece_count", "created_at", "user_id")) {
                        filter {
                            eq("id", id)
                            eq("user_id", userId)
                        }
                    }
                    .decodeSingle<SupabasePuzzleDto>()

            }
            emit(response.toDomain())
        } catch (e: Exception) {
            emit(null)
        }
    }

    override suspend fun addPuzzle(puzzle: Puzzle): String = withContext(Dispatchers.IO) {
        try {
            Log.d("SupabasePuzzleDataSource", "addPuzzle: start id=${puzzle.id} name=${puzzle.name} difficulty=${puzzle.difficulty} pieces=${puzzle.pieces.size}")

            // 1) Upload image to Supabase Storage if available
            val imageUrl: String? = puzzle.originalImage?.let { bmp ->
                val path = "$userId/${puzzle.id}.png"
                Log.d("SupabasePuzzleDataSource", "addPuzzle: compressing bitmap to PNG for path=$path")
                val baos = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
                val bytes = baos.toByteArray()

                Log.d("SupabasePuzzleDataSource", "addPuzzle: uploading ${bytes.size} bytes to bucket=$BUCKET path=$path")
                SupabaseModule.storage
                    .from(BUCKET)
                    .upload(path, bytes, upsert = true)

                val publicUrl = SupabaseModule.storage
                    .from(BUCKET)
                    .publicUrl(path)
                Log.d("SupabasePuzzleDataSource", "addPuzzle: got publicUrl=$publicUrl")
                publicUrl
            }

            // 2) Build DTO with image_url populated (if upload happened)
            val dto = puzzle
                .toSupabaseDto()
                .copy(userId = userId, deviceId = userId, imageUrl = imageUrl ?: "")
            Log.d("SupabasePuzzleDataSource", "addPuzzle: inserting DTO name=${dto.name} pieceCount=${dto.pieceCount} imageUrl='${dto.imageUrl}' userId='${dto.userId}'")

            // 3) Insert and return id
            val response = SupabaseModule.database
                .from("puzzles")
                .insert(dto){
                    select(columns = Columns.list("id"))
                }.decodeSingle<InsertedPuzzleId>()
            Log.d("SupabasePuzzleDataSource", "addPuzzle: insert complete returned id=${response.id}")
            response.id
        } catch (e: Exception) {
            Log.e("SupabasePuzzleDataSource", "addPuzzle: failed for id=${puzzle.id}", e)
            throw Exception("Failed to add puzzle: ${e.message}")
        }
    }

    override suspend fun updatePuzzle(puzzle: Puzzle) = withContext(Dispatchers.IO) {
        try {
            val dto = puzzle.toSupabaseDto().copy(userId = userId)

            val userDto = SupabaseUserDto(userId, userId, System.currentTimeMillis().toString())

            SupabaseModule.database
                .from("users")
                .insert(userDto)

            SupabaseModule.database
                .from("puzzles")
                .update(dto) {
                    filter {
                        eq("id", puzzle.id)
                        eq("user_id", userId)
                    }
                }
            Unit // Explicitly return Unit
        } catch (e: Exception) {
            throw Exception("Failed to update puzzle: ${e.message}")
        }
    }

    override suspend fun deletePuzzle(id: String) = withContext(Dispatchers.IO) {
        try {
            SupabaseModule.database
                .from("puzzles")
                .delete {
                    filter {
                        eq("id", id)
                        eq("user_id", userId)
                    }
                }
            Unit // Explicitly return Unit
        } catch (e: Exception) {
            throw Exception("Failed to delete puzzle: ${e.message}")
        }
    }

    override suspend fun getDefaultPuzzles(): List<Puzzle> = withContext(Dispatchers.IO) {
        try {
            val response = SupabaseModule.database
                .from("puzzles")
                .select(columns = Columns.list("id", "name", "difficulty", "image_url", "piece_count", "created_at", "user_id")) {
                    filter { eq("user_id", "default") }
                }
                .decodeList<SupabasePuzzleDto>()
            response.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
