package com.md.mypuzzleapp.presentation.puzzle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.md.mypuzzleapp.presentation.common.UiEvent
import kotlin.math.roundToInt

data class CellBounds(
    val position: Int,
    val rect: Rect
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleScreen(
    onNavigateBack: () -> Unit,
    viewModel: PuzzleViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }
    val dragAndDropState = remember { DragAndDropState() }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val cellBounds = remember { mutableStateMapOf<Int, CellBounds>() }
    var startPosition by remember { mutableStateOf(Offset.Zero) }
    
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.NavigateUp -> {
                    onNavigateBack()
                }
                else -> Unit
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(state.puzzle?.name ?: "")
                        Text(
                            text = "Moves: ${state.moves}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(PuzzleEvent.NavigateBack) }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(PuzzleEvent.RevealImage) }
                    ) {
                        Icon(Icons.Default.Visibility, "Reveal Image")
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(PuzzleEvent.RestartPuzzle) }
                    ) {
                        Icon(Icons.Default.Refresh, "Restart")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Puzzle Board
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (state.isRevealingImage) {
                            // Show full image
                            state.puzzle?.let { puzzle ->
                                Image(
                                    bitmap = state.puzzlePieces.first().bitmap.asImageBitmap(),
                                    contentDescription = "Full puzzle image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        } else {
                            // Show puzzle grid
                            val gridSize = state.puzzle?.difficulty?.gridSize ?: return@Box
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(gridSize),
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                items(
                                    items = state.boardPieces,
                                    key = { index -> index?.id ?: index.toString() }
                                ) { piece ->
                                    PuzzleCell(
                                        piece = piece,
                                        isDropTarget = dragAndDropState.isDragging,
                                        onDrop = { pieceId ->
                                            viewModel.onEvent(PuzzleEvent.DropPiece(pieceId, piece?.currentPosition ?: -1))
                                        },
                                        onPositioned = { position, layoutCoordinates ->
                                            cellBounds[position] = CellBounds(
                                                position = position,
                                                rect = Rect(
                                                    left = layoutCoordinates.left,
                                                    top = layoutCoordinates.top,
                                                    right = layoutCoordinates.right,
                                                    bottom = layoutCoordinates.bottom
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Unplaced Pieces
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = state.unplacedPieces,
                                key = { it.id }
                            ) { piece ->
                                UnplacedPieceItem(
                                    piece = piece,
                                    isDragging = dragAndDropState.draggedPieceId == piece.id,
                                    dragOffset = if (dragAndDropState.draggedPieceId == piece.id) dragOffset else Offset.Zero,
                                    onDragStart = { offset ->
                                        startPosition = offset
                                        dragAndDropState.startDragging(piece.id, -1)
                                        dragOffset = Offset.Zero
                                    },
                                    onDrag = { change ->
                                        dragOffset += change
                                        val currentPosition = startPosition + dragOffset
                                        
                                        // Check if we're over a valid drop target
                                        cellBounds.values.forEach { cellBound ->
                                            if (isPositionInBounds(currentPosition, cellBound.rect)) {
                                                viewModel.onEvent(PuzzleEvent.DropPiece(piece.id, cellBound.position))
                                                dragAndDropState.stopDragging()
                                                dragOffset = Offset.Zero
                                                return@forEach
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        dragAndDropState.stopDragging()
                                        dragOffset = Offset.Zero
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Dragged piece overlay
            if (dragAndDropState.isDragging) {
                dragAndDropState.draggedPieceId?.let { pieceId ->
                    val piece = state.unplacedPieces.find { it.id == pieceId }
                        ?: state.boardPieces.filterNotNull().find { it.id == pieceId }
                    
                    piece?.let {
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(
                                    (startPosition.x + dragOffset.x).roundToInt(),
                                    (startPosition.y + dragOffset.y).roundToInt()
                                )}
                                .size(100.dp)
                                .shadow(8.dp)
                                .zIndex(1f)
                        ) {
                            Image(
                                bitmap = it.bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun isPositionInBounds(position: Offset, bounds: Rect): Boolean {
    return position.x >= bounds.left && position.x <= bounds.right &&
           position.y >= bounds.top && position.y <= bounds.bottom
}

@Composable
private fun PuzzleCell(
    piece: PuzzlePiece?,
    isDropTarget: Boolean,
    onDrop: (Int) -> Unit,
    onPositioned: (Int, Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(
                width = if (isDropTarget) 2.dp else 1.dp,
                color = if (isDropTarget) MaterialTheme.colorScheme.primary else Color.Gray
            )
            .background(MaterialTheme.colorScheme.surface)
            .onGloballyPositioned { coordinates ->
                piece?.let {
                    onPositioned(it.currentPosition, coordinates.boundsInWindow())
                }
            }
    ) {
        piece?.let { puzzlePiece ->
            Image(
                bitmap = puzzlePiece.bitmap.asImageBitmap(),
                contentDescription = "Puzzle piece ${puzzlePiece.id}",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isDropTarget) 0.7f else 1f)
            )
        }
    }
}

@Composable
private fun UnplacedPieceItem(
    piece: PuzzlePiece,
    isDragging: Boolean,
    dragOffset: Offset,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .border(1.dp, Color.Gray)
            .alpha(if (isDragging) 0.5f else 1f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onDragStart(offset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = {
                        onDragEnd()
                    }
                )
            }
    ) {
        Image(
            bitmap = piece.bitmap.asImageBitmap(),
            contentDescription = "Puzzle piece ${piece.id}",
            modifier = Modifier.fillMaxSize()
        )
    }
} 