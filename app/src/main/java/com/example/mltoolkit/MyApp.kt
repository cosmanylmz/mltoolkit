package com.example.mltoolkit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.concurrent.ExecutorService

@Composable
fun MyApp(cameraExecutor: ExecutorService) {
    var recognizedText by remember { mutableStateOf("") }
    var isCameraStarted by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isCameraStarted) {
                CameraPreview(cameraExecutor) { text ->
                    recognizedText = text
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = recognizedText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Button(onClick = { isCameraStarted = true }) {
                    Text(text = "Kartınızı okumak için butona tıklayınız")
                }
            }
        }
    }
}
