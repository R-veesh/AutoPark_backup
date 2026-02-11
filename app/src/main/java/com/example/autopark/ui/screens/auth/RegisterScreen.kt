package com.example.autopark.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.ui.components.*
import com.example.autopark.ui.viewmodel.AuthUiState
import com.example.autopark.ui.viewmodel.AuthViewModel
import com.example.autopark.util.FormValidator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("driver") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    // Animation states
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        showContent = true
    }

    /* ---------------- STATE HANDLING ---------------- */

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Authenticated -> {
                val user = (uiState as AuthUiState.Authenticated).user
                when (user.role) {
                    "admin" -> navController.navigate("admin_dashboard") {
                        popUpTo("register") { inclusive = true }
                    }
                    "driver" -> navController.navigate("driver_dashboard") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            }

            is AuthUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = (uiState as AuthUiState.Error).message,
                        withDismissAction = true
                    )
                }
            }

            else -> Unit
        }
    }

    /* ---------------- VALIDATION ---------------- */

    fun validateForm(): Boolean {
        val nameResult = FormValidator.validateName(name)
        nameError = nameResult.errorMessage

        val emailResult = FormValidator.validateEmail(email)
        emailError = emailResult.errorMessage

        val phoneResult = FormValidator.validatePhoneNumber(phoneNumber)
        phoneError = phoneResult.errorMessage

        val passwordResult = FormValidator.validatePassword(password)
        passwordError = passwordResult.errorMessage

        val confirmResult =
            if (password == confirmPassword)
                FormValidator.ValidationResult(true)
            else
                FormValidator.ValidationResult(false, "Passwords do not match")

        confirmPasswordError = confirmResult.errorMessage

        return nameResult.isValid &&
                emailResult.isValid &&
                phoneResult.isValid &&
                passwordResult.isValid &&
                confirmResult.isValid
    }

    /* ---------------- UI ---------------- */

    Scaffold(
        snackbarHost = { 
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
            )

            // Decorative circles
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = (-30).dp, y = (-30).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Title Section
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(600)) +
                            slideInVertically(animationSpec = tween(600)) { -it / 2 }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Join AutoPark today",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Registration Form Card
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) +
                            slideInVertically(animationSpec = tween(600, delayMillis = 200)) { it }
                ) {
                    ModernCard(
                        shape = RoundedCornerShape(24.dp),
                        elevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp)
                        ) {
                            // Name Field
                            ModernTextField(
                                value = name,
                                onValueChange = { 
                                    name = it
                                    nameError = null
                                },
                                label = "Full Name",
                                placeholder = "Enter your full name",
                                leadingIcon = Icons.Default.Person,
                                isError = nameError != null,
                                errorMessage = nameError ?: ""
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Email Field
                            ModernTextField(
                                value = email,
                                onValueChange = { 
                                    email = it
                                    emailError = null
                                },
                                label = "Email Address",
                                placeholder = "Enter your email",
                                leadingIcon = Icons.Default.Email,
                                keyboardType = KeyboardType.Email,
                                isError = emailError != null,
                                errorMessage = emailError ?: ""
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Phone Field
                            ModernTextField(
                                value = phoneNumber,
                                onValueChange = { 
                                    phoneNumber = it
                                    phoneError = null
                                },
                                label = "Phone Number",
                                placeholder = "Enter your phone number",
                                leadingIcon = Icons.Default.Phone,
                                keyboardType = KeyboardType.Phone,
                                isError = phoneError != null,
                                errorMessage = phoneError ?: ""
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Password Field
                            ModernTextField(
                                value = password,
                                onValueChange = { 
                                    password = it
                                    passwordError = null
                                },
                                label = "Password",
                                placeholder = "Create a password",
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                isError = passwordError != null,
                                errorMessage = passwordError ?: ""
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Confirm Password Field
                            ModernTextField(
                                value = confirmPassword,
                                onValueChange = { 
                                    confirmPassword = it
                                    confirmPasswordError = null
                                },
                                label = "Confirm Password",
                                placeholder = "Confirm your password",
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                isError = confirmPasswordError != null,
                                errorMessage = confirmPasswordError ?: ""
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Role Selection
                            Text(
                                text = "Select Role",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                listOf("driver" to "Vehicle Owner", "admin" to "Administrator").forEach { (role, label) ->
                                    RoleSelectionCard(
                                        role = role,
                                        label = label,
                                        isSelected = selectedRole == role,
                                        onSelect = { selectedRole = role },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Register Button
                            GradientButton(
                                text = "Create Account",
                                onClick = {
                                    if (validateForm()) {
                                        viewModel.register(
                                            email,
                                            password,
                                            name,
                                            phoneNumber,
                                            selectedRole
                                        )
                                    }
                                },
                                enabled = name.isNotBlank() && email.isNotBlank() && 
                                         phoneNumber.isNotBlank() && password.isNotBlank() && 
                                         confirmPassword.isNotBlank(),
                                isLoading = uiState == AuthUiState.Loading
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Back to Login
                            SecondaryButton(
                                text = "Back to Sign In",
                                onClick = { navController.navigateUp() }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun RoleSelectionCard(
    role: String,
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onSelect,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
