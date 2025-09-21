package com.md.mypuzzleapp.presentation.puzzle

import android.content.ClipData
import android.graphics.Canvas
import android.graphics.Point
import android.view.DragEvent
import android.view.View
import android.widget.FrameLayout
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
 
 
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.md.mypuzzleapp.presentation.common.UiEvent
import com.md.mypuzzleapp.ui.theme.LocalExtendedColors
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
 

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
    
    val ext = LocalExtendedColors.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            state.puzzle?.name ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Moves: ${state.moves}",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ext.primaryBackground,
                    titleContentColor = ext.onPrimaryBackground,
                    navigationIconContentColor = ext.onPrimaryBackground,
                    actionIconContentColor = ext.onPrimaryBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
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
                            state.puzzle?.let { puzzle ->
                                val revealBitmap = puzzle.originalImage ?: state.puzzlePieces.firstOrNull()?.bitmap
                                revealBitmap?.let { bmp ->
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "Full puzzle image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        } else {
                            val gridSize = state.puzzle?.difficulty?.gridSize ?: return@Box
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(gridSize),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(0.5.dp, MaterialTheme.colorScheme.outline),
                                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    items(gridSize * gridSize) { position ->
                                        val placedPiece = placedPieces[position]
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                // Remove individual borders to prevent doubling up
                                        ) {
                                            PuzzleDropTarget(
                                                position = position,
                                                onDrop = { droppedPieceId ->
                                                    viewModel.onEvent(PuzzleEvent.DropPiece(droppedPieceId, position))
                                                }
                                            ) { isInBound ->
                                                PuzzleCell(
                                                    piece = placedPiece,
                                                    isDropTarget = isInBound,
                                                    onPositioned = { pos, rect ->
                                                        cellBounds[pos] = CellBounds(pos, rect)
                                                    },
                                                    onReturnPiece = { piece ->
                                                        viewModel.onEvent(PuzzleEvent.ReturnPiece(piece, position))
                                                    },
                                                    isCorrectlyPlaced = correctlyPlacedPieces.contains(position)
                                                )
                                            }
                                        }
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
                        isDragging = state.isDragging,
                        placedPieces = placedPieces,
                        cellBounds = cellBounds
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
    isDragging: Boolean,
    gridPosition: Int? = null,
    cellBounds: Map<Int, CellBounds> = emptyMap()
) {
    val context = LocalContext.current
    var isCurrentlyDragging by remember { mutableStateOf(false) }
    
    // Animation values for the original piece
    val scale = remember { Animatable(1f) }
    var alpha = remember { Animatable(1f) }
    val dragOffsetX = remember { Animatable(0f) }
    val dragOffsetY = remember { Animatable(0f) }
    
    // Animation values for position when moving to grid
    val positionX = remember { Animatable(0f) }
    val positionY = remember { Animatable(0f) }
    
    // Animation values for the drag shadow
    var shadowScale by remember { mutableStateOf(1.1f) }
    var shadowAlpha by remember { mutableStateOf(0.8f) }
    
    // Track drag position for original piece
    var dragX by remember { mutableStateOf(0f) }
    var dragY by remember { mutableStateOf(0f) }
    
    // Animate drag offset when drag position changes
    LaunchedEffect(dragX, dragY) {
        if (isCurrentlyDragging) {
            dragOffsetX.animateTo(
                targetValue = dragX,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            dragOffsetY.animateTo(
                targetValue = dragY,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
    
    // Animate when dragging starts
    LaunchedEffect(isCurrentlyDragging) {
        if (isCurrentlyDragging) {
            // Scale up and fade slightly
            scale.animateTo(
                targetValue = 1.1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            alpha.animateTo(
                targetValue = 0.9f, // Keep very visible during drag
                animationSpec = tween(durationMillis = 200)
            )
            // Update shadow values
            shadowScale = 1.2f
            shadowAlpha = 0.9f
        }
    }
    
    // Reset animations when dragging ends
    LaunchedEffect(isDragging) {
        if (!isDragging) {
            isCurrentlyDragging = false
            // Animate back to normal state
            scale.animateTo(
                targetValue = if (gridPosition != null) cellBounds[gridPosition]?.rect?.width?.div(100f) ?: 1f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200)
            )
            // Reset drag offset
            dragOffsetX.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            dragOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            // Reset shadow values
            shadowScale = 1.1f
            shadowAlpha = 0.8f
            dragX = 0f
            dragY = 0f
        }
    }

    // Animate to grid position when piece is placed
    LaunchedEffect(gridPosition) {
        if (gridPosition != null) {
            val bounds = cellBounds[gridPosition]
            if (bounds != null) {
                // Animate to grid position
                launch {
                    positionX.animateTo(
                        targetValue = bounds.rect.left,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
                launch {
                    positionY.animateTo(
                        targetValue = bounds.rect.top,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
                // Animate scale to fit grid cell
                launch {
                    scale.animateTo(
                        targetValue = bounds.rect.width / 100f, // Assuming piece size is 100.dp
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
            }
        } else {
            // Reset position when piece is returned to row
            launch {
                positionX.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                positionY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }
    }
    
    val ext = LocalExtendedColors.current
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
//                translationX = positionX.value + dragOffsetX.value
//                translationY = positionY.value + dragOffsetY.value
            }
            .shadow(
                elevation = if (isCurrentlyDragging) 12.dp else 2.dp,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        // Original piece image
        Image(
            bitmap = piece.bitmap.asImageBitmap(),
            contentDescription = "Puzzle piece ${piece.id}",
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha.value),
            contentScale = ContentScale.FillBounds
        )
        
        // Android View for drag handling
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0f), // Make the AndroidView invisible but still interactive
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    isLongClickable = true
                    
                    setOnLongClickListener { view ->
                        if (!isCurrentlyDragging) {
                            isCurrentlyDragging = true
                            onDragStarted(piece.id)
                            
                            val shadowBuilder = object : View.DragShadowBuilder(view) {
                                private var lastX = 0f
                                private var lastY = 0f
                                private var currentScale = 1.1f
                                private val paint = android.graphics.Paint().apply {
                                    this.alpha = (shadowAlpha * 255).toInt()
                                }
                                
                                override fun onProvideShadowMetrics(outShadowSize: Point, outShadowTouchPoint: Point) {
                                    val width = (view.width * currentScale).toInt()
                                    val height = (view.height * currentScale).toInt()
                                    outShadowSize.set(width, height)
                                    outShadowTouchPoint.set(width / 2, height / 2)
                                }

                                override fun onDrawShadow(canvas: Canvas) {
                                    // Calculate smooth position
                                    val targetX = canvas.width / 2f
                                    val targetY = canvas.height / 2f
                                    
                                    // Smooth position transition
                                    lastX += (targetX - lastX) * 0.3f
                                    lastY += (targetY - lastY) * 0.3f
                                    
                                    // Update drag position for original piece
                                    dragX = lastX - canvas.width / 2f
                                    dragY = lastY - canvas.height / 2f
                                    
                                    // Smooth scale transition
                                    currentScale += (shadowScale - currentScale) * 0.3f
                                    
                                    // Update paint alpha
                                    paint.alpha = (shadowAlpha * 255).toInt()
                                    
                                    canvas.save()
//                                    canvas.translate(lastX - canvas.width / 2f, lastY - canvas.height / 2f)
                                    canvas.scale(currentScale, currentScale, canvas.width / 2f, canvas.height / 2f)
                                    canvas.drawBitmap(piece.bitmap, 0f, 0f, paint)
                                    canvas.restore()
                                }
                            }
                            
                            try {
                                view.startDragAndDrop(
                                    ClipData.newPlainText("piece_id", piece.id.toString()),
                                    shadowBuilder,
                                    piece,
                                    View.DRAG_FLAG_OPAQUE
                                )
                            } catch (e: Exception) {
                                isCurrentlyDragging = false
                                onDragEnded()
                            }
                        }
                        true
                    }

                    setOnDragListener { _, event ->
                        when (event.action) {
                            DragEvent.ACTION_DRAG_STARTED -> {
                                shadowScale = 1.2f
                                shadowAlpha = 0.9f
                                true
                            }
                            DragEvent.ACTION_DRAG_ENDED -> {
                                isCurrentlyDragging = false
                                shadowScale = 1.1f
                                shadowAlpha = 0.8f
                                onDragEnded()
                                true
                            }
                            DragEvent.ACTION_DRAG_ENTERED -> true
                            DragEvent.ACTION_DRAG_EXITED -> true
                            DragEvent.ACTION_DROP -> {
                                isCurrentlyDragging = false
                                true
                            }
                            else -> false
                        }
                    }
                }
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
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }
    
    // Animate when piece is correctly placed
    LaunchedEffect(isCorrectlyPlaced) {
        if (isCorrectlyPlaced) {
            launch {
                scale.animateTo(
                    targetValue = 1.05f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                alpha.animateTo(
                    targetValue = 0.8f,
                    animationSpec = tween(durationMillis = 300)
                )
            }
        }
    }
    
    val ext = LocalExtendedColors.current
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .background(ext.tileBackground)
            .border(
                width = 0.5.dp,
                color = when {
                    isCorrectlyPlaced -> MaterialTheme.colorScheme.tertiary
                    isDropTarget -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
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
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alpha.value),
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
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillBounds
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
    isDragging: Boolean,
    placedPieces: Map<Int, PuzzlePiece> = emptyMap(),
    cellBounds: Map<Int, CellBounds> = emptyMap()
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
    
    val ext = LocalExtendedColors.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = ext.cardBackground,
            contentColor = ext.onCardBackground
        )
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
                    onDragEnded = onDragEnd,
                    gridPosition = placedPieces.entries.find { it.value.id == piece.id }?.key,
                    cellBounds = cellBounds
                )
            }
        }
    }
}