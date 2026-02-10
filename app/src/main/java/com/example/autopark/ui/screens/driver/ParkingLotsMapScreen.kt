package com.example.autopark.ui.screens.driver

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.data.model.ParkingLot
import com.example.autopark.ui.viewmodel.ParkingLotViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingLotsMapScreen(
    navController: NavController,
    viewModel: ParkingLotViewModel = hiltViewModel()
) {
    val parkingLots by viewModel.parkingLots.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadAllParkingLots()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Parking Lots") },
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Default camera position (New York City)
            val defaultLocation = LatLng(40.7128, -74.0060)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = true,
                        mapType = MapType.NORMAL
                    )
                ) {
                    // Add markers for each parking lot
                    parkingLots.forEach { lot ->
                        if (lot.latitude != 0.0 && lot.longitude != 0.0) {
                            val lotLocation = LatLng(lot.latitude, lot.longitude)
                            val status = if (lot.availableSpots > 0) "OPEN" else "FULL"
                            val statusColor = if (lot.availableSpots > 0) 
                                androidx.compose.ui.graphics.Color.Green 
                            else 
                                androidx.compose.ui.graphics.Color.Red
                            
                            Marker(
                                state = MarkerState(position = lotLocation),
                                title = lot.name,
                                snippet = "$status - ${lot.availableSpots}/${lot.totalSpots} spots available",
                                icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                                    if (lot.availableSpots > 0) 
                                        com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
                                    else 
                                        com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
                                ),
                                onInfoWindowClick = {
                                    // Navigate to parking lot details or booking
                                    // navController.navigate("driver_parking_details/${lot.id}")
                                }
                            )
                        }
                    }
                }

                // Map overlay with parking lot count
                Card(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "${parkingLots.size} Parking Lots",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${parkingLots.count { it.availableSpots > 0 }} Available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
