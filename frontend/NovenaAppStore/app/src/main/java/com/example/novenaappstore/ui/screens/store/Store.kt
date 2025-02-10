package com.example.novenaappstore.ui.screens.store

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.novenaappstore.ApkInstaller
import com.example.novenaappstore.R
import com.example.novenaappstore.data.model.App
import com.example.novenaappstore.ui.theme.PoppinsFontFamily


@Composable
fun StoreScreen(viewModel: StoreViewModel) {
    val apps = viewModel.apps.observeAsState(emptyList()).value
    val loading = viewModel.loading.observeAsState(false).value
    val error = viewModel.error.observeAsState().value

    // Loading Indicator
    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator() // Show loading indicator
        }

    } else {
        // If there's an error, show the error message
        error?.let {
            Text(text = it, color = Color.Red)
        }

        // Display apps if successfully fetched
        if (apps.isNotEmpty()) {
            LazyColumn {
                itemsIndexed(
                    apps
                ) { index, app ->
                    AppItem(app) // Display app name
                }
            }
        } else {
            Text("No apps found") // If no apps are available
        }
    }
}

@Composable
fun AppItem(app: App) {
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
                    .fillMaxWidth(0.2f)
                    .aspectRatio(1f)
            ) {
//                AsyncImage(
//                    model = ImageRequest.Builder(LocalContext.current)
//                        .data(app.appIcon)
//                        .crossfade(true)
//                        .build(),
//                    placeholder = painterResource(R.drawable.ic_launcher_foreground),
//                    contentDescription = "App icon",
//                    contentScale = ContentScale.Inside,
//                    modifier = Modifier.clip(CircleShape),
//                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    app.file_name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis

                )
                Text(
                    app.version, fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 8.sp
                )
            }


            val context = LocalContext.current
            Button(
                onClick = {
                    Log.e("Install", "Install app");
                    ApkInstaller.requestInstallPermission(context)
                    ApkInstaller.installApk(context, app.file_name)
                },
                modifier = Modifier
                    .wrapContentSize()  // Ensures it doesn't take extra space
                    .height(25.dp), // Force a small height
                contentPadding = PaddingValues(horizontal = 10.dp), // Minimal padding

            ) {
                Text(
                    "DOWNLOAD",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp
                )
            }

        }
    }
}