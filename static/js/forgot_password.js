document.addEventListener('DOMContentLoaded', () => {
    const forgotPasswordForm = document.getElementById('forgotPasswordForm');

    if (forgotPasswordForm) {
        forgotPasswordForm.addEventListener('submit', (event) => {
            event.preventDefault();

            const username = document.getElementById('username').value;
            const securityQuestion = document.getElementById('securityQuestion').value;
            const securityAnswer = document.getElementById('securityAnswer').value;
            const newPassword = document.getElementById('newPassword').value;

            // Cargar usuarios desde localStorage
            const users = JSON.parse(localStorage.getItem('users')) || [];

            // Encontrar al usuario por nombre de usuario
            const user = users.find(user => user.username === username);

            if (user) {
                // Verificar la pregunta y respuesta de seguridad
                if (user.securityQuestion === securityQuestion && user.securityAnswer === securityAnswer) {
                    // Actualizar la contraseña del usuario
                    user.password = newPassword; // En una aplicación real, hashear la contraseña
                    localStorage.setItem('users', JSON.stringify(users));
                    alert('Contraseña cambiada con éxito.');
                    window.location.href = 'login.html'; // Redirigir a la página de login
                } else {
                    alert('Pregunta o respuesta de seguridad incorrecta.');
                }
            } else {
                alert('Usuario no encontrado.');
            }
        });
    }
});