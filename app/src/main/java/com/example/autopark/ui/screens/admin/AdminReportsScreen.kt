package com.example.autopark.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.ui.viewmodel.ReportsViewModel
import com.example.autopark.util.CurrencyFormatter
import com.example.autopark.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val monthlyReport by viewModel.monthlyReport.collectAsStateWithLifecycle()
    val revenueStats by viewModel.revenueStats.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val (currentMonth, currentYear) = DateFormatter.getMonthAndYear()
    var selectedMonth by remember { mutableStateOf(currentMonth.toString()) }
    var selectedYear by remember { mutableStateOf(currentYear.toString()) }

    LaunchedEffect(Unit) {
        viewModel.generateMonthlyReport(currentMonth, currentYear)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            item {
                Text(
                    text = "Select Report Period",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = selectedMonth,
                        onValueChange = { selectedMonth = it },
                        label = { Text("Month") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = selectedYear,
                        onValueChange = { selectedYear = it },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        val month = selectedMonth.toIntOrNull() ?: currentMonth
                        val year = selectedYear.toIntOrNull() ?: currentYear
                        viewModel.generateMonthlyReport(month, year)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text("Generate Report")
                }
            }

            if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (monthlyReport.isNotEmpty()) {

                item {
                    Text(
                        text = "Monthly Report",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SimpleReportRow(
                                "Total Parkings",
                                monthlyReport["totalParkings"]?.toString() ?: "0"
                            )
                            SimpleReportRow(
                                "Total Revenue",
                                CurrencyFormatter.formatCurrency(
                                    (monthlyReport["totalRevenue"] as? Number)?.toDouble() ?: 0.0
                                )
                            )
                            SimpleReportRow(
                                "Total Owners",
                                monthlyReport["totalOwners"]?.toString() ?: "0"
                            )
                            SimpleReportRow(
                                "Total Vehicles",
                                monthlyReport["totalVehicles"]?.toString() ?: "0"
                            )
                            SimpleReportRow(
                                "Average Charge",
                                CurrencyFormatter.formatCurrency(
                                    (monthlyReport["averageCharge"] as? Number)?.toDouble() ?: 0.0
                                )
                            )
                        }
                    }
                }

                if (revenueStats.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Revenue Statistics",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                SimpleReportRow(
                                    "Total Revenue",
                                    CurrencyFormatter.formatCurrency(revenueStats["total"] ?: 0.0)
                                )
                                SimpleReportRow(
                                    "Normal Revenue",
                                    CurrencyFormatter.formatCurrency(revenueStats["normal"] ?: 0.0)
                                )
                                SimpleReportRow(
                                    "VIP Revenue",
                                    CurrencyFormatter.formatCurrency(revenueStats["vip"] ?: 0.0)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleReportRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
