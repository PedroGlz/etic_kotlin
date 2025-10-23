package com.example.etic.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.etic.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userName: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    // Paleta del drawer negro
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
                    screenWidth < 360.dp -> screenWidth - 24.dp // casi a full en pantallas muy chicas
                    screenWidth < 600.dp -> 250.dp              // teléfonos promedio
                    else -> 260.dp                              // tablets / pantallas grandes
                }
            }

            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidth),    // ⬅️ ancho dinámico
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
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent  // sin color de fondo
                        )
                    )

                    Spacer(Modifier.height(4.dp))

                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 4.dp),
                        thickness = DividerDefaults.Thickness,
                        color = Color.White.copy(alpha = 0.15f)
                    )

                    NavigationDrawerItem(
                            label = { Text("Inspección Actual") },
                            selected = true,
                            onClick = { scope.launch { drawerState.close() } },
                            modifier = Modifier.padding(vertical = 4.dp),
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.ChecklistRtl,
                                    contentDescription = "Seleccionado"
                                )
                            },
                            colors = drawerItemColors
                        )
                    NavigationDrawerItem(
                        label = { Text("Reportes") },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() } },
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
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Pantalla principal") },
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
                            Icon(
                                imageVector = Icons.Filled.Logout,
                                contentDescription = "Cerrar sesión"
                            )
                        }
                    }
                    // Si lo quieres oscuro para combinar con el drawer, descomenta:
                     , colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bienvenido, $userName",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }

    // Diálogo de confirmación de cierre de sesión
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
}
