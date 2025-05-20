// shared_menu.js

document.addEventListener('DOMContentLoaded', function() {
    const menuIcon = document.querySelector('.menu-icon');
    const dropdownMenu = document.querySelector('.dropdown-menu');

    if (menuIcon && dropdownMenu) {
        menuIcon.addEventListener('click', function() {
            dropdownMenu.classList.toggle('show');
        });

        // Close the dropdown if the user clicks outside of it
        window.addEventListener('click', function(event) {
            if (!event.target.matches('.menu-icon') && !dropdownMenu.contains(event.target)) {
                if (dropdownMenu.classList.contains('show')) {
                    dropdownMenu.classList.remove('show');
                }
            }
        });
    }
});

// The logout function is expected to be defined in the HTML or another script
// function logout() { ... } // Keep this in HTML for now as it's already there