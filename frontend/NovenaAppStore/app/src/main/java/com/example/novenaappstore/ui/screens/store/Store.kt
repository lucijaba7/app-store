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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.novenaappstore.ApkInstaller
import com.example.novenaappstore.R
import com.example.novenaappstore.ui.theme.PoppinsFontFamily


@Composable
fun StoreScreen() {
    //val viewModel: StoreViewModel = hiltViewModel()
    LazyColumn {
        // Tu ce bit lista apps a ne nazivi samo
    itemsIndexed(
        listOf("Neka Novenina app", "Lalalla", "audio vodic", "Opet audio vodic al duzi naziv", "LAlalalala lal aa")
    ) {
        index, string -> AppItem(string)
    }

    //items(10000) {
        //    AppItem();
        //}
    }


}

@Composable
fun AppItem(title: String) {
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
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Photo of android",
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis

                )
                Text(
                    "v0.0.1", fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 8.sp
                )
            }


            val context = LocalContext.current
            Button(onClick = {
                Log.e("Install", "Install app");
                ApkInstaller.requestInstallPermission(context)
                ApkInstaller.installApk(context)
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