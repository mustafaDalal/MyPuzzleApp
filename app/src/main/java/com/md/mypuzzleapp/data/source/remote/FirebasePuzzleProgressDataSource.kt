package com.md.mypuzzleapp.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.md.mypuzzleapp.data.source.PuzzleProgressDataSource
import com.md.mypuzzleapp.domain.model.PiecePlacement
import com.md.mypuzzleapp.domain.model.PuzzleProgress
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of the PuzzleProgressDataSource interface.
 * Handles all puzzle progress data operations with Firebase Firestore.
 */
@Singleton
class FirebasePuzzleProgressDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : PuzzleProgressDataSource {
    private val progressCollection = firestore.collection("puzzle_progress")
    
    override fun getPuzzleProgress(puzzleId: String): Flow<PuzzleProgress?> = callbackFlow {
        val listenerRegistration = progressCollection.document(puzzleId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val progress = snapshot?.toObject(PuzzleProgress::class.java)
                trySend(progress)
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    override suspend fun savePuzzleProgress(puzzleProgress: PuzzleProgress) {
        progressCollection.document(puzzleProgress.puzzleId)
            .set(puzzleProgress)
            .await()
    }
    
    override suspend fun updatePiecePlacement(puzzleId: String, piecePlacement: PiecePlacement) {
        val progressDoc = progressCollection.document(puzzleId)
        val progressSnapshot = progressDoc.get().await()
        
        if (progressSnapshot.exists()) {
            progressDoc.update(
                "piecePlacements.${piecePlacement.pieceId}", piecePlacement,
                "lastUpdated", System.currentTimeMillis()
            ).await()
        } else {
            val piecePlacements = mapOf(piecePlacement.pieceId to piecePlacement)
            val puzzleProgress = PuzzleProgress(
                puzzleId = puzzleId,
                piecePlacements = piecePlacements,
                startTime = System.currentTimeMillis()
            )
            savePuzzleProgress(puzzleProgress)
        }
    }
    
    override suspend fun deletePuzzleProgress(puzzleId: String) {
        progressCollection.document(puzzleId).delete().await()
    }
} 