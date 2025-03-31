package com.md.mypuzzleapp.presentation.puzzle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class DragAndDropState {
    var isDragging by mutableStateOf(false)
        private set
    
    var draggedPieceId by mutableStateOf<Int?>(null)
        private set
    
    var draggedPiecePosition by mutableStateOf<Int?>(null)
        private set
    
    fun startDragging(pieceId: Int, position: Int) {
        isDragging = true
        draggedPieceId = pieceId
        draggedPiecePosition = position
    }
    
    fun stopDragging() {
        isDragging = false
        draggedPieceId = null
        draggedPiecePosition = null
    }
} 