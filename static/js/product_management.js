document.addEventListener('DOMContentLoaded', () => {
    const token = sessionStorage.getItem('token');
    if (!token) {
        alert('No autorizado. Redirigiendo al login.');
        window.location.href = '/login.html';
        return;
    }

    const userIdIndicator = document.getElementById('user-id-indicator');
    try {
        const decodedToken = JSON.parse(atob(token.split('.')[1]));
        if (userIdIndicator && decodedToken && decodedToken.id) { // Changed from decodedToken.user_id to decodedToken.id
            userIdIndicator.textContent = `User ID: ${decodedToken.id}`;
        }
    } catch (e) {
        console.error('Error decodificando token:', e);
        // No es crítico si falla, podría ser un token opaco o no JWT
    }

    // Referencias a elementos del DOM
    const btnNuevoProducto = document.getElementById('btnNuevoProducto');
    const inputBuscarProducto = document.getElementById('inputBuscarProducto');
    const tbodyProductos = document.getElementById('tbodyProductos');

    // Modal y Formulario de Producto
    const modalProducto = document.getElementById('modalProducto');
    const formProducto = document.getElementById('formProducto');
    const modalTitle = document.getElementById('modalTitle'); // Para el título del modal de producto
    const productIdField = document.getElementById('productId'); // Corregido el ID para coincidir con HTML y face2.txt
    const productNameField = document.getElementById('productName');
    const productSkuField = document.getElementById('productSku');
    const productDescriptionField = document.getElementById('productDescription');
    const productPriceBuyField = document.getElementById('productPriceBuy');
    const productPriceSellField = document.getElementById('productPriceSell');
    const productStockField = document.getElementById('productStock');
    const productMinStockField = document.getElementById('productMinStock');
    const productBarcodeField = document.getElementById('productBarcode');
    const productUnitOfMeasureField = document.getElementById('productUnitOfMeasure');
    const productAllowDecimalQuantitiesField = document.getElementById('productAllowDecimalQuantities');
    const productCategoryField = document.getElementById('productCategory');
    const productSupplierField = document.getElementById('productSupplier');
    const productIsActiveField = document.getElementById('productIsActive');

    // Modal y Formulario de Ajuste de Stock
    const modalAjusteStock = document.getElementById('modalAjusteStock'); // Corregido el ID para coincidir con HTML y face2.txt
    const formAjusteStock = document.getElementById('formAjustarStock');
    const ajusteProductoIdField = document.getElementById('ajusteProductoId'); // Corregido el ID para coincidir con HTML y face2.txt
    const ajusteNombreProductoSpan = document.getElementById('ajusteNombreProducto'); // Corregido el ID para coincidir con HTML y face2.txt
    const ajusteCantidadField = document.getElementById('ajusteCantidad');
    const ajusteTipoMovimientoField = document.getElementById('ajusteTipoMovimiento'); // Corregido el ID para coincidir con HTML y face2.txt
    const ajusteNotasField = document.getElementById('ajusteMotivo'); // Corregido el ID para coincidir con HTML y face2.txt
    const infoDecimalesAjuste = document.getElementById('infoDecimalesAjuste');

    // Filtros Avanzados
    const filtroCategoriaSelect = document.getElementById('filtroCategoria');
    const filtroProveedorSelect = document.getElementById('filtroProveedor');
    const filtroEstadoSelect = document.getElementById('filtroEstado');
    const btnAplicarFiltros = document.getElementById('btnAplicarFiltros');
    const btnLimpiarFiltros = document.getElementById('btnLimpiarFiltros');

    // Paginación
    const controlesPaginacionDiv = document.getElementById('controlesPaginacion');

    // Alertas de Stock Bajo
    const listaAlertasStockUL = document.getElementById('listaAlertasStock');

    // Modal de Detalle de Producto
    const modalDetalleProducto = document.getElementById('modalDetalleProducto');
    const detalleProductoInfoDiv = document.getElementById('detalleProductoInfo');
    const tablaHistorialMovimientosBody = document.getElementById('tablaHistorialMovimientos');

    const formCategoria = document.getElementById('formCategoria');
    const categoriaIdField = document.getElementById('categoriaId');
    const categoriaNombreField = document.getElementById('categoriaNombre');
    const listaCategoriasUL = document.getElementById('listaCategorias');

    const formProveedor = document.getElementById('formProveedor');
    const proveedorIdField = document.getElementById('proveedorId');
    const proveedorNombreField = document.getElementById('proveedorNombre');
    const proveedorContactoField = document.getElementById('proveedorContacto');
    const listaProveedoresUL = document.getElementById('listaProveedores');

    const API_BASE_URL = '/api';

    // Estado de la aplicación (paginación, filtros, etc.)
    let currentPage = 1;
    let productsPerPage = 10; // O el valor que prefieras por defecto
    let currentFilters = {};
    let currentSearchTerm = '';

    // Función genérica para realizar peticiones fetch
    async function fetchData(url, options = {}) {
        const headers = {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            ...options.headers,
        };
        try {
            const response = await fetch(url, { ...options, headers });
            if (!response.ok) {
                let errorData;
                try {
                    errorData = await response.json();
                } catch (e) {
                    errorData = { message: response.statusText };
                }
                throw new Error(errorData.message || `Error ${response.status}`);
            }
            if (response.status === 204) return null; // No content
            return response.json();
        } catch (error) {
            console.error(`Fetch error for ${url}:`, error);
            alert(`Error en la solicitud: ${error.message}`);
            throw error;
        }
    }

    // --- Gestión de Productos ---
    async function cargarProductos(searchTerm = currentSearchTerm, page = currentPage, limit = productsPerPage, filters = currentFilters) {
        try {
            let queryParams = `page=${page}&limit=${limit}`;
            if (searchTerm) {
                queryParams += `&search=${encodeURIComponent(searchTerm)}`;
            }
            for (const key in filters) {
                if (filters[key]) { // Solo añadir si el filtro tiene valor
                    queryParams += `&${encodeURIComponent(key)}=${encodeURIComponent(filters[key])}`;
                }
            }

            const url = `${API_BASE_URL}/products?${queryParams}`;
            const data = await fetchData(url);
            const productos = data.products;
            
            tbodyProductos.innerHTML = '';
            if (productos && productos.length > 0) {
                productos.forEach(producto => {
                    const tr = document.createElement('tr');
                    tr.dataset.productId = producto.id;
                    tr.innerHTML = `
                        <td data-label="ID">${producto.id}</td>
                        <td data-label="Nombre">${producto.name}</td>
                        <td data-label="SKU">${producto.sku || 'N/A'}</td>
                        <td data-label="Categoría">${producto.category_name || 'N/A'}</td>
                        <td data-label="Proveedor">${producto.supplier_name || 'N/A'}</td>
                        <td data-label="Precio Venta">${parseFloat(producto.price_sell).toFixed(2)}</td>
                        <td data-label="Stock">${producto.allow_decimal_quantities ? parseFloat(producto.stock_quantity).toFixed(2) : parseInt(producto.stock_quantity)}</td>
                        <td data-label="Stock Mín.">${producto.min_stock_level !== null ? producto.min_stock_level : 'N/A'}</td>
                        <td data-label="Cód. Barras">${producto.barcode || 'N/A'}</td>
                        <td data-label="Unidad Med.">${producto.unit_of_measure || 'N/A'}</td>
                        <td data-label="Permite Dec.">${producto.allow_decimal_quantities ? 'Sí' : 'No'}</td>
                        <td data-label="Activo">${producto.is_active ? 'Sí' : 'No'}</td>
                        <td data-label="Acciones">
                            <button class="btn-ver-detalle btn-sm btn-info" data-id="${producto.id}">Ver Detalle</button>
                            <button class="btn-editar-producto btn-sm btn-warning" data-id="${producto.id}">Editar</button>
                            <button class="btn-activar-producto btn-sm ${producto.is_active ? 'btn-danger' : 'btn-success'}" data-id="${producto.id}" data-active="${producto.is_active}">
                                ${producto.is_active ? 'Desactivar' : 'Activar'}
                            </button>
                            <button class="btn-ajustar-stock btn-sm btn-secondary" data-id="${producto.id}" data-name="${producto.name}" data-allow-decimals="${producto.allow_decimal_quantities}">Ajustar Stock</button>
                        </td>
                    `;
                    tbodyProductos.appendChild(tr);
                });
            } else {
                tbodyProductos.innerHTML = '<tr><td colspan="13" class="text-center">No se encontraron productos.</td></tr>';
            }
            actualizarControlesPaginacion(data.pagination);
        } catch (error) {
            console.error('Error al cargar productos:', error);
            tbodyProductos.innerHTML = '<tr><td colspan="13" class="text-center">Error al cargar productos.</td></tr>';
            // El error ya se maneja en fetchData, aquí solo evitamos que se propague más si no es necesario
        }
    }

    function actualizarControlesPaginacion(pagination) {
        controlesPaginacionDiv.innerHTML = ''; // Limpiar controles existentes
        if (!pagination || pagination.totalPages <= 1) {
            return; // No mostrar paginación si hay 0 o 1 páginas
        }

        const { currentPage, totalPages, limit, totalProducts } = pagination;

        let paginationHTML = `<nav aria-label="Paginación de productos"><ul class="pagination justify-content-center">`;

        // Botón Anterior
        paginationHTML += `<li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage - 1}">Anterior</a>
        </li>`;

        // Números de Página (simplificado, se puede mejorar para muchos números)
        // Mostrar hasta 5 números de página alrededor de la actual
        let startPage = Math.max(1, currentPage - 2);
        let endPage = Math.min(totalPages, currentPage + 2);

        if (startPage > 1) {
            paginationHTML += `<li class="page-item"><a class="page-link" href="#" data-page="1">1</a></li>`;
            if (startPage > 2) {
                paginationHTML += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = startPage; i <= endPage; i++) {
            paginationHTML += `<li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${i}">${i}</a>
            </li>`;
        }

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                paginationHTML += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            paginationHTML += `<li class="page-item"><a class="page-link" href="#" data-page="${totalPages}">${totalPages}</a></li>`;
        }

        // Botón Siguiente
        paginationHTML += `<li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage + 1}">Siguiente</a>
        </li>`;

        paginationHTML += `</ul></nav>`;
        paginationHTML += `<div class="text-center mt-2"><small>Mostrando productos ${ (currentPage - 1) * limit + 1 }-${Math.min(currentPage * limit, totalProducts)} de ${totalProducts}</small></div>`;


        controlesPaginacionDiv.innerHTML = paginationHTML;

        // Añadir event listeners a los links de paginación
        controlesPaginacionDiv.querySelectorAll('.page-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const page = parseInt(e.target.dataset.page);
                if (page && page !== currentPage) {
                    cargarProductos(currentSearchTerm, page, productsPerPage, currentFilters);
                }
            });
        });
    }

    inputBuscarProducto.addEventListener('input', (e) => {
        currentSearchTerm = e.target.value.trim();
        currentPage = 1; // Resetear a la primera página con nueva búsqueda
        cargarProductos(currentSearchTerm, currentPage, productsPerPage, currentFilters);
    });

    // Abrir modal para nuevo producto
    btnNuevoProducto.addEventListener('click', () => {
        formProducto.reset();
        productIdField.value = '';
        modalTitle.textContent = 'Crear Nuevo Producto';
        productIsActiveField.checked = true; // Por defecto activo al crear
        productAllowDecimalQuantitiesField.checked = false; // Por defecto no permite decimales
        productStockField.step = '1'; // Paso por defecto para enteros
        ajusteCantidadField.step = '1'; // También para el modal de ajuste
        modalProducto.style.display = 'block';
    });

    // Manejo del submit de formProducto (Crear/Editar)
    formProducto.addEventListener('submit', async (event) => {
        event.preventDefault();
        // Limpiar mensajes de error previos
        document.querySelectorAll('#formProducto .error-message').forEach(el => el.textContent = '');
        clearErrorMessage('error-formProducto'); // Limpiar error general del formulario

        const id = productIdField.value;
        const allowDecimalQuantities = productAllowDecimalQuantitiesField.checked;
        
        let isValid = true;

        // Validaciones
        const name = productNameField.value.trim();
        if (!isValidText(name)) {
            displayErrorMessage('error-productName', 'El nombre es obligatorio.');
            isValid = false;
        }

        const priceSell = productPriceSellField.value;
        if (!isValidPrice(priceSell)) {
            displayErrorMessage('error-productPriceSell', 'El precio de venta es obligatorio y debe ser un número positivo.');
            isValid = false;
        }

        const stockQuantityValue = productStockField.value;
        if (!isValidNumber(stockQuantityValue, allowDecimalQuantities, false)) {
            const msg = allowDecimalQuantities ? 'El stock debe ser un número positivo.' : 'El stock debe ser un número entero positivo.';
            displayErrorMessage('error-productStock', msg);
            isValid = false;
        }
        
        const priceBuyValue = productPriceBuyField.value;
        if (priceBuyValue && !isValidPrice(priceBuyValue)) { // Opcional, pero si se ingresa debe ser válido
            displayErrorMessage('error-productPriceBuy', 'El precio de compra debe ser un número positivo si se especifica.');
            isValid = false;
        }

        const minStockValue = productMinStockField.value;
        if (minStockValue && !isValidNumber(minStockValue, false, false)) { // Stock mínimo debe ser entero positivo
            displayErrorMessage('error-productMinStock', 'El stock mínimo debe ser un número entero positivo si se especifica.');
            isValid = false;
        }

        const sku = productSkuField.value.trim();
        // Podría añadirse validación para SKU si es necesario (ej. longitud, caracteres permitidos)

        const barcode = productBarcodeField.value.trim();
        // Podría añadirse validación para código de barras

        if (!isValid) {
            displayErrorMessage('error-formProducto', 'Por favor, corrija los errores en el formulario.');
            return; // Detener si la validación falla
        }

        let stockQuantityForPayload;
        if (allowDecimalQuantities) {
            stockQuantityForPayload = parseFloat(stockQuantityValue);
        } else {
            stockQuantityForPayload = parseInt(stockQuantityValue);
        }

        const productoData = {
            name: name,
            sku: sku || null,
            description: productDescriptionField.value.trim() || '',
            price_buy: priceBuyValue ? parseFloat(priceBuyValue) : 0,
            price_sell: parseFloat(priceSell),
            stock_quantity: stockQuantityForPayload,
            min_stock_level: minStockValue ? parseInt(minStockValue) : 0,
            barcode: barcode || null,
            unit_of_measure: productUnitOfMeasureField.value.trim() || 'unidad',
            allow_decimal_quantities: allowDecimalQuantities,
            category_id: productCategoryField.value ? parseInt(productCategoryField.value) : null,
            supplier_id: productSupplierField.value ? parseInt(productSupplierField.value) : null,
            is_active: productIsActiveField.checked
        };

        try {
            let response;
            if (id) { // Editar producto existente
                response = await fetchData(`${API_BASE_URL}/products/${id}`, {
                    method: 'PUT',
                    body: JSON.stringify(productoData)
                });
            } else { // Crear nuevo producto
                response = await fetchData(`${API_BASE_URL}/products`, {
                    method: 'POST',
                    body: JSON.stringify(productoData)
                });
            }
            alert(response.message || (id ? 'Producto actualizado.' : 'Producto creado.'));
            modalProducto.style.display = 'none';
            cargarProductos(currentSearchTerm, currentPage, productsPerPage, currentFilters);
            cargarAlertasStock(); // Recargar alertas por si el stock cambió
        } catch (error) {
            console.error('Error al guardar producto:', error);
            displayErrorMessage('error-formProducto', `Error al guardar: ${error.message}`);
        }
    });

    // Delegación de eventos para botones en la tabla de productos
    tbodyProductos.addEventListener('click', async (event) => {
        const target = event.target;
        const productId = target.dataset.id;

        if (target.classList.contains('btn-editar-producto')) {
            try {
                const producto = await fetchData(`${API_BASE_URL}/products/${productId}`);
                document.querySelector('#modalProducto h2').textContent = 'Editar Producto';
                productIdField.value = producto.id;
                productSkuField.value = producto.sku || '';
                modalTitle.textContent = 'Editar Producto';
                productIdField.value = producto.id;
                productNameField.value = producto.name;
                productSkuField.value = producto.sku || '';
                productDescriptionField.value = producto.description || '';
                productPriceBuyField.value = producto.price_buy !== undefined ? producto.price_buy.toFixed(2) : '0.00';
                productPriceSellField.value = producto.price_sell.toFixed(2);
                
                productAllowDecimalQuantitiesField.checked = producto.allow_decimal_quantities;
                if (producto.allow_decimal_quantities) {
                    productStockField.step = '0.01';
                    productStockField.value = parseFloat(producto.stock_quantity).toFixed(2);
                } else {
                    productStockField.step = '1';
                    productStockField.value = parseInt(producto.stock_quantity);
                }

                productMinStockField.value = producto.min_stock_level !== null ? producto.min_stock_level : '0';
                productBarcodeField.value = producto.barcode || '';
                productUnitOfMeasureField.value = producto.unit_of_measure || 'unidad';
                productCategoryField.value = producto.category_id || '';
                productSupplierField.value = producto.supplier_id || '';
                productIsActiveField.checked = producto.is_active;
                
                modalProducto.style.display = 'block';
            } catch (error) {
                // El error ya se maneja en fetchData
            }
        } else if (target.classList.contains('btn-activar-producto')) {
            const isActive = target.dataset.active === 'true';
            const confirmAction = confirm(`¿Seguro que quieres ${isActive ? 'desactivar' : 'activar'} este producto?`);
            if (confirmAction) {
                try {
                    if (isActive) { // Si está activo, lo desactivamos (DELETE)
                         await fetchData(`${API_BASE_URL}/products/${productId}`, { method: 'DELETE' });
                         alert('Producto desactivado (marcado como inactivo).');
                    } else { // Si está inactivo, lo activamos (PUT con is_active: true)
                        // Primero, obtener los datos actuales del producto
                        const producto = await fetchData(`${API_BASE_URL}/products/${productId}`);
                        // Preparar los datos para la actualización, incluyendo todos los campos existentes
                        const updatedProductData = {
                            ...producto, // Copiar todos los campos existentes
                            sku: producto.sku || '', 
                            description: producto.description || '', 
                            price_buy: producto.price_buy !== undefined ? producto.price_buy : 0, // Asegurar price_buy
                            price_sell: producto.price_sell, // price_sell ya debería estar definido
                            // stock_quantity, category_id, supplier_id ya deberían estar definidos
                            is_active: true // Establecer is_active a true
                        };
                        // Eliminar el id del cuerpo si el backend no lo espera en el body del PUT para actualizar
                        // delete updatedProductData.id; // Descomentar si es necesario
                        // Eliminar campos que no deben enviarse o que el backend no espera
                        delete updatedProductData.category_name;
                        delete updatedProductData.supplier_name;

                        await fetchData(`${API_BASE_URL}/products/${productId}`, { 
                            method: 'PUT', 
                            body: JSON.stringify(updatedProductData) 
                        });
                        alert('Producto activado.');
                    }
                    cargarProductos(inputBuscarProducto.value.trim());
                } catch (error) {
                     // El error ya se maneja en fetchData
                }
            }
        } else if (target.classList.contains('btn-ajustar-stock')) {
                ajusteProductoIdField.value = productId;
                ajusteNombreProductoSpan.textContent = target.dataset.name;
                formAjusteStock.reset();
                const allowDecimals = target.dataset.allowDecimals === 'true';
                ajusteCantidadField.step = allowDecimals ? '0.01' : '1';
                infoDecimalesAjuste.textContent = allowDecimals ? 'Este producto permite decimales.' : 'Este producto NO permite decimales (se redondeará si es necesario).';
                modalAjusteStock.style.display = 'block';
            } else if (target.classList.contains('btn-ver-detalle')) { // Nuevo manejador para ver detalle
                abrirModalDetalleProducto(productId);
            }
        });

    // Manejo del Modal de Ajuste de Stock
    formAjusteStock.addEventListener('submit', async (event) => {
        event.preventDefault();
        const productoId = ajusteProductoIdField.value;
        const formData = new FormData(formAjusteStock);
        const productoParaAjuste = await fetchData(`${API_BASE_URL}/products/${productoId}`); // Obtener info del producto para saber si permite decimales
        const allowDecimalsOnProduct = productoParaAjuste.allow_decimal_quantities;

        let cantidadAjuste = parseFloat(formAjusteStock.elements.ajusteCantidad.value);
        if (!allowDecimalsOnProduct) {
            cantidadAjuste = Math.floor(cantidadAjuste); // Redondear si el producto no permite decimales
        }

        const ajusteData = {
            quantity: cantidadAjuste,
            movement_type: formAjusteStock.elements.tipoMovimiento.value,
            notes: formAjusteStock.elements.ajusteNotas.value.trim()
        };

        if (isNaN(ajusteData.quantity)) {
            alert('La cantidad a ajustar debe ser un número.');
            return;
        }

        try {
            await fetchData(`${API_BASE_URL}/products/${productoId}/adjust-stock`, {
                method: 'POST',
                body: JSON.stringify(ajusteData)
            });
            alert('Stock ajustado con éxito.');
            modalAjusteStock.style.display = 'none';
            cargarProductos(); // Recargar con filtros y paginación actuales
            cargarAlertasStock(); // Recargar alertas ya que el stock cambió
        } catch (error) {
            // El error ya se maneja en fetchData
        }
    });

    // --- Gestión de Categorías ---
    async function cargarCategorias() {
        try {
            const categorias = await fetchData(`${API_BASE_URL}/categories`);
            listaCategoriasUL.innerHTML = '';
            productCategoryField.innerHTML = '<option value="">Seleccione una categoría</option>';
            categorias.forEach(cat => {
                const li = document.createElement('li');
                li.textContent = `${cat.name} (ID: ${cat.id})`;
                li.dataset.id = cat.id;
                li.dataset.name = cat.name;

                const editButton = document.createElement('button');
                editButton.textContent = 'Editar';
                editButton.classList.add('btn-editar-categoria');
                editButton.style.marginLeft = '10px';
                li.appendChild(editButton);

                const deleteButton = document.createElement('button');
                deleteButton.textContent = 'Eliminar';
                deleteButton.classList.add('btn-eliminar-categoria');
                deleteButton.style.marginLeft = '5px';
                li.appendChild(deleteButton);

                listaCategoriasUL.appendChild(li);

                const option = document.createElement('option');
                option.value = cat.id;
                option.textContent = cat.name;
                productCategoryField.appendChild(option);
            });
        } catch (error) {
            // El error ya se maneja en fetchData
        }
    }

    formCategoria.addEventListener('submit', async (event) => {
        event.preventDefault();
        const nombre = categoriaNombreField.value.trim();
        const id = categoriaIdField.value;
        if (!nombre) {
            alert('El nombre de la categoría es requerido.');
            return;
        }
        const method = id ? 'PUT' : 'POST';
        const url = id ? `${API_BASE_URL}/categories/${id}` : `${API_BASE_URL}/categories`;
        try {
            await fetchData(url, { method, body: JSON.stringify({ name: nombre }) });
            alert(`Categoría ${id ? 'actualizada' : 'creada'} con éxito.`);
            formCategoria.reset();
            categoriaIdField.value = '';
            document.querySelector('#formCategoria button[type="submit"]').textContent = 'Guardar Categoría';
            cargarCategorias();
        } catch (error) {
            // El error ya se maneja en fetchData
        }
    });

    listaCategoriasUL.addEventListener('click', async (event) => {
        const target = event.target;
        const li = target.closest('li');
        if (!li) return;
        const catId = li.dataset.id;

        if (target.classList.contains('btn-editar-categoria')) {
            categoriaNombreField.value = li.dataset.name;
            categoriaIdField.value = catId;
            document.querySelector('#formCategoria button[type="submit"]').textContent = 'Actualizar Categoría';
            categoriaNombreField.focus();
        } else if (target.classList.contains('btn-eliminar-categoria')) {
            if (confirm(`¿Seguro que quieres eliminar la categoría "${li.dataset.name}"? Esto podría afectar a productos existentes.`)) {
                try {
                    await fetchData(`${API_BASE_URL}/categories/${catId}`, { method: 'DELETE' });
                    alert('Categoría eliminada.');
                    cargarCategorias(); // Recargar para actualizar la lista y el select de productos
                    cargarProductos(inputBuscarProducto.value.trim()); // Recargar productos por si alguna categoría cambió
                } catch (error) {
                    // El error ya se maneja en fetchData
                }
            }
        }
    });

    // --- Gestión de Proveedores ---
    async function cargarProveedores() {
        try {
            const proveedores = await fetchData(`${API_BASE_URL}/suppliers`);
            listaProveedoresUL.innerHTML = '';
            productSupplierField.innerHTML = '<option value="">Seleccione un proveedor</option>';
            proveedores.forEach(prov => {
                const li = document.createElement('li');
                li.textContent = `${prov.name} (ID: ${prov.id}) - Contacto: ${prov.contact_info || 'N/A'}`;
                li.dataset.id = prov.id;
                li.dataset.name = prov.name;
                li.dataset.contact = prov.contact_info || '';

                const editButton = document.createElement('button');
                editButton.textContent = 'Editar';
                editButton.classList.add('btn-editar-proveedor');
                editButton.style.marginLeft = '10px';
                li.appendChild(editButton);

                const deleteButton = document.createElement('button');
                deleteButton.textContent = 'Eliminar';
                deleteButton.classList.add('btn-eliminar-proveedor');
                deleteButton.style.marginLeft = '5px';
                li.appendChild(deleteButton);

                listaProveedoresUL.appendChild(li);

                const option = document.createElement('option');
                option.value = prov.id;
                option.textContent = prov.name;
                productSupplierField.appendChild(option);
            });
        } catch (error) {
            // El error ya se maneja en fetchData
        }
    }

    formProveedor.addEventListener('submit', async (event) => {
        event.preventDefault();
        const nombre = proveedorNombreField.value.trim();
        const contacto = proveedorContactoField.value.trim();
        const id = proveedorIdField.value;
        if (!nombre) {
            alert('El nombre del proveedor es requerido.');
            return;
        }
        const proveedorData = { name: nombre, contact_info: contacto };
        const method = id ? 'PUT' : 'POST';
        const url = id ? `${API_BASE_URL}/suppliers/${id}` : `${API_BASE_URL}/suppliers`;
        try {
            await fetchData(url, { method, body: JSON.stringify(proveedorData) });
            alert(`Proveedor ${id ? 'actualizado' : 'creado'} con éxito.`);
            formProveedor.reset();
            proveedorIdField.value = '';
            document.querySelector('#formProveedor button[type="submit"]').textContent = 'Guardar Proveedor';
            cargarProveedores();
        } catch (error) {
            // El error ya se maneja en fetchData
        }
    });

    listaProveedoresUL.addEventListener('click', async (event) => {
        const target = event.target;
        const li = target.closest('li');
        if (!li) return;
        const provId = li.dataset.id;

        if (target.classList.contains('btn-editar-proveedor')) {
            proveedorNombreField.value = li.dataset.name;
            proveedorContactoField.value = li.dataset.contact;
            proveedorIdField.value = provId;
            document.querySelector('#formProveedor button[type="submit"]').textContent = 'Actualizar Proveedor';
            proveedorNombreField.focus();
        } else if (target.classList.contains('btn-eliminar-proveedor')) {
            if (confirm(`¿Seguro que quieres eliminar el proveedor "${li.dataset.name}"? Esto podría afectar a productos existentes.`)) {
                try {
                    await fetchData(`${API_BASE_URL}/suppliers/${provId}`, { method: 'DELETE' });
                    alert('Proveedor eliminado.');
                    cargarProveedores(); // Recargar para actualizar la lista y el select de productos
                    cargarProductos(inputBuscarProducto.value.trim()); // Recargar productos por si algún proveedor cambió
                } catch (error) {
                    // El error ya se maneja en fetchData
                }
            }
        }
    });

    // --- Manejo General de Modals ---
    document.querySelectorAll('.modal .close-button').forEach(button => {
        button.addEventListener('click', () => {
            button.closest('.modal').style.display = 'none';
        });
    });

    window.addEventListener('click', (event) => {
        if (event.target.classList.contains('modal')) {
            event.target.style.display = 'none';
        }
    });

    // Event listener para el checkbox de permitir decimales que ajusta el step del input de stock
    productAllowDecimalQuantitiesField.addEventListener('change', (e) => {
        if (e.target.checked) {
            productStockField.step = '0.01';
            // Opcional: si hay un valor, intentar convertirlo a decimal
            // if(productStockField.value) productStockField.value = parseFloat(productStockField.value).toFixed(2);
        } else {
            productStockField.step = '1';
            // Opcional: si hay un valor, intentar convertirlo a entero
            // if(productStockField.value) productStockField.value = Math.floor(parseFloat(productStockField.value));
        }
    });

    // --- Filtros Avanzados ---
    async function cargarFiltrosDropdowns() {
        try {
            const [categoriesData, suppliersData] = await Promise.all([
                fetchData(`${API_BASE_URL}/categories`),
                fetchData(`${API_BASE_URL}/suppliers`)
            ]);

            filtroCategoriaSelect.innerHTML = '<option value="">Todas las categorías</option>';
            categoriesData.forEach(cat => {
                const option = document.createElement('option');
                option.value = cat.id;
                option.textContent = cat.name;
                filtroCategoriaSelect.appendChild(option);
            });

            filtroProveedorSelect.innerHTML = '<option value="">Todos los proveedores</option>';
            suppliersData.forEach(sup => {
                const option = document.createElement('option');
                option.value = sup.id;
                option.textContent = sup.name;
                filtroProveedorSelect.appendChild(option);
            });

        } catch (error) {
            console.error('Error cargando dropdowns de filtros:', error);
            // No es crítico si fallan, los filtros simplemente no se poblarán
        }
    }

    btnAplicarFiltros.addEventListener('click', () => {
        currentFilters = {
            category_id: filtroCategoriaSelect.value,
            supplier_id: filtroProveedorSelect.value,
            is_active: filtroEstadoSelect.value,
            // Aquí se podrían añadir más filtros si el HTML los tuviera (ej. min_price_sell)
        };
        currentSearchTerm = inputBuscarProducto.value.trim(); // Tomar también el término de búsqueda general
        currentPage = 1; // Resetear a la primera página
        cargarProductos(currentSearchTerm, currentPage, productsPerPage, currentFilters);
    });

    btnLimpiarFiltros.addEventListener('click', () => {
        inputBuscarProducto.value = '';
        filtroCategoriaSelect.value = '';
        filtroProveedorSelect.value = '';
        filtroEstadoSelect.value = '';
        currentFilters = {};
        currentSearchTerm = '';
        currentPage = 1;
        cargarProductos(); // Cargar sin filtros ni término de búsqueda, desde la página 1
    });

    // --- Alertas de Stock Bajo ---
    async function cargarAlertasStock() {
        try {
            // La ruta correcta según product_routes.js es /api/products/inventory-alerts
            const alertas = await fetchData(`${API_BASE_URL}/products/inventory-alerts`); 
            listaAlertasStockUL.innerHTML = '';
            if (alertas && alertas.length > 0) {
                alertas.forEach(alerta => {
                    const li = document.createElement('li');
                    li.innerHTML = `
                        Producto: <a href="#" onclick="window.abrirModalDetalleProducto(${alerta.id}); return false;">${alerta.name} (SKU: ${alerta.sku || 'N/A'})</a><br>
                        Stock Actual: ${alerta.stock_quantity} ${alerta.unit_of_measure || ''} (Mínimo: ${alerta.min_stock_level})
                    `;
                    listaAlertasStockUL.appendChild(li);
                });
            } else {
                listaAlertasStockUL.innerHTML = '<li>No hay alertas de stock bajo.</li>';
            }
        } catch (error) {
            console.error('Error al cargar alertas de stock:', error);
            listaAlertasStockUL.innerHTML = '<li>Error al cargar alertas.</li>';
        }
    }
    // Event listener para botones 'Ver Producto' en alertas de stock
    listaAlertasStockUL.addEventListener('click', (event) => {
        if (event.target.classList.contains('btn-ver-detalle-alerta')) {
            const productId = event.target.dataset.id;
            abrirModalDetalleProducto(productId);
        }
    });

    // --- Modal de Detalle de Producto ---
    async function abrirModalDetalleProducto(productId) {
        // Hacer la función global para que pueda ser llamada desde onclick en las alertas
        window.abrirModalDetalleProducto = abrirModalDetalleProducto;

        try {
            // Obtener detalles del producto
            const producto = await fetchData(`${API_BASE_URL}/products/${productId}`);
            if (!producto) {
                alert('Producto no encontrado.');
                return;
            }

            // Poblar información básica del producto
            let detalleHtml = `
                <p><strong>ID:</strong> ${producto.id}</p>
                <p><strong>Nombre:</strong> ${producto.name}</p>
                <p><strong>SKU:</strong> ${producto.sku || 'N/A'}</p>
                <p><strong>Descripción:</strong> ${producto.description || 'N/A'}</p>
                <p><strong>Precio Compra:</strong> ${producto.price_buy !== null ? parseFloat(producto.price_buy).toFixed(2) : 'N/A'}</p>
                <p><strong>Precio Venta:</strong> ${parseFloat(producto.price_sell).toFixed(2)}</p>
                <p><strong>Stock Actual:</strong> ${producto.allow_decimal_quantities ? parseFloat(producto.stock_quantity).toFixed(2) : parseInt(producto.stock_quantity)} ${producto.unit_of_measure || ''}</p>
                <p><strong>Stock Mínimo:</strong> ${producto.min_stock_level !== null ? producto.min_stock_level : 'N/A'}</p>
                <p><strong>Código de Barras:</strong> ${producto.barcode || 'N/A'}</p>
                <p><strong>Unidad de Medida:</strong> ${producto.unit_of_measure || 'N/A'}</p>
                <p><strong>Permite Decimales:</strong> ${producto.allow_decimal_quantities ? 'Sí' : 'No'}</p>
                <p><strong>Categoría:</strong> ${producto.category_name || 'N/A'}</p>
                <p><strong>Proveedor:</strong> ${producto.supplier_name || 'N/A'}</p>
                <p><strong>Activo:</strong> ${producto.is_active ? 'Sí' : 'No'}</p>
            `;
            detalleProductoInfoDiv.innerHTML = detalleHtml;

            // Obtener historial de movimientos
            const movimientos = await fetchData(`${API_BASE_URL}/products/${productId}/inventory-movements`);
            tablaHistorialMovimientosBody.innerHTML = ''; // Limpiar historial previo

            if (movimientos && movimientos.length > 0) {
                movimientos.forEach(mov => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td>${new Date(mov.movement_date).toLocaleString()}</td>
                        <td>${mov.movement_type}</td>
                        <td>${mov.quantity}</td>
                        <td>${mov.username || 'Sistema'}</td>
                        <td>${mov.notes || ''}</td>
                    `;
                    tablaHistorialMovimientosBody.appendChild(tr);
                });
            } else {
                tablaHistorialMovimientosBody.innerHTML = '<tr><td colspan="5" class="text-center">No hay movimientos registrados para este producto.</td></tr>';
            }

            modalDetalleProducto.style.display = 'block';

        } catch (error) {
            console.error('Error al abrir modal de detalle de producto:', error);
            alert('Error al cargar los detalles del producto.');
        }
    }


    // Cargar datos iniciales
    cargarFiltrosDropdowns(); // Cargar opciones para los filtros
    cargarProductos(); // Carga con valores por defecto de paginación y filtros
    cargarCategorias(); // Para los selects del formulario (ya no es necesario para filtros si se usa cargarFiltrosDropdowns)
    cargarProveedores(); // Para los selects del formulario (ya no es necesario para filtros si se usa cargarFiltrosDropdowns)
    cargarAlertasStock();
});
// Elementos del DOM globales
let modalProducto, formProducto, modalTitle, productIdInput, productSkuInput, productNameInput, productDescriptionInput, productPriceBuyInput, productPriceSellInput, productStockInput, productCategorySelect, productSupplierSelect, productMinStockInput, productBarcodeInput, productUnitOfMeasureInput, productAllowDecimalQuantitiesCheckbox, productIsActiveCheckbox;
let modalAjusteStock, formAjusteStock, ajusteProductoIdInput, ajusteNombreProductoSpan, ajusteCantidadInput, ajusteMotivoInput, ajusteTipoMovimientoSelect, infoDecimalesAjuste;
let modalDetalleProducto, detalleProductoInfoDiv, tablaHistorialMovimientosBody;
let btnNuevoProducto, inputBuscarProducto, filtroCategoria, filtroProveedor, filtroEstado, btnAplicarFiltros, btnLimpiarFiltros, tbodyProductos, controlesPaginacion;

// ... existing code ...
document.addEventListener('DOMContentLoaded', () => {
    // Inicialización de elementos globales del DOM
    modalProducto = document.getElementById('modalProducto');
    formProducto = document.getElementById('formProducto');
    modalTitle = document.getElementById('modalTitle');
    productIdInput = document.getElementById('productId');
    productSkuInput = document.getElementById('productSku');
    productNameInput = document.getElementById('productName');
    productDescriptionInput = document.getElementById('productDescription');
    productPriceBuyInput = document.getElementById('productPriceBuy');
    productPriceSellInput = document.getElementById('productPriceSell');
    productStockInput = document.getElementById('productStock');
    productCategorySelect = document.getElementById('productCategory');
    productSupplierSelect = document.getElementById('productSupplier');
    productMinStockInput = document.getElementById('productMinStock');
    productBarcodeInput = document.getElementById('productBarcode');
    productUnitOfMeasureInput = document.getElementById('productUnitOfMeasure');
    productAllowDecimalQuantitiesCheckbox = document.getElementById('productAllowDecimalQuantities');
    productIsActiveCheckbox = document.getElementById('productIsActive');

    modalAjusteStock = document.getElementById('modalAjusteStock');
    formAjusteStock = document.getElementById('formAjusteStock');
    ajusteProductoIdInput = document.getElementById('ajusteProductoId');
    ajusteNombreProductoSpan = document.getElementById('ajusteNombreProducto');
    ajusteCantidadInput = document.getElementById('ajusteCantidad');
    ajusteMotivoInput = document.getElementById('ajusteMotivo');
    ajusteTipoMovimientoSelect = document.getElementById('ajusteTipoMovimiento');
    infoDecimalesAjuste = document.getElementById('infoDecimalesAjuste');

    modalDetalleProducto = document.getElementById('modalDetalleProducto');
    detalleProductoInfoDiv = document.getElementById('detalleProductoInfo');
    tablaHistorialMovimientosBody = document.getElementById('tbodyHistorialMovimientos');

    btnNuevoProducto = document.getElementById('btnNuevoProducto');
    inputBuscarProducto = document.getElementById('inputBuscarProducto');
    filtroCategoria = document.getElementById('filtroCategoria');
    filtroProveedor = document.getElementById('filtroProveedor');
    filtroEstado = document.getElementById('filtroEstado');
    btnAplicarFiltros = document.getElementById('btnAplicarFiltros');
    btnLimpiarFiltros = document.getElementById('btnLimpiarFiltros');
    tbodyProductos = document.getElementById('tbodyProductos');
    controlesPaginacion = document.getElementById('controlesPaginacion');

    // ACCIÓN CRÍTICA REQUERIDA POR EL USUARIO:
    // Las siguientes funciones deben ser MOVIDAS de DENTRO de este bloque DOMContentLoaded
    // al ÁMBITO GLOBAL (fuera de este bloque, usualmente cerca del inicio del archivo, después de las declaraciones de variables globales).
    // Asegúrese de que estas funciones utilicen las variables globales definidas arriba en lugar de document.getElementById repetidamente.
    // - abrirModalEditar(productId)
    // - eliminarProducto(productId)
    // - abrirModalAjusteStock(productId, productName, allowDecimals) // Verifique los parámetros actuales de su función
    // - abrirModalDetalleProducto(productId)
    //
    // Ejemplo de cómo debería quedar una función movida y usando variables globales:
    // async function abrirModalDetalleProducto(productId) { // Esta línea estaría FUERA de DOMContentLoaded
    //     if (!modalDetalleProducto || !detalleProductoInfoDiv || !tablaHistorialMovimientosBody) {
    //         console.error("Elementos del modal de detalle no están listos.");
    //         return;
    //     }
    //     // ... resto de la lógica de la función usando modalDetalleProducto, detalleProductoInfoDiv, etc. ...
    // }
    //
    // El código existente dentro de DOMContentLoaded (event listeners para botones, submits de formularios,
    // carga inicial de datos, etc., EXCEPTO las definiciones de las funciones mencionadas arriba)
    // debe ser revisado para usar estas variables globales en lugar de llamar a getElementById repetidamente.
    // Por ejemplo, si tenía: document.getElementById('modalTitle').textContent = 'Crear Producto';
    // Ahora debería ser: modalTitle.textContent = 'Crear Producto';
    //
    // De manera similar, corrija otros accesos a elementos del DOM para usar las variables globales:
    // - Para 'modalAjustarStock', use la variable global 'modalAjusteStock'.
    // - Para 'productoIdAjuste', use 'ajusteProductoIdInput'.
    // - Para 'nombreProductoAjuste', use 'ajusteNombreProductoSpan'.
    // - Para 'tipoMovimiento', use 'ajusteTipoMovimientoSelect'.
    // - Para 'ajusteNotas' (el input), use 'ajusteMotivoInput'.

    // ... existing code ...
});
// ... existing code ...