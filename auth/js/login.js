document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');

    if (loginForm) {
        loginForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            console.log('Login form submitted');

            const username = event.target.username.value;
            const password = event.target.password.value;

            if (!username || !password) {
                alert('Por favor, completa todos los campos.');
                return;
            }

            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username, password })
                });

                const data = await response.json();

                if (response.ok) {
                    // Autenticación exitosa
                    sessionStorage.setItem('token', data.token); // Guardar el token JWT

                    // Decodificar el token para obtener el ID y el rol del usuario
                    try {
                        const tokenPayload = JSON.parse(atob(data.token.split('.')[1]));
                        console.log('Token Payload:', tokenPayload);
                        sessionStorage.setItem('userRole', tokenPayload.role); // Guardar el rol
                        sessionStorage.setItem('userId', tokenPayload.id); // Guardar el ID
                        console.log('Inicio de sesión exitoso para el usuario:', username, 'con rol:', tokenPayload.role, 'e ID:', tokenPayload.id);
                    } catch (e) {
                        console.error('Error al decodificar el token:', e);
                        alert('Error al procesar la información del usuario.');
                        // Considerar redirigir o manejar el error de token aquí
                        return;
                    }

                    alert(data.message);
                    // Redirigir a la página de inicio protegida
                    window.location.href = '../../protected_pages/index.html'; // Redirigir a la página de inicio protegida (HTML)
                } else {
                    // Autenticación fallida
                    alert('Error en el login: ' + data.message);
                    console.log('Intento de inicio de sesión fallido para el usuario:', username);
                }
            } catch (error) {
                console.error('Error al iniciar sesión:', error);
                alert('Error al conectar con el servidor.');
            }
        });
    }
});