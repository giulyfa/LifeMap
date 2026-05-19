package com.example.lifemap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifemap.ui.RegistrationState
import com.example.lifemap.ui.RegistrationViewModel
import com.example.lifemap.ui.theme.Gold
import com.example.lifemap.ui.theme.Green2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    navController: NavController,
    vm: RegistrationViewModel = viewModel()
) {
    var nome by remember { mutableStateOf("") }
    var cognome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val state by vm.state.collectAsState()

    LaunchedEffect(state) {
        if (state is RegistrationState.Success) {
            vm.resetState()
            navController.popBackStack()
        }
    }

    val customTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Green2,
        focusedLabelColor = Green2,
        cursorColor = Green2,
        focusedLeadingIconColor = Green2,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedLeadingIconColor = Color.Gray
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Green2)
                .padding(24.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Torna indietro",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    text = "Crea Account",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Inizia a mappare i tuoi ricordi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // NOME
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Gold) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = customTextFieldColors, // Applichiamo i colori custom senza blu
                modifier = Modifier.fillMaxWidth()
            )

            // COGNOME
            OutlinedTextField(
                value = cognome,
                onValueChange = { cognome = it },
                label = { Text("Cognome") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Gold) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = customTextFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            // EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Gold) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = customTextFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            // PASSWORD
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Gold) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = customTextFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            // CONFERMA PASSWORD
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Conferma Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Gold) },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = customTextFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            // GESTIONE DEGLI ERRORI
            if (state is RegistrationState.Error) {
                Text(
                    text = (state as RegistrationState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // PULSANTE DI REGISTRAZIONE
            Button(
                onClick = { vm.register(nome, cognome, email, password, confirmPassword) },
                enabled = state !is RegistrationState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green2,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (state is RegistrationState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.Black
                    )
                } else {
                    Text(
                        text = "Registrati",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.ime))
        }
    }
}
