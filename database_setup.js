const bcrypt = require('bcrypt');

function initializeDatabase(db) {
  db.serialize(() => {
    db.run('PRAGMA foreign_keys = ON;', (pragmaErr) => {
      if (pragmaErr) {
        console.error("Error al habilitar foreign keys:", pragmaErr.message);
      } else {
        console.log("Foreign key constraints habilitadas.");
      }
    });

    // Creación de la tabla de usuarios
    db.run(`CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      username TEXT UNIQUE NOT NULL,
      password TEXT NOT NULL,
      role TEXT NOT NULL CHECK(role IN ('admin', 'employee')),
      securityQuestion TEXT,
      securityAnswer TEXT
    )`, (err) => {
      if (err) {
        console.error('Error al crear la tabla de usuarios:', err.message);
      } else {
        console.log('Tabla de usuarios verificada/creada.');
        // Seeding del admin se hará después de crear todas las tablas
      }
    });

    // Creación de la tabla de categorías
    db.run(`CREATE TABLE IF NOT EXISTS categories (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT UNIQUE NOT NULL
    )`, (err) => {
      if (err) {
        console.error('Error al crear la tabla de categorías:', err.message);
      } else {
        console.log('Tabla de categorías verificada/creada.');
      }
    });

    // Creación de la tabla de proveedores
    db.run(`CREATE TABLE IF NOT EXISTS suppliers (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT UNIQUE NOT NULL,
      contact_info TEXT
    )`, (err) => {
      if (err) {
        console.error('Error al crear la tabla de proveedores:', err.message);
      } else {
        console.log('Tabla de proveedores verificada/creada.');
      }
    });

    // Creación de la tabla de productos
    db.run(`CREATE TABLE IF NOT EXISTS products (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      sku TEXT UNIQUE,
      description TEXT,
      price_buy REAL DEFAULT 0,          -- Precio de compra
      price_sell REAL NOT NULL,        -- Precio de venta (anteriormente 'price')
      stock_quantity REAL NOT NULL DEFAULT 0,
      min_stock_level INTEGER DEFAULT 0,
      barcode TEXT,
      unit_of_measure TEXT DEFAULT 'unidad',
      allow_decimal_quantities BOOLEAN DEFAULT 0,
      category_id INTEGER,
      supplier_id INTEGER,
      is_active BOOLEAN DEFAULT 1,
      FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
      FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL
    )`, (err) => {
      if (err) {
        console.error('Error al crear la tabla de productos:', err.message);
      } else {
        console.log('Tabla de productos verificada/creada.');
      }
    });

    // Creación de la tabla de movimientos de inventario
    db.run(`CREATE TABLE IF NOT EXISTS inventory_movements (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      product_id INTEGER NOT NULL,
      movement_type TEXT NOT NULL CHECK(movement_type IN ('compra', 'venta', 'ajuste')),
      quantity REAL NOT NULL,
      movement_date DATETIME DEFAULT CURRENT_TIMESTAMP,
      user_id INTEGER, /* Para rastrear quién hizo el movimiento */
      notes TEXT, /* Notas adicionales sobre el movimiento */
      FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
    )`, (err) => {
      if (err) {
        console.error('Error al crear la tabla de movimientos de inventario:', err.message);
      } else {
        console.log('Tabla de movimientos de inventario verificada/creada.');
        // Ahora, después de que todas las tablas principales estén definidas, seedeamos el admin
        seedAdminUser(db);
      }
    });
  });
}

function seedAdminUser(db) {
  db.get('SELECT COUNT(*) AS count FROM users WHERE role = ?', ['admin'], (err, row) => {
    if (err) {
      console.error('Error al contar usuarios admin:', err.message);
      return;
    }

    if (row.count === 0) {
      const defaultUsername = 'admin';
      const defaultPassword = 'adminpassword'; // Considerar mover a variable de entorno
      const defaultRole = 'admin';
      const defaultSecurityQuestion = 'What is your favorite color?';
      const defaultSecurityAnswer = 'blue'; // Considerar hashear también o usar un sistema más robusto

      bcrypt.hash(defaultPassword, 10, (hashErr, hashedPassword) => {
        if (hashErr) {
          console.error('Error al hashear la contraseña del admin por defecto:', hashErr.message);
          return;
        }

        db.run('INSERT INTO users (username, password, role, securityQuestion, securityAnswer) VALUES (?, ?, ?, ?, ?)',
          [defaultUsername, hashedPassword, defaultRole, defaultSecurityQuestion, defaultSecurityAnswer],
          function(insertErr) {
            if (insertErr) {
              console.error('Error al insertar usuario admin por defecto:', insertErr.message);
            } else {
              console.log(`Usuario administrador por defecto '${defaultUsername}' creado con ID: ${this.lastID}`);
            }
          }
        );
      });
    }
  });
}

module.exports = { initializeDatabase };