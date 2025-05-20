const express = require('express');
const db = require('../database'); // Ajustar la ruta según la ubicación del archivo
const { authenticateToken } = require('../middleware/auth_middleware');

const router = express.Router();

// Rutas para la gestión de productos (CRUD)
router.get('/', authenticateToken, (req, res) => {
  const { search, page = 1, limit = 10, category_id, supplier_id, is_active, min_price_sell, max_price_sell, min_stock_quantity, max_stock_quantity, barcode } = req.query;
  const offset = (parseInt(page) - 1) * parseInt(limit);
  let params = [];
  let countParams = [];

  let query = 'SELECT p.*, c.name AS category_name, s.name AS supplier_name FROM products p LEFT JOIN categories c ON p.category_id = c.id LEFT JOIN suppliers s ON p.supplier_id = s.id';
  let countQuery = 'SELECT COUNT(*) AS totalProducts FROM products p';
  let whereClauses = [];

  if (is_active === 'true' || is_active === true) {
    whereClauses.push('p.is_active = 1');
  } else if (is_active === 'false' || is_active === false) {
    whereClauses.push('p.is_active = 0');
  } // Si is_active es 'all' o no se especifica, no se filtra por estado de actividad.

  if (search) {
    whereClauses.push('(p.name LIKE ? OR p.sku LIKE ? OR p.description LIKE ? OR p.barcode LIKE ?)');
    const likeTerm = `%${search}%`;
    params.push(likeTerm, likeTerm, likeTerm, likeTerm);
    countParams.push(likeTerm, likeTerm, likeTerm, likeTerm);
  }

  if (barcode) { // Nuevo filtro por barcode específico
    whereClauses.push('p.barcode = ?');
    params.push(barcode);
    countParams.push(barcode);
  }

  if (category_id) {
    whereClauses.push('p.category_id = ?');
    params.push(category_id);
    countParams.push(category_id);
  }

  if (supplier_id) {
    whereClauses.push('p.supplier_id = ?');
    params.push(supplier_id);
    countParams.push(supplier_id);
  }

  if (min_price_sell) {
    whereClauses.push('p.price_sell >= ?');
    params.push(parseFloat(min_price_sell));
    countParams.push(parseFloat(min_price_sell));
  }

  if (max_price_sell) {
    whereClauses.push('p.price_sell <= ?');
    params.push(parseFloat(max_price_sell));
    countParams.push(parseFloat(max_price_sell));
  }

  if (min_stock_quantity) {
    whereClauses.push('p.stock_quantity >= ?');
    params.push(parseFloat(min_stock_quantity));
    countParams.push(parseFloat(min_stock_quantity));
  }

  if (max_stock_quantity) {
    whereClauses.push('p.stock_quantity <= ?');
    params.push(parseFloat(max_stock_quantity));
    countParams.push(parseFloat(max_stock_quantity));
  }

  if (whereClauses.length > 0) {
    const whereString = ' WHERE ' + whereClauses.join(' AND ');
    query += whereString;
    countQuery += whereString;
  }

  query += ' ORDER BY p.id DESC LIMIT ? OFFSET ?';
  params.push(parseInt(limit), offset);

  db.get(countQuery, countParams, (err, countRow) => {
    if (err) {
      console.error('Error al contar productos:', err.message);
      return res.status(500).json({ message: 'Error interno del servidor al contar productos.' });
    }
    const totalProducts = countRow.totalProducts;
    const totalPages = Math.ceil(totalProducts / parseInt(limit));

    db.all(query, params, (err, rows) => {
      if (err) {
        console.error('Error al obtener productos:', err.message);
        return res.status(500).json({ message: 'Error interno del servidor al obtener productos.' });
      }
      res.status(200).json({
        products: rows,
        pagination: {
          currentPage: parseInt(page),
          totalPages,
          totalProducts,
          limit: parseInt(limit)
        }
      });
    });
  });
});

