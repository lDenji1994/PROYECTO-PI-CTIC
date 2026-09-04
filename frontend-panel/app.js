/* =========================================================
           FUNCIÓN PRINCIPAL DE NAVEGACIÓN
           
           Esta función permite cambiar entre:
           
           dashboard
           carga
           cursos
           certificaciones
           estudiantes
           academico
           historial
           ========================================================= */

        function mostrarVista(vista) {


            /* Obtener todas las vistas */

            const vistas =
                document.querySelectorAll(".page-view");


            /* Ocultar todas */

            vistas.forEach(function(elemento) {

                elemento.classList.remove("active-view");

            });


            /* Buscar la vista seleccionada */

            const vistaSeleccionada =
                document.getElementById(
                    "vista-" + vista
                );


            /* Mostrar la vista */

            if (vistaSeleccionada) {

                vistaSeleccionada.classList.add(
                    "active-view"
                );

            }


            /* Actualizar botones del menú */

            const botones =
                document.querySelectorAll(".menu-item");


            botones.forEach(function(boton) {

                boton.classList.remove("active");

            });


            /* Activar el botón correspondiente */

            const botonActivo =
                document.getElementById(
                    "btn-" + vista
                );


            if (botonActivo) {

                botonActivo.classList.add("active");

            }


            /* Cambiar título */

            const titulo =
                document.getElementById("page-title");


            if (vista === "dashboard") {

                titulo.textContent =
                    "Dashboard";

            }

            else if (vista === "carga") {

                titulo.textContent =
                    "Carga de información";

            }

            else if (vista === "cursos") {

                titulo.textContent =
                    "Contenido de cursos";

            }

            else if (vista === "certificaciones") {

                titulo.textContent =
                    "Certificaciones";

            }

            else if (vista === "historial") {

                titulo.textContent =
                    "Historial";

            }

            else if (vista === "estudiantes") {

                titulo.textContent =
                    "Estudiantes";

            }

            else if (vista === "academico") {

                titulo.textContent =
                    "Programas y Planes";

            }


            /* Cargar datos reales al entrar a un modulo conectado
               con el backend */

            if (vista === "certificaciones") {

                poblarSelectEstudiantes();
                cargarCertificados();

            }

            else if (vista === "estudiantes") {

                cargarEstudiantes();

            }

        }



        /* =========================================================
           MOSTRAR MÁS ACTIVIDADES
           
           Inicialmente se muestran 10.
           
           Al presionar el botón:
           
           "Ver toda la actividad"
           
           se muestran las actividades adicionales.
           ========================================================= */

        function mostrarMasActividades() {


            const actividades =
                document.querySelectorAll(
                    ".actividad-extra"
                );


            actividades.forEach(function(actividad) {

                actividad.style.display =
                    "grid";

            });


            /* Cambiar texto del botón */

            const boton =
                document.getElementById(
                    "btn-ver-actividades"
                );


            boton.textContent =
                "Mostrando toda la actividad ↑";


            /* Evitar que se vuelva a presionar */

            boton.disabled = true;

            boton.style.opacity = "0.6";

            boton.style.cursor = "default";

        }



        /* =========================================================
           CAMBIO ENTRE PESTAÑAS (nuevo)

           Se usa en el módulo "Programas y Planes" para alternar
           entre Asignaturas / Programas / Planes / Asociar
           documentos.
           ========================================================= */

        function mostrarTab(tab) {

            const contenidos =
                document.querySelectorAll(".tab-content");

            contenidos.forEach(function (elemento) {
                elemento.classList.remove("active-tab");
            });

            const botones =
                document.querySelectorAll(".tab-button");

            botones.forEach(function (boton) {
                boton.classList.remove("active");
            });

            const contenidoSeleccionado =
                document.getElementById("contenido-" + tab);

            const botonSeleccionado =
                document.getElementById("tab-" + tab);

            if (contenidoSeleccionado) {
                contenidoSeleccionado.classList.add("active-tab");
            }

            if (botonSeleccionado) {
                botonSeleccionado.classList.add("active");
            }

        }



        /* =========================================================
           DATOS VISUALES DEL HISTORIAL
           
           IMPORTANTE:
           
           Estos datos son solamente ejemplos.
           
           Si quieres modificar lo que aparece en el historial,
           cambia los textos aquí.
           ========================================================= */

        const historiales = {


            /* =====================================================
               HISTORIAL DE DOCUMENTOS
               ===================================================== */

            documentos: {

                titulo:
                    "Historial de documentos registrados",

                total:
                    248,

                actividades: [

                    [
                        "Documento actualizado",
                        "Syllabus de Programación de Computadores",
                        "Hace 15 minutos"
                    ],

                    [
                        "Nuevo documento registrado",
                        "Carta descriptiva — Bases de Datos",
                        "Hace 1 hora"
                    ],

                    [
                        "Documento modificado",
                        "Contenido programático — Ingeniería de Software",
                        "Hace 2 horas"
                    ],

                    [
                        "Documento actualizado",
                        "Syllabus — Ingeniería Electrónica",
                        "Hace 3 horas"
                    ],

                    [
                        "Nuevo registro",
                        "Contenido académico — Matemáticas",
                        "Hace 5 horas"
                    ],

                    [
                        "Documento enviado a revisión",
                        "Syllabus — Arquitectura de Software",
                        "Ayer"
                    ]

                ]

            },


            /* =====================================================
               HISTORIAL DE CURSOS
               ===================================================== */

            cursos: {

                titulo:
                    "Historial de cursos registrados",

                total:
                    126,

                actividades: [

                    [
                        "Curso actualizado",
                        "Programación de Computadores",
                        "Hace 1 hora"
                    ],

                    [
                        "Nuevo curso registrado",
                        "Desarrollo Web",
                        "Hace 3 horas"
                    ],

                    [
                        "Contenido modificado",
                        "Bases de Datos",
                        "Hace 5 horas"
                    ],

                    [
                        "Syllabus actualizado",
                        "Ingeniería de Software",
                        "Ayer"
                    ],

                    [
                        "Curso registrado",
                        "Arquitectura de Software",
                        "Ayer"
                    ],

                    [
                        "Contenido académico actualizado",
                        "Sistemas Operativos",
                        "Hace 2 días"
                    ]

                ]

            },


            /* =====================================================
               HISTORIAL DE CERTIFICACIONES
               ===================================================== */

            certificaciones: {

                titulo:
                    "Historial de certificaciones",

                total:
                    84,

                actividades: [

                    [
                        "Certificación finalizada",
                        "Solicitud #CERT-00122",
                        "Hace 2 horas"
                    ],

                    [
                        "Certificación en proceso",
                        "Solicitud #CERT-00123",
                        "Hace 4 horas"
                    ],

                    [
                        "Nueva solicitud",
                        "Solicitud #CERT-00124",
                        "Hace 5 horas"
                    ],

                    [
                        "Certificación actualizada",
                        "Solicitud #CERT-00120",
                        "Ayer"
                    ],

                    [
                        "Documento certificado",
                        "Ingeniería de Sistemas",
                        "Ayer"
                    ],

                    [
                        "Solicitud revisada",
                        "Solicitud #CERT-00118",
                        "Hace 2 días"
                    ]

                ]

            },


            /* =====================================================
               HISTORIAL DE PENDIENTES
               ===================================================== */

            pendientes: {

                titulo:
                    "Historial de elementos pendientes",

                total:
                    12,

                actividades: [

                    [
                        "Pendiente de revisión",
                        "Certificación #CERT-00124",
                        "Hace 2 horas"
                    ],

                    [
                        "Documento pendiente",
                        "Syllabus — Ingeniería de Software",
                        "Hace 5 horas"
                    ],

                    [
                        "Validación pendiente",
                        "Contenido académico",
                        "Ayer"
                    ],

                    [
                        "Certificación pendiente",
                        "Solicitud #CERT-00118",
                        "Ayer"
                    ],

                    [
                        "Documento requiere modificación",
                        "Bases de Datos",
                        "Hace 2 días"
                    ],

                    [
                        "Registro pendiente",
                        "Información académica",
                        "Hace 3 días"
                    ]

                ]

            }

        };



        /* =========================================================
           MOSTRAR HISTORIAL
           
           Esta función se ejecuta cuando el usuario hace clic
           en cualquiera de las cuatro tarjetas del Dashboard.
           ========================================================= */

        function mostrarHistorial(tipo) {


            /* Obtener información */

            const datos =
                historiales[tipo];


            /* Cambiar título */

            document.getElementById(
                "history-title"
            ).textContent =
                datos.titulo;


            /* Cambiar total */

            document.getElementById(
                "history-total"
            ).textContent =
                datos.total;


            /* Obtener contenedor */

            const lista =
                document.getElementById(
                    "history-list"
                );


            /* Limpiar historial anterior */

            lista.innerHTML = "";


            /* Crear cada registro */

            datos.actividades.forEach(
                function(actividad) {


                    const fila =
                        document.createElement(
                            "div"
                        );


                    fila.className =
                        "history-row";


                    fila.innerHTML = `

                        <div class="history-marker">
                            ✓
                        </div>

                        <div>

                            <strong>
                                ${actividad[0]}
                            </strong>

                            <span>
                                ${actividad[1]}
                            </span>

                        </div>

                        <time>
                            ${actividad[2]}
                        </time>

                    `;


                    lista.appendChild(fila);

                }
            );


            /* Mostrar vista historial */

            mostrarVista("historial");

        }





