const jwt = require('jsonwebtoken');
const jwtSecret = 'your_jwt_secret_key'; // Debería estar en una configuración centralizada y segura

function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1]; // Bearer TOKEN

  if (token == null) {
    return res.sendStatus(401); // No autorizado
  }

  jwt.verify(token, jwtSecret, (err, user) => {
    if (err) {
      console.error('Error al verificar token:', err);
      return res.sendStatus(403); // Prohibido (token inválido o expirado)
    }
    req.user = user; // Añadir el usuario decodificado a la solicitud
    next(); // Continuar con la siguiente función middleware o ruta
  });
}

module.exports = { authenticateToken, jwtSecret };