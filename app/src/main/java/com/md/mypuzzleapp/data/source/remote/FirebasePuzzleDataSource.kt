package com.md.mypuzzleapp.data.source.remote

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.md.mypuzzleapp.data.source.PuzzleDataSource
import com.md.mypuzzleapp.domain.model.Puzzle
import com.md.mypuzzleapp.domain.model.PuzzleDifficulty
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of the PuzzleDataSource interface.
 * Handles all puzzle-related data operations with Firebase Firestore and Storage.
 */
@Singleton
class FirebasePuzzleDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : PuzzleDataSource {
    private val puzzlesCollection = firestore.collection("puzzles")
    
    override fun getAllPuzzles(): Flow<List<Puzzle>> = callbackFlow {
        val listenerRegistration = puzzlesCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val puzzles = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Puzzle::class.java)
                } ?: emptyList()
                
                trySend(puzzles)
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    override fun getPuzzleById(id: String): Flow<Puzzle?> = callbackFlow {
        val listenerRegistration = puzzlesCollection.document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val puzzle = snapshot?.toObject(Puzzle::class.java)
                trySend(puzzle)
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    override suspend fun addPuzzle(puzzle: Puzzle): String {
        val documentReference = puzzlesCollection.add(puzzle).await()
        return documentReference.id
    }
    
    override suspend fun updatePuzzle(puzzle: Puzzle) {
        puzzlesCollection.document(puzzle.id).set(puzzle).await()
    }
    
    override suspend fun deletePuzzle(id: String) {
        puzzlesCollection.document(id).delete().await()
    }
    
//    override suspend fun uploadCustomImage(
//        uri: Uri,
//        name: String,
//        difficulty: PuzzleDifficulty
//    ): Puzzle {
//        val storageRef = storage.reference.child("puzzle_images/${System.currentTimeMillis()}_${uri.lastPathSegment}")
//        val uploadTask = storageRef.putFile(uri).await()
//        val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
//
//        /*val puzzle = Puzzle(
//            name = name,
//            imageUrl = downloadUrl,
//            difficulty = difficulty,
//            isCustom = true
//        )
//
//        val documentReference = puzzlesCollection.add(puzzle).await()
//        return puzzle.copy(id = documentReference.id)
//        */
//
//
//    }
    
    override suspend fun getDefaultPuzzles(): List<Puzzle> {
        val snapshot = puzzlesCollection
            .whereEqualTo("isCustom", false)
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { document ->
            document.toObject(Puzzle::class.java)
        }
    }
} 