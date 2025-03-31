package com.md.mypuzzleapp.presentation.common

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * A composable that handles image picking from the gallery
 */
@Composable
fun ImagePicker(
    onImagePicked: (Uri) -> Unit
) {
    var hasPermission by remember { mutableStateOf(false) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImagePicked(it) }
    }
    
    // Determine which permission to request based on Android version
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    // Permission handler
    PermissionHandler(
        permission = permission,
        onPermissionResult = { granted ->
            hasPermission = granted
        }
    )
    
    // Launch image picker when permission is granted
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            imagePickerLauncher.launch("image/*")
        }
    }
} 