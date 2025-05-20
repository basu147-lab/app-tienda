// static/js/modules/user_creation_admin.js

/**
 * Inicializa el formulario de creación/edición de usuarios para administradores.
 * @param {HTMLFormElement} userFormElement - El formulario de creación/edición de usuarios.
 * @param {function} onSuccess - Función a ejecutar después de una creación/edición exitosa.
 */
export function initializeUserCreationForm(userFormElement, onSuccess) {
    console.log('initializeUserCreationForm called');
    if (!userFormElement) {
        console.log('userFormElement is null or undefined');
        return;
    }

    userFormElement.addEventListener('submit', async (event) => {
        event.preventDefault();

        const userId = userFormElement.dataset.userId; // Podría usarse para edición
        const isEditing = !!userId;

        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value; // Solo requerido para creación o cambio de contraseña en edición
        const role = document.getElementById('role').value;
        const securityQuestion = document.getElementById('securityQuestion').value;
        const securityAnswer = document.getElementById('securityAnswer').value;

        // Validaciones básicas
        if (!username || !role) {
            alert('Nombre de usuario y rol son obligatorios.');
            return;
        }

        // La contraseña es obligatoria solo para la creación de un nuevo usuario.
        // Para la edición, la contraseña es opcional (se maneja con el modal de cambio de contraseña).
        if (!isEditing && !password) {
             alert('La contraseña es obligatoria para crear un nuevo usuario.');
             return;
        }

        const token = sessionStorage.getItem('token');
        const method = isEditing ? 'PUT' : 'POST';
        const url = isEditing ? `/api/users/${userId}` : '/api/users'; // Usar endpoint general para edición

        let userData;

        if (isEditing) {
            // Para edición, enviamos todos los datos relevantes
            userData = {
                username: username,
                role: role,
                securityQuestion: securityQuestion || null,
                securityAnswer: securityAnswer || null
            };
            // No incluir la contraseña en la edición general, se maneja por separado
        } else {
            // Para creación, enviamos todos los datos al endpoint general
            userData = {
                username: username,
                password: password, // Contraseña es obligatoria para creación
                role: role,
                securityQuestion: securityQuestion || null,
                securityAnswer: securityAnswer || null
            };
        }

        console.log(`Submitting user form (${method}):`, userData);

        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(userData)
            });

            const data = await response.json();
            console.log('API response:', response.status, data);

            if (response.ok) {
                alert(data.message || `Usuario ${isEditing ? 'actualizado' : 'creado'} exitosamente.`);
                userFormElement.reset(); // Limpiar formulario
                if (onSuccess) {
                    onSuccess(); // Recargar lista de usuarios u otra acción
                }
                 // Ocultar campo de ID y botón de edición si estaba en modo edición
                 document.getElementById('userIdField').style.display = 'none';
                 userFormElement.dataset.userId = ''; // Limpiar ID de usuario
                 userFormElement.querySelector('button[type="submit"]').textContent = 'Guardar Usuario'; // Restaurar texto del botón

                 // Asegurar que el campo de contraseña esté visible y sea requerido para el modo de creación
                 const passwordFieldAfterSubmit = document.getElementById('password');
                 if (passwordFieldAfterSubmit) {
                     passwordFieldAfterSubmit.required = true;
                     if (passwordFieldAfterSubmit.parentElement) {
                         // Asegurarse de que el contenedor del campo de contraseña esté visible
                         passwordFieldAfterSubmit.parentElement.style.display = '';
                     }
                 }

            } else {
                alert(`Error al ${isEditing ? 'actualizar' : 'crear'} usuario: ${data.message || response.statusText}`);
                 if (response.status === 401 || response.status === 403) {
                   if (typeof logout === 'function') logout(); else window.location.href = '../../auth/login.html';
                }
            }
        } catch (error) {
            console.error(`Error al ${isEditing ? 'actualizar' : 'crear'} usuario:`, error);
            alert(`Error de red al ${isEditing ? 'actualizar' : 'crear'} usuario.`);
        }
    });
}