package com.puntofacil.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.puntofacil.ui.theme.PuntoFacilTheme

data class DashboardItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSales: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val dashboardItems = listOf(
        DashboardItem(
            title = "Ventas",
            icon = Icons.Default.ShoppingCart,
            onClick = onNavigateToSales
        ),
        DashboardItem(
            title = "Inventario",
            icon = Icons.Default.Inventory,
            onClick = onNavigateToInventory
        ),
        DashboardItem(
            title = "Clientes",
            icon = Icons.Default.People,
            onClick = onNavigateToCustomers
        ),
        DashboardItem(
            title = "Reportes",
            icon = Icons.Default.Assessment,
            onClick = onNavigateToReports
        ),
        DashboardItem(
            title = "Configuración",
            icon = Icons.Default.Settings,
            onClick = onNavigateToSettings
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    text = "PuntoFácil",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Cerrar sesión"
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Welcome message
        Text(
            text = "Bienvenido, ${uiState.currentUser?.firstName ?: "Usuario"}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "¿Qué deseas hacer hoy?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Quick stats cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Ventas Hoy",
                value = "$${uiState.todaySales}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Productos",
                value = uiState.totalProducts.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Clientes",
                value = uiState.totalCustomers.toString(),
                modifier = Modifier.weight(1f)
            )
        }
        
        // Dashboard grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dashboardItems) { item ->
                DashboardCard(
                    title = item.title,
                    icon = item.icon,
                    onClick = item.onClick
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    PuntoFacilTheme {
        DashboardScreen(
            onNavigateToSales = {},
            onNavigateToInventory = {},
            onNavigateToCustomers = {},
            onNavigateToReports = {},
            onNavigateToSettings = {},
            onLogout = {}
        )
    }
}