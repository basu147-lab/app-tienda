document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');

    if (registerForm) {
        registerForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            console.log('Register form submitted');

            const newUsername = event.target.newUsername.value;
            const newPassword = event.target.newPassword.value;
            const confirmPassword = event.target.confirmPassword.value;
            const securityQuestion = event.target.securityQuestion.value; // Asumiendo que el select tiene este nombre
            const securityAnswer = event.target.securityAnswer.value; // Asumiendo que el input tiene este nombre
            const role = 'user'; // Rol por defecto para nuevos registros

            if (newPassword !== confirmPassword) {
                alert('Las contraseñas no coinciden.');
                return;
            }

            if (!newUsername || !newPassword || !securityQuestion || !securityAnswer) {
                 alert('Por favor, completa todos los campos.');
                 return;
            }

            try {
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username: newUsername, password: newPassword, role, securityQuestion, securityAnswer })
                });

                const data = await response.json();

                if (response.ok) {
                    alert(data.message);
                    // Redirigir a la página de login después de un registro exitoso
                    window.location.href = 'login.html';
                } else {
                    alert('Error en el registro: ' + data.message);
                }
            } catch (error) {
                console.error('Error al registrar usuario:', error);
                alert('Error al conectar con el servidor.');
            }
        });
    }
});