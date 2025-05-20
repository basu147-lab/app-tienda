const express = require('express');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const db = require('../database'); // Ajustar la ruta según la ubicación del archivo
const { jwtSecret } = require('../middleware/auth_middleware'); // Importar jwtSecret

const router = express.Router();

// Ruta de registro de usuario
router.post('/register', async (req, res) => {
  const { username, password, role, securityQuestion, securityAnswer } = req.body;

  if (!username || !password || !role) {
    return res.status(400).json({ message: 'Faltan campos obligatorios.' });
  }

  try {
    db.get('SELECT * FROM users WHERE username = ?', [username], async (err, row) => {
      if (err) {
        console.error('Error al verificar usuario existente:', err.message);
        return res.status(500).json({ message: 'Error interno del servidor.' });
      }
      if (row) {
        return res.status(409).json({ message: 'El nombre de usuario ya existe.' });
      }

      const hashedPassword = await bcrypt.hash(password, 10);

      db.run('INSERT INTO users (username, password, role, securityQuestion, securityAnswer) VALUES (?, ?, ?, ?, ?)',
        [username, hashedPassword, role, securityQuestion, securityAnswer],
        function (err) {
          if (err) {
            console.error('Error al guardar nuevo usuario:', err.message);
            return res.status(500).json({ message: 'Error interno del servidor.' });
          }
          console.log(`Usuario registrado con ID: ${this.lastID}`);
          res.status(201).json({ message: 'Usuario registrado exitosamente.' });
        }
      );
    });
  } catch (error) {
    console.error('Error al registrar usuario:', error);
    res.status(500).json({ message: 'Error interno del servidor.' });
  }
});

// Ruta de login de usuario
router.post('/login', async (req, res) => {
  const { username, password } = req.body;

  if (!username || !password) {
    return res.status(400).json({ message: 'Faltan campos obligatorios.' });
  }

  try {
    db.get('SELECT * FROM users WHERE username = ?', [username], async (err, user) => {
      if (err) {
        console.error('Error al buscar usuario:', err.message);
        return res.status(500).json({ message: 'Error interno del servidor.' });
      }

      if (!user) {
        console.log(`Login attempt failed: User not found for username - ${username}`);
        return res.status(401).json({ message: 'Usuario o contraseña incorrectos.' });
      }
      console.log(`[LOGIN] User found: ${user.username}, ID: ${user.id}. Stored hash: ${user.password}`); // Log hash almacenado
      console.log(`[LOGIN] Comparing provided password: ${password} with stored hash for user: ${user.username}`); // Log contraseña proporcionada

      const passwordMatch = await bcrypt.compare(password, user.password);

      if (!passwordMatch) {
        console.log(`Login attempt failed: Password mismatch for user - ${user.username}`);
        return res.status(401).json({ message: 'Usuario o contraseña incorrectos.' });
      }
      console.log(`Login successful for user - ${user.username}`);

      const token = jwt.sign({ id: user.id, username: user.username, role: user.role }, jwtSecret, { expiresIn: '1h' });

      res.status(200).json({ message: 'Login exitoso.', token, userId: user.id, userRole: user.role }); // Devuelve userId y userRole
    });
  } catch (error) {
    console.error('Error al iniciar sesión:', error);
    res.status(500).json({ message: 'Error interno del servidor.' });
  }
});

module.exports = router;