package com.md.mypuzzleapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL

/**
 * Utility class for handling image operations
 */
object ImageUtils {
    
    /**
     * Loads a bitmap from a URI with proper scaling to avoid OutOfMemoryError
     */
    suspend fun loadBitmapFromUri(context: Context, uri: Uri, maxWidth: Int = 1024, maxHeight: Int = 1024): Bitmap = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }
        
        var width = options.outWidth
        var height = options.outHeight
        var inSampleSize = 1
        
        if (width > maxWidth || height > maxHeight) {
            val halfWidth = width / 2
            val halfHeight = height / 2
            
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfWidth / inSampleSize) >= maxWidth && (halfHeight / inSampleSize) >= maxHeight) {
                inSampleSize *= 2
            }
        }
        
        options.apply {
            inJustDecodeBounds = false
            inSampleSize = inSampleSize
        }
        
        var bitmap: Bitmap? = null
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            bitmap = BitmapFactory.decodeStream(inputStream, null, options)
        }
        
        // Ensure the bitmap is in a square format for the puzzle
        bitmap?.let { 
            createSquareBitmap(it)
        } ?: throw IllegalStateException("Could not load bitmap from URI")
    }
    
    /**
     * Loads a bitmap from a URL (e.g., Firebase Storage download URL)
     */
    suspend fun loadBitmapFromUrl(url: String): Bitmap = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection()
        connection.connect()
        val inputStream = connection.getInputStream()
        BitmapFactory.decodeStream(inputStream) ?: throw IllegalStateException("Could not load bitmap from URL")
    }
    
    /**
     * Creates a square bitmap from any bitmap by cropping the center
     */
    fun createSquareBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        return if (width == height) {
            bitmap
        } else {
            val size = minOf(width, height)
            val x = (width - size) / 2
            val y = (height - size) / 2
            
            Bitmap.createBitmap(bitmap, x, y, size, size)
        }
    }
    
    /**
     * Compresses a bitmap to a byte array
     */
    suspend fun compressBitmap(bitmap: Bitmap, quality: Int = 90): ByteArray = withContext(Dispatchers.IO) {
        ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            stream.toByteArray()
        }
    }
} 