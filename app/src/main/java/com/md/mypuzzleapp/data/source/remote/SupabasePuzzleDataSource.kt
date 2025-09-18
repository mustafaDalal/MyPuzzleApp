package com.md.mypuzzleapp.data.source.remote

import android.content.Context
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import com.md.mypuzzleapp.data.source.PuzzleDataSource
import com.md.mypuzzleapp.di.SupabaseModule
import com.md.mypuzzleapp.domain.model.*
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import android.graphics.drawable.BitmapDrawable
import com.md.mypuzzleapp.data.local.UserPreferences
import kotlinx.coroutines.flow.firstOrNull
import android.util.Log

class SupabasePuzzleDataSource(private val context: Context,  private val userPreferences: UserPreferences) : PuzzleDataSource {

    private val BUCKET = "puzzle-images"

    companion object {
        private const val TAG = "SupabasePuzzleDS"
    }

    override fun getAllPuzzles(): Flow<List<Puzzle>> = flow {
        try {
            val hashedEmail = userPreferences.hashedEmail.firstOrNull() ?: ""
            Log.d(TAG, "Getting all puzzles for hashed email: ${if (hashedEmail.isNotEmpty()) hashedEmail else "[empty]"}")
            val response = withContext(Dispatchers.IO) {
                SupabaseModule.database
                    .from("puzzles")
                    .select(columns = Columns.list("id", "name", "difficulty", "image_url", "piece_count", "created_at", "user_id")) {
                        filter { eq("user_id", userPreferences.hashedEmail.firstOrNull() ?: "") }
                    }
                    .decodeList<SupabasePuzzleDto>()
            }
            val puzzles = withContext(Dispatchers.IO) {
                response
                    .filter { !it.imageUrl.isNullOrBlank() }
                    .map { dto -> dto.toDomain() }
                    .sortedByDescending { it.createdAt }
            }
            emit(puzzles)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
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
                            eq("user_id", userPreferences.hashedEmail.firstOrNull() ?: "")
                        }
                    }
                    .decodeSingle<SupabasePuzzleDto>()
            }
            val base = response.toDomain()
            val bucket = SupabaseModule.storage.from(BUCKET)
            // Prefer existing image_url; else assume .png path
            val finalUrl = response.imageUrl?.takeIf { it.isNotBlank() } ?: run {
                val folder = userPreferences.hashedEmail.firstOrNull() ?: "guest"
                val path = "$folder/${response.id}.png"
                bucket.publicUrl(path)
            }
            val bmp = try { fetchBitmap(finalUrl) } catch (e: Exception) { null }
            val result = if (bmp != null) {
                base.copy(originalImage = bmp, localImageUri = android.net.Uri.parse(finalUrl))
            } else {
                base.copy(localImageUri = android.net.Uri.parse(finalUrl))
            }
            emit(result)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            emit(null)
        }
    }

    private suspend fun fetchBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false) // needed to get a software bitmap
            .build()
        val result = loader.execute(request)
        if (result is SuccessResult) {
            val drawable = result.drawable
            (drawable as? BitmapDrawable)?.bitmap
        } else null
    }

    override suspend fun addPuzzle(puzzle: Puzzle): String = withContext(Dispatchers.IO) {
        try {
            val userId = userPreferences.hashedEmail.firstOrNull() ?: "guest"
            Log.d(TAG, "Adding puzzle. Hashed email being used: $userId")
            // 1) Upload image to Supabase Storage if available
            val imageUrl: String? = puzzle.originalImage?.let { bmp ->
                val path = "$userId/${puzzle.id}.png"
                val baos = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
                val bytes = baos.toByteArray()
                SupabaseModule.storage
                    .from(BUCKET)
                    .upload(path, bytes, upsert = true)
                val publicUrl = SupabaseModule.storage
                    .from(BUCKET)
                    .publicUrl(path)
                publicUrl
            }

            // 2) Build DTO with image_url populated (if upload happened)
            val dto = puzzle
                .toSupabaseDto()
                .copy(userId = userId, imageUrl = imageUrl ?: "")

            // 3) Insert and return id
            val response = SupabaseModule.database
                .from("puzzles")
                .insert(dto){
                    select(columns = Columns.list("id"))
                }.decodeSingle<InsertedPuzzleId>()
            response.id
        } catch (e: Exception) {
            throw Exception("Failed to add puzzle: ${e.message}")
        }
    }

    override suspend fun updatePuzzle(puzzle: Puzzle) = withContext(Dispatchers.IO) {
        try {
            val userId = userPreferences.hashedEmail.firstOrNull() ?: ""
            Log.d(TAG, "Updating puzzle ${puzzle.id}. Hashed email being used: ${if (userId.isNotEmpty()) userId else "[empty]"}")
            val dto = puzzle.toSupabaseDto().copy(userId = userId)

            val userDto = SupabaseUserDto("","", System.currentTimeMillis().toString())

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
            val userId = userPreferences.hashedEmail.firstOrNull() ?: ""
            Log.d(TAG, "Deleting puzzle $id. Hashed email being used: ${if (userId.isNotEmpty()) userId else "[empty]"}")
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
            response
                .map { it.toDomain() }
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