/* =====================================================================
   INTEGRACION CON EL BACKEND (Spring Boot)

   A partir de aqui el panel deja de ser un prototipo visual: los
   modulos "Estudiantes" y "Certificaciones" leen y escriben datos
   reales a traves de la API REST del backend, servida bajo /api.

   Los modulos "Carga de informacion", "Contenido de cursos" y
   "Programas y Planes" siguen siendo visuales/mock, porque el
   backend actual no tiene entidades para documentos academicos,
   cursos, asignaturas, programas, planes ni asociaciones.
   ===================================================================== */

const API_BASE = '/api';

let estudiantesCache = [];

/* ---------------------------------------------------------------------
   ESTUDIANTES
   --------------------------------------------------------------------- */

async function cargarEstudiantes() {

    const tabla = document.getElementById('tabla-estudiantes');

    try {

        const respuesta = await fetch(`${API_BASE}/estudiantes`);

        if (!respuesta.ok) {
            throw new Error('No se pudo obtener la lista de estudiantes');
        }

        estudiantesCache = await respuesta.json();

        renderEstudiantes();

    } catch (error) {

        console.error(error);

        if (tabla) {
            tabla.innerHTML =
                '<tr><td colspan="6">No se pudo cargar la lista de estudiantes. ' +
                'Verifica que el backend este corriendo.</td></tr>';
        }
    }
}

