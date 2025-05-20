document.addEventListener('DOMContentLoaded', function() {
    // Verificar si el usuario ha iniciado sesión (comprobando el token)
    const token = sessionStorage.getItem('token');
    const userRole = sessionStorage.getItem('userRole');
    const userId = sessionStorage.getItem('userId'); // Obtener el ID del usuario
    const currentPage = window.location.pathname;

    console.log('auth_check.js cargado');
    console.log('Token:', token);
    console.log('User Role:', userRole);
    console.log('User ID:', userId); // Log del ID del usuario

    // Actualizar el indicador de rol si el elemento existe
    const roleIndicator = document.getElementById('role-indicator');
    if (roleIndicator && userRole) {
        roleIndicator.textContent = userRole === 'admin' ? 'A' : 'U';
    }

    // Actualizar el indicador de ID de usuario si el elemento existe
    const userIdIndicator = document.getElementById('user-id-indicator');
    if (userIdIndicator && userId) {
        userIdIndicator.textContent = 'ID: ' + userId;
    }

    if (!token) {
        // Si no hay token, redirigir a la página de inicio de sesión
        if (!currentPage.includes('/auth/login.html') && !currentPage.includes('/auth/forgot_password.html')) {
             window.location.href = '../auth/login.html';
        }
    } else {
        // Si hay token, verificar el rol y la página actual
        if (currentPage.includes('/protected_pages/configuracion.html')) {
            if (userRole === 'admin') {
                // Si es admin y está en configuracion.html, redirigir a configuracion_admin.html
                window.location.href = '../protected_pages/configuracion_admin.html';
            } else {
                // Si no es admin y está en configuracion.html, no hacer nada (ya está en la página correcta para no-admins)
            }
        } else if (currentPage.includes('/protected_pages/configuracion_admin.html')) {
            if (userRole !== 'admin') {
                // Si no es admin y está en configuracion_admin.html, redirigir a configuracion.html
                window.location.href = '../protected_pages/configuracion.html';
            }
        }
        // Aquí se pueden añadir más verificaciones para otras páginas protegidas si es necesario
    }
});

// Función para cerrar sesión
function logout() {
    sessionStorage.removeItem('token'); // Eliminar el token de sesión
    sessionStorage.removeItem('userRole'); // Eliminar el rol del usuario
    sessionStorage.removeItem('userId'); // Eliminar el ID del usuario
    window.location.href = '../auth/login.html'; // Redirigir a la página de inicio de sesión
}