router.get('/:id', authenticateToken, (req, res) => {
  const productId = req.params.id;
  // No filtramos por is_active aquí para permitir ver detalles de productos inactivos si se accede por ID directo
  db.get('SELECT p.*, c.name AS category_name, s.name AS supplier_name FROM products p LEFT JOIN categories c ON p.category_id = c.id LEFT JOIN suppliers s ON p.supplier_id = s.id WHERE p.id = ?', [productId], (err, row) => {
    if (err) {
      console.error('Error al obtener producto por ID:', err.message);
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    if (!row) {
      return res.status(404).json({ message: 'Producto no encontrado.' });
    }
    // Convertir valores booleanos a true/false para el frontend
    row.is_active = !!row.is_active; // Asegura que sea booleano
    row.allow_decimal_quantities = !!row.allow_decimal_quantities; // Asegura que sea booleano
    res.status(200).json(row);
  });
});

router.post('/', authenticateToken, (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para crear productos.' });
  }
  const { name, sku, description, price_buy, price_sell, stock_quantity, category_id, supplier_id, min_stock_level = 0, barcode, unit_of_measure = 'unidad', allow_decimal_quantities = false } = req.body;
  if (!name || price_sell === undefined || stock_quantity === undefined) {
    return res.status(400).json({ message: 'Faltan campos obligatorios (name, price_sell, stock_quantity).' });
  }

  const finalPriceBuy = price_buy !== undefined ? parseFloat(price_buy) : 0;
  let finalStockQuantity = parseFloat(stock_quantity);
  const finalMinStockLevel = min_stock_level !== undefined ? parseInt(min_stock_level) : 0;
  const finalAllowDecimalQuantities = allow_decimal_quantities === true || allow_decimal_quantities === 'true' || allow_decimal_quantities === 1;

  if (!finalAllowDecimalQuantities && !Number.isInteger(finalStockQuantity)) {
    // Considerar si se debe redondear o rechazar
    // Por ahora, se podría redondear o enviar un error específico.
    // Para este ejemplo, vamos a redondear hacia abajo si no se permiten decimales y se envía uno.
    // O mejor aún, validar en el frontend y aquí también por si acaso.
    // Por simplicidad en el backend, si no permite decimales y se envía uno, se podría truncar o rechazar.
    // Aquí optamos por rechazar si no es entero y no se permiten decimales.
    // Opcionalmente: finalStockQuantity = Math.floor(finalStockQuantity);
     return res.status(400).json({ message: 'La cantidad de stock debe ser un número entero para este producto.' });
  }
  if (finalAllowDecimalQuantities) {
    finalStockQuantity = parseFloat(finalStockQuantity.toFixed(2)); // Asegurar 2 decimales si se permiten
  }


  db.run('INSERT INTO products (name, sku, description, price_buy, price_sell, stock_quantity, category_id, supplier_id, min_stock_level, barcode, unit_of_measure, allow_decimal_quantities, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)', // is_active por defecto a 1 al crear
    [name, sku, description, finalPriceBuy, parseFloat(price_sell), finalStockQuantity, category_id, supplier_id, finalMinStockLevel, barcode, unit_of_measure, finalAllowDecimalQuantities ? 1 : 0],
    function(err) {
      if (err) {
        console.error('Error al crear producto:', err.message);
        if (err.message.includes('UNIQUE constraint failed: products.sku') && sku) {
            return res.status(409).json({ message: `El SKU '${sku}' ya existe.` });
        }
        return res.status(500).json({ message: 'Error interno del servidor al crear el producto.' });
      }
      res.status(201).json({ message: 'Producto creado exitosamente.', productId: this.lastID });
    }
  );
});

