package com.focusos.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusos.app.data.repository.FocusOsRepository
import com.focusos.app.ui.components.GlassBackgroundBox
import com.focusos.app.ui.components.GlassCard
import com.focusos.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    repository: FocusOsRepository,
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    GlassBackgroundBox {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GlassDarkTextPrimary)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Create your NOVA",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = GlassDarkTextPrimary
                    )
                }
            }

            item {
                Text(
                    text = "Start your personalized workspace for focus, studies, and career.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassDarkTextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StatusRedSubtle)
                            .border(1.dp, StatusRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(text = errorMessage!!, color = StatusRed, fontSize = 13.sp)
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Your Name", color = GlassDarkTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color(0x25FFFFFF),
                        focusedContainerColor = Color(0x12FFFFFF),
                        unfocusedContainerColor = Color(0x12FFFFFF),
                        focusedTextColor = GlassDarkTextPrimary,
                        unfocusedTextColor = GlassDarkTextPrimary
                    )
                )
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email address", color = GlassDarkTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color(0x25FFFFFF),
                        focusedContainerColor = Color(0x12FFFFFF),
                        unfocusedContainerColor = Color(0x12FFFFFF),
                        focusedTextColor = GlassDarkTextPrimary,
                        unfocusedTextColor = GlassDarkTextPrimary
                    )
                )
            }

            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password (min 6 characters)", color = GlassDarkTextSecondary) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password",
                                tint = GlassDarkTextSecondary
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color(0x25FFFFFF),
                        focusedContainerColor = Color(0x12FFFFFF),
                        unfocusedContainerColor = Color(0x12FFFFFF),
                        focusedTextColor = GlassDarkTextPrimary,
                        unfocusedTextColor = GlassDarkTextPrimary
                    )
                )
            }

            item {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    label = { Text("Confirm Password", color = GlassDarkTextSecondary) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color(0x25FFFFFF),
                        focusedContainerColor = Color(0x12FFFFFF),
                        unfocusedContainerColor = Color(0x12FFFFFF),
                        focusedTextColor = GlassDarkTextPrimary,
                        unfocusedTextColor = GlassDarkTextPrimary
                    )
                )
            }

            item {
                val buttonShape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(buttonShape)
                        .background(
                            if (isLoading) Brush.linearGradient(listOf(Color(0x20FFFFFF), Color(0x20FFFFFF))) else Brush.linearGradient(listOf(AccentBlue, AccentCyan))
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.4f), buttonShape)
                        .clickable(enabled = !isLoading) {
                            if (name.isBlank()) {
                                errorMessage = "Please enter your name."
                                return@clickable
                            }
                            if (email.isBlank() || !email.contains("@")) {
                                errorMessage = "Please enter a valid email address."
                                return@clickable
                            }
                            if (password.length < 6) {
                                errorMessage = "Password must be at least 6 characters."
                                return@clickable
                            }
                            if (password != confirmPassword) {
                                errorMessage = "Passwords do not match."
                                return@clickable
                            }

                            isLoading = true
                            coroutineScope.launch {
                                val res = repository.signUp(name.trim(), email.trim(), password)
                                isLoading = false
                                if (res.isSuccess) {
                                    Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                    onSignUpSuccess()
                                } else {
                                    errorMessage = res.exceptionOrNull()?.message ?: "Signup failed. Try again."
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Create Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already have an account? ", color = GlassDarkTextSecondary, fontSize = 13.sp)
                    Text(
                        text = "Sign In",
                        color = AccentCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onNavigateToLogin)
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    repository: FocusOsRepository,
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    GlassBackgroundBox {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GlassDarkTextPrimary)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Welcome back",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = GlassDarkTextPrimary
                    )
                }
            }

            item {
                Text(
                    text = "Sign in to access your personal workspace and momentum.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassDarkTextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StatusRedSubtle)
                            .border(1.dp, StatusRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(text = errorMessage!!, color = StatusRed, fontSize = 13.sp)
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email address", color = GlassDarkTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color(0x25FFFFFF),
                        focusedContainerColor = Color(0x12FFFFFF),
                        unfocusedContainerColor = Color(0x12FFFFFF),
                        focusedTextColor = GlassDarkTextPrimary,
                        unfocusedTextColor = GlassDarkTextPrimary
                    )
                )
            }

            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password", color = GlassDarkTextSecondary) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password",
                                tint = GlassDarkTextSecondary
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color(0x25FFFFFF),
                        focusedContainerColor = Color(0x12FFFFFF),
                        unfocusedContainerColor = Color(0x12FFFFFF),
                        focusedTextColor = GlassDarkTextPrimary,
                        unfocusedTextColor = GlassDarkTextPrimary
                    )
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot password?",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onForgotPassword)
                    )
                }
            }

            item {
                val buttonShape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(buttonShape)
                        .background(
                            if (isLoading) Brush.linearGradient(listOf(Color(0x20FFFFFF), Color(0x20FFFFFF))) else Brush.linearGradient(listOf(AccentBlue, AccentCyan))
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.4f), buttonShape)
                        .clickable(enabled = !isLoading) {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter both email and password."
                                return@clickable
                            }

                            isLoading = true
                            coroutineScope.launch {
                                val res = repository.signIn(email.trim(), password)
                                isLoading = false
                                if (res.isSuccess) {
                                    Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                } else {
                                    errorMessage = res.exceptionOrNull()?.message ?: "Sign in failed."
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Don't have an account? ", color = GlassDarkTextSecondary, fontSize = 13.sp)
                    Text(
                        text = "Create one",
                        color = AccentCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onNavigateToSignUp)
                    )
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    repository: FocusOsRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    GlassBackgroundBox {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GlassDarkTextPrimary)
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = GlassDarkTextPrimary
                )
            }

            Text(
                text = "Enter your email address to receive password reset instructions.",
                style = MaterialTheme.typography.bodyMedium,
                color = GlassDarkTextSecondary
            )

            if (isSubmitted) {
                GlassCard {
                    Text(
                        text = "Reset Link Sent",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusGreen
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Check your email inbox for instructions to reset your password.",
                        fontSize = 13.sp,
                        color = GlassDarkTextSecondary
                    )
                }
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email address", color = GlassDarkTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color(0x25FFFFFF),
                        focusedContainerColor = Color(0x12FFFFFF),
                        unfocusedContainerColor = Color(0x12FFFFFF),
                        focusedTextColor = GlassDarkTextPrimary,
                        unfocusedTextColor = GlassDarkTextPrimary
                    )
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = StatusRed, fontSize = 12.sp)
                }

                val buttonShape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(buttonShape)
                        .background(Brush.linearGradient(listOf(AccentBlue, AccentCyan)))
                        .clickable(enabled = !isLoading) {
                            if (email.isBlank() || !email.contains("@")) {
                                errorMessage = "Please enter a valid email."
                                return@clickable
                            }
                            isLoading = true
                            coroutineScope.launch {
                                val res = repository.resetPassword(email.trim())
                                isLoading = false
                                if (res.isSuccess) {
                                    isSubmitted = true
                                } else {
                                    errorMessage = res.exceptionOrNull()?.message ?: "Reset request failed."
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send Reset Link", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