function renderEstudiantes() {

    const tabla = document.getElementById('tabla-estudiantes');

    if (!tabla) {
        return;
    }

    if (estudiantesCache.length === 0) {
        tabla.innerHTML =
            '<tr><td colspan="6">Aun no hay estudiantes registrados.</td></tr>';
        return;
    }

    tabla.innerHTML = estudiantesCache.map(function (e) {

        return `
            <tr>
                <td>${escaparTexto(e.codigoEstudiantil)}</td>
                <td>${escaparTexto(e.nombres)}</td>
                <td>${escaparTexto(e.apellidos)}</td>
                <td>${escaparTexto(e.email)}</td>
                <td>${escaparTexto(e.programaAcademico || '-')}</td>
                <td>
                    <button
                        type="button"
                        class="outline-button"
                        onclick="eliminarEstudiante(${e.id})">
                        Eliminar
                    </button>
                </td>
            </tr>
        `;

    }).join('');
}

async function registrarEstudiante(evento) {

    evento.preventDefault();

    const nuevoEstudiante = {
        codigoEstudiantil: document.getElementById('est-codigo').value.trim(),
        nombres: document.getElementById('est-nombres').value.trim(),
        apellidos: document.getElementById('est-apellidos').value.trim(),
        email: document.getElementById('est-email').value.trim(),
        programaAcademico: document.getElementById('est-programa').value.trim()
    };

    try {

        const respuesta = await fetch(`${API_BASE}/estudiantes`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(nuevoEstudiante)
        });

        if (!respuesta.ok) {
            const error = await respuesta.json().catch(function () { return null; });
            const mensaje = (error && error.mensajes)
                ? error.mensajes.join(', ')
                : 'No se pudo registrar el estudiante';
            alert(mensaje);
            return;
        }

        document.getElementById('form-estudiante').reset();
        await cargarEstudiantes();

    } catch (error) {

        console.error(error);
        alert('No se pudo conectar con el backend. Verifica que este corriendo.');
    }
}

