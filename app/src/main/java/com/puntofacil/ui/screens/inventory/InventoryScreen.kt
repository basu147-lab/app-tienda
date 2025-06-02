package com.puntofacil.ui.screens.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.puntofacil.pos.data.local.entities.ProductEntity
import com.puntofacil.ui.theme.PuntoFacilTheme
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar
        TopAppBar(
            title = { Text("Inventario") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            },
            actions = {
                IconButton(onClick = { showAddProductDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar producto")
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchProducts(it)
                },
                label = { Text("Buscar productos") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { viewModel.filterProducts("ALL") },
                    label = { Text("Todos") },
                    selected = uiState.currentFilter == "ALL"
                )
                FilterChip(
                    onClick = { viewModel.filterProducts("LOW_STOCK") },
                    label = { Text("Stock bajo") },
                    selected = uiState.currentFilter == "LOW_STOCK"
                )
                FilterChip(
                    onClick = { viewModel.filterProducts("OUT_OF_STOCK") },
                    label = { Text("Sin stock") },
                    selected = uiState.currentFilter == "OUT_OF_STOCK"
                )
            }
            
            // Stats cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Productos",
                    value = uiState.totalProducts.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Stock Bajo",
                    value = uiState.lowStockCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Sin Stock",
                    value = uiState.outOfStockCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Products list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.products) { product ->
                        ProductCard(
                            product = product,
                            onEditClick = { selectedProduct = product },
                            onStockAdjust = { productId, adjustment ->
                                viewModel.adjustStock(productId, adjustment)
                            },
                            currencyFormatter = currencyFormatter
                        )
                    }
                }
            }
        }
    }
    
    // Add/Edit Product Dialog
    if (showAddProductDialog || selectedProduct != null) {
        ProductDialog(
            product = selectedProduct,
            onDismiss = {
                showAddProductDialog = false
                selectedProduct = null
            },
            onSave = { product ->
                if (selectedProduct != null) {
                    viewModel.updateProduct(product)
                } else {
                    viewModel.addProduct(product)
                }
                showAddProductDialog = false
                selectedProduct = null
            }
        )
    }
    
    // Show messages
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            // In a real app, show snackbar
            viewModel.clearMessage()
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    onEditClick: () -> Unit,
    onStockAdjust: (Long, Int) -> Unit,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEditClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (product.description?.isNotBlank() == true) {
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    Text(
                        text = "Código: ${product.barcode ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = currencyFormatter.format(product.salePrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "Costo: ${currencyFormatter.format(product.costPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stock: ${product.stock}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            product.stock <= 0 -> MaterialTheme.colorScheme.error
                            product.stock <= product.minStock -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    if (product.stock <= product.minStock) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Stock bajo",
                            modifier = Modifier
                                .size(16.dp)
                                .padding(start = 4.dp),
                            tint = if (product.stock <= 0) MaterialTheme.colorScheme.error
                                   else MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                
                // Stock adjustment buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onStockAdjust(product.id, -1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Reducir stock",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { onStockAdjust(product.id, 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aumentar stock",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var costPrice by remember { mutableStateOf(product?.costPrice?.toString() ?: "") }
    var salePrice by remember { mutableStateOf(product?.salePrice?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var minStock by remember { mutableStateOf(product?.minStock?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (product != null) "Editar Producto" else "Agregar Producto")
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Código de barras") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = costPrice,
                        onValueChange = { costPrice = it },
                        label = { Text("Precio de costo *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = salePrice,
                        onValueChange = { salePrice = it },
                        label = { Text("Precio de venta *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stock actual *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = minStock,
                        onValueChange = { minStock = it },
                        label = { Text("Stock mínimo *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newProduct = ProductEntity(
                        id = product?.id ?: 0L,
                        name = name,
                        description = description.takeIf { it.isNotBlank() },
                        barcode = barcode.takeIf { it.isNotBlank() },
                        costPrice = costPrice.toDoubleOrNull() ?: 0.0,
                        salePrice = salePrice.toDoubleOrNull() ?: 0.0,
                        stock = stock.toIntOrNull() ?: 0,
                        minStock = minStock.toIntOrNull() ?: 0,
                        categoryId = product?.categoryId ?: 1L,
                        supplierId = product?.supplierId,
                        isActive = product?.isActive ?: true,
                        createdAt = product?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(newProduct)
                },
                enabled = name.isNotBlank() && 
                         costPrice.toDoubleOrNull() != null &&
                         salePrice.toDoubleOrNull() != null &&
                         stock.toIntOrNull() != null &&
                         minStock.toIntOrNull() != null
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun InventoryScreenPreview() {
    PuntoFacilTheme {
        InventoryScreen(
            onNavigateBack = {}
        )
    }
}