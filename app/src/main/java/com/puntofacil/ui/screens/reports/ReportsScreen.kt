package com.puntofacil.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puntofacil.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class ReportItem(
    val title: String,
    val value: String,
    val subtitle: String? = null,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.TODAY) }

    LaunchedEffect(selectedPeriod) {
        viewModel.loadReports(selectedPeriod)
    }

    // Show error message
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // In a real app, you might want to show a Snackbar
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Period Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Report Period",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportPeriod.values().forEach { period ->
                            FilterChip(
                                onClick = { selectedPeriod = period },
                                label = { Text(period.displayName) },
                                selected = selectedPeriod == period,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sales Summary
                    item {
                        ReportSection(
                            title = "Sales Summary",
                            items = listOf(
                                ReportItem(
                                    title = "Total Sales",
                                    value = NumberFormat.getCurrencyInstance().format(uiState.totalSales),
                                    subtitle = "${uiState.totalTransactions} transactions",
                                    icon = Icons.Default.AttachMoney,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                ReportItem(
                                    title = "Average Sale",
                                    value = NumberFormat.getCurrencyInstance().format(uiState.averageSale),
                                    icon = Icons.Default.TrendingUp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                    }

                    // Product Performance
                    item {
                        ReportSection(
                            title = "Product Performance",
                            items = listOf(
                                ReportItem(
                                    title = "Items Sold",
                                    value = uiState.totalItemsSold.toString(),
                                    icon = Icons.Default.ShoppingCart,
                                    color = MaterialTheme.colorScheme.tertiary
                                ),
                                ReportItem(
                                    title = "Top Product",
                                    value = uiState.topProduct ?: "N/A",
                                    subtitle = if (uiState.topProductSales > 0) "${uiState.topProductSales} sold" else null,
                                    icon = Icons.Default.Star,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        )
                    }

                    // Inventory Status
                    item {
                        ReportSection(
                            title = "Inventory Status",
                            items = listOf(
                                ReportItem(
                                    title = "Total Products",
                                    value = uiState.totalProducts.toString(),
                                    icon = Icons.Default.Inventory,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                ReportItem(
                                    title = "Low Stock",
                                    value = uiState.lowStockProducts.toString(),
                                    subtitle = "Need attention",
                                    icon = Icons.Default.Warning,
                                    color = MaterialTheme.colorScheme.error
                                ),
                                ReportItem(
                                    title = "Out of Stock",
                                    value = uiState.outOfStockProducts.toString(),
                                    subtitle = "Requires restocking",
                                    icon = Icons.Default.ErrorOutline,
                                    color = MaterialTheme.colorScheme.error
                                )
                            )
                        )
                    }

                    // Customer Insights
                    item {
                        ReportSection(
                            title = "Customer Insights",
                            items = listOf(
                                ReportItem(
                                    title = "Total Customers",
                                    value = uiState.totalCustomers.toString(),
                                    icon = Icons.Default.People,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                ReportItem(
                                    title = "New Customers",
                                    value = uiState.newCustomers.toString(),
                                    subtitle = "This period",
                                    icon = Icons.Default.PersonAdd,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                    }

                    // Recent Sales
                    if (uiState.recentSales.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Recent Sales",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                        
                        items(uiState.recentSales.take(5)) { sale ->
                            SaleItem(sale = sale)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportSection(
    title: String,
    items: List<ReportItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            items.forEach { item ->
                ReportItemCard(item = item)
                if (item != items.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ReportItemCard(
    item: ReportItem
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = item.color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (item.subtitle != null) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        Text(
            text = item.value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun SaleItem(
    sale: SaleInfo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sale #${sale.id.take(8)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(sale.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = NumberFormat.getCurrencyInstance().format(sale.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${sale.itemCount} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}