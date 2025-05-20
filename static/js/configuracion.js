// --- START OF FILE configuracion.js ---
import { initializeUserList } from './modules/user_management_admin.js';
import { initializePasswordChange } from './modules/user_profile_password.js';

document.addEventListener('DOMContentLoaded', () => {
    const userListElement = document.getElementById('userList');
    const userListHeading = document.getElementById('userListHeading');
    const userProfileSection = document.getElementById('userProfileSection');
    const openChangePasswordModalBtn = document.getElementById('openChangePasswordModalBtn');
    const changePasswordModal = document.getElementById('changePasswordModal');
    const changePasswordFormModal = document.getElementById('changePasswordFormModal');
    const userForm = document.getElementById('userForm'); // Para creación/edición general de usuarios por admin
    // const forgotPasswordForm = document.getElementById('forgotPasswordForm'); // Para recuperación de contraseña

    const loggedInUserRole = sessionStorage.getItem('userRole');
    // const loggedInUserId = sessionStorage.getItem('userId');

    // Configuración inicial de la UI basada en el rol
    if (loggedInUserRole !== 'admin') {
        if (userListHeading) userListHeading.style.display = 'none';
        if (userListElement) userListElement.style.display = 'none';
        // if (userForm) userForm.style.display = 'none'; // Ocultar formulario de creación/edición de usuarios si no es admin
        if (userProfileSection) userProfileSection.style.display = 'block';
        if (openChangePasswordModalBtn) openChangePasswordModalBtn.style.display = 'block';
    
    } else { // Es admin
        if (userListHeading) userListHeading.style.display = 'block';
        if (userListElement) userListElement.style.display = 'block';
        if (userForm) userForm.style.display = 'block'; // Mostrar formulario de creación/edición de usuarios si es admin

        // Inicializar la lista de usuarios para el admin (CRUD de usuarios)
        // Esto configurará los botones "Cambiar Contraseña" en la lista de usuarios
        // para abrir el changePasswordModal y establecer los dataset.targetUserId/targetUsername.
        if (userListElement && changePasswordModal && changePasswordFormModal) {
            initializeUserList(userListElement, changePasswordModal, changePasswordFormModal);
        }
        // El admin también puede usar el botón "Cambiar Contraseña" de su perfil.
        // Asegurarse de que el botón sea visible para el admin si existe.
        if (userProfileSection) userProfileSection.style.display = 'block'; // El perfil también es visible para el admin
        if (openChangePasswordModalBtn) openChangePasswordModalBtn.style.display = 'block';
    }

    // Inicializar el manejador del formulario de cambio de contraseña UNA SOLA VEZ.
    // Este manejador funcionará tanto si el modal se abre por openChangePasswordModalBtn
    // (para el usuario actual o el admin cambiando su propia contraseña)
    // como si se abre desde la lista de usuarios (por initializeUserList para un admin).
    // La función initializePasswordChange ya maneja el openModalBtn opcionalmente.
    if (changePasswordModal && changePasswordFormModal) {
        initializePasswordChange(openChangePasswordModalBtn, changePasswordModal, changePasswordFormModal);
    }

    // La lógica para userForm (creación/edición general de usuarios por admin)
    if (loggedInUserRole === 'admin' && userForm) {
        import('./modules/user_creation_admin.js')
            .then(module => {
                module.initializeUserCreationForm(userForm, () => initializeUserList(userListElement, changePasswordModal, changePasswordFormModal));
            })
            .catch(err => console.error('Error al cargar user_creation_admin.js', err));
    }

    // y forgotPasswordForm (recuperación de contraseña) se ha omitido en esta refactorización
    // ya que el enfoque principal estaba en la lista de usuarios y el cambio de contraseña personal.
    // Si estas funcionalidades son parte de configuracion.js, deberían extraerse a sus propios módulos también.
    // Por ejemplo, user_creation_admin.js y password_recovery.js.

    // Ejemplo: si el userForm para admin debe estar aquí:
    // if (loggedInUserRole === 'admin' && userForm) {
    //     import('./modules/user_creation_admin.js')
    //         .then(module => {
    //             module.initializeUserCreationForm(userForm, () => initializeUserList(userListElement, changePasswordModal, changePasswordFormModal));
    //         })
    //         .catch(err => console.error('Error al cargar user_creation_admin.js', err));
    // }

    // Ejemplo: si el forgotPasswordForm debe estar aquí (aunque usualmente está en login):
    // if (forgotPasswordForm) {
    //     import('./modules/password_recovery.js')
    //         .then(module => {
    //             module.initializePasswordRecovery(forgotPasswordForm);
    //         })
    //         .catch(err => console.error('Error al cargar password_recovery.js', err));
    // }
});

// --- END OF FILE configuracion.js ---