package com.example.etic.features.auth

import com.example.etic.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.etic.data.local.DbProvider
import kotlinx.coroutines.launch
import at.favre.lib.crypto.bcrypt.BCrypt

@Composable
fun LoginScreen(onLogin: (String) -> Unit) {

    val ctx = LocalContext.current
    // Obtenemos el DAO una sola vez
    val usuarioDao = remember { DbProvider.get(ctx).usuarioDao() }
    val scope = rememberCoroutineScope()

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }
    var loading by rememberSaveable { mutableStateOf(false) }

    fun tryLogin() {
        if (username.isBlank() || password.isBlank()) {
            errorMsg = "Usuario y contraseña requeridos"
            return
        }
        loading = true
        errorMsg = null
        scope.launch {
            val user = usuarioDao.getByUsuario(username)
            val ok = user?.password?.let { storedHash ->
                // storedHash ej: $2y$10$JS8Mhtef4AdF…
                BCrypt.verifyer()
                    .verify(password.toCharArray(), storedHash.toCharArray())
                    .verified
            } ?: false

            loading = false
            if (ok) {
                errorMsg = null
                onLogin(user?.usuario ?: user?.idUsuario ?: username)
            } else {
                errorMsg = "Usuario o contraseña incorrecto"
            }
        }
    }

    // ——— Ancho responsivo del Card ———
    val config = LocalConfiguration.current
    val screenWidthDp = config.screenWidthDp.dp
    val cardWidth = remember(screenWidthDp) {
        if (screenWidthDp < 460.dp) screenWidthDp - 48.dp else 400.dp
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo con desenfoque (igual que tu diseño)
        Image(
            painter = painterResource(id = R.drawable.termografia),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(12.dp),
            contentScale = ContentScale.Crop
        )

        // Leve capa para contraste (igual)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.20f))
        )

        // Contenido centrado (igual)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Card angosto y semitransparente (igual)
            Card(
                modifier = Modifier.width(cardWidth),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.70f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo (igual)
                    Image(
                        painter = painterResource(id = R.drawable.etic_logo_login),
                        contentDescription = "Logo ETIC",
                        contentScale = ContentScale.Fit
                    )

                    Spacer(Modifier.height(40.dp))

                    // Campo Usuario (igual, con icono)
                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Usuario") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Icono usuario"
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Text
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Campo Contraseña (igual, con toggle ojo)
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Icono candado"
                            )
                        },
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

                    val canSubmit = username.isNotBlank() && password.isNotBlank() && !loading
                    Button(
                        onClick = { tryLogin() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canSubmit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF292C57),
                            contentColor = Color.White
                        )
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp),
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Validando…", color = Color.White)
                        } else {
                            Text("Ingresar", color = Color.White)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
