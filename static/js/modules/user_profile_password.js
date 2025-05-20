// static/js/modules/user_profile_password.js

/**
 * Configura el modal y el formulario para cambio de contraseña.
 * Puede ser usado tanto por el usuario para cambiar su propia contraseña como por un admin para cambiar la de otro.
 * @param {HTMLElement | null} openModalBtn - El botón para abrir el modal (opcional). Si es null, se asume que el modal se abre por otros medios.
 * @param {HTMLElement} modalElement - El elemento HTML del modal.
 * @param {HTMLFormElement} formElement - El formulario dentro del modal.
 */
export function initializePasswordChange(openModalBtn, modalElement, formElement) {
    if (!modalElement || !formElement) {
        console.warn('Modal o formulario para cambio de contraseña no encontrados en el DOM.');
        return;
    }

    if (openModalBtn) {
        openModalBtn.addEventListener('click', () => {
            formElement.reset(); // Reset form first
            
            delete formElement.dataset.targetUserId;
            delete formElement.dataset.targetUsername;
            
            const modalTitle = modalElement.querySelector('h2');
            if (modalTitle) modalTitle.textContent = 'Cambiar Mi Contraseña';

            // Configure for self-change: currentPassword is required and visible
            const currentPasswordElem = formElement.elements['currentPassword'];
            const currentPasswordLabel = formElement.querySelector('label[for="currentPassword"]');
            if (currentPasswordElem) {
                currentPasswordElem.style.display = ''; // Make visible
                currentPasswordElem.required = true;
            }
            if (currentPasswordLabel) {
                currentPasswordLabel.style.display = ''; // Make visible
            }

            // Ensure new/confirm are also visible and required
            const newPasswordElem = formElement.elements['newPassword'];
            const newPasswordLabel = formElement.querySelector('label[for="newPassword"]');
            if (newPasswordElem) {
                newPasswordElem.style.display = '';
                newPasswordElem.required = true;
            }
            if (newPasswordLabel) newPasswordLabel.style.display = '';
            
            const confirmNewPasswordElem = formElement.elements['confirmNewPassword'];
            const confirmNewPasswordLabel = formElement.querySelector('label[for="confirmNewPassword"]');
            if (confirmNewPasswordElem) {
                confirmNewPasswordElem.style.display = '';
                confirmNewPasswordElem.required = true;
            }
            if (confirmNewPasswordLabel) confirmNewPasswordLabel.style.display = '';

            modalElement.style.display = 'block'; // Show modal last
        });
    }

    const closeButton = modalElement.querySelector('.close-btn');
    if (closeButton) {
        closeButton.addEventListener('click', () => {
            modalElement.style.display = 'none';
        });
    }

    window.addEventListener('click', (event) => {
        if (event.target === modalElement) {
            modalElement.style.display = 'none';
        }
    });

    formElement.addEventListener('submit', async (event) => {
        event.preventDefault();
        const token = sessionStorage.getItem('token');
        const loggedInUserId = sessionStorage.getItem('userId');
        const loggedInUserRole = sessionStorage.getItem('userRole'); // Obtener el rol del usuario logueado

        const targetUserId = formElement.dataset.targetUserId || loggedInUserId;
        const isAdminChange = formElement.dataset.targetUserId && loggedInUserRole === 'admin';

        let newPassword = '';
        let confirmPassword = '';
        let currentPassword = null;

        if (isAdminChange) {
            // Admin está cambiando la contraseña para otro usuario
            const newPasswordElemModal = formElement.elements['newPasswordModal'];
            newPassword = newPasswordElemModal ? newPasswordElemModal.value : '';

            const confirmPasswordElemModal = formElement.elements['confirmNewPasswordModal'];
            confirmPassword = confirmPasswordElemModal ? confirmPasswordElemModal.value : '';
            
            // currentPassword no es necesario para el cambio por admin, permanece null
            // El campo currentPasswordModal existe en el HTML de admin pero está oculto y no es 'required'
            // por lo que no necesitamos leerlo aquí.
        } else {
            // Usuario cambiando su propia contraseña
            const newPasswordElem = formElement.elements['newPassword'];
            newPassword = newPasswordElem ? newPasswordElem.value : '';

            const confirmPasswordElem = formElement.elements['confirmNewPassword'];
            confirmPassword = confirmPasswordElem ? confirmPasswordElem.value : '';

            const currentPasswordElem = formElement.elements['currentPassword'];
            // Check element existence and its value if it's supposed to be used
            if (!currentPasswordElem || !currentPasswordElem.value) { 
                alert('Por favor, introduce tu contraseña actual.');
                return;
            }
            currentPassword = currentPasswordElem.value;
        }

        if (newPassword !== confirmPassword) {
            alert('Las nuevas contraseñas no coinciden.');
            return;
        }

        let apiEndpoint;
        let payload;

        console.log('[PROFILE_PASSWORD_SUBMIT] isAdminChange:', isAdminChange);
        console.log('[PROFILE_PASSWORD_SUBMIT] targetUserId (from dataset):', formElement.dataset.targetUserId);
        console.log('[PROFILE_PASSWORD_SUBMIT] loggedInUserId (from session):', loggedInUserId);
        console.log('[PROFILE_PASSWORD_SUBMIT] effective targetUserId for API:', targetUserId);
        console.log('[PROFILE_PASSWORD_SUBMIT] loggedInUserRole:', loggedInUserRole);

        if (isAdminChange) {
            // Admin está cambiando la contraseña de otro usuario (no requiere contraseña actual)
            apiEndpoint = `/api/users/${targetUserId}/reset-password-admin`; // Endpoint específico para admin
            payload = { newPassword };
        } else {
            // Usuario cambiando su propia contraseña
            apiEndpoint = `/api/users/update-password`; // Endpoint para el usuario actualizando su propia contraseña
            payload = { currentPassword, newPassword };
        }

        console.log('[PROFILE_PASSWORD_SUBMIT] API Endpoint:', apiEndpoint);
        console.log('[PROFILE_PASSWORD_SUBMIT] Payload:', JSON.stringify(payload));

        try {
            const response = await fetch(apiEndpoint, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (response.ok) {
                alert(data.message || 'Contraseña actualizada exitosamente.');
                modalElement.style.display = 'none';
                formElement.reset();
                // Si es admin quien cambió la contraseña de otro (flujo futuro), no hacer logout.
                // Si el usuario cambió su propia contraseña, podría ser buena idea forzar un nuevo login, pero no es estrictamente necesario.
            } else {
                alert(`Error al actualizar contraseña: ${data.message || response.statusText}`);
                if (response.status === 401 || response.status === 403) {
                    // Podría ser que el token expiró durante el proceso
                    if (typeof logout === 'function') logout(); else window.location.href = '../../auth/login.html'; 
                }
            }
        } catch (error) {
            console.error('Error de red al actualizar contraseña:', error);
            alert('Error de red al intentar actualizar la contraseña.');
        }
    });
}