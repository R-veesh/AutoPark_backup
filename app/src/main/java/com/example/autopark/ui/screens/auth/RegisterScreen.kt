package com.example.autopark.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autopark.ui.viewmodel.AuthUiState
import com.example.autopark.ui.viewmodel.AuthViewModel
import com.example.autopark.util.FormValidator
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                snackbarHostState.showSnackbar(
                    (uiState as AuthUiState.Error).message
                )
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                supportingText = { nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                isError = emailError != null,
                supportingText = { emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it; phoneError = null },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                isError = phoneError != null,
                supportingText = { phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = null },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                isError = passwordError != null,
                supportingText = { passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; confirmPasswordError = null },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                isError = confirmPasswordError != null,
                supportingText = { confirmPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            Spacer(Modifier.height(16.dp))

            Text("Select Role", style = MaterialTheme.typography.labelLarge)

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("driver", "admin").forEach { role ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.selectable(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            role = Role.RadioButton
                        )
                    ) {
                        RadioButton(selected = selectedRole == role, onClick = null)
                        Text(role.replaceFirstChar { it.uppercase() })
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Up")
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Login")
            }
        }
    }
}
