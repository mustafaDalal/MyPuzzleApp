package com.md.mypuzzleapp.presentation.puzzle

import android.content.ClipData
import android.graphics.Canvas
import android.graphics.Point
import android.view.DragEvent
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.md.mypuzzleapp.presentation.common.UiEvent

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
    val placedPieces by viewModel.placedPieces.collectAsState()
    val unplacedPieces by viewModel.unplacedPieces.collectAsState()
    val correctlyPlacedPieces by viewModel.correctlyPlacedPieces.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val cellBounds = remember { mutableStateMapOf<Int, CellBounds>() }

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
                    IconButton(onClick = { viewModel.onEvent(PuzzleEvent.RevealImage) }) {
                        Icon(Icons.Default.Visibility, "Reveal Image")
                    }
                    IconButton(onClick = { viewModel.onEvent(PuzzleEvent.RestartPuzzle) }) {
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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                            state.puzzle?.let { puzzle ->
                                Image(
                                    bitmap = state.puzzlePieces.first().bitmap.asImageBitmap(),
                                    contentDescription = "Full puzzle image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        } else {
                            val gridSize = state.puzzle?.difficulty?.gridSize ?: return@Box
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(gridSize),
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                items(gridSize * gridSize) { position ->
                                    val placedPiece = placedPieces[position]
                                    PuzzleDropTarget(
                                        position = position,
                                        onDrop = { droppedPieceId ->
                                            viewModel.onEvent(PuzzleEvent.DropPiece(droppedPieceId, position))
                                        }
                                    ) { isInBound ->
                                        PuzzleCell(
                                            piece = placedPiece,
                                            isDropTarget = isInBound,
                                            onPositioned = { pos, bounds ->
                                                cellBounds[pos] = CellBounds(pos, bounds)
                                            },
                                            onReturnPiece = { piece ->
                                                viewModel.onEvent(PuzzleEvent.ReturnPiece(piece, position))
                                            },
                                            isCorrectlyPlaced = placedPiece?.let { 
                                                correctlyPlacedPieces.contains(it.id) 
                                            } ?: false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Unplaced Pieces Row
                    UnplacedPiecesRow(
                        pieces = unplacedPieces,
                        onDragStart = { pieceId ->
                            viewModel.onEvent(PuzzleEvent.DragPiece(pieceId, 0))
                        },
                        onDragEnd = {
                            viewModel.onEvent(PuzzleEvent.StopDragging)
                        },
                        scrollEnabled = !state.isDragging,
                        isDragging = state.isDragging
                    )
                }
            }
        }
    }
}

@Composable
fun DraggablePuzzlePiece(
    piece: PuzzlePiece,
    onDragStarted: (Int) -> Unit,
    onDragEnded: () -> Unit,
    isDragging: Boolean
) {
    val context = LocalContext.current
    var isCurrentlyDragging by remember { mutableStateOf(false) }
    
    // Reset local dragging state when isDragging changes to false
    LaunchedEffect(isDragging) {
        if (!isDragging) {
            isCurrentlyDragging = false
        }
    }
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .shadow(
                elevation = if (isCurrentlyDragging) 12.dp else 2.dp,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    isLongClickable = true
                    
                    setOnLongClickListener { view ->
                        if (!isCurrentlyDragging) {
                            isCurrentlyDragging = true
                            onDragStarted(piece.id)
                            
                            val shadowBuilder = object : View.DragShadowBuilder(view) {
                                override fun onProvideShadowMetrics(outShadowSize: Point, outShadowTouchPoint: Point) {
                                    val width = (view.width * 1.1f).toInt()
                                    val height = (view.height * 1.1f).toInt()
                                    outShadowSize.set(width, height)
                                    outShadowTouchPoint.set(width / 2, height / 2)
                                }

                                override fun onDrawShadow(canvas: Canvas) {
                                    canvas.save()
                                    canvas.scale(1.1f, 1.1f)
                                    view.draw(canvas)
                                    canvas.restore()
                                }
                            }
                            
                            try {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                    view.startDragAndDrop(
                                        ClipData.newPlainText("piece_id", piece.id.toString()),
                                        shadowBuilder,
                                        piece,
                                        View.DRAG_FLAG_OPAQUE
                                    )
                                } else {
                                    @Suppress("DEPRECATION")
                                    view.startDrag(
                                        ClipData.newPlainText("piece_id", piece.id.toString()),
                                        shadowBuilder,
                                        piece,
                                        View.DRAG_FLAG_OPAQUE
                                    )
                                }
                            } catch (e: Exception) {
                                // If drag start fails, reset states
                                isCurrentlyDragging = false
                                onDragEnded()
                            }
                        }
                        true
                    }

                    setOnDragListener { _, event ->
                        when (event.action) {
                            DragEvent.ACTION_DRAG_STARTED -> {
                                true
                            }
                            DragEvent.ACTION_DRAG_ENDED -> {
                                isCurrentlyDragging = false
                                onDragEnded()
                                true
                            }
                            DragEvent.ACTION_DRAG_ENTERED -> {
                                true
                            }
                            DragEvent.ACTION_DRAG_EXITED -> {
                                true
                            }
                            DragEvent.ACTION_DROP -> {
                                isCurrentlyDragging = false
                                true
                            }
                            else -> false
                        }
                    }
                }
            },
            update = { view ->
                view.removeAllViews()
                ComposeView(context).apply {
                    setContent {
                        Image(
                            bitmap = piece.bitmap.asImageBitmap(),
                            contentDescription = "Puzzle piece ${piece.id}",
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(if (isCurrentlyDragging) 0.6f else 1f)
                        )
                    }
                }.also { view.addView(it) }
            }
        )
    }
}

