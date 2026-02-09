package com.example.autopark.ui.screens.driver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.ui.viewmodel.OverdueChargesViewModel
import com.example.autopark.util.CurrencyFormatter
import com.example.autopark.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverdueChargesScreen(
    navController: NavController,
    viewModel: OverdueChargesViewModel = hiltViewModel()
) {
    val overdueCharges by viewModel.overdueCharges.collectAsStateWithLifecycle()
    val totalOverdue by viewModel.totalOverdue.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadOverdueCharges()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overdue Charges") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (totalOverdue > 0.0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Total Outstanding Amount",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            CurrencyFormatter.formatCurrency(totalOverdue),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)) {
                    Text(
                        errorMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (overdueCharges.isEmpty()) {
                Text(
                    "No overdue charges",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(overdueCharges) { charge ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Invoice: ${charge.invoiceId.take(8)}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        CurrencyFormatter.formatCurrency(charge.totalDueAmount),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Original: ${CurrencyFormatter.formatCurrency(charge.originalAmount)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "Late Fee: ${CurrencyFormatter.formatCurrency(charge.lateFeeAmount)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "Days Overdue: ${charge.daysOverdue}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "Due Date: ${DateFormatter.formatDate(charge.dueDate)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "Status: ${charge.paymentStatus}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (charge.paymentStatus == "PAID")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
