package com.puntofacil.ui.screens.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.puntofacil.pos.data.local.entities.ProductEntity
import com.puntofacil.ui.theme.PuntoFacilTheme
import java.text.NumberFormat
import java.util.*

data class SaleItem(
    val product: ProductEntity,
    val quantity: Int,
    val unitPrice: Double
) {
    val total: Double get() = quantity * unitPrice
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    onNavigateBack: () -> Unit,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showProductSearch by remember { mutableStateOf(false) }
    
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar
        TopAppBar(
            title = { Text("Punto de Venta") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            },
            actions = {
                IconButton(onClick = { viewModel.clearSale() }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpiar venta")
                }
            }
        )
        
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left side - Product search and selection
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Buscar Productos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.searchProducts(it)
                        },
                        label = { Text("Buscar por nombre o código") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    
                    // Product list
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.searchResults) { product ->
                            ProductSearchItem(
                                product = product,
                                onAddToSale = { viewModel.addProductToSale(product) }
                            )
                        }
                    }
                }
            }
            
            // Right side - Sale items and total
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Venta Actual",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Sale items
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.saleItems) { saleItem ->
                            SaleItemCard(
                                saleItem = saleItem,
                                onQuantityChange = { newQuantity ->
                                    viewModel.updateItemQuantity(saleItem.product.id, newQuantity)
                                },
                                onRemove = {
                                    viewModel.removeItemFromSale(saleItem.product.id)
                                },
                                currencyFormatter = currencyFormatter
                            )
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    // Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total:",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currencyFormatter.format(uiState.total),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Complete sale button
                    Button(
                        onClick = { viewModel.completeSale() },
                        enabled = uiState.saleItems.isNotEmpty() && !uiState.isProcessing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Completar Venta")
                        }
                    }
                }
            }
        }
    }
    
    // Show success/error messages
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            // In a real app, you'd show a snackbar here
            viewModel.clearMessage()
        }
    }
}

@Composable
fun ProductSearchItem(
    product: ProductEntity,
    onAddToSale: () -> Unit
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    
    Card(
        onClick = onAddToSale,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Código: ${product.barcode ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Stock: ${product.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.stock > 0) MaterialTheme.colorScheme.onSurfaceVariant 
                           else MaterialTheme.colorScheme.error
                )
            }
            Text(
                text = currencyFormatter.format(product.salePrice),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SaleItemCard(
    saleItem: SaleItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = saleItem.product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity controls
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onQuantityChange(maxOf(1, saleItem.quantity - 1)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Disminuir",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = saleItem.quantity.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = { onQuantityChange(saleItem.quantity + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aumentar",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Text(
                    text = currencyFormatter.format(saleItem.total),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SalesScreenPreview() {
    PuntoFacilTheme {
        SalesScreen(
            onNavigateBack = {}
        )
    }
}