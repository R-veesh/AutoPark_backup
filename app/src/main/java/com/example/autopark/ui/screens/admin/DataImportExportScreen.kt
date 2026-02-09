package com.example.autopark.ui.screens.admin

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.ui.viewmodel.DataImportExportViewModel
import com.example.autopark.util.JsonDataManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataImportExportScreen(
    navController: NavController,
    viewModel: DataImportExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val jsonDataManager = remember { JsonDataManager(context) }
    val scope = rememberCoroutineScope()

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // EXPORT ALL DATA
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    try {
                        jsonDataManager.exportSampleDataToFile(uri)
                        snackbarHostState.showSnackbar("Export completed successfully")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Export failed: ${e.message}")
                    }
                }
            }
        }
    }

    // IMPORT DATA
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    try {
                        jsonDataManager.importData(uri)
                        snackbarHostState.showSnackbar("Import completed successfully")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Import failed: ${e.message}")
                    }
                }
            }
        }
    }

    // EXPORT SAMPLE DATA
    val exportSampleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    try {
                        jsonDataManager.exportSampleDataToFile(uri)
                        snackbarHostState.showSnackbar("Sample data exported")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Export failed: ${e.message}")
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Import / Export") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // EXPORT CARD
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Export Data", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Export all parking data to JSON")

                    if (isLoading) {
                        LinearProgressIndicator(progress = { progress / 100f })
                        Spacer(Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_TITLE, "autopark_data.json")
                            }
                            exportLauncher.launch(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Export")
                    }
                }
            }

            // IMPORT CARD
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Import Data", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Import parking data from JSON")

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                type = "application/json"
                            }
                            importLauncher.launch(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Import")
                    }
                }
            }

            // SAMPLE DATA
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Sample Data", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Generate sample data for testing")

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_TITLE, "sample_data.json")
                            }
                            exportSampleLauncher.launch(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ArrowDropDown, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Export Sample")
                    }
                }
            }
        }
    }
}
