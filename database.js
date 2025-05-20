const sqlite3 = require('sqlite3').verbose();
const { initializeDatabase } = require('./database_setup'); // Importar la función de inicialización

const dbFile = './database.sqlite';

const db = new sqlite3.Database(dbFile, (err) => {
  if (err) {
    console.error('Error al conectar con la base de datos:', err.message);
    // Si hay un error al conectar, no se puede continuar.
    // Considerar lanzar una excepción o terminar el proceso si la BD es crítica.
    process.exit(1); 
  } else {
    console.log('Conectado a la base de datos SQLite.');
    // Llamar a la función para configurar las tablas y datos iniciales
    initializeDatabase(db);
  }
});

// Manejo de cierre de la base de datos al terminar la aplicación (opcional pero buena práctica)
process.on('SIGINT', () => {
  db.close((err) => {
    if (err) {
      return console.error(err.message);
    }
    console.log('Conexión a la base de datos cerrada.');
    process.exit(0);
  });
});

module.exports = db;