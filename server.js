// --- START OF FILE server.js ---

const express = require('express');
const path = require('path');
// const db = require('./database'); // No se usa directamente aquí después de la refactorización

// Importar middleware y rutas
const { authenticateToken } = require('./middleware/auth_middleware');
const authRoutes = require('./routes/auth_routes');
const productRoutes = require('./routes/product_routes');
const categoryRoutes = require('./routes/category_routes');
const supplierRoutes = require('./routes/supplier_routes');
const userRoutes = require('./routes/user_routes');

const app = express();
const port = 3000; // Puedes cambiar el puerto si es necesario

app.use(express.json()); // Para parsear cuerpos de solicitud JSON

// Usar las rutas modularizadas
// Se añade /api como prefijo común para todas las rutas de la API
app.use('/api/auth', authRoutes); // Rutas de autenticación (login, register)
app.use('/api/users', userRoutes); // Rutas de gestión de usuarios, incluyendo recuperación y actualización de contraseña
app.use('/api/products', productRoutes); // Rutas de productos
app.use('/api/categories', categoryRoutes); // Rutas de categorías
app.use('/api/suppliers', supplierRoutes); // Rutas de proveedores

// Rutas para servir archivos estáticos (HTML, CSS, JS del cliente)
// Estos sirven el frontend de la aplicación

// Servir archivos de la carpeta 'auth' (login.html, register.html, etc.) bajo la ruta /auth
app.use('/auth', express.static(path.join(__dirname, 'auth')));

// Servir archivos de la carpeta 'static' (JS y CSS compartidos, etc.) bajo la ruta /static
app.use('/static', express.static(path.join(__dirname, 'static')));

// Servir archivos estáticos de la carpeta 'protected_pages' SIN autenticación directa aquí.
// La autenticación se manejará en rutas específicas para cada página protegida.
app.use('/protected_pages', express.static(path.join(__dirname, 'protected_pages')));


// Servir la página principal (index.html que podría redirigir o ser el landing page)
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'index.html')); 
});

// Ejemplo de cómo redirigir a la página de login si se accede a una página protegida sin autenticar
// Esto es más bien una lógica que el frontend manejaría con auth_check.js, 
// pero el middleware authenticateToken ya devuelve 401/403 si no hay token o es inválido.

// Iniciar el servidor
app.listen(port, () => {
  console.log(`Servidor escuchando en http://localhost:${port}`);
});


// --- END OF FILE server.js ---