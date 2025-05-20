const express = require('express');
const bcrypt = require('bcrypt');
const db = require('../database'); // Ajustar la ruta según la ubicación del archivo
const { authenticateToken } = require('../middleware/auth_middleware');

const router = express.Router();

// Obtener todos los usuarios (solo admin)
router.get('/', authenticateToken, (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para ver todos los usuarios.' });
  }
  db.all('SELECT id, username, role FROM users', [], (err, rows) => {
    if (err) {
      console.error('Error al obtener usuarios:', err.message);
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    res.status(200).json(rows);
  });
});

// Crear un nuevo usuario (solo admin)
router.post('/', authenticateToken, async (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para crear usuarios.' });
  }

  const { username, password, role, securityQuestion, securityAnswer } = req.body;

  if (!username || !password || !role) {
    return res.status(400).json({ message: 'Nombre de usuario, contraseña y rol son obligatorios.' });
  }

  // Validar el rol
  if (!['admin', 'employee'].includes(role)) {
    return res.status(400).json({ message: 'Rol inválido.' });
  }

  try {
    // Verificar si el nombre de usuario ya existe
    db.get('SELECT username FROM users WHERE username = ?', [username], async (err, row) => {
      if (err) {
        console.error('Error al verificar usuario existente:', err.message);
        return res.status(500).json({ message: 'Error interno del servidor.' });
      }
      if (row) {
        return res.status(409).json({ message: 'El nombre de usuario ya existe.' });
      }

      // Hashear la contraseña
      const hashedPassword = await bcrypt.hash(password, 10);

      // Insertar el nuevo usuario en la base de datos
      db.run('INSERT INTO users (username, password, role, securityQuestion, securityAnswer) VALUES (?, ?, ?, ?, ?)',
        [username, hashedPassword, role, securityQuestion || null, securityAnswer || null], function(err) {
          if (err) {
            console.error('Error al crear usuario:', err.message);
            return res.status(500).json({ message: 'Error interno del servidor al crear usuario.' });
          }
          res.status(201).json({ message: 'Usuario creado exitosamente.', userId: this.lastID });
        });
    });
  } catch (error) {
    console.error('Error al hashear la contraseña:', error);
    res.status(500).json({ message: 'Error interno del servidor al procesar la contraseña.' });
  }
});

// Obtener un usuario por ID (solo admin o el propio usuario)
router.get('/:id', authenticateToken, (req, res) => {
  const userId = parseInt(req.params.id, 10);
  if (req.user.role !== 'admin' && req.user.id !== userId) {
    return res.status(403).json({ message: 'No autorizado para ver este usuario.' });
  }
  db.get('SELECT id, username, role, securityQuestion FROM users WHERE id = ?', [userId], (err, row) => {
    if (err) {
      console.error('Error al obtener usuario por ID:', err.message);
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    if (!row) {
      return res.status(404).json({ message: 'Usuario no encontrado.' });
    }
    // No devolver la pregunta de seguridad si no es el propio usuario o un admin solicitándola específicamente para recuperación
    // En este endpoint general, solo devolvemos id, username, role.
    // Si se necesita la pregunta para recuperación, se usará un endpoint específico.
    res.status(200).json({ id: row.id, username: row.username, role: row.role });
  });
});

// Actualizar rol de usuario (solo admin)
router.put('/:id/role', authenticateToken, (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para cambiar roles.' });
  }
  const userIdToUpdate = req.params.id;
  const { role } = req.body;

  if (!role || !['admin', 'employee'].includes(role)) {
    return res.status(400).json({ message: 'Rol inválido.' });
  }

  // Evitar que el admin se quite su propio rol de admin si es el único
  if (parseInt(userIdToUpdate, 10) === req.user.id && req.user.role === 'admin' && role !== 'admin') {
    db.get('SELECT COUNT(*) as adminCount FROM users WHERE role = ?', ['admin'], (err, row) => {
      if (err) {
        console.error('Error al contar admins:', err.message);
        return res.status(500).json({ message: 'Error interno del servidor.' });
      }
      if (row.adminCount <= 1) {
        return res.status(400).json({ message: 'No se puede cambiar el rol del único administrador.' });
      }
      updateUserRole(userIdToUpdate, role, res);
    });
  } else {
    updateUserRole(userIdToUpdate, role, res);
  }
});