@Composable
fun PuzzleDropTarget(
    position: Int,
    onDrop: (Int) -> Unit,
    content: @Composable (Boolean) -> Unit
) {
    var isInDropZone by remember { mutableStateOf(false) }
    
    Box {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                FrameLayout(context).apply {
                    setOnDragListener { _, event ->
                        when (event.action) {
                            DragEvent.ACTION_DRAG_ENTERED -> {
                                isInDropZone = true
                                true
                            }
                            DragEvent.ACTION_DRAG_EXITED -> {
                                isInDropZone = false
                                true
                            }
                            DragEvent.ACTION_DROP -> {
                                isInDropZone = false
                                val draggedPiece = event.localState as? PuzzlePiece
                                draggedPiece?.let {
                                    onDrop(it.id)
                                }
                                true
                            }
                            DragEvent.ACTION_DRAG_ENDED -> {
                                isInDropZone = false
                                true
                            }
                            else -> true
                        }
                    }
                }
            }
        ) {
            ComposeView(it.context).apply {
                setContent {
                    content(isInDropZone)
                }
            }.also { view -> it.addView(view) }
        }
    }
}

@Composable
private fun PuzzleCell(
    piece: PuzzlePiece?,
    isDropTarget: Boolean,
    onPositioned: (Int, Rect) -> Unit,
    onReturnPiece: (PuzzlePiece) -> Unit,
    isCorrectlyPlaced: Boolean
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(
                width = if (isDropTarget) 2.dp else 1.dp,
                color = when {
                    isCorrectlyPlaced -> Color.Green
                    isDropTarget -> MaterialTheme.colorScheme.primary
                    else -> Color.Gray
                }
            )
            .onGloballyPositioned { coordinates ->
                piece?.let {
                    onPositioned(it.currentPosition, coordinates.boundsInParent())
                }
            }
    ) {
        piece?.let { puzzlePiece ->
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    FrameLayout(ctx).apply {
                        setOnClickListener {
                            onReturnPiece(puzzlePiece)
                        }
                    }
                },
                update = { view ->
                    view.removeAllViews()
                    ComposeView(context).apply {
                        setContent {
                            Image(
                                bitmap = puzzlePiece.bitmap.asImageBitmap(),
                                contentDescription = "Puzzle piece ${puzzlePiece.id}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(if (isCorrectlyPlaced) 0.8f else 1f)
                            )
                        }
                    }.also { view.addView(it) }
                }
            )
        }
    }
}

@Composable
fun UnplacedPiecesRow(
    pieces: List<PuzzlePiece>,
    onDragStart: (Int) -> Unit,
    onDragEnd: () -> Unit,
    scrollEnabled: Boolean,
    isDragging: Boolean
) {
    var isScrolling by remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()
    
    // Reset scrolling state when dragging ends
    LaunchedEffect(isDragging) {
        if (!isDragging) {
            isScrolling = false
        }
    }
    
    // Handle scroll state changes
    LaunchedEffect(scrollState.isScrollInProgress) {
        isScrolling = scrollState.isScrollInProgress
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        LazyRow(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = scrollEnabled && !isDragging && !isScrolling
        ) {
            items(
                items = pieces,
                key = { it.id } // Ensure stable keys for items
            ) { piece ->
                DraggablePuzzlePiece(
                    isDragging = isDragging,
                    piece = piece,
                    onDragStarted = onDragStart,
                    onDragEnded = onDragEnd
                )
            }
        }
    }
} 