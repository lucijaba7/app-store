package com.example.novenaappstore.ui.screens.store

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.novenaappstore.data.model.AppState
import com.example.novenaappstore.data.model.AppWithState
import com.example.novenaappstore.ui.theme.PoppinsFontFamily


@Composable
fun StoreScreen(viewModel: StoreViewModel) {

    val apps = viewModel.apps.observeAsState(emptyList()).value
    val loading = viewModel.loading.observeAsState(false).value
    val error = viewModel.error.observeAsState().value
    val openAlertDialog = remember { mutableStateOf(false) }

    // Register the receiver when the composable is first created
    LaunchedEffect(Unit) {
        viewModel.fetchApps()
        viewModel.registerInstallReceiver()
        viewModel.registerUninstallReceiver()
    }

    // Unregister the receiver when the composable is disposed of
    DisposableEffect(Unit) {
        onDispose {
            viewModel.unregisterInstallReceiver()
            viewModel.unregisterUninstallReceiver()
        }
    }

    // Show the error dialog when there is an error
    ErrorDialog(
        error = error,
        onDismiss = { viewModel.clearError(); openAlertDialog.value = false })


    // Refresh
    SimpleInversePullRefresh(viewModel) {
        error?.let {
            openAlertDialog.value = true
        }

        // Display apps if successfully fetched
        if (apps.isNotEmpty()) {

            LazyColumn {
                itemsIndexed(
                    apps
                ) { _, app ->
                    AppItem(app, viewModel) // Display app name
                }
            }
        }

    }

    // Logout
    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = {
                viewModel.logout()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp) // Adjust padding as needed

        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout"
            )
        }
    }
}


@SuppressLint("QueryPermissionsNeeded")
@Composable
fun AppItem(appWithState: AppWithState, viewModel: StoreViewModel) {
    val context = LocalContext.current
    val downloadingAppId by viewModel.downloadingAppId.observeAsState()
    val isAnyDownloading by viewModel.isAnyDownloading.observeAsState(false)
    val isDownloading = downloadingAppId == appWithState.app.id.toString() // Is this app downloading
    val savingAppId by viewModel.savingAppId.observeAsState()
    val isAnySaving by viewModel.isAnySaving.observeAsState(false)
    val isSaving = savingAppId == appWithState.app.id.toString() // Is this app downloading
    val downloadProgress by viewModel.downloadProgress.observeAsState(0)

    Card(
        modifier = Modifier
            .height(100.dp)
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black)
//                    .fillMaxWidth(0.2f)
                    .aspectRatio(1f)
            ) {
                AppImage(appWithState.app.icon)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    appWithState.app.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis

                )
                Text(
                    appWithState.app.version, fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 8.sp
                )
            }

            Button(
                onClick = {
                    when (appWithState.state) {
                        AppState.NOT_INSTALLED -> {
                            viewModel.downloadFile(
                                context,
                                appWithState.app.fileName,
                                appWithState.app.id.toString()
                            )
                        }
                        AppState.OUTDATED -> {
                            // Handle update action (e.g., re-install or update)
                            Log.e("Update", "Update app")
                        }
                        AppState.UP_TO_DATE -> {

                            val packageManager: PackageManager = context.packageManager
                            val intent: Intent? =  packageManager.getLaunchIntentForPackage(appWithState.app.packageName);

                            if (intent != null) {
                                try {
                                    context.startActivity(intent)
                                } catch (e: SecurityException) {
                                    Log.e("APP LAUNCH", "Error starting app launch intent: ", e)
                                    // apps that don't export their launch activity cause this
                                    Toast.makeText(
                                        context,
                                        "Cannot launch app",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            }
                        }
                        AppState.DOWNLOADING -> TODO()
                    }
                },
                modifier = Modifier
                    .wrapContentSize()
                    .height(25.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),

                enabled = !(isAnyDownloading || isAnySaving) // Disable ALL buttons if any download is in progress

            ) {
                if (isDownloading){
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(15.dp),
                        strokeWidth = 2.dp
                    )
                }
                else if (isSaving) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, // Align items to the center vertically
                        horizontalArrangement = Arrangement.spacedBy(5.dp) // Add spacing between items
                    ) {
                        CircularProgressIndicator(
                            progress = (downloadProgress / 100f).coerceIn(0f, 1f),
                            color = Color.White,
                            modifier = Modifier.size(15.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "${downloadProgress}%",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = when (appWithState.state) {
                            AppState.NOT_INSTALLED -> "Install"
                            AppState.OUTDATED -> "Update"
                            AppState.UP_TO_DATE -> "Open"
                            AppState.DOWNLOADING -> TODO()
                        }.uppercase(),
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp
                    )
                }
            }
            // Show "Uninstall" Button if App is Installed
            if (appWithState.state == AppState.UP_TO_DATE) {
                Spacer(modifier = Modifier.width(8.dp)) // Add space between buttons

                Button(
                    onClick = {
                        viewModel.uninstallApp(context, appWithState.app.packageName)
                    },
                    modifier = Modifier
                        .wrapContentSize()
                        .height(25.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    colors = ButtonDefaults.buttonColors(Color.Red)
                ) {
                    Text(
                        text = "Uninstall",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp,
                        color = Color.White
                    )
                }
            }
        }

        }
    }


@Composable
fun LoadingDialog(isVisible: Boolean) {
    if (isVisible) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = if (isSystemInDarkTheme()) Color.DarkGray else Color.White,
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}

@Composable
fun AppImage(base64Icon: String) {
    base64Icon.let {
        val bitmap = decodeBase64ToBitmap(it)
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "App Icon",
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleInversePullRefresh(
    viewModel: StoreViewModel,
    content: @Composable () -> Unit // Child composable as a parameter
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val loading = viewModel.loading.observeAsState(false)

    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = pullToRefreshState,
        isRefreshing = loading.value,
        onRefresh = {
            viewModel.fetchApps()
        }
    ) {
        // Wrap content in AnimatedVisibility for a smooth fade transition
        AnimatedVisibility(
            visible = !loading.value, // Make content visible only when not loading
            enter = fadeIn(animationSpec = tween(durationMillis = 300)), // Fade in effect
            exit = fadeOut(animationSpec = tween(durationMillis = 300)) // Fade out effect
        ) {
            content() // Your main content composable
        }
    }
}

@Composable
fun ErrorDialog(error: String?, onDismiss: () -> Unit) {
    if (error != null) {

        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(text = "Error", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = error)
            },
            confirmButton = {
                TextButton(
                    onClick = { onDismiss() }
                ) {
                    Text("OK")
                }
            }
        )
    }
}