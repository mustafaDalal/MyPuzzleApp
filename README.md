# MyPuzzleApp

A modern Android puzzle app built with Jetpack Compose, Hilt, Coroutines, and a Supabase backend.

## Features
- Device-scoped puzzles (no login needed)
- Default starter puzzles for first-time users
- Open puzzle by ID with image URL derivation and square thumbnails
- Material 3 UI with Compose
- Lightweight persistence with DataStore

## Tech Stack
- Android: Kotlin, Jetpack Compose, Material 3, Navigation Compose
- DI: Hilt
- Concurrency: Kotlin Coroutines
- Networking/Backend: Supabase (PostgREST, Storage, Realtime, GoTrue) via Ktor client
- Images: Coil
- Build: AGP 8.1.0, Kotlin 1.9.0, R8/ProGuard enabled for release

## App Info
- Application ID: `com.md.mypuzzleapp`
- Min SDK: 24
- Target SDK: 34
- Version: `1.0` (versionCode `1`)

## Architecture Overview
- UI: Jetpack Compose screens with Navigation
- Domain/Data: Repository + Remote data source (Supabase)
- Scoping: Device ID used as `user_id` for reads/writes
- Defaults: Built-in puzzles tagged with `user_id = "default"`

### Key Docs
- `docs_private/feature-flows/defaults-and-seeding.md`
- `docs_private/feature-flows/device-identity-and-scoping.md`
- `docs_private/feature-flows/get-puzzle-by-id.md`
- `SUPABASE_SETUP.md`

## Project Structure (high level)
```
MyPuzzleApp/
├─ app/
│  ├─ src/
│  │  ├─ main/
│  │  └─ test/ ...
│  ├─ build.gradle.kts
├─ docs_private/
│  └─ feature-flows/
├─ supabase_setup.sql
├─ SUPABASE_SETUP.md
├─ build.gradle.kts
├─ settings.gradle.kts
```

## Getting Started

### Prerequisites
- Android Studio Giraffe+ (AGP 8.1 compatible)
- JDK 17
- A Supabase project (URL + anon key)

### 1) Clone & Open
Open the project in Android Studio and let Gradle sync.

### 2) Configure Supabase
Follow `SUPABASE_SETUP.md`. In short:
1. Create a bucket `puzzle-images` (public for now).
2. Run `supabase_setup.sql` in Supabase SQL Editor.
3. Add your credentials in `SupabaseModule.kt`:
```kotlin
private const val SUPABASE_URL = "https://your-project-id.supabase.co"
private const val SUPABASE_ANON_KEY = "your-anon-key-here"
```

### 3) Run the App
Use Android Studio Run, or from terminal (Windows):
```powershell
.\gradlew.bat installDebug
```

## Building Release

### Signing Setup
Create an upload keystore and add `key.properties` at the project root:
```
storeFile=../my-upload-keystore.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=upload
keyPassword=YOUR_KEY_PASSWORD
```
The module uses this automatically for `release` if present.

### Build AAB
```powershell
.\gradlew.bat clean bundleRelease
```
Artifacts:
- `app/build/outputs/bundle/release/app-release.aab`
- `app/build/outputs/mapping/release/mapping.txt`

## Play Console (Internal Testing)
1. Create an Internal testing release and upload `app-release.aab`.
2. Add release notes (see below) and testers.
3. Roll out and wait for processing.

### Sample Release Notes (Internal)
```
MyPuzzleApp v1.0 — Internal Test
- First release! 🎉
- Compose + Material 3 UI
- Device-scoped saves (no login)
- Starter puzzles included
- Open by ID; auto thumbnails
- Optimized, smaller build
```

## ProGuard/R8
- Release uses minify and resource shrinking.
- If you see reflection/serialization issues, add rules in `app/proguard-rules.pro`.
- Check `app/build/outputs/mapping/release/` (`mapping.txt`, `missing_rules.txt`).

## Testing
- Unit tests for mapping and data scoping
- Integration tests for Supabase flows and image handling

## Troubleshooting
- Invalid API key: verify Supabase URL and anon key in `SupabaseModule.kt`.
- Table not found: run `supabase_setup.sql`.
- Permission denied: tighten/fix RLS policies in Supabase (currently permissive for testing).
- Upload rejected: bump `versionCode` in `app/build.gradle.kts`.

## Roadmap
- Tight RLS by `user_id` in production
- Optional sign-in and migration from device-scoped data
- More UI polish and tests

## License
This project is provided as-is for personal/learning use. Add your preferred license here.