router.put('/:id', authenticateToken, (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para actualizar productos.' });
  }
  const productId = req.params.id;
  const { name, sku, description, price_buy, price_sell, stock_quantity, category_id, supplier_id, is_active, min_stock_level, barcode, unit_of_measure, allow_decimal_quantities } = req.body;

  // Validar campos obligatorios básicos
  if (name === undefined || price_sell === undefined || stock_quantity === undefined || is_active === undefined) {
    return res.status(400).json({ message: 'Faltan campos obligatorios para la actualización (name, price_sell, stock_quantity, is_active).' });
  }

  const finalPriceBuy = price_buy !== undefined ? parseFloat(price_buy) : null; // Permitir null si no se envía para no sobreescribir con 0
  let finalStockQuantity = parseFloat(stock_quantity);
  const finalMinStockLevel = min_stock_level !== undefined ? parseInt(min_stock_level) : null;
  const finalAllowDecimalQuantities = allow_decimal_quantities === true || allow_decimal_quantities === 'true' || allow_decimal_quantities === 1;
  const finalIsActive = is_active === true || is_active === 'true' || is_active === 1 || String(is_active) === '1';

  if (!finalAllowDecimalQuantities && !Number.isInteger(finalStockQuantity)) {
    // return res.status(400).json({ message: 'La cantidad de stock debe ser un número entero para este producto.' });
    finalStockQuantity = Math.floor(finalStockQuantity); // O redondear si se prefiere no rechazar
  }
  if (finalAllowDecimalQuantities) {
    finalStockQuantity = parseFloat(finalStockQuantity.toFixed(2));
  }

  // Construir la lista de campos a actualizar dinámicamente para evitar sobreescribir con undefined
  // Sin embargo, para este caso, el documento pide que se manejen todos los campos, así que los pasamos todos.
  // Si un campo no se envía en el body, req.body.campo será undefined, y la lógica de arriba lo manejará (ej. finalPriceBuy = null)

  db.run('UPDATE products SET name = ?, sku = ?, description = ?, price_buy = COALESCE(?, price_buy), price_sell = ?, stock_quantity = ?, category_id = ?, supplier_id = ?, is_active = ?, min_stock_level = COALESCE(?, min_stock_level), barcode = ?, unit_of_measure = ?, allow_decimal_quantities = ? WHERE id = ?',
    [name, sku, description, finalPriceBuy, parseFloat(price_sell), finalStockQuantity, category_id, supplier_id, finalIsActive ? 1 : 0, finalMinStockLevel, barcode, unit_of_measure, finalAllowDecimalQuantities ? 1 : 0, productId],
    function(err) {
      if (err) {
        console.error('Error al actualizar producto:', err.message);
        if (err.message.includes('UNIQUE constraint failed: products.sku') && sku) {
            return res.status(409).json({ message: `El SKU '${sku}' ya existe para otro producto.` });
        }
        return res.status(500).json({ message: 'Error interno del servidor al actualizar el producto.' });
      }
      if (this.changes === 0) {
        return res.status(404).json({ message: 'Producto no encontrado o datos sin cambios.' });
      }
      res.status(200).json({ message: 'Producto actualizado exitosamente.' });
    }
  );
});

// Endpoint para marcar producto como inactivo (soft delete)
router.delete('/:id', authenticateToken, (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para eliminar productos.' });
  }
  const productId = req.params.id;
  db.run('UPDATE products SET is_active = 0 WHERE id = ?',
    [productId],
    function(err) {
      if (err) {
        console.error('Error al marcar producto como inactivo:', err.message);
        return res.status(500).json({ message: 'Error interno del servidor.' });
      }
      if (this.changes === 0) {
        return res.status(404).json({ message: 'Producto no encontrado.' });
      }
      res.status(200).json({ message: 'Producto marcado como inactivo exitosamente.' });
    }
  );
});

