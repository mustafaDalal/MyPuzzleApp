package com.md.mypuzzleapp.presentation.common

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Composable that handles permission requests using Activity-based approach
 */
@Composable
fun PermissionHandler(
    permission: String,
    rationale: String = "This permission is needed to access images from your device",
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }
    
    // Check if we already have the permission
    val hasPermission = remember(permission) {
        checkPermission(context, permission)
    }
    
    // Permission request launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }
    
    // Check if we should show rationale
    val shouldShowRationale = remember(permission) {
        if (context is Activity) {
            ActivityCompat.shouldShowRequestPermissionRationale(context, permission)
        } else {
            false
        }
    }
    
    // Request permission on first composition
    LaunchedEffect(key1 = permission) {
        when {
            hasPermission -> {
                onPermissionResult(true)
            }
            shouldShowRationale -> {
                showRationale = true
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
    
    // Show rationale dialog if needed
    if (showRationale) {
        AlertDialog(
            onDismissRequest = {
                showRationale = false
                onPermissionResult(false)
            },
            title = { Text("Permission Required") },
            text = { Text(rationale) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        requestPermissionLauncher.launch(permission)
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        onPermissionResult(false)
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Helper function to check if a permission is granted
 */
private fun checkPermission(context: Context, permission: String): Boolean {
    // For Android 13+ (API 33+), use the appropriate permission for images
    val permissionToCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
                               permission == Manifest.permission.READ_EXTERNAL_STORAGE) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        permission
    }
    
    return ContextCompat.checkSelfPermission(
        context,
        permissionToCheck
    ) == PackageManager.PERMISSION_GRANTED
} 