const express = require('express');
const db = require('../database'); // Ajustar la ruta según la ubicación del archivo
const { authenticateToken } = require('../middleware/auth_middleware');

const router = express.Router();

// Obtener todas las categorías
router.get('/', authenticateToken, (req, res) => {
  db.all('SELECT * FROM categories', [], (err, rows) => {
    if (err) {
      console.error('Error al obtener categorías:', err.message);
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    res.status(200).json(rows);
  });
});

// Crear una nueva categoría
router.post('/', authenticateToken, (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para crear categorías.' });
  }
  const { name } = req.body;
  if (!name) {
    return res.status(400).json({ message: 'El nombre de la categoría es obligatorio.' });
  }
  db.run('INSERT INTO categories (name) VALUES (?)', [name], function(err) {
    if (err) {
      console.error('Error al crear categoría:', err.message);
      // Comprobar si el error es por restricción UNIQUE
      if (err.message.includes('UNIQUE constraint failed: categories.name')) {
        return res.status(409).json({ message: 'Ya existe una categoría con ese nombre.' });
      }
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    res.status(201).json({ message: 'Categoría creada exitosamente.', categoryId: this.lastID });
  });
});

// Actualizar una categoría
router.put('/:id', authenticateToken, (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para actualizar categorías.' });
  }
  const categoryId = req.params.id;
  const { name } = req.body;
  if (!name) {
    return res.status(400).json({ message: 'El nombre de la categoría es obligatorio.' });
  }
  db.run('UPDATE categories SET name = ? WHERE id = ?', [name, categoryId], function(err) {
    if (err) {
      console.error('Error al actualizar categoría:', err.message);
      if (err.message.includes('UNIQUE constraint failed: categories.name')) {
        return res.status(409).json({ message: 'Ya existe otra categoría con ese nombre.' });
      }
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    if (this.changes === 0) {
      return res.status(404).json({ message: 'Categoría no encontrada.' });
    }
    res.status(200).json({ message: 'Categoría actualizada exitosamente.' });
  });
});

// Eliminar una categoría
router.delete('/:id', authenticateToken, (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para eliminar categorías.' });
  }
  const categoryId = req.params.id;
  // Antes de eliminar, verificar si algún producto usa esta categoría
  db.get('SELECT COUNT(*) AS count FROM products WHERE category_id = ? AND is_active = 1', [categoryId], (err, row) => {
    if (err) {
      console.error('Error al verificar productos asociados a la categoría:', err.message);
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    if (row.count > 0) {
      return res.status(400).json({ message: `No se puede eliminar la categoría porque está asignada a ${row.count} producto(s) activo(s).` });
    }

    db.run('DELETE FROM categories WHERE id = ?', [categoryId], function(err) {
      if (err) {
        console.error('Error al eliminar categoría:', err.message);
        return res.status(500).json({ message: 'Error interno del servidor.' });
      }
      if (this.changes === 0) {
        return res.status(404).json({ message: 'Categoría no encontrada.' });
      }
      res.status(200).json({ message: 'Categoría eliminada exitosamente.' });
    });
  });
});

module.exports = router;