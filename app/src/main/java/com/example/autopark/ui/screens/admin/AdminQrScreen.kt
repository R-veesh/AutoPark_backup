package com.example.autopark.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.autopark.viewmodel.ParkingViewModel

@Composable
fun AdminQrScreen(
    parkingViewModel: ParkingViewModel = viewModel()
) {
    var vehicleNumber by remember { mutableStateOf("") }
    var vehicleId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "Admin QR Confirm",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = vehicleNumber,
            onValueChange = { vehicleNumber = it },
            label = { Text("Vehicle Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = vehicleId,
            onValueChange = { vehicleId = it },
            label = { Text("Vehicle ID (QR result)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                parkingViewModel.confirmVehicle(
                    vehicleNumber = vehicleNumber,
                    vehicleId = vehicleId
                )
            }
        ) {
            Text("CONFIRM ENTRY")
        }
    }
}
