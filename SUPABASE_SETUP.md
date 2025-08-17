# Supabase Setup Guide

## Quick Setup Steps

### 1. Get Your Supabase Credentials
1. Go to [supabase.com](https://supabase.com/) and sign in
2. Create a new project (if you haven't already)
3. Go to **Settings** → **API**
4. Copy your **Project URL** and **anon/public key**

### 2. Update SupabaseModule.kt
Replace the placeholder values in `app/src/main/java/com/md/mypuzzleapp/di/SupabaseModule.kt`:

```kotlin
private const val SUPABASE_URL = "https://your-project-id.supabase.co"
private const val SUPABASE_ANON_KEY = "your-anon-key-here"
```

### 3. Set Up Database
1. Go to your Supabase Dashboard → **SQL Editor**
2. Copy and paste the contents of `supabase_setup.sql`
3. Click **Run** to execute the script

### 4. Set Up Storage
1. Go to **Storage** in your Supabase dashboard
2. Create a new bucket called `puzzle-images`
3. Set it to **Public** (for now)

### 5. Test the App
1. Sync your project with Gradle
2. Build and run your app
3. Check the logs for any connection errors

## Common Issues & Solutions

### Issue: "Invalid API key" error
**Solution**: Double-check your Supabase URL and anon key in `SupabaseModule.kt`

### Issue: "Table not found" error
**Solution**: Make sure you've run the `supabase_setup.sql` script in your Supabase dashboard

### Issue: "Permission denied" error
**Solution**: Check your Row Level Security (RLS) policies in Supabase

## What's Fixed

✅ Updated Supabase dependencies to version 2.1.3
✅ Added Kotlin serialization plugin
✅ Fixed DTO field mappings
✅ Updated mapper functions
✅ Fixed Supabase API calls
✅ Added proper serialization annotations

## Next Steps

Once you've completed the setup:
1. Test basic puzzle operations
2. Test image upload/download
3. Migrate your existing data from Firebase
4. Fine-tune any specific requirements

Your app should now compile without errors and work with Supabase! 🚀

