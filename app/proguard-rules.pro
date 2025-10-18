# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Suppress SLF4J missing-class warnings (e.g., org.slf4j.impl.StaticLoggerBinder)
# This avoids R8 failing on release builds when only slf4j-api is present
# and no runtime binding is packaged.
-dontwarn org.slf4j.**

# ========== SUPABASE RULES ==========
# Keep Supabase core client classes for puzzle data operations
-keep class io.github.jan.supabase.SupabaseClient { *; }
-keep class io.github.jan.supabase.SupabaseClientBuilder { *; }

# Keep only used modules for saving and progress
-keep class io.github.jan.supabase.gotrue.** { *; }
-keep class io.github.jan.supabase.postgrest.** { *; }
-keep class io.github.jan.supabase.storage.** { *; }
-keep class io.github.jan.supabase.realtime.** { *; }

# ========== KOTLIN SERIALIZATION RULES ==========
# Keep serializable classes and their companion objects for puzzle data
-keep class ** implements kotlinx.serialization.Serializable { *; }
-keep class **.Companion implements kotlinx.serialization.internal.GeneratedSerializer { *; }

# Keep core serialization classes
-keep class kotlinx.serialization.json.** { *; }

# ========== GSON RULES ==========
# Keep Gson classes and their constructors
-keep class com.google.gson.Gson { *; }
-keep class com.google.gson.GsonBuilder { *; }
-keep class com.google.gson.stream.JsonReader { *; }
-keep class com.google.gson.stream.JsonWriter { *; }
-keep class com.google.gson.TypeAdapter { *; }
-keep class com.google.gson.reflect.TypeToken { *; }

# ========== DAGGER HILT RULES ==========
# Keep Dagger Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.AndroidEntryPoint { *; }
-keep class * implements dagger.hilt.EntryPoint { *; }

# Keep Hilt modules and their provide methods
-keep class * implements dagger.hilt.InstallIn { *; }
-keep class * implements dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Keep Hilt generated components
-keep class dagger.hilt.internal.**.** { *; }
-keep class dagger.hilt.android.internal.**.** { *; }

# ========== COROUTINES RULES ==========
# Keep coroutine core classes for puzzle async operations
-keep class kotlinx.coroutines.** { *; }

# ========== DATASTORE RULES ==========
# Keep DataStore classes and their serializers
-keep class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.** { *; }

# ========== COIL IMAGE LOADING RULES ==========
# Keep Coil classes and their image loaders
-keep class coil.** { *; }
-keep class coil.request.** { *; }
-keep class coil.target.** { *; }

# ========== GENERAL ANDROID RULES ==========
# Keep ViewModel classes and their constructors
-keep class androidx.lifecycle.ViewModel { *; }
-keep class androidx.lifecycle.AndroidViewModel { *; }

# Keep Compose core classes for UI rendering in puzzles
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.runtime.** { *; }

# Keep navigation classes
-keep class androidx.navigation.** { *; }

# Keep activity and fragment classes
-keep class * extends androidx.activity.ComponentActivity { *; }
-keep class * extends androidx.fragment.app.Fragment { *; }

# ========== RETROFIT AND OKHTTP RULES ==========
# Keep Retrofit and OkHttp core classes for networking
-keep class com.squareup.retrofit2.Retrofit { *; }
-keep class okhttp3.OkHttpClient { *; }
-keep class retrofit2.http.** { *; }
-dontwarn okhttp3.**
-dontwarn com.squareup.retrofit2.**

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
 -keep,allowobfuscation,allowshrinking interface retrofit2.Call
 -keep,allowobfuscation,allowshrinking class retrofit2.Response

 # With R8 full mode generic signatures are stripped for classes that are not
 # kept. Suspend functions are wrapped in continuations where the type argument
 # is used.
 -keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# ========== ACCOMPANIST RULES ==========
# Keep Accompanist classes
-keep class com.google.accompanist.** { *; }

# ========== MODEL CLASSES ==========
# Keep PuzzlePiece and its fields for UI state restoration
-keep class com.md.mypuzzleapp.domain.model.PuzzlePiece { *; }
-keep class com.md.mypuzzleapp.presentation.puzzle.PuzzlePiece { *; }

# Keep PuzzleManager state and flows for Compose observation
-keep class com.md.mypuzzleapp.manager.PuzzleManager { *; }
-keep class kotlinx.coroutines.flow.MutableStateFlow { *; }
-keep class kotlinx.coroutines.flow.StateFlow { *; }

# Ensure PiecePlacement serialization for progress
-keep class com.md.mypuzzleapp.domain.model.PiecePlacement { *; }
-keep class com.md.mypuzzleapp.domain.model.PuzzleProgress { *; }

# Keep your domain model classes that are serialized
-keep class com.md.mypuzzleapp.domain.model.** { *; }
-keep class com.md.mypuzzleapp.data.source.** { *; }

# Keep enum classes
-keep class * extends java.lang.Enum { *; }

# Keep logging for release debugging
#-keep class android.util.Log { *; }
#-keep class java.util.logging.Logger { *; }
#-keep class org.slf4j.** { *; }  # If using SLF4J