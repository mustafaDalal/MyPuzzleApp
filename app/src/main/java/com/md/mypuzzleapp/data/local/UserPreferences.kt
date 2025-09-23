package com.md.mypuzzleapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.md.mypuzzleapp.util.EmailUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_secure_prefs")

private const val TAG = "UserPreferences"
@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.dataStore

    val userEmail: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[USER_EMAIL_KEY]
        }

    val hashedEmail: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[HASHED_EMAIL_KEY]
        }

    suspend fun saveUserEmail(email: String): Boolean {
        Log.d(TAG, "saveUserEmail called with email: $email")
        val processed = EmailUtils.processEmail(email)
        if (processed == null) {
            Log.w(TAG, "Email processing failed. Not saving to DataStore.")
            return false
        }

        Log.d(TAG, "Processed email -> normalized: ${processed.first}, hashed: ${processed.second}")

        dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = processed.first  // normalized email
            preferences[HASHED_EMAIL_KEY] = processed.second // hashed email
        }

        // Read back immediately to verify
        val prefs = dataStore.data.first()
        val savedEmail = prefs[USER_EMAIL_KEY]
        val savedHashed = prefs[HASHED_EMAIL_KEY]
        Log.d(TAG, "Post-save check -> user_email: ${savedEmail ?: "[null]"}, hashed_email: ${savedHashed ?: "[null]"}")

        return true
    }

    suspend fun clearUserData() {
        Log.d(TAG, "Clearing user data from DataStore")
        dataStore.edit { preferences ->
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(HASHED_EMAIL_KEY)
        }
        val prefs = dataStore.data.first()
        Log.d(TAG, "After clear -> user_email: ${prefs[USER_EMAIL_KEY]}, hashed_email: ${prefs[HASHED_EMAIL_KEY]}")
    }

    companion object {
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val HASHED_EMAIL_KEY = stringPreferencesKey("hashed_email")
    }
}
