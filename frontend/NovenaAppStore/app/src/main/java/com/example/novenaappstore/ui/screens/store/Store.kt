package com.example.novenaappstore.ui.screens.store

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import androidx.navigation.NavController
import com.example.novenaappstore.R

@Composable
fun StoreScreen() {
    //val viewModel: StoreViewModel = hiltViewModel()
    Column {
        AppItem();
    }
}

@Composable
fun AppItem() {
    Card(
        modifier = Modifier
            .height(100.dp)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Photo of a castle",
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
            )
            Text("APP", modifier = Modifier.weight(1f))
            Button(onClick = {

            }) { Text("Download") }
        }
    }
}


suspend fun downloadApk(appName: String) {

}