async function eliminarEstudiante(id) {

    if (!confirm('¿Eliminar este estudiante? Tambien se eliminaran sus certificados.')) {
        return;
    }

    try {

        const respuesta = await fetch(`${API_BASE}/estudiantes/${id}`, {
            method: 'DELETE'
        });

        if (!respuesta.ok) {
            alert('No se pudo eliminar el estudiante');
            return;
        }

        await cargarEstudiantes();

    } catch (error) {

        console.error(error);
        alert('No se pudo conectar con el backend.');
    }
}

async function poblarSelectEstudiantes() {

    const select = document.getElementById('cert-estudiante');

    if (!select) {
        return;
    }

    try {

        if (estudiantesCache.length === 0) {

            const respuesta = await fetch(`${API_BASE}/estudiantes`);

            if (respuesta.ok) {
                estudiantesCache = await respuesta.json();
            }
        }

        const opciones = estudiantesCache.map(function (e) {
            return `<option value="${e.id}">` +
                `${escaparTexto(e.nombres)} ${escaparTexto(e.apellidos)} ` +
                `(${escaparTexto(e.codigoEstudiantil)})</option>`;
        }).join('');

        select.innerHTML =
            '<option value="" disabled selected>Seleccione un estudiante</option>' +
            opciones;

    } catch (error) {

        console.error(error);
    }
}

/* ---------------------------------------------------------------------
   CERTIFICADOS
   --------------------------------------------------------------------- */

const ESTADO_BADGE = {
    PENDIENTE: 'warning',
    EMITIDO: 'success',
    ANULADO: 'processing'
};

const ESTADO_TEXTO = {
    PENDIENTE: 'Pendiente',
    EMITIDO: 'Emitido',
    ANULADO: 'Anulado'
};

let certificadosCache = [];

async function cargarCertificados() {

    const tabla = document.getElementById('tabla-certificados');

    try {

        const respuesta = await fetch(`${API_BASE}/certificados`);

        if (!respuesta.ok) {
            throw new Error('No se pudo obtener la lista de certificados');
        }

        certificadosCache = await respuesta.json();

        actualizarStats(certificadosCache);
        aplicarFiltroCertificados();

    } catch (error) {

        console.error(error);

        if (tabla) {
            tabla.innerHTML =
                '<tr><td colspan="6">No se pudo cargar la lista de certificados. ' +
                'Verifica que el backend este corriendo.</td></tr>';
        }
    }
}

function obtenerFiltroEstadoActivo() {

    const select = document.getElementById('filtro-estado-certificados');
    return select ? select.value : 'TODOS';
}

function aplicarFiltroCertificados() {

    const filtro = obtenerFiltroEstadoActivo();

    const filtrados = (filtro === 'TODOS')
        ? certificadosCache
        : certificadosCache.filter(function (c) { return c.estado === filtro; });

    renderCertificados(filtrados, filtro !== 'TODOS');
}

function renderCertificados(certificados, filtroActivo) {

    const tabla = document.getElementById('tabla-certificados');

    if (!tabla) {
        return;
    }

    if (certificados.length === 0) {

        const mensaje = filtroActivo
            ? 'No hay solicitudes con el estado seleccionado.'
            : 'Aun no hay certificados solicitados.';

        tabla.innerHTML = `<tr><td colspan="6">${mensaje}</td></tr>`;
        return;
    }

    tabla.innerHTML = certificados.map(function (c) {

        const badgeClase = ESTADO_BADGE[c.estado] || 'warning';
        const badgeTexto = ESTADO_TEXTO[c.estado] || c.estado;

        const acciones = [];

        if (c.estado === 'PENDIENTE') {
            acciones.push(
                `<button type="button" class="outline-button" onclick="emitirCertificado(${c.id})">Emitir</button>`
            );
        }

        if (c.estado !== 'ANULADO') {
            acciones.push(
                `<button type="button" class="outline-button" onclick="anularCertificado(${c.id})">Anular</button>`
            );
        }

        return `
            <tr>
                <td>${escaparTexto(c.codigoVerificacion)}</td>
                <td>${escaparTexto(c.nombreEstudiante)}</td>
                <td>${escaparTexto(c.tipo)}</td>
                <td>${escaparTexto(c.fechaSolicitud)}</td>
                <td><span class="badge ${badgeClase}">${badgeTexto}</span></td>
                <td>${acciones.join(' ')}</td>
            </tr>
        `;

    }).join('');
}

