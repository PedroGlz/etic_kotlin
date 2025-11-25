package com.example.etic.features.home

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.etic.R
import com.example.etic.core.current.LocalCurrentInspection
import com.example.etic.core.current.ProvideCurrentInspection
import com.example.etic.core.current.ProvideCurrentUser
import com.example.etic.core.export.exportRoomDbToDownloads
import com.example.etic.data.local.DbProvider
import com.example.etic.features.components.ImageInputButtonGroup
import com.example.etic.features.inspection.ui.home.InspectionScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.etic.ui.theme.FontSizeOption
import kotlin.math.max

private enum class HomeSection { Inspection, Reports }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userName: String,
    currentFontSize: FontSizeOption = FontSizeOption.Large,
    onChangeFontSize: (FontSizeOption) -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current
    val inspeccionDao = remember { DbProvider.get(appContext).inspeccionDao() }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var section by rememberSaveable { mutableStateOf(HomeSection.Inspection) }
    var isLoading by rememberSaveable { mutableStateOf(true) }
    var fontsExpanded by rememberSaveable { mutableStateOf(false) }
    var showInitImagesDialog by rememberSaveable { mutableStateOf(false) }
    var initThermalPath by rememberSaveable { mutableStateOf("") }
    var initDigitalPath by rememberSaveable { mutableStateOf("") }

    val drawerItemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = Color(0xFF202327),
        unselectedContainerColor = Color.Transparent,
        selectedTextColor = Color.White,
        unselectedTextColor = Color.White,
        selectedIconColor = Color.White,
        unselectedIconColor = Color.White
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true, // permite cerrar al tocar fuera / gesto
        scrimColor = Color.Black.copy(alpha = 0.32f),
        drawerContent = {
            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            val drawerWidth = when {
                screenWidth < 360.dp -> screenWidth - 24.dp
                screenWidth < 600.dp -> 250.dp
                else -> 260.dp
            }

            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidth),
                drawerContainerColor = Color(0xFF202327),
                drawerContentColor = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    ListItem(
                        headlineContent = { Text("ETIC System", color = Color.White) },
                        supportingContent = { Text(userName, color = Color.LightGray) },
                        leadingContent = {
                            Image(
                                painter = painterResource(id = R.drawable.img_etic_menu),
                                contentDescription = "Logo ETIC",
                                modifier = Modifier.size(65.dp)
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 4.dp),
                        thickness = androidx.compose.material3.DividerDefaults.Thickness,
                        color = Color.White.copy(alpha = 0.15f)
                    )

                    NavigationDrawerItem(
                        label = { Text("Inspección Actual") },
                        selected = section == HomeSection.Inspection,
                        onClick = {
                            section = HomeSection.Inspection
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 4.dp),
                        icon = { Icon(Icons.Filled.ChecklistRtl, contentDescription = null) },
                        colors = drawerItemColors
                    )
                    NavigationDrawerItem(
                        label = { Text("Reportes") },
                        selected = section == HomeSection.Reports,
                        onClick = {
                            section = HomeSection.Reports
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 4.dp),
                        colors = drawerItemColors
                    )
                    NavigationDrawerItem(
                        label = { Text("Exportar DB") },
                        selected = false,
                        onClick = {
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    exportRoomDbToDownloads(appContext)
                                }
                                Toast.makeText(
                                    appContext,
                                    if (result.success) result.message
                                    else "Fallo: ${result.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                drawerState.close()
                            }
                        },
                        modifier = Modifier.padding(vertical = 4.dp),
                        colors = drawerItemColors
                    )
                    NavigationDrawerItem(
                        label = { Text("Inicializar imágenes") },
                        selected = false,
                        onClick = {
                            showInitImagesDialog = true
                            fontsExpanded = false
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 4.dp),
                        icon = { Icon(Icons.Filled.Image, contentDescription = null) },
                        colors = drawerItemColors
                    )

                    // Opción Fuentes con sub-opciones
                    NavigationDrawerItem(
                        label = { Text("Fuentes") },
                        selected = false,
                        onClick = { fontsExpanded = !fontsExpanded },
                        modifier = Modifier.padding(vertical = 4.dp),
                        icon = {
                            Icon(
                                imageVector = if (fontsExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (fontsExpanded) "Contraer" else "Expandir"
                            )
                        },
                        colors = drawerItemColors
                    )
                    if (fontsExpanded) {
                        val isSmall = currentFontSize == FontSizeOption.Small
                        val isMedium = currentFontSize == FontSizeOption.Medium
                        val isLarge = currentFontSize == FontSizeOption.Large
                        NavigationDrawerItem(
                            label = { Text("Pequeña") },
                            selected = isSmall,
                            onClick = {
                                onChangeFontSize(FontSizeOption.Small)
                                fontsExpanded = false
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(start = 24.dp, top = 2.dp, bottom = 2.dp),
                            icon = { if (isSmall) Icon(Icons.Filled.Check, contentDescription = null) },
                            colors = drawerItemColors
                        )
                        NavigationDrawerItem(
                            label = { Text("Mediana") },
                            selected = isMedium,
                            onClick = {
                                onChangeFontSize(FontSizeOption.Medium)
                                fontsExpanded = false
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(start = 24.dp, top = 2.dp, bottom = 2.dp),
                            icon = { if (isMedium) Icon(Icons.Filled.Check, contentDescription = null) },
                            colors = drawerItemColors
                        )
                        NavigationDrawerItem(
                            label = { Text("Grande") },
                            selected = isLarge,
                            onClick = {
                                onChangeFontSize(FontSizeOption.Large)
                                fontsExpanded = false
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(start = 24.dp, top = 2.dp, bottom = 2.dp),
                            icon = { if (isLarge) Icon(Icons.Filled.Check, contentDescription = null) },
                            colors = drawerItemColors
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        thickness = androidx.compose.material3.DividerDefaults.Thickness,
                        color = Color.White.copy(alpha = 0.15f)
                    )
                }
            }
        }
    ) {
        ProvideCurrentUser {
            ProvideCurrentInspection {
                val currentInspection = LocalCurrentInspection.current
                fun loadInitialImageFromDb(isThermal: Boolean, onResult: (String) -> Unit) {
                    val inspId = currentInspection?.idInspeccion
                    if (inspId.isNullOrBlank()) {
                        Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    scope.launch {
                        val value = withContext(Dispatchers.IO) {
                            runCatching { inspeccionDao.getById(inspId) }
                                .getOrNull()
                                ?.let { if (isThermal) it.irImagenInicial else it.digImagenInicial }
                        }
                        if (!value.isNullOrBlank()) {
                            val normalized = composeImageName(parseImageName(value))
                            onResult("")
                            onResult(normalized)
                        } else {
                            onResult("")
                            val label = if (isThermal) "IR" else "digital"
                            Toast.makeText(
                                appContext,
                                "No se encontró imagen $label inicial.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                // BLUR para TODO el Scaffold (header + contenido)
                val blurModifier =
                    if (drawerState.isOpen) Modifier.blur(8.dp) else Modifier

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                val info = LocalCurrentInspection.current
                                val titleText = info?.let { data ->
                                    val no = data.noInspeccion?.toString() ?: "-"
                                    val cliente =
                                        data.nombreCliente?.ifBlank { data.idCliente ?: "-" } ?: "-"
                                    "No. Inspección Actual: $no - Cliente: $cliente"
                                } ?: "Pantalla principal"
                                Text(titleText)
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        if (drawerState.isOpen) drawerState.close()
                                        else drawerState.open()
                                    }
                                }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                                }
                            },
                            actions = {
                                IconButton(onClick = { showLogoutDialog = true }) {
                                    Icon(Icons.Filled.Logout, contentDescription = "Cerrar sesión")
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color(0xFF202327),
                                titleContentColor = Color.White,
                                navigationIconContentColor = Color.White,
                                actionIconContentColor = Color.White
                            )
                        )
                    },
                    modifier = modifier.then(blurModifier)
                ) { innerPadding ->

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {

                        when (section) {
                            HomeSection.Inspection ->
                                InspectionScreen(
                                    onReady = {
                                        scope.launch {
                                            delay(900)
                                            isLoading = false
                                        }
                                    }
                                )

                            HomeSection.Reports ->
                                Box(
                                    Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Reportes")
                                }
                        }

                        if (isLoading) {
                            LoadingOverlay("Cargando datos…")
                        }
                    }
                }

                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Cerrar sesión") },
                        text = { Text("¿Seguro que deseas cerrar sesión?") },
                        dismissButton = {
                            TextButton(onClick = { showLogoutDialog = false }) {
                                Text("Cancelar")
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showLogoutDialog = false
                                onLogout()
                            }) {
                                Text("Cerrar sesión")
                            }
                        }
                    )
                }

                if (showInitImagesDialog) {
                    Dialog(
                        onDismissRequest = { /* bloqueo: solo botones cierran */ },
                        properties = DialogProperties(
                            dismissOnClickOutside = false,
                            dismissOnBackPress = false
                        )
                    ) {
                        Card(
                            colors = CardDefaults.cardColors()
                        ) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 385.dp)
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = "Inicializar imágenes")

                                Text(text = "Imagen térmica", style = MaterialTheme.typography.titleSmall)
                    ImageInputButtonGroup(
                        label = "Archivo IR",
                        value = initThermalPath,
                        onValueChange = { initThermalPath = it },
                        modifier = Modifier.fillMaxWidth(),
                        isRequired = true,
                        onMoveUp = { initThermalPath = adjustImageSequence(initThermalPath, +1) },
                        onMoveDown = { initThermalPath = adjustImageSequence(initThermalPath, -1) },
                        onDotsClick = { loadInitialImageFromDb(true) { initThermalPath = it } },
                        onFolderClick = { /* TODO: IR explorador */ },
                        onCameraClick = { /* TODO: IR cámara */ }
                    )
                                Text(text = "Imagen digital", style = MaterialTheme.typography.titleSmall)
                    ImageInputButtonGroup(
                        label = "Archivo ID",
                        value = initDigitalPath,
                        onValueChange = { initDigitalPath = it },
                        modifier = Modifier.fillMaxWidth(),
                        isRequired = true,
                        onMoveUp = { initDigitalPath = adjustImageSequence(initDigitalPath, +1) },
                        onMoveDown = { initDigitalPath = adjustImageSequence(initDigitalPath, -1) },
                        onDotsClick = { loadInitialImageFromDb(false) { initDigitalPath = it } },
                        onFolderClick = { /* TODO: ID explorador */ },
                        onCameraClick = { /* TODO: ID cámara */ }
                    )
                                Button(
                                    onClick = { showInitImagesDialog = false },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Cerrar")
                                }
                            }
                        }
                    }
                }

            } // ProvideCurrentInspection
        } // ProvideCurrentUser
    }
}



