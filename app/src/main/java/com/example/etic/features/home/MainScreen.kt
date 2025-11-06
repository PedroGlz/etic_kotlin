package com.example.etic.features.home

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.etic.R
import com.example.etic.features.inspection.ui.home.InspectionScreen
import kotlinx.coroutines.launch

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
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var section by rememberSaveable { mutableStateOf(HomeSection.Inspection) }

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
                    title = {
                        val ctx = androidx.compose.ui.platform.LocalContext.current
                        val db = remember { com.example.etic.data.local.DbProvider.get(ctx) }
                        val current by produceState(initialValue = null as com.example.etic.data.local.queries.CurrentInspectionInfo?) {
                            value = runCatching { com.example.etic.data.local.queries.getCurrentInspectionInfo(db) }.getOrNull()
                        }
                        val titleText = current?.let { info ->
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
                    HomeSection.Inspection -> InspectionScreen()
                    HomeSection.Reports -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Reportes") }
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