function actualizarStats(certificados) {

    const total = certificados.length;
    const pendientes = certificados.filter(function (c) { return c.estado === 'PENDIENTE'; }).length;
    const emitidas = certificados.filter(function (c) { return c.estado === 'EMITIDO'; }).length;
    const anuladas = certificados.filter(function (c) { return c.estado === 'ANULADO'; }).length;

    const elTotal = document.getElementById('stat-total');
    const elPendientes = document.getElementById('stat-pendientes');
    const elEmitidas = document.getElementById('stat-emitidas');
    const elAnuladas = document.getElementById('stat-anuladas');

    if (elTotal) elTotal.textContent = total;
    if (elPendientes) elPendientes.textContent = pendientes;
    if (elEmitidas) elEmitidas.textContent = emitidas;
    if (elAnuladas) elAnuladas.textContent = anuladas;
}

async function solicitarCertificado(evento) {

    evento.preventDefault();

    const nuevaSolicitud = {
        estudianteId: Number(document.getElementById('cert-estudiante').value),
        tipo: document.getElementById('cert-tipo').value,
        observaciones: document.getElementById('cert-observaciones').value.trim()
    };

    if (!nuevaSolicitud.estudianteId || !nuevaSolicitud.tipo) {
        alert('Selecciona un estudiante y un tipo de certificado');
        return;
    }

    try {

        const respuesta = await fetch(`${API_BASE}/certificados`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(nuevaSolicitud)
        });

        if (!respuesta.ok) {
            const error = await respuesta.json().catch(function () { return null; });
            const mensaje = (error && error.mensajes)
                ? error.mensajes.join(', ')
                : 'No se pudo registrar la solicitud';
            alert(mensaje);
            return;
        }

        document.getElementById('form-certificado').reset();
        await cargarCertificados();

    } catch (error) {

        console.error(error);
        alert('No se pudo conectar con el backend. Verifica que este corriendo.');
    }
}

async function emitirCertificado(id) {

    try {

        const respuesta = await fetch(`${API_BASE}/certificados/${id}/emitir`, {
            method: 'PATCH'
        });

        if (!respuesta.ok) {
            alert('No se pudo emitir el certificado');
            return;
        }

        await cargarCertificados();

    } catch (error) {

        console.error(error);
        alert('No se pudo conectar con el backend.');
    }
}

async function anularCertificado(id) {

    if (!confirm('¿Anular este certificado?')) {
        return;
    }

    try {

        const respuesta = await fetch(`${API_BASE}/certificados/${id}/anular`, {
            method: 'PATCH'
        });

        if (!respuesta.ok) {
            alert('No se pudo anular el certificado');
            return;
        }

        await cargarCertificados();

    } catch (error) {

        console.error(error);
        alert('No se pudo conectar con el backend.');
    }
}

/* ---------------------------------------------------------------------
   UTILIDADES
   --------------------------------------------------------------------- */

function escaparTexto(valor) {

    if (valor === null || valor === undefined) {
        return '';
    }

    const div = document.createElement('div');
    div.textContent = String(valor);
    return div.innerHTML;
}

/* ---------------------------------------------------------------------
   INICIALIZACION: conectar los formularios y cargar datos al abrir
   --------------------------------------------------------------------- */

document.addEventListener('DOMContentLoaded', function () {

    const formEstudiante = document.getElementById('form-estudiante');
    const formCertificado = document.getElementById('form-certificado');

    if (formEstudiante) {
        formEstudiante.addEventListener('submit', registrarEstudiante);
    }

    if (formCertificado) {
        formCertificado.addEventListener('submit', solicitarCertificado);
    }

    const filtroEstado = document.getElementById('filtro-estado-certificados');

    if (filtroEstado) {
        filtroEstado.addEventListener('change', aplicarFiltroCertificados);
    }

    /* Precargar estudiantes en memoria para el select de certificados */
    cargarEstudiantes();

});



/* =========================================================
   MÓDULO "PROGRAMAS Y PLANES" (nuevo)

   Cubre: US-07.01, US-08.01, US-09.01, US-10.02, US-11.01.

   Como el backend todavia no tiene entidades para
   asignaturas, programas, planes ni asociaciones, estos
   formularios solo validan y muestran una confirmacion
   visual, igual que el resto del prototipo de Carga y
   Cursos.
   ========================================================= */

