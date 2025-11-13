package com.example.etic.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.etic.R
import com.example.etic.features.inspection.ui.home.InspectionScreen
import com.example.etic.core.current.LocalCurrentInspection
import com.example.etic.core.current.ProvideCurrentInspection
import com.example.etic.core.current.ProvideCurrentUser
import com.example.etic.core.export.exportRoomDbToDownloads
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

private enum class HomeSection { Inspection, Reports }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userName: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var section by rememberSaveable { mutableStateOf(HomeSection.Inspection) }
    var isLoading by rememberSaveable { mutableStateOf(true) }

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
        drawerContent = {
            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            val drawerWidth = remember(screenWidth) {
                when {
                    screenWidth < 360.dp -> screenWidth - 24.dp
                    screenWidth < 600.dp -> 250.dp
                    else -> 260.dp
                }
            }

            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidth),
                drawerContainerColor = Color(0xFF202327),
                drawerContentColor = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    ListItem(
                        headlineContent = { Text("ETIC System", color = Color.White) },
                        supportingContent = { Text("Rafael Garcia", color = Color.LightGray) },
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
                        thickness = DividerDefaults.Thickness,
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
                        icon = { Icon(Icons.Filled.ChecklistRtl, contentDescription = "Seleccionado") },
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
                                val result = withContext(Dispatchers.IO) { exportRoomDbToDownloads(appContext) }
                                Toast.makeText(appContext, if (result.success) result.message else "Fallo: ${result.message}", Toast.LENGTH_LONG).show()
                                drawerState.close()
                            }
                        },
                        modifier = Modifier.padding(vertical = 4.dp),
                        colors = drawerItemColors
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalDivider(
                        thickness = DividerDefaults.Thickness,
                        color = Color.White.copy(alpha = 0.15f)
                    )
                }
            }
        }
    ) {
        ProvideCurrentUser {
        ProvideCurrentInspection {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        val info = LocalCurrentInspection.current
                        val titleText = info?.let { info ->
                            val no = info.noInspeccion?.toString() ?: "-"
                            val cliente = info.nombreCliente?.takeIf { it.isNotBlank() } ?: (info.idCliente ?: "-")
                            "No. Inspeccion Actual: $no - Cliente: $cliente"
                        } ?: "Pantalla principal"
                        Text(titleText)
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isOpen) drawerState.close() else drawerState.open()
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
            modifier = modifier
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                when (section) {
                    HomeSection.Inspection -> InspectionScreen(onReady = { scope.launch { delay(900); isLoading = false } })
                    HomeSection.Reports -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Reportes") }
                }

                if (isLoading) {
                    LoadingOverlay(message = "Cargando datos…")
                }
            }
        }
        }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que deseas cerrar sesión?") },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") } },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("Cerrar sesión") }
            }
        )
    }
}

@Composable
private fun LoadingOverlay(message: String) {
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
        animationSpec = infiniteRepeatable(tween(900, delayMillis = 0), RepeatMode.Reverse),
        label = "dot1"
    )
    val dot2 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, delayMillis = 150), RepeatMode.Reverse),
        label = "dot2"
    )
    val dot3 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, delayMillis = 300), RepeatMode.Reverse),
        label = "dot3"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF202327), contentColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Logo girando para un efecto más atractivo
                Image(
                    painter = painterResource(id = R.drawable.etic_logo_login),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp).rotate(angle)
                )
                Spacer(Modifier.height(16.dp))
                Text(message)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(10.dp).scale(1f).background(Color.White.copy(alpha = dot1), CircleShape))
                    Box(Modifier.size(10.dp).scale(1f).background(Color.White.copy(alpha = dot2), CircleShape))
                    Box(Modifier.size(10.dp).scale(1f).background(Color.White.copy(alpha = dot3), CircleShape))
                }
            }
        }
    }
}