// Endpoint para ajustar stock y registrar movimiento
router.post('/:id/adjust-stock', authenticateToken, (req, res) => {
    if (req.user.role !== 'admin' && req.user.role !== 'employee') { // Asumiendo que empleados también pueden ajustar
        return res.status(403).json({ message: 'No autorizado para ajustar stock.' });
    }
    const productId = req.params.id;
    let { movement_type, quantity, notes } = req.body; // Añadir notes

    if (!movement_type || quantity === undefined || !['compra', 'venta', 'ajuste'].includes(movement_type)) {
        return res.status(400).json({ message: 'Datos inválidos para ajuste de stock (tipo de movimiento o cantidad).' });
    }
    
    let parsedQuantity = parseFloat(quantity);
    if (isNaN(parsedQuantity)) {
        return res.status(400).json({ message: 'La cantidad debe ser un número.' });
    }

    db.get('SELECT stock_quantity, allow_decimal_quantities FROM products WHERE id = ?', [productId], (err, product) => {
        if (err) {
            console.error('Error al obtener producto para ajuste de stock:', err.message);
            return res.status(500).json({ message: 'Error interno del servidor.' });
        }
        if (!product) {
            return res.status(404).json({ message: 'Producto no encontrado.' });
        }

        // Validar cantidad si no se permiten decimales
        if (!product.allow_decimal_quantities && !Number.isInteger(parsedQuantity)) {
            // return res.status(400).json({ message: 'Este producto no permite cantidades decimales. La cantidad debe ser un entero.' });
            // Opcionalmente, redondear la cantidad del movimiento si no se permiten decimales en el producto
            parsedQuantity = Math.floor(parsedQuantity);
        }

        let current_stock_quantity = parseFloat(product.stock_quantity);
        let new_stock_quantity;

        if (movement_type === 'compra') {
            new_stock_quantity = current_stock_quantity + parsedQuantity;
        } else if (movement_type === 'venta') {
            if (current_stock_quantity < parsedQuantity) {
                return res.status(400).json({ message: 'Stock insuficiente para la venta.' });
            }
            new_stock_quantity = current_stock_quantity - parsedQuantity;
        } else if (movement_type === 'ajuste') { // 'ajuste' puede ser positivo o negativo
            new_stock_quantity = current_stock_quantity + parsedQuantity; 
            // No permitir stock negativo por ajuste, a menos que se decida lo contrario
            // if (new_stock_quantity < 0) new_stock_quantity = 0; 
        }

        // Redondear el nuevo stock a 2 decimales si el producto lo permite, sino a entero
        new_stock_quantity = product.allow_decimal_quantities ? parseFloat(new_stock_quantity.toFixed(2)) : Math.floor(new_stock_quantity);
        if (new_stock_quantity < 0 && movement_type !== 'ajuste') { // Evitar stock negativo para compra/venta, ajuste podría permitirlo si se quisiera
             new_stock_quantity = 0;
        }

        db.serialize(() => {
            db.run('BEGIN TRANSACTION');
            db.run('UPDATE products SET stock_quantity = ? WHERE id = ?',
                [new_stock_quantity, productId],
                function(err) {
                    if (err) {
                        console.error('Error al actualizar stock del producto:', err.message);
                        db.run('ROLLBACK');
                        return res.status(500).json({ message: 'Error al actualizar stock.' });
                    }
                    if (this.changes === 0) {
                         db.run('ROLLBACK');
                        return res.status(404).json({ message: 'Producto no encontrado al intentar actualizar stock.' });
                    }

                    // Registrar el movimiento con la cantidad original del request (parsedQuantity) y notes
                    db.run('INSERT INTO inventory_movements (product_id, movement_type, quantity, user_id, notes) VALUES (?, ?, ?, ?, ?)',
                        [productId, movement_type, parsedQuantity, req.user.id, notes], 
                        function(err) {
                            if (err) {
                                console.error('Error al registrar movimiento de inventario:', err.message);
                                db.run('ROLLBACK');
                                return res.status(500).json({ message: 'Error al registrar movimiento.' });
                            }
                            db.run('COMMIT');
                            res.status(200).json({ message: 'Stock ajustado y movimiento registrado exitosamente.', new_stock_quantity });
                        }
                    );
                }
            );
        });
    });
});


// Nuevo Endpoint: Alertas de Stock Bajo (la ruta será /api/products/inventory-alerts)
router.get('/inventory-alerts', authenticateToken, (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para ver alertas de stock.' });
  }
  const query = `
    SELECT id, name, sku, stock_quantity, min_stock_level, unit_of_measure 
    FROM products 
    WHERE stock_quantity <= min_stock_level 
      AND min_stock_level > 0 
      AND is_active = 1 
    ORDER BY name ASC`;

  db.all(query, [], (err, rows) => {
    if (err) {
      console.error('Error al obtener alertas de stock bajo:', err.message);
      return res.status(500).json({ message: 'Error interno del servidor al obtener alertas de stock.' });
    }
    res.json(rows);
  });
});

// Nuevo Endpoint: Historial de Movimientos por Producto (la ruta será /api/products/:id/inventory-movements)
router.get('/:id/inventory-movements', authenticateToken, (req, res) => {
  const productId = req.params.id;
  // Verificar si el producto existe primero podría ser una buena adición
  db.get('SELECT 1 FROM products WHERE id = ?', [productId], (err, product) => {
    if (err) {
      console.error('Error verificando producto para historial:', err.message);
      return res.status(500).json({ message: 'Error interno del servidor.'});
    }
    if (!product) {
      return res.status(404).json({ message: 'Producto no encontrado.' });
    }

    const query = `
      SELECT im.*, u.username AS user_username 
      FROM inventory_movements im 
      LEFT JOIN users u ON im.user_id = u.id 
      WHERE im.product_id = ? 
      ORDER BY im.movement_date DESC`;

    db.all(query, [productId], (err, rows) => {
      if (err) {
        console.error('Error al obtener historial de movimientos del producto:', err.message);
        return res.status(500).json({ message: 'Error interno del servidor al obtener historial.' });
      }
      res.status(200).json(rows);
    });
  });
});

module.exports = router;