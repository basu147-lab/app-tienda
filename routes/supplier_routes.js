const express = require('express');
const db = require('../database'); // Ajustar la ruta según la ubicación del archivo
const { authenticateToken } = require('../middleware/auth_middleware');

const router = express.Router();

// Obtener todos los proveedores
router.get('/', authenticateToken, (req, res) => {
  db.all('SELECT * FROM suppliers', [], (err, rows) => {
    if (err) {
      console.error('Error al obtener proveedores:', err.message);
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    res.status(200).json(rows);
  });
});

// Crear un nuevo proveedor
router.post('/', authenticateToken, (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para crear proveedores.' });
  }
  const { name, contact_info } = req.body;
  if (!name) {
    return res.status(400).json({ message: 'El nombre del proveedor es obligatorio.' });
  }
  db.run('INSERT INTO suppliers (name, contact_info) VALUES (?, ?)', [name, contact_info], function(err) {
    if (err) {
      console.error('Error al crear proveedor:', err.message);
      if (err.message.includes('UNIQUE constraint failed: suppliers.name')) {
        return res.status(409).json({ message: 'Ya existe un proveedor con ese nombre.' });
      }
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    res.status(201).json({ message: 'Proveedor creado exitosamente.', supplierId: this.lastID });
  });
});

// Actualizar un proveedor
router.put('/:id', authenticateToken, (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para actualizar proveedores.' });
  }
  const supplierId = req.params.id;
  const { name, contact_info } = req.body;
  if (!name) {
    return res.status(400).json({ message: 'El nombre del proveedor es obligatorio.' });
  }
  db.run('UPDATE suppliers SET name = ?, contact_info = ? WHERE id = ?', [name, contact_info, supplierId], function(err) {
    if (err) {
      console.error('Error al actualizar proveedor:', err.message);
      if (err.message.includes('UNIQUE constraint failed: suppliers.name')) {
        return res.status(409).json({ message: 'Ya existe otro proveedor con ese nombre.' });
      }
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    if (this.changes === 0) {
      return res.status(404).json({ message: 'Proveedor no encontrado.' });
    }
    res.status(200).json({ message: 'Proveedor actualizado exitosamente.' });
  });
});

// Eliminar un proveedor
router.delete('/:id', authenticateToken, (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para eliminar proveedores.' });
  }
  const supplierId = req.params.id;
  // Antes de eliminar, verificar si algún producto usa este proveedor
  db.get('SELECT COUNT(*) AS count FROM products WHERE supplier_id = ? AND is_active = 1', [supplierId], (err, row) => {
    if (err) {
      console.error('Error al verificar productos asociados al proveedor:', err.message);
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    if (row.count > 0) {
      return res.status(400).json({ message: `No se puede eliminar el proveedor porque está asignado a ${row.count} producto(s) activo(s).` });
    }

    db.run('DELETE FROM suppliers WHERE id = ?', [supplierId], function(err) {
      if (err) {
        console.error('Error al eliminar proveedor:', err.message);
        return res.status(500).json({ message: 'Error interno del servidor.' });
      }
      if (this.changes === 0) {
        return res.status(404).json({ message: 'Proveedor no encontrado.' });
      }
      res.status(200).json({ message: 'Proveedor eliminado exitosamente.' });
    });
  });
});

module.exports = router;