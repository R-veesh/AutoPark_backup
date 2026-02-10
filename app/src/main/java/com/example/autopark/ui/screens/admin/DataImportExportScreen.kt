package com.example.autopark.ui.screens.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataImportExportScreen(
    navController: NavController,
    viewModel: DataImportExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val exportResult by viewModel.exportResult.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show result messages
    LaunchedEffect(exportResult) {
        exportResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Export") },
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
            // Info Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Data Export",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Export parking system data for backup or analysis purposes. " +
                        "Reports can be generated from the Reports section with PDF export support."
                    )
                }
            }

            // Export Options
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Available Reports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    ListItem(
                        headlineContent = { Text("Admin Reports") },
                        supportingContent = { Text("System-wide statistics and revenue reports with PDF export") },
                        leadingContent = {
                            Icon(Icons.Default.AccountBox, null)
                        },
                        trailingContent = {
                            TextButton(
                                onClick = { navController.navigate("admin_reports") }
                            ) {
                                Text("Open")
                            }
                        }
                    )
                    
                    HorizontalDivider()
                    
                    ListItem(
                        headlineContent = { Text("Driver Reports") },
                        supportingContent = { Text("Individual driver parking history with PDF export") },
                        leadingContent = {
                            Icon(Icons.Default.Person, null)
                        },
                        trailingContent = {
                            Text(
                                "Available in Driver Dashboard",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                }
            }

            // Note Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Note",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "All reports are generated using real-time data from Firebase Firestore. " +
                        "PDF export is available for both admin and driver reports.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
