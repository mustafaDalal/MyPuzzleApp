-- Supabase Database Setup for MyPuzzleApp
-- Run this in your Supabase Dashboard -> SQL Editor

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Puzzles table
CREATE TABLE IF NOT EXISTS puzzles (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    name TEXT NOT NULL,
    difficulty INTEGER NOT NULL DEFAULT 3, -- 3=EASY, 4=MEDIUM, 5=HARD
    image_url TEXT,
    piece_count INTEGER NOT NULL DEFAULT 9,
    created_at BIGINT NOT NULL, -- Unix timestamp
    user_id UUID,
    is_completed BOOLEAN DEFAULT FALSE,
    moves INTEGER DEFAULT 0
);

-- Puzzle progress table
CREATE TABLE IF NOT EXISTS puzzle_progress (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    puzzle_id UUID REFERENCES puzzles(id) ON DELETE CASCADE,
    user_id UUID,
    completed_pieces INTEGER DEFAULT 0,
    total_pieces INTEGER NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    last_played BIGINT NOT NULL, -- Unix timestamp
    piece_placements TEXT -- JSON string for piece placements
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_puzzles_user_id ON puzzles(user_id);
CREATE INDEX IF NOT EXISTS idx_puzzles_created_at ON puzzles(created_at);
CREATE INDEX IF NOT EXISTS idx_puzzle_progress_puzzle_id ON puzzle_progress(puzzle_id);
CREATE INDEX IF NOT EXISTS idx_puzzle_progress_user_id ON puzzle_progress(user_id);

-- Enable Row Level Security (RLS)
ALTER TABLE puzzles ENABLE ROW LEVEL SECURITY;
ALTER TABLE puzzle_progress ENABLE ROW LEVEL SECURITY;

-- Create RLS policies (for now, allow all operations - you can restrict later)
CREATE POLICY "Allow all operations on puzzles" ON puzzles FOR ALL USING (true);
CREATE POLICY "Allow all operations on puzzle_progress" ON puzzle_progress FOR ALL USING (true);

-- Insert some sample data for testing
INSERT INTO puzzles (name, difficulty, piece_count, created_at) VALUES
('Sample Easy Puzzle', 3, 9, EXTRACT(EPOCH FROM NOW()) * 1000),
('Sample Medium Puzzle', 4, 16, EXTRACT(EPOCH FROM NOW()) * 1000),
('Sample Hard Puzzle', 5, 25, EXTRACT(EPOCH FROM NOW()) * 1000)
ON CONFLICT DO NOTHING;