document.addEventListener('DOMContentLoaded', function () {

    const formulariosMock = [
        'form-asignatura',
        'form-programa',
        'form-plan',
        'form-asociacion'
    ];

    formulariosMock.forEach(function (idFormulario) {

        const formulario =
            document.getElementById(idFormulario);

        if (formulario) {

            formulario.addEventListener('submit', function (evento) {

                evento.preventDefault();

                alert(
                    'Guardado correctamente (prototipo visual, ' +
                    'aun sin conexion a backend).'
                );

                formulario.reset();

            });

        }

    });

});



/* =========================================================
   LOGIN (US-26.01)

   Todo lo de aquí en adelante es nuevo: controla la
   pantalla de inicio de sesión. Nada de lo anterior en
   este archivo fue modificado.
   ========================================================= */

/* =====================================================
   MOSTRAR / OCULTAR CONTRASEÑA
   ===================================================== */

function alternarVisibilidadPassword() {

    const campoPassword =
        document.getElementById("login-password");

    const botonAlternar =
        document.getElementById("btn-toggle-password");


    if (campoPassword.type === "password") {

        campoPassword.type = "text";
        botonAlternar.textContent = "Ocultar";

    }

    else {

        campoPassword.type = "password";
        botonAlternar.textContent = "Mostrar";

    }

}



/* =====================================================
   VALIDAR Y ENVIAR EL FORMULARIO DE LOGIN
   ===================================================== */

function iniciarSesion(evento) {

    evento.preventDefault();


    const campoUsuario =
        document.getElementById("login-usuario");

    const campoPassword =
        document.getElementById("login-password");

    const errorUsuario =
        document.getElementById("login-usuario-error");

    const errorPassword =
        document.getElementById("login-password-error");

    const nota =
        document.getElementById("login-note");


    /* Limpiar estado previo */

    campoUsuario.classList.remove("input-error");
    campoPassword.classList.remove("input-error");

    errorUsuario.textContent = "";
    errorPassword.textContent = "";

    nota.textContent = "";
    nota.classList.remove(
        "login-note-error",
        "login-note-success"
    );


    const usuario = campoUsuario.value.trim();
    const password = campoPassword.value;

    let formularioValido = true;


    /* Validar usuario */

    if (usuario === "") {

        campoUsuario.classList.add("input-error");
        errorUsuario.textContent =
            "Ingresa tu usuario o correo institucional";

        formularioValido = false;

    }


    /* Validar contraseña */

    if (password.length < 6) {

        campoPassword.classList.add("input-error");
        errorPassword.textContent =
            "La contraseña debe tener al menos 6 caracteres";

        formularioValido = false;

    }


    /* Si algo falló, avisar y detener aquí */

    if (!formularioValido) {

        nota.textContent =
            "Revisa los campos marcados en rojo.";

        nota.classList.add("login-note-error");

        return false;

    }


    /* Todo válido: mostrar mensaje y pasar al panel */

    nota.textContent = "Sesión iniciada correctamente.";
    nota.classList.add("login-note-success");

    mostrarPanelPrincipal();

    return false;

}



/* =====================================================
   OCULTAR EL LOGIN Y MOSTRAR EL PANEL PRINCIPAL

   No modifica el HTML del app-container, solo cambia
   su visibilidad por JavaScript.
   ===================================================== */

function mostrarPanelPrincipal() {

    const pantallaLogin =
        document.getElementById("pantalla-login");

    const panelPrincipal =
        document.querySelector(".app-container");


    setTimeout(function() {

        if (pantallaLogin) {
            pantallaLogin.classList.add("login-oculto");
        }

        if (panelPrincipal) {
            panelPrincipal.style.display = "flex";
        }

    }, 500);

}



/* =====================================================
   INICIALIZACIÓN DEL LOGIN

   - El panel principal empieza oculto.
   - El formulario de login se conecta aquí, en un
     "DOMContentLoaded" aparte para no tocar el que ya
     existía arriba en este archivo.
   ===================================================== */

document.addEventListener("DOMContentLoaded", function() {

    const panelPrincipal =
        document.querySelector(".app-container");

    if (panelPrincipal) {
        panelPrincipal.style.display = "none";
    }


    const formLogin =
        document.getElementById("form-login");

    if (formLogin) {
        formLogin.addEventListener("submit", iniciarSesion);
    }

});
