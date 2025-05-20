document.addEventListener('DOMContentLoaded', () => {
    const forgotPasswordForm = document.getElementById('forgotPasswordForm');

    if (forgotPasswordForm) {
        forgotPasswordForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const username = document.getElementById('username').value;
            const securityQuestion = document.getElementById('securityQuestion').value;
            const securityAnswer = document.getElementById('securityAnswer').value;
            const newPassword = document.getElementById('newPassword').value;

            if (!username || !securityQuestion || !securityAnswer || !newPassword) {
                alert('Por favor, completa todos los campos.');
                return;
            }

            try {
                const response = await fetch('/forgot-password', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username, securityQuestion, securityAnswer, newPassword })
                });

                const data = await response.json();

                if (response.ok) {
                    alert(data.message);
                    // Redirigir al login después de cambiar la contraseña
                    window.location.href = 'login.html';
                } else {
                    alert('Error al cambiar contraseña: ' + data.message);
                }
            } catch (error) {
                console.error('Error al recuperar contraseña:', error);
                alert('Error al conectar con el servidor.');
            }
        });
    }
});