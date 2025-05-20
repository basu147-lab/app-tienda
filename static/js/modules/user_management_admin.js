// static/js/modules/user_management_admin.js

/**
 * Obtiene y muestra la lista de usuarios, y configura los listeners para editar, eliminar y cambiar contraseña.
 * Esta función está destinada a ser utilizada por administradores.
 * @param {HTMLElement} userListElement - El elemento HTML donde se mostrará la lista de usuarios.
 * @param {HTMLElement} changePasswordModal - El modal para cambiar la contraseña.
 * @param {HTMLFormElement} changePasswordFormModal - El formulario dentro del modal de cambio de contraseña.
 */
export async function initializeUserList(userListElement, changePasswordModal, changePasswordFormModal) {
    console.log('initializeUserList called'); // Log para verificar si la función se llama
    if (!userListElement) {
        console.log('userListElement is null or undefined'); // Log si el elemento no se encuentra
        return;
    }

    try {
        const token = sessionStorage.getItem('token');
        console.log('Fetching users from /api/users'); // Log antes de la llamada API
        const response = await fetch('/api/users', { // Asegúrate que la ruta API sea correcta
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        console.log('API response status:', response.status); // Log del estado de la respuesta

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                alert('Sesión expirada o no autorizada. Por favor, inicia sesión de nuevo.');
                // Asumiendo que existe una función global logout o redirige
                if (typeof logout === 'function') logout(); else window.location.href = '../../auth/login.html'; 
                return;
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const users = await response.json();
        console.log('Users received:', users); // Log de los datos de usuarios recibidos

        userListElement.innerHTML = ''; // Limpiar lista anterior

        if (users.length === 0) {
            userListElement.innerHTML = '<p>No hay usuarios disponibles.</p>'; // Mensaje si no hay usuarios
            console.log('No users available.');
        } else {
            users.forEach(user => {
                const listItem = document.createElement('li');
                listItem.className = 'user-list-item'; // Clase para estilizar si es necesario
                
                let buttonsHtml = `
                    <button class="edit-btn black-border-btn" data-id="${user.id}" data-username="${user.username}">Editar</button>
                    <button class="delete-btn black-border-btn" data-id="${user.id}">Eliminar Usuario</button>
                    <button class="open-change-password-modal-btn-list black-border-btn" data-user-id="${user.id}" data-username="${user.username}">Cambiar Contraseña</button>
                `;

                listItem.innerHTML = `
                    <span>ID: ${user.id}, Usuario: ${user.username}, Rol: ${user.role}</span>
                    <div class="user-actions">${buttonsHtml}</div>
                `;
                userListElement.appendChild(listItem);
            });
            console.log('User list populated.');
        }

        setupUserActionListeners(userListElement, changePasswordModal, changePasswordFormModal);

    } catch (error) {
        console.error('Error al obtener usuarios:', error); // Log del error
        if (userListElement) userListElement.innerHTML = '<p>Error al cargar la lista de usuarios.</p>';
    }
}

function setupUserActionListeners(userListElement, changePasswordModal, changePasswordFormModal) {
    userListElement.addEventListener('click', async (event) => {
        const target = event.target;
        const token = sessionStorage.getItem('token');

        if (target.classList.contains('edit-btn')) {
            const userId = target.dataset.id;
            // const currentUsername = target.dataset.username; // Ya no es necesario si obtenemos los datos completos
            console.log('Edit button clicked for user ID:', userId);
            // Obtener los datos completos del usuario para llenar el formulario de edición
            try {
                const response = await fetch(`/api/users/${userId}`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });

                if (!response.ok) {
                    if (response.status === 401 || response.status === 403) {
                        alert('Sesión expirada o no autorizada. Por favor, inicia sesión de nuevo.');
                        if (typeof logout === 'function') logout(); else window.location.href = '../../auth/login.html';
                        return;
                    }
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const user = await response.json();
                console.log('User data fetched for editing:', user);

                // Llenar el formulario de creación/edición con los datos del usuario
                const userFormElement = document.getElementById('userForm'); // Asume que el formulario tiene este ID
                if (userFormElement) {
                    document.getElementById('userIdField').style.display = 'block';
                    document.getElementById('userId').value = user.id;
                    document.getElementById('username').value = user.username;
                    document.getElementById('role').value = user.role;
                    // Asumiendo que tienes campos para pregunta y respuesta de seguridad
                    const securityQuestionField = document.getElementById('securityQuestion');
                    const securityAnswerField = document.getElementById('securityAnswer');
                    if (securityQuestionField) securityQuestionField.value = user.securityQuestion || '';
                    if (securityAnswerField) securityAnswerField.value = user.securityAnswer || '';

                    // Ocultar o hacer opcional el campo de contraseña para edición
                    // Ocultar o hacer opcional el campo de contraseña para edición
                    const passwordField = document.getElementById('password');
                    if (passwordField) {
                         passwordField.value = ''; // Limpiar campo de contraseña
                         passwordField.required = false; // Contraseña no requerida para edición
                         // Hide the parent div of the password field
                         if (passwordField.parentElement) {
                             passwordField.parentElement.style.display = 'none';
                         }
                    }

                    userFormElement.dataset.userId = user.id; // Guardar ID en el formulario para el submit
                    userFormElement.querySelector('button[type="submit"]').textContent = 'Actualizar Usuario';

                    // Opcional: Desplazarse al formulario
                    userFormElement.scrollIntoView({ behavior: 'smooth' });

                } else {
                    console.error('User form element not found.');
                }

            } catch (error) {
                console.error('Error fetching user data for editing:', error);
                alert('Error al cargar los datos del usuario para edición.');
            }
        }

        if (target.classList.contains('delete-btn')) {
            const userId = target.dataset.id;
            if (confirm(`¿Estás seguro de que quieres eliminar al usuario ID: ${userId}?`)) {
                try {
                    const response = await fetch(`/api/users/${userId}`, { // Asegúrate que la ruta API sea correcta
                        method: 'DELETE',
                        headers: {
                            'Authorization': `Bearer ${token}`
                        }
                    });
                    const data = await response.json();
                    if (response.ok) {
                        alert(data.message || 'Usuario eliminado.');
                        initializeUserList(userListElement, changePasswordModal, changePasswordFormModal); // Recargar lista
                    } else {
                        alert(`Error al eliminar usuario: ${data.message || response.statusText}`);
                         if (response.status === 401 || response.status === 403) {
                           if (typeof logout === 'function') logout(); else window.location.href = '../../auth/login.html';
                        }
                    }
                } catch (error) {
                    console.error('Error al eliminar usuario:', error);
                    alert('Error de red al eliminar usuario.');
                }
            }
        }

        if (target.classList.contains('open-change-password-modal-btn-list')) {
            const targetUserId = target.dataset.userId;
            const targetUsername = target.dataset.username;
            if (changePasswordModal && changePasswordFormModal) {
                changePasswordFormModal.reset(); // Reset form first

                // Guardar el ID y nombre del usuario para el que se cambiará la contraseña
                changePasswordFormModal.dataset.targetUserId = targetUserId;
                changePasswordFormModal.dataset.targetUsername = targetUsername;

                // Actualizar título o algún indicador en el modal
                const modalTitle = changePasswordModal.querySelector('h2');
                if(modalTitle) modalTitle.textContent = `Cambiar Contraseña para ${targetUsername}`;

                // Configurar para cambio por admin: currentPassword NO es requerida y se oculta
                const currentPasswordElem = changePasswordFormModal.elements['currentPassword'];
                const currentPasswordLabel = changePasswordFormModal.querySelector('label[for="currentPassword"]');
                if (currentPasswordElem) {
                    currentPasswordElem.style.display = 'none'; // Ocultar
                    currentPasswordElem.required = false;
                }
                if (currentPasswordLabel) {
                    currentPasswordLabel.style.display = 'none'; // Ocultar
                }

                // Asegurar que newPassword y confirmNewPassword estén visibles y requeridos
                const newPasswordElem = changePasswordFormModal.elements['newPassword'];
                const newPasswordLabel = changePasswordFormModal.querySelector('label[for="newPassword"]');
                if (newPasswordElem) {
                    newPasswordElem.style.display = '';
                    newPasswordElem.required = true;
                }
                if (newPasswordLabel) newPasswordLabel.style.display = '';

                const confirmNewPasswordElem = changePasswordFormModal.elements['confirmNewPassword'];
                const confirmNewPasswordLabel = changePasswordFormModal.querySelector('label[for="confirmNewPassword"]');
                if (confirmNewPasswordElem) {
                    confirmNewPasswordElem.style.display = '';
                    confirmNewPasswordElem.required = true;
                }
                if (confirmNewPasswordLabel) confirmNewPasswordLabel.style.display = '';

                changePasswordModal.style.display = 'block'; // Mostrar modal al final
            } else {
                console.error('Modal de cambio de contraseña o su formulario no encontrados.');
            }
        }
    });
}