function updateUserRole(userId, newRole, res) {
  db.run('UPDATE users SET role = ? WHERE id = ?', [newRole, userId], function(err) {
    if (err) {
      console.error('Error al actualizar rol del usuario:', err.message);
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    if (this.changes === 0) {
      return res.status(404).json({ message: 'Usuario no encontrado.' });
    }
    res.status(200).json({ message: 'Rol de usuario actualizado exitosamente.' });
  });
}

// Eliminar usuario (solo admin)
router.delete('/:id', authenticateToken, (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para eliminar usuarios.' });
  }
  const userIdToDelete = parseInt(req.params.id, 10);

  if (userIdToDelete === req.user.id) {
    return res.status(400).json({ message: 'No puedes eliminarte a ti mismo.' });
  }

  db.run('DELETE FROM users WHERE id = ?', [userIdToDelete], function(err) {
    if (err) {
      console.error('Error al eliminar usuario:', err.message);
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    if (this.changes === 0) {
      return res.status(404).json({ message: 'Usuario no encontrado.' });
    }
    res.status(200).json({ message: 'Usuario eliminado exitosamente.' });
  });
});

// Actualizar datos generales del usuario (solo admin)
router.put('/:id', authenticateToken, async (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para actualizar usuarios.' });
  }
  const userIdToUpdate = parseInt(req.params.id, 10);
  const { username, role, securityQuestion, securityAnswer } = req.body;

  if (!username || !role) {
    return res.status(400).json({ message: 'Nombre de usuario y rol son obligatorios.' });
  }

  // Validar el rol
  if (!['admin', 'employee'].includes(role)) {
    return res.status(400).json({ message: 'Rol inválido.' });
  }

  // Verificar si el nombre de usuario ya existe para otro usuario
  db.get('SELECT id FROM users WHERE username = ? AND id != ?', [username, userIdToUpdate], (err, row) => {
    if (err) {
      console.error('Error al verificar usuario existente para actualización:', err.message);
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    if (row) {
      return res.status(409).json({ message: 'El nombre de usuario ya existe para otro usuario.' });
    }

    // Actualizar datos del usuario
    db.run('UPDATE users SET username = ?, role = ?, securityQuestion = ?, securityAnswer = ? WHERE id = ?', 
      [username, role, securityQuestion || null, securityAnswer || null, userIdToUpdate], function(err) {
      if (err) {
        console.error('Error al actualizar usuario:', err.message);
        return res.status(500).json({ message: 'Error interno del servidor al actualizar usuario.' });
      }
      if (this.changes === 0) {
        console.log(`[ADMIN RESET PW] Failed to update password for User ID: ${userIdToUpdate}. User not found or password not changed.`);
        return res.status(404).json({ message: 'Usuario no encontrado o la contraseña no se modificó.' });
      }
      res.status(200).json({ message: 'Usuario actualizado exitosamente.' });
    });
  });
});

// Ruta para que un admin resetee la contraseña de otro usuario
router.put('/:id/reset-password-admin', authenticateToken, async (req, res) => {
  if (req.user.role !== 'admin') {
    return res.status(403).json({ message: 'No autorizado para resetear contraseñas.' });
  }
  const userIdToUpdate = parseInt(req.params.id, 10);
  const { newPassword } = req.body;
  console.log(`[ADMIN RESET PW] User ID: ${req.params.id}, Received new password: ${newPassword}`); // Log contraseña recibida

  if (!newPassword) {
    return res.status(400).json({ message: 'La nueva contraseña es obligatoria.' });
  }

  try {
    const hashedPassword = await bcrypt.hash(newPassword, 10);
    console.log(`[ADMIN RESET PW] User ID: ${req.params.id}, Generated hash: ${hashedPassword}`); // Log hash generado

    console.log(`Attempting to update password for user ID: ${userIdToUpdate} with new hashed password: ${hashedPassword}`);
    db.run('UPDATE users SET password = ? WHERE id = ?', [hashedPassword, userIdToUpdate], function(err) {
      if (err) {
        console.error('Error al resetear contraseña del usuario:', err.message);
        return res.status(500).json({ message: 'Error interno del servidor.' });
      }
      if (this.changes === 0) {
        console.log(`[ADMIN RESET PW] Failed to update password for User ID: ${userIdToUpdate}. User not found or password not changed.`);
        return res.status(404).json({ message: 'Usuario no encontrado o la contraseña no se modificó.' });
      }
      console.log(`Password update successful for user ID: ${userIdToUpdate}. Changes: ${this.changes}`);
      res.status(200).json({ message: 'Contraseña reseteada exitosamente.' });
    });
  } catch (error) {
    console.error('Error al hashear la nueva contraseña:', error);
    res.status(500).json({ message: 'Error interno del servidor al procesar la contraseña.' });
  }
});

// Ruta para obtener la pregunta de seguridad de un usuario
router.post('/get-security-question', async (req, res) => {
  const { username } = req.body;
  if (!username) {
    return res.status(400).json({ message: 'Nombre de usuario requerido.' });
  }
  db.get('SELECT securityQuestion FROM users WHERE username = ?', [username], (err, row) => {
    if (err) {
      console.error('Error al obtener pregunta de seguridad:', err.message);
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    if (!row || !row.securityQuestion) {
      return res.status(404).json({ message: 'Usuario no encontrado o sin pregunta de seguridad configurada.' });
    }
    res.status(200).json({ securityQuestion: row.securityQuestion });
  });
});

// Ruta para resetear la contraseña usando la respuesta de seguridad
router.post('/reset-password', async (req, res) => {
  const { username, securityAnswer, newPassword } = req.body;
  if (!username || !securityAnswer || !newPassword) {
    return res.status(400).json({ message: 'Faltan campos obligatorios.' });
  }

  db.get('SELECT * FROM users WHERE username = ?', [username], async (err, user) => {
    if (err) {
      console.error('Error al buscar usuario para reseteo:', err.message);
      return res.status(500).json({ message: 'Error interno del servidor.' });
    }
    if (!user) {
      return res.status(404).json({ message: 'Usuario no encontrado.' });
    }
    if (!user.securityAnswer) {
        return res.status(400).json({ message: 'El usuario no tiene configurada una respuesta de seguridad.' });
    }

    // Aquí se debería comparar la respuesta de seguridad. 
    // IMPORTANTE: En una aplicación real, la respuesta de seguridad también debería estar hasheada.
    // Por simplicidad en este ejemplo, se compara directamente (NO SEGURO PARA PRODUCCIÓN).
    if (securityAnswer.toLowerCase() !== user.securityAnswer.toLowerCase()) { // Comparación insensible a mayúsculas
      return res.status(401).json({ message: 'Respuesta de seguridad incorrecta.' });
    }

    try {
      const hashedPassword = await bcrypt.hash(newPassword, 10);
      db.run('UPDATE users SET password = ? WHERE username = ?', [hashedPassword, username], function(err) {
        if (err) {
          console.error('Error al actualizar contraseña:', err.message);
          return res.status(500).json({ message: 'Error al actualizar la contraseña.' });
        }
        res.status(200).json({ message: 'Contraseña actualizada exitosamente.' });
      });
    } catch (error) {
      console.error('Error al hashear nueva contraseña:', error);
      res.status(500).json({ message: 'Error interno del servidor al procesar la nueva contraseña.' });
    }
  });
});

// Ruta para que un usuario autenticado actualice su propia contraseña
router.put('/update-password', authenticateToken, async (req, res) => {
    const { currentPassword, newPassword } = req.body;
    const userId = req.user.id;

    if (!currentPassword || !newPassword) {
        return res.status(400).json({ message: 'Contraseña actual y nueva son requeridas.' });
    }

    db.get('SELECT password FROM users WHERE id = ?', [userId], async (err, user) => {
        if (err) {
            console.error('Error al buscar usuario para actualizar contraseña:', err.message);
            return res.status(500).json({ message: 'Error interno del servidor.' });
        }
        if (!user) {
            return res.status(404).json({ message: 'Usuario no encontrado.' }); // Aunque debería existir por authenticateToken
        }

        const passwordMatch = await bcrypt.compare(currentPassword, user.password);
        if (!passwordMatch) {
            return res.status(401).json({ message: 'La contraseña actual es incorrecta.' });
        }

        try {
            const hashedNewPassword = await bcrypt.hash(newPassword, 10);
            db.run('UPDATE users SET password = ? WHERE id = ?', [hashedNewPassword, userId], function(err) {
                if (err) {
                    console.error('Error al actualizar la contraseña del usuario:', err.message);
                    return res.status(500).json({ message: 'Error al actualizar la contraseña.' });
                }
                res.status(200).json({ message: 'Contraseña actualizada exitosamente.' });
            });
        } catch (error) {
            console.error('Error al hashear la nueva contraseña:', error);
            res.status(500).json({ message: 'Error interno del servidor al procesar la nueva contraseña.' });
        }
    });
});


module.exports = router;