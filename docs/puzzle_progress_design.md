# Design doc: Persisting and restoring piece placement correctly

This outlines how to persist “board state” so reopening a puzzle restores pieces exactly where they were (placed on board vs still unplaced), and the moves count is correct.

## Current behavior (from code)
- **Save**: `PuzzleViewModel.saveProgress()` builds `PiecePlacement` for every piece with:
  - `currentX = piece.currentPosition`
  - `isPlaced = piece.currentPosition == piece.correctPosition`
  - Writes as JSON to `puzzle_progress.piece_placements` via `SupabasePuzzleProgressDataSource.savePuzzleProgress()`.
- **Load**: `PuzzleViewModel.loadPuzzle()` mapped `currentPosition` from progress back to pieces and then hydrated manager, which currently put all pieces into `placedPieces` and none in `unplacedPieces`.

## Problems
- **No “on board” flag**: `isPlaced` means “correctly placed” (right spot), not “is on board”. Pieces on board but in a wrong position get saved with `isPlaced=false` and then we can’t distinguish them from pieces still in the drawer.
- **Hydration places everything**: Hydration set all pieces as placed, which incorrectly moves previously unplaced pieces to the board.
- **Moves metric**: Currently derived from count of `isPlaced` entries, which is only correct if you define moves as count of correctly placed pieces. Often you want the real move count persisted.

## Target model

Persist enough data to reconstruct the full board:
- **Per piece**:
  - `pieceId: Int`
  - `position: Int` (0..N-1 board index the piece occupies; meaningful only if on board)
  - `onBoard: Boolean` (true if the piece is placed on the grid, false if still unplaced)
  - `isCorrect: Boolean` (optional; can be recomputed as `position == correctPosition`)
- **Per puzzle**:
  - `moves: Int`
  - `lastPlayed: Long` (epoch millis)

## Data model changes

- Update `PiecePlacement` in `app/src/main/java/com/md/mypuzzleapp/domain/model/PuzzleProgress.kt`:
  - Replace `currentX/currentY/isPlaced` with:
    - `position: Int`
    - `onBoard: Boolean`
    - `isCorrect: Boolean` (optional if you prefer to recompute)
- Update `SupabasePuzzleProgressDto` in `app/src/main/java/com/md/mypuzzleapp/domain/model/SupabasePuzzleDto.kt` to add:
  - `moves: Int` (new column in `puzzle_progress`)
  - Keep `piece_placements` JSON; the schema within JSON now includes `position`, `onBoard`, `isCorrect`.

DB migrations (Supabase):
- Add `moves int` to `puzzle_progress`.
- No change required if `piece_placements` is JSON and backward compatible; new fields will just start appearing.

## Save flow (changes needed)
Files:
- `PuzzleViewModel.kt` save logic
- `PuzzleManager.kt` source of truth for “on board” vs “unplaced”

Steps:
1. Determine “on board” set: use `PuzzleManager.placedPieces` keys (positions) to know which pieces are currently on the grid.
2. For each piece:
   - `onBoard = placedPieces` contains this piece (by id/position mapping).
   - `position = piece.currentPosition` (if `onBoard==true`; can set to `-1` or ignore when `false`).
   - `isCorrect = piece.currentPosition == piece.correctPosition` (or omit and recompute on load).
3. Compute `moves = state.moves`.
4. Build `PuzzleProgress` and map to DTO with these fields; persist via `savePuzzleProgressUseCase`.

Note: You might add a helper in `PuzzleManager` to generate `PiecePlacement` from internal state to avoid duplicate logic.

## Load flow (changes needed)
Files:
- `SupabasePuzzleProgressDataSource.kt` (already picking latest safely)
- `PuzzleViewModel.kt` load logic
- `PuzzleManager.kt` hydration

Steps:
1. Fetch latest progress for the `puzzleId`.
2. Rebuild pieces:
   - For each piece, if the placement for its `pieceId` has `onBoard == true`: set `currentPosition = placement.position`.
   - If `onBoard == false`: keep it in `unplaced`.
3. Hydrate manager with a precise API, e.g.:
   - `puzzleManager.applyRestoredState(placed: Map<Int, PuzzlePiece>, unplaced: List<PuzzlePiece>)`
   - This separates placed and unplaced explicitly; avoid using a single `applyRestoredPieces(List)` that loses this distinction.
4. Set `state.moves = progress.moves` (read from DB). If not persisted yet, fallback to a derived value (e.g., count of on-board or correct pieces).

## Code changes summary (to implement later)
- **Models**:
  - Change `PiecePlacement` to `{ pieceId, position, onBoard, isCorrect }`.
  - Add `moves` to progress DTO/model and table.
- **Data source**:
  - `SupabasePuzzleProgressDataSource.savePuzzleProgress()`: include `moves` and new placement fields.
  - `getPuzzleProgress()`: parse new JSON shape; preserve backward compatibility if needed by defaulting `onBoard`.
- **Manager**:
  - Replace `applyRestoredPieces(List)` with `applyRestoredState(placed: Map<Int, PuzzlePiece>, unplaced: List<PuzzlePiece>)`.
- **ViewModel**:
  - Save: derive `onBoard` from manager’s `placedPieces`, persist `moves`.
  - Load: split into placed/unplaced from progress and hydrate via new manager method.

## Minimal viable alternative (no schema change)
If you can’t change the schema now:
- Keep current DTO but encode `onBoard` inside the existing JSON by adding that field only in JSON without changing SQL columns. The app reads/writes the richer JSON.
- During load, treat entries missing `onBoard` as:
  - `onBoard = true` if that piece has a valid board position; else `false`.
  - This is lossy; better to add `onBoard`.

## Testing checklist
- Start new puzzle, place a subset of pieces (some correct, some incorrect), leave others unplaced.
- Save implicitly via interactions; reopen puzzle:
  - Placed pieces appear on the board at their last positions.
  - Unplaced pieces remain in the drawer.
  - Correctly placed set is reflected.
  - Moves equals persisted value.
- Regression: saving multiple times selects truly latest snapshot.
