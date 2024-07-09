package com.example.mltoolkit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.util.concurrent.ExecutorService

@Composable
fun AppContent(cameraExecutor: ExecutorService) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "mainScreen") {
        composable("mainScreen") {
            MainScreen(navController = navController, cameraExecutor = cameraExecutor)
        }
        composable("resultScreen/{recognizedText}") { backStackEntry ->
            val recognizedText = backStackEntry.arguments?.getString("recognizedText") ?: ""
            ResultScreen(recognizedText)
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController, cameraExecutor: ExecutorService) {
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
                    navController.navigate("resultScreen/$text")
                }
            } else {
                Button(onClick = { isCameraStarted = true }) {
                    Text(text = "Kartınızı okumak için butona tıklayınız")
                }
            }
        }
    }
}

@Composable
fun ResultScreen(recognizedText: String) {
    val lines = recognizedText.split("\n")
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Taranan Bilgiler:",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}
