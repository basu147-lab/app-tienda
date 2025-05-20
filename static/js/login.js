document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');

    if (loginForm) {
        loginForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            console.log('Login form submitted');
            const username = event.target.username.value;
            const password = event.target.password.value;

            try {
                const response = await fetch('/login', {
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
                    // Decodificar el token para obtener el rol (esto es básico, en producción se haría en el backend o se enviaría el rol aparte)
                    // Para este ejemplo, asumimos que el token contiene el rol en el payload
                    const tokenPayload = JSON.parse(atob(data.token.split('.')[1]));
                    console.log('Token Payload:', tokenPayload);
                    sessionStorage.setItem('userRole', tokenPayload.role);
                    sessionStorage.setItem('userId', tokenPayload.id); // Guardar el ID del usuario usando la clave correcta 'id'

                    console.log('Inicio de sesión exitoso para el usuario:', username, 'con rol:', tokenPayload.role);
                    // Redirigir a la página principal o a una página protegida
                    window.location.href = '../protected_pages/product_management.html';
                } else {
                    // Autenticación fallida
                    alert('Error al iniciar sesión: ' + data.message);
                    console.log('Intento de inicio de sesión fallido para el usuario:', username);
                }
            } catch (error) {
                console.error('Error al iniciar sesión:', error);
                alert('Error al conectar con el servidor.');
            }
        });
    }
});