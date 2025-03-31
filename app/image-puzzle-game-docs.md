# Image Puzzle Game - Android Application

## Overview
This is an Android application for solving image puzzles. Users can select difficulty levels, upload images, and save progress to Firebase. The solving board supports drag-and-drop functionality.

## Features
- **Puzzle Selection:** Home screen displays a list of available puzzles.
- **Random & Custom Puzzles:** Users can solve system-provided images or upload their own.
- **Difficulty Levels:** Grid size is determined based on the selected difficulty:
  - Easy: 3x3
  - Medium: 4x4
  - Hard: 5x5 (or more)
- **Saving Progress:** Users can save puzzle progress to Firebase and resume later.
- **Solving Board:**
  - A puzzle grid for arranging pieces.
  - A button to temporarily reveal the full image.
  - A scrollable list containing all puzzle pieces.
  - Drag-and-drop functionality between the list and the board.

## UI Layout

### **Home Screen**
- Displays a list of puzzles.
- Options to start a new puzzle or continue saved ones.
- Upload image button.

### **Puzzle Solving Screen**
- **Puzzle Grid:** Displays the board where users arrange pieces.
- **Scrollable Piece List:** Holds all the shuffled puzzle pieces.
- **Reveal Button:** Shows the full image momentarily.
- **Drag & Drop Support:** Move pieces between the list and the board.
- **Save Progress Option:** Allows saving the current puzzle state.

## Functional Requirements

1. **Image Splitting Logic**  
   - Divide the selected image into equal square pieces based on grid size.
   - Shuffle pieces before displaying.

2. **Drag-and-Drop Mechanism**  
   - Users can move pieces between the grid and the list.
   - Snapping logic to detect correct placement.

3. **Firebase Integration**  
   - Save user progress with the board state and puzzle metadata.
   - Retrieve saved progress when resuming a puzzle.

4. **Puzzle Completion Detection**  
   - Validate when all pieces are placed in correct positions.
   - Show a success message upon completion.

5. **Image Display Logic**  
   - When the "Reveal" button is pressed, show the full image briefly.

## Tech Stack
- **Frontend:** Jetpack Compose (or XML UI)
- **Backend:** Firebase Firestore (for progress saving)
- **Storage:** Firebase Storage (for uploaded images)
- **Image Handling:** Glide/Picasso for loading images
- **Drag-and-Drop:** Compose's Drag-and-Drop API (or custom implementation)

## Notes
- Ensure smooth UI transitions and animations.
- Optimize Firebase reads/writes to minimize data usage.
- Support multiple aspect ratios for uploaded images.
