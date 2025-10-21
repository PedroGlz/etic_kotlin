package com.example.etic.features.auth

import com.example.etic.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun LoginScreen(onLogin: (String) -> Unit) {
    // Credenciales “en duro”
    val VALID_USER = "admin"
    val VALID_PASS = "123"

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }

    fun tryLogin() {
        if (username == VALID_USER && password == VALID_PASS) {
            errorMsg = null
            onLogin(username)
        } else {
            errorMsg = "Usuario o contraseña incorrecto"
        }
    }

    // ——— Ancho responsivo del Card ———
    val config = LocalConfiguration.current
    val screenWidthDp = config.screenWidthDp.dp
    // objetivo “compacto”: 300.dp; si la pantalla es muy angosta, usa (ancho - 48.dp de padding)
    val cardWidth = remember(screenWidthDp) {
        if (screenWidthDp < 460.dp) screenWidthDp - 48.dp else 400.dp
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo con desenfoque
        Image(
            painter = painterResource(id = R.drawable.termografia),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(12.dp),
            contentScale = ContentScale.Crop
        )

        // Leve capa para contraste
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.20f))
        )

        // Contenido centrado
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Card angosto y semitransparente
            Card(
                modifier = Modifier.width(cardWidth), // ⬅️ ahora sí se nota
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.75f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bienvenido",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Usuario") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Email
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Password
                        ),
                        keyboardActions = KeyboardActions(onDone = { tryLogin() }),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (passwordVisible)
                                        "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        }
                    )

                    if (errorMsg != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    val canSubmit = username.isNotBlank() && password.isNotBlank()
                    Button(
                        onClick = { tryLogin() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canSubmit
                    ) {
                        Text("Entrar")
                    }

                    Spacer(Modifier.height(8.dp))

                }
            }
        }
    }
}
