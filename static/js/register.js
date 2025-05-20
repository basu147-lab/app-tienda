document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');

    if (registerForm) {
        registerForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            console.log('Register form submitted');

            const newUsername = event.target.newUsername.value;
            const newPassword = event.target.newPassword.value;
            const confirmPassword = event.target.confirmPassword.value;
            const securityQuestion = event.target.securityQuestion.value;
            const securityAnswer = event.target.securityAnswer.value;

            console.log('Valores del formulario:', { newUsername, newPassword, confirmPassword, securityQuestion, securityAnswer });
            // Aquí irá la lógica para manejar el registro de usuarios
            // Por ejemplo, recoger los datos del formulario, validarlos y enviarlos a un backend

            if (newPassword !== confirmPassword) {
                console.error('Las contraseñas no coinciden.');
                alert('Las contraseñas no coinciden.');
                return;
            }

            if (newUsername && newPassword && securityQuestion && securityAnswer) {
                // Cargar usuarios existentes
                let users = [];
                console.log('Intentando cargar usuarios de localStorage');
                try {
                    users = JSON.parse(localStorage.getItem('users')) || [];
                    console.log('Usuarios cargados:', users);
                } catch (e) {
                    console.error('Error al cargar usuarios de localStorage:', e);
                    alert('Error al cargar datos de usuario. Por favor, inténtalo de nuevo.');
                    return;
                }

                // Verificar si el usuario ya existe
                const userExists = users.some(user => user.username === newUsername);
                if (userExists) {
                    console.error('El nombre de usuario ya existe.');
                    alert('El nombre de usuario ya existe.');
                    console.log('Usuario', newUsername, 'ya existe.');
                    return;
                }

                // Crear nuevo usuario con pregunta y respuesta de seguridad
                const newUser = {
                    id: Date.now().toString(), // Simple unique ID
                    username: newUsername,
                    password: newPassword, // En una aplicación real, hashear la contraseña
                    role: 'employee', // Rol por defecto para nuevos registros (cambiado de 'customer' a 'employee')
                    securityQuestion: securityQuestion,
                    securityAnswer: securityAnswer // En una aplicación real, hashear la respuesta
                };

                console.log('Creando nuevo usuario:', newUser);
                console.log('Estado de la lista de usuarios antes de añadir:', users);
                users.push(newUser);
                console.log('Usuario añadido a la lista:', users);
                console.log('Intentando guardar usuarios en localStorage:', users);
                try {
                    const usersToSave = JSON.stringify(users);
                    console.log('Datos a guardar en localStorage:', usersToSave);
localStorage.setItem('users', usersToSave);
console.log('Usuarios guardados en localStorage con éxito.');
                } catch (e) {
                    console.error('Error al guardar usuarios en localStorage:', e);
                    alert('Error al guardar datos de usuario. Por favor, inténtalo de nuevo.');
                    return;
                }


                window.location.href = 'login.html'; // Redirigir a la página de login
            } else {
                console.error('Campos incompletos.');
                alert('Por favor, completa todos los campos.');
                console.log('Campos incompletos.');
            }
        });
    }
});