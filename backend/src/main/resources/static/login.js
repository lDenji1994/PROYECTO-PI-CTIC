/* ---------------------------------------------------------------------
   PANTALLA DE LOGIN (US-25)

   Envia usuario y contrasena a POST /api/auth/login. Si son correctos, el
   servidor deja una cookie de sesion y se entra al panel (index.html).

   SEGURIDAD (NO MODIFICAR):
   - La contrasena NUNCA se guarda en el navegador (ni localStorage ni
     cookies propias): solo viaja al servidor en esta peticion.
   - Los mensajes se pintan con textContent (no innerHTML).
   --------------------------------------------------------------------- */

'use strict';

document.addEventListener('DOMContentLoaded', function () {

    const form = document.getElementById('form-login');
    const inputUsuario = document.getElementById('login-usuario');
    const inputPassword = document.getElementById('login-password');
    const errorUsuario = document.getElementById('error-usuario');
    const errorPassword = document.getElementById('error-password');
    const botonMostrar = document.getElementById('btn-toggle-password');
    const botonEntrar = form.querySelector('button[type="submit"]');
    const nota = document.getElementById('login-note');

    /* Si ya hay una sesion activa, entra directo al panel */
    fetch('/api/auth/yo').then(function (respuesta) {
        if (respuesta.ok) {
            window.location.replace('/');
        }
    }).catch(function () { /* servidor apagado: se queda en el login */ });

    /* Mostrar / ocultar contrasena */
    botonMostrar.addEventListener('click', function () {
        const estaOculta = inputPassword.type === 'password';
        inputPassword.type = estaOculta ? 'text' : 'password';
        botonMostrar.textContent = estaOculta ? 'Ocultar' : 'Mostrar';
    });

    function limpiarError(input, mensaje) {
        input.classList.remove('input-error');
        mensaje.textContent = '';
    }

    function marcarError(input, mensaje, texto) {
        input.classList.add('input-error');
        mensaje.textContent = texto;
    }

    function mostrarNota(texto, esError) {
        nota.classList.toggle('login-note-error', esError);
        nota.classList.toggle('login-note-success', !esError);
        nota.textContent = texto;
    }

    inputUsuario.addEventListener('input', function () { limpiarError(inputUsuario, errorUsuario); });
    inputPassword.addEventListener('input', function () { limpiarError(inputPassword, errorPassword); });

    form.addEventListener('submit', async function (evento) {
        evento.preventDefault();

        const usuario = inputUsuario.value.trim();
        const contrasena = inputPassword.value;
        let esValido = true;

        if (!usuario) {
            marcarError(inputUsuario, errorUsuario, 'Ingresa tu usuario.');
            esValido = false;
        }
        if (!contrasena) {
            marcarError(inputPassword, errorPassword, 'Ingresa tu contraseña.');
            esValido = false;
        }
        if (!esValido) {
            mostrarNota('Completa los campos marcados en rojo.', true);
            return;
        }

        botonEntrar.disabled = true;
        mostrarNota('Verificando…', false);

        try {
            const respuesta = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ usuario: usuario, contrasena: contrasena })
            });

            if (respuesta.ok) {
                window.location.replace('/');
                return;
            }

            /* El backend responde { "messages": ["..."] } */
            let mensaje = 'No fue posible iniciar sesión.';
            try {
                const cuerpo = await respuesta.json();
                if (cuerpo && Array.isArray(cuerpo.messages) && cuerpo.messages.length) {
                    mensaje = cuerpo.messages.join(' ');
                }
            } catch (e) { /* respuesta sin JSON */ }

            inputPassword.value = '';
            mostrarNota(mensaje, true);
        } catch (e) {
            mostrarNota('No se pudo conectar con el servidor. ¿Está encendido el backend?', true);
        } finally {
            botonEntrar.disabled = false;
        }
    });
});
