package com.md.mypuzzleapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.md.mypuzzleapp.data.local.UserPreferences
import com.md.mypuzzleapp.data.source.remote.SupabasePuzzleProgressDataSource
import com.md.mypuzzleapp.util.EmailUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    ) : ViewModel() {

    private val _emailState = MutableStateFlow("")
    val emailState: StateFlow<String> = _emailState

    private val _isEmailValid = MutableStateFlow(false)
    val isEmailValid: StateFlow<Boolean> = _isEmailValid

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveResult = MutableStateFlow<SaveResult?>(null)
    val saveResult: StateFlow<SaveResult?> = _saveResult

    init {
        viewModelScope.launch {
            userPreferences.userEmail.collectLatest { email ->
                email?.let {
                    _emailState.value = it
                    validateEmail(it)
                }
            }
        }
    }

    fun onEmailChanged(email: String) {
        _emailState.value = email
        validateEmail(email)
    }

    fun saveEmail() {
        val email = _emailState.value.trim()
        if (!_isEmailValid.value) {
            _saveResult.value = SaveResult.Error("Please enter a valid email address")
            return
        }

        _isSaving.value = true
        viewModelScope.launch {
            try {
                // Save to DataStore with validation and hashing
                val success = userPreferences.saveUserEmail(email)
                
                if (success) {
                    // Here you would also update the Supabase user record with the hashed email
                    // For example: supabaseClient.updateUserHashedEmail(hashedEmail)
                    _saveResult.value = SaveResult.Success("Email saved successfully!")
                } else {
                    _saveResult.value = SaveResult.Error("Failed to save email: Invalid format")
                }
            } catch (e: Exception) {
                _saveResult.value = SaveResult.Error("Failed to save email: ${e.message ?: "Unknown error"}")
            } finally {
                _isSaving.value = false
            }
        }
    }

    private fun validateEmail(email: String) {
        _isEmailValid.value = EmailUtils.isValidEmail(email)
    }

    fun clearSaveResult() {
        _saveResult.value = null
    }
}

sealed class SaveResult {
    data class Success(val message: String) : SaveResult()
    data class Error(val message: String) : SaveResult()
}
