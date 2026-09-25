/* ---------------------------------------------------------------------
   PANTALLA DE LOGIN (US-26.01)

   El HTML trae la estructura, el CSS define los colores de cada estado
   (input-error, login-note-error, login-note-success). Este script solo
   decide CUANDO se aplica cada clase, igual que hace ESTADO_BADGE en
   app.js con los certificados.
   --------------------------------------------------------------------- */

document.addEventListener('DOMContentLoaded', function () {

    const form = document.getElementById('form-login');
    const inputUsuario = document.getElementById('login-usuario');
    const inputPassword = document.getElementById('login-password');
    const errorUsuario = document.getElementById('error-usuario');
    const errorPassword = document.getElementById('error-password');
    const botonMostrar = document.getElementById('btn-toggle-password');
    const nota = document.getElementById('login-note');

    /* Mostrar / ocultar contraseña */
    if (botonMostrar && inputPassword) {

        botonMostrar.addEventListener('click', function () {

            const estaOculta = inputPassword.type === 'password';

            inputPassword.type = estaOculta ? 'text' : 'password';
            botonMostrar.textContent = estaOculta ? 'Ocultar' : 'Mostrar';

        });
    }

    function limpiarError(input, mensaje) {
        input.classList.remove('input-error');
        mensaje.textContent = '';
    }

    function marcarError(input, mensaje, texto) {
        input.classList.add('input-error');
        mensaje.textContent = texto;
    }

    /* Quitar el estado de error apenas el usuario vuelve a escribir */
    if (inputUsuario) {
        inputUsuario.addEventListener('input', function () {
            limpiarError(inputUsuario, errorUsuario);
        });
    }

    if (inputPassword) {
        inputPassword.addEventListener('input', function () {
            limpiarError(inputPassword, errorPassword);
        });
    }

    if (form) {

        form.addEventListener('submit', function (evento) {

            evento.preventDefault();

            let esValido = true;

            if (!inputUsuario.value.trim()) {
                marcarError(inputUsuario, errorUsuario, 'Ingresa tu usuario institucional.');
                esValido = false;
            }

            if (!inputPassword.value.trim()) {
                marcarError(inputPassword, errorPassword, 'Ingresa tu contraseña.');
                esValido = false;
            }

            if (!nota) {
                return;
            }

            if (!esValido) {
                nota.classList.remove('login-note-success');
                nota.classList.add('login-note-error');
                nota.textContent = 'Completa los campos marcados en rojo.';
                return;
            }

            /* Aun no hay backend de autenticacion conectado (ver README),
               asi que por ahora solo confirmamos que el formulario esta
               completo y listo para conectarse mas adelante. */
            nota.classList.remove('login-note-error');
            nota.classList.add('login-note-success');
            nota.textContent = 'Datos completos. Falta conectar el backend de autenticación (Spring Security + JWT).';

        });
    }

});