// ------------------------------------------------------------
//  LOADING OVERLAY (fuera de MainScreen)
// ------------------------------------------------------------

@Composable
fun LoadingOverlay(message: String) {
    val transition = rememberInfiniteTransition(label = "loading")

    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    val dot1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(900, delayMillis = 0),
            RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(900, delayMillis = 150),
            RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(900, delayMillis = 300),
            RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF202327),
                contentColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.etic_logo_login),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .rotate(angle)
                )

                Spacer(Modifier.height(16.dp))
                Text(message)

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .background(Color.White.copy(alpha = dot1), CircleShape)
                    )
                    Box(
                        Modifier
                            .size(10.dp)
                            .background(Color.White.copy(alpha = dot2), CircleShape)
                    )
                    Box(
                        Modifier
                            .size(10.dp)
                            .background(Color.White.copy(alpha = dot3), CircleShape)
                    )
                }
            }
        }
    }
}

private data class ImageNameParts(
    val prefix: String,
    val number: Int,
    val digits: Int,
    val suffix: String
)

private fun parseImageName(raw: String): ImageNameParts {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return ImageNameParts("", 0, 0, "")
    val regex = Regex("^(.*?)(\\d+)(.*)$")
    val match = regex.find(trimmed)
    return if (match != null) {
        val prefix = match.groupValues[1]
        val digitsStr = match.groupValues[2]
        val suffix = match.groupValues[3]
        val number = digitsStr.toIntOrNull() ?: 0
        ImageNameParts(prefix, number, digitsStr.length, suffix)
    } else {
        ImageNameParts(trimmed, 0, 0, "")
    }
}

private fun composeImageName(parts: ImageNameParts): String {
    val safeNumber = parts.number.coerceAtLeast(0)
    val formattedNumber = when {
        parts.digits > 0 -> safeNumber.toString().padStart(parts.digits, '0')
        safeNumber > 0 -> safeNumber.toString()
        else -> ""
    }
    return buildString {
        append(parts.prefix)
        append(formattedNumber)
        append(parts.suffix)
    }
}

private fun adjustImageSequence(current: String, delta: Int): String {
    if (delta == 0) return current
    val parts = parseImageName(current)
    val newNumber = (parts.number + delta).coerceAtLeast(0)
    val fallbackDigits = max(
        1,
        max(parts.number.toString().length, newNumber.toString().length)
    )
    val digits = if (parts.digits > 0) parts.digits else fallbackDigits
    return composeImageName(parts.copy(number = newNumber, digits = digits))
}
