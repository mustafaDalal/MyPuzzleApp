package com.md.mypuzzleapp.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.md.mypuzzleapp.util.EmailUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
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

    val deviceGuestId: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[DEVICE_GUEST_ID_KEY]
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
            preferences.remove(DEVICE_GUEST_ID_KEY)
        }
        val prefs = dataStore.data.first()
        Log.d(TAG, "After clear -> user_email: ${prefs[USER_EMAIL_KEY]}, hashed_email: ${prefs[HASHED_EMAIL_KEY]}, device_guest_id: ${prefs[DEVICE_GUEST_ID_KEY]}")
    }

    /**
     * Gets or creates a device-specific guest ID for users who aren't logged in.
     * This ensures each device/installation has its own isolated puzzle storage space.
     */
    suspend fun getOrCreateDeviceGuestId(): String {
        val currentGuestId = deviceGuestId.first()
        if (currentGuestId != null) {
            Log.d(TAG, "Using existing device guest ID: $currentGuestId")
            return currentGuestId
        }

        // Generate a new unique device guest ID
        val newGuestId = "device_${UUID.randomUUID().toString().take(8)}"
        Log.d(TAG, "Generated new device guest ID: $newGuestId")

        dataStore.edit { preferences ->
            preferences[DEVICE_GUEST_ID_KEY] = newGuestId
        }

        return newGuestId
    }

    /**
     * Gets the effective user ID for database operations.
     * Returns hashed email if user is logged in, otherwise returns device guest ID.
     */
    suspend fun getEffectiveUserId(): String {
        val hashedEmail = hashedEmail.first()
        if (hashedEmail != null) {
            Log.d(TAG, "Using logged-in user ID: $hashedEmail")
            return hashedEmail
        }

        val guestId = getOrCreateDeviceGuestId()
        Log.d(TAG, "Using device guest ID: $guestId")
        return guestId
    }

    companion object {
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val HASHED_EMAIL_KEY = stringPreferencesKey("hashed_email")
        private val DEVICE_GUEST_ID_KEY = stringPreferencesKey("device_guest_id")
    }
}
