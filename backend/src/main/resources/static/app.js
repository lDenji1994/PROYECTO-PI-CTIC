/* =====================================================================
   PANEL DE GESTION ACADEMICA - UPB (CTIC)
   Logica del panel: sesion, navegacion y conexion con el backend.

   ORGANIZACION DEL ARCHIVO
     1. Configuracion y estado
     2. Utilidades (API, seguridad, fechas, mensajes)
     3. Sesion y permisos por rol
     4. Navegacion entre vistas
     5. Catalogos
     6. Dashboard + Actividad reciente + Historial
     7. Datos del curso (campos que se escriben a mano)
     8. Carga de informacion (documentos, borrador y reintento)
     9. Corregir una carga (editar datos / reemplazar archivo)
    10. Contenido de cursos (asignaturas y programas)
    11. Certificaciones (solicitudes y vista previa)
    12. Usuarios (solo administrador) y Mi cuenta
    13. Inicio

   REGLAS DE SEGURIDAD (NO MODIFICAR)
   - Todo texto que venga del servidor se pinta con escaparTexto() o
     textContent: evita inyeccion de HTML/JS (XSS).
   - Los IDs que van en URLs se validan como numeros (idSeguro()).
   - Las contrasenas nunca se guardan en el navegador.
   - Ocultar un boton segun el rol es solo comodidad: quien decide de
     verdad lo que puede hacer cada rol es el backend (SecurityConfig).
   - No hay datos de ejemplo quemados: si una lista esta vacia es
     porque la base de datos esta vacia.
   ===================================================================== */

'use strict';

/* =====================================================================
   1. CONFIGURACION Y ESTADO
   ===================================================================== */

/* SE PUEDE MODIFICAR: si el panel se abre desde otro servidor, cambia
   esto por 'http://localhost:8080/api'. */
const API_BASE = '/api';

/* SE PUEDE MODIFICAR: cuantas actividades se ven en el Dashboard. */
const ACTIVIDADES_DASHBOARD = 8;
const ACTIVIDADES_HISTORIAL = 200;

/* Textos y colores de los estados de una solicitud (enum del backend). */
const ESTADOS_SOLICITUD = {
    PENDIENTE: { texto: 'Pendiente', clase: 'warning' },
    PROCESANDO: { texto: 'Procesando', clase: 'processing' },
    ESPERANDO_DOCUMENTOS: { texto: 'Esperando documentos', clase: 'warning' },
    REALIZADO: { texto: 'Realizado', clase: 'success' },
    ERROR: { texto: 'Error', clase: 'danger' }
};

const TITULOS_VISTA = {
    dashboard: 'Dashboard',
    carga: 'Carga de información',
    cursos: 'Contenido de cursos',
    certificaciones: 'Certificaciones',
    usuarios: 'Usuarios',
    cuenta: 'Mi cuenta',
    historial: 'Historial'
};

/* Vistas que solo puede abrir el administrador. */
const VISTAS_SOLO_ADMIN = ['usuarios'];

const TITULOS_HISTORIAL = {
    todos: 'Toda la actividad',
    documentos: 'Historial de documentos académicos',
    cursos: 'Historial de asignaturas y programas',
    certificaciones: 'Historial de certificaciones',
    usuarios: 'Historial de usuarios'
};

const NOMBRES_ROL = { ADMINISTRADOR: 'Administrador', AUXILIAR: 'Auxiliar' };

/* Valor especial del select de formatos para escribir uno a mano. */
const FORMATO_OTRO = '__OTRO__';

/*
 * DATOS DEL CURSO QUE SE REGISTRAN A MANO (copiar y pegar desde la carta
 * descriptiva o el syllabus). "id" debe ser igual al nombre del campo en
 * DatosCursoDTO del backend.
 *
 * SE PUEDE MODIFICAR: etiquetas, ayudas y el orden. Para agregar un campo
 * nuevo hay que agregarlo tambien en DatosCursoDTO y VersionDocumentoService.
 */
const CAMPOS_CURSO = {
    obligatorios: [
        { id: 'descripcion', etiqueta: 'Descripción del curso', tipo: 'area', filas: 5, maximo: 6000, ancho: true,
          ayuda: 'Pega aquí la descripción tal como aparece en el documento.' },
        { id: 'detalleContenido', etiqueta: 'Detalle de contenido del curso', tipo: 'area', filas: 8, ancho: true,
          ayuda: 'Un tema o unidad por línea. Así saldrá listado en el certificado.' },
        { id: 'creditos', etiqueta: 'Créditos', tipo: 'numero', ejemplo: 'Ej. 3' },
        { id: 'horasTeoricas', etiqueta: 'Horas teóricas', tipo: 'numero', opcional: true, ejemplo: 'Ej. 3' },
        { id: 'horasPracticas', etiqueta: 'Horas prácticas', tipo: 'numero', opcional: true, ejemplo: 'Ej. 0' },
        { id: 'horasLaboratorio', etiqueta: 'Horas de laboratorio', tipo: 'numero', opcional: true, ejemplo: 'Ej. 0' },
        { id: 'horasIndependientes', etiqueta: 'Horas de trabajo independiente', tipo: 'numero', opcional: true, ejemplo: 'Ej. 6' }
    ],
    opcionales: [
        { id: 'escuela', etiqueta: 'Escuela', tipo: 'texto', maximo: 150 },
        { id: 'facultad', etiqueta: 'Facultad', tipo: 'texto', maximo: 150 },
        { id: 'clasificacionCINE', etiqueta: 'Clasificación CINE', tipo: 'texto', maximo: 100 },
        { id: 'nucleoBasicoConocimiento', etiqueta: 'Núcleo Básico de Conocimiento (NBC)', tipo: 'texto', maximo: 2000 },
        { id: 'ciclo', etiqueta: 'Ciclo de formación', tipo: 'texto', maximo: 100 },
        { id: 'nivelFormacion', etiqueta: 'Nivel de formación', tipo: 'texto', maximo: 100 },
        { id: 'modoCalificacion', etiqueta: 'Modo de calificación', tipo: 'texto', maximo: 100 },
        { id: 'requisitos', etiqueta: 'Requisitos / saberes previos', tipo: 'area', filas: 3, maximo: 4000, ancho: true },
        { id: 'proposito', etiqueta: 'Propósito de formación', tipo: 'area', filas: 3, maximo: 6000, ancho: true },
        { id: 'justificacion', etiqueta: 'Justificación', tipo: 'area', filas: 3, maximo: 6000, ancho: true },
        { id: 'modalidades', etiqueta: 'Modalidades', tipo: 'area', filas: 2, maximo: 2000, ancho: true },
        { id: 'observaciones', etiqueta: 'Observaciones', tipo: 'area', filas: 2, maximo: 4000, ancho: true }
    ]
};

/* Cache en memoria de lo que devolvio el backend (se recarga tras cada cambio). */
const estado = {
    usuario: null,               // quien inicio sesion { id, usuario, nombreCompleto, rol, administrador }
    configuracion: { extensionesPermitidas: ['pdf'], tamanoMaximoMb: 20 },
    asignaturas: [],
    tipos: [],
    formatos: [],
    programas: [],
    usuarios: [],
    tiposCertificado: [],
    documentos: [],
    solicitudes: [],
    solicitudSeleccionada: null,
    versionEnEdicion: null,
    usuarioContrasena: null
};

/* =====================================================================
   2. UTILIDADES
   ===================================================================== */

/** Escapa texto para insertarlo en HTML de forma segura. NO MODIFICAR. */
function escaparTexto(valor) {
    if (valor === null || valor === undefined) {
        return '';
    }
    return String(valor)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

/** Devuelve el id como entero positivo o lanza error (para armar URLs). */
function idSeguro(valor) {
    const numero = Number(valor);
    if (!Number.isInteger(numero) || numero <= 0) {
        throw new Error('Identificador inválido');
    }
    return numero;
}

/** Extrae el mensaje de error que envia el backend (campo "messages"). */
async function leerMensajeError(respuesta) {
    try {
        const cuerpo = await respuesta.json();
        if (cuerpo && Array.isArray(cuerpo.messages) && cuerpo.messages.length) {
            return cuerpo.messages.join(' · ');
        }
        if (cuerpo && cuerpo.message) {
            return cuerpo.message;
        }
    } catch (e) {
        /* respuesta sin JSON */
    }
    if (respuesta.status === 413) {
        return 'El archivo supera el tamaño máximo permitido.';
    }
    return 'Error ' + respuesta.status + ' al comunicarse con el servidor';
}

/**
 * Llamada al backend. Devuelve el JSON (o null si no hay cuerpo).
 * Lanza Error con el mensaje del servidor si la respuesta no es 2xx.
 * Si la sesion se vencio (401) envia al login.
 */
async function api(ruta, opciones) {
    let respuesta;
    try {
        respuesta = await fetch(API_BASE + ruta, opciones);
    } catch (e) {
        marcarServidor(false);
        throw new Error('No se pudo conectar con el servidor. Revisa tu conexión y que el backend esté encendido.');
    }
    marcarServidor(true);
    if (respuesta.status === 401) {
        window.location.replace('/login.html');
        throw new Error('Tu sesión terminó. Inicia sesión de nuevo.');
    }
    if (!respuesta.ok) {
        throw new Error(await leerMensajeError(respuesta));
    }
    const tipo = respuesta.headers.get('content-type') || '';
    if (respuesta.status === 204 || !tipo.includes('application/json')) {
        return null;
    }
    return respuesta.json();
}

function apiJson(ruta, metodo, datos) {
    return api(ruta, {
        method: metodo,
        headers: { 'Content-Type': 'application/json' },
        body: datos === undefined ? undefined : JSON.stringify(datos)
    });
}

function marcarServidor(conectado) {
    const punto = document.getElementById('estado-servidor');
    if (punto) {
        punto.classList.toggle('offline', !conectado);
        punto.title = conectado ? 'Servidor conectado' : 'Servidor desconectado';
    }
}

/** Mensaje emergente. tipo: 'success' | 'error' | 'warning'. */
function mostrarMensaje(texto, tipo) {
    const contenedor = document.getElementById('toast-container');
    if (!contenedor) {
        return;
    }
    const aviso = document.createElement('div');
    aviso.className = 'toast ' + (tipo || 'success');
    aviso.textContent = texto;
    contenedor.appendChild(aviso);
    setTimeout(function () { aviso.remove(); }, tipo === 'error' ? 8000 : 4000);
}

function formatearFecha(iso) {
    if (!iso) {
        return '—';
    }
    const fecha = new Date(iso);
    if (Number.isNaN(fecha.getTime())) {
        return '—';
    }
    return fecha.toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
}

/** "Hace 5 min", "Hace 2 h", o la fecha si es antigua. */
function tiempoRelativo(iso) {
    if (!iso) {
        return '';
    }
    const fecha = new Date(iso);
    const segundos = Math.round((Date.now() - fecha.getTime()) / 1000);
    if (Number.isNaN(segundos)) {
        return '';
    }
    if (segundos < 60) { return 'Hace un momento'; }
    if (segundos < 3600) { return 'Hace ' + Math.floor(segundos / 60) + ' min'; }
    if (segundos < 86400) { return 'Hace ' + Math.floor(segundos / 3600) + ' h'; }
    if (segundos < 604800) { return 'Hace ' + Math.floor(segundos / 86400) + ' d'; }
    return formatearFecha(iso);
}

/** Periodo sugerido a partir de la fecha actual (ene-jun = 1, jul-dic = 2). */
function periodoActual() {
    const hoy = new Date();
    return hoy.getFullYear() + '-' + (hoy.getMonth() < 6 ? '1' : '2');
}

/** Los ID UPB tienen 9 digitos con ceros a la izquierda (la BD los guarda como numero). */
function formatearIdEstudiante(id) {
    return String(id === null || id === undefined ? '' : id).padStart(9, '0');
}

/** 3 -> "3", 2.5 -> "2.5", null -> "—" */
function formatearNumero(valor) {
    return valor === null || valor === undefined ? '—' : String(Number(valor));
}

function extensionDe(nombreArchivo) {
    const punto = String(nombreArchivo || '').lastIndexOf('.');
    return punto < 0 ? '' : String(nombreArchivo).slice(punto + 1).toLowerCase();
}

function badgeFormato(vigente) {
    return vigente
        ? '<span class="badge success">Vigente</span>'
        : '<span class="badge danger">Desactualizado</span>';
}

function badgeSolicitud(estadoSolicitud) {
    const info = ESTADOS_SOLICITUD[estadoSolicitud] || { texto: estadoSolicitud, clase: 'neutral' };
    return '<span class="badge ' + info.clase + '">' + escaparTexto(info.texto) + '</span>';
}

/** Una version tiene los datos del curso si se registraron descripcion y creditos. */
function versionTieneDatos(version) {
    return Boolean(version && version.descripcion && version.creditos !== null && version.creditos !== undefined);
}

function badgeDatos(version) {
    return versionTieneDatos(version)
        ? '<span class="badge success">Completos</span>'
        : '<span class="badge warning">Por completar</span>';
}

/** Enlace para abrir (PDF) o descargar (Word / Excel) el archivo de una version. */
function enlaceArchivo(version) {
    const esPdf = extensionDe(version.nombreArchivo) === 'pdf';
    return '<a class="link-button" target="_blank" rel="noopener" href="' + API_BASE + '/versiones-documentos/' +
        idSeguro(version.id) + '/archivo">' + (esPdf ? 'Ver PDF' : 'Descargar') + '</a>';
}

function codigoAsignatura(a) {
    return '<span class="code-chip" title="Código de materia">' + escaparTexto(a.codigoMateria) + '</span>' +
        '<span class="code-chip course" title="Código de curso">' + escaparTexto(a.codigoCurso || '—') + '</span>';
}

function llenarSelect(select, elementos, obtenerValor, obtenerTexto, textoVacio) {
    const anterior = select.value;
    select.innerHTML = '';
    const vacio = document.createElement('option');
    vacio.value = '';
    vacio.textContent = elementos.length ? textoVacio : 'No hay registros todavía';
    select.appendChild(vacio);
    elementos.forEach(function (el) {
        const opcion = document.createElement('option');
        opcion.value = obtenerValor(el);
        opcion.textContent = obtenerTexto(el);
        select.appendChild(opcion);
    });
    if (anterior && Array.from(select.options).some(function (o) { return o.value === anterior; })) {
        select.value = anterior;
    }
}

function filaVacia(tbody, columnas, texto) {
    tbody.innerHTML = '<tr><td colspan="' + columnas + '" class="empty-state">' + escaparTexto(texto) + '</td></tr>';
}

/* =====================================================================
   3. SESION Y PERMISOS POR ROL
   ===================================================================== */

function esAdmin() {
    return Boolean(estado.usuario && estado.usuario.administrador);
}

/** Pregunta al backend quien tiene la sesion; si nadie, api() envia al login. */
async function cargarSesion() {
    estado.usuario = await api('/auth/yo');
    const u = estado.usuario;
    const rol = NOMBRES_ROL[u.rol] || u.rol;
    const inicial = (u.nombreCompleto || u.usuario || '?').trim().charAt(0).toUpperCase();

    document.getElementById('perfil-nombre').textContent = u.nombreCompleto;
    document.getElementById('perfil-rol').textContent = rol;
    document.getElementById('perfil-inicial').textContent = inicial;
    document.getElementById('usuario-nombre').textContent = u.nombreCompleto;
    document.getElementById('usuario-rol').textContent = rol;
    document.getElementById('usuario-inicial').textContent = inicial;
    document.getElementById('cuenta-nombre').textContent = u.nombreCompleto;
    document.getElementById('cuenta-detalle').textContent = 'Usuario: ' + u.usuario + ' · Rol: ' + rol;

    /* Lo marcado con data-solo-admin solo se muestra al administrador */
    document.querySelectorAll('[data-solo-admin]').forEach(function (el) { el.hidden = !esAdmin(); });
    document.getElementById('programas-solo-admin').hidden = esAdmin();
    /* El administrador tambien puede filtrar la actividad del modulo de usuarios */
    const filtro = document.getElementById('filtro-actividad');
    if (esAdmin() && !filtro.querySelector('option[value="usuarios"]')) {
        const opcion = document.createElement('option');
        opcion.value = 'usuarios';
        opcion.textContent = 'Usuarios';
        filtro.appendChild(opcion);
    }
    /* La auxiliar siempre queda como encargada de las solicitudes que crea */
    document.getElementById('grupo-encargado').hidden = !esAdmin();
}

async function cerrarSesion() {
    try {
        await fetch(API_BASE + '/auth/logout', { method: 'POST' });
    } catch (e) {
        /* aunque falle la red, se sale al login */
    }
    window.location.replace('/login.html');
}

/* =====================================================================
   4. NAVEGACION ENTRE VISTAS
   ===================================================================== */

function mostrarVista(vista) {
    if (!TITULOS_VISTA[vista]) {
        return;
    }
    if (VISTAS_SOLO_ADMIN.includes(vista) && !esAdmin()) {
        mostrarMensaje('Solo el administrador puede abrir esa sección.', 'error');
        return;
    }
    document.querySelectorAll('.page-view').forEach(function (s) { s.classList.remove('active-view'); });
    document.querySelectorAll('.menu-item').forEach(function (b) {
        b.classList.toggle('active', b.dataset.vista === vista);
    });
    const seccion = document.getElementById('vista-' + vista);
    if (seccion) {
        seccion.classList.add('active-view');
    }
    document.getElementById('page-title').textContent = TITULOS_VISTA[vista];
    window.scrollTo(0, 0);

    /* Cada vez que se entra a una vista se recargan sus datos reales */
    if (vista === 'dashboard') { cargarDashboard(); }
    if (vista === 'carga') { cargarDocumentos(); }
    if (vista === 'cursos') { cargarDocumentos(); }
    if (vista === 'certificaciones') { cargarSolicitudes(); }
    if (vista === 'usuarios') { cargarUsuarios(); }
}

/* =====================================================================
   5. CATALOGOS
   ===================================================================== */

async function cargarCatalogos() {
    const resultados = await Promise.allSettled([
        api('/asignaturas'),
        api('/tipos-documentos-academicos'),
        api('/formatos'),
        api('/programas'),
        api('/usuarios'),
        api('/tipos-certificados'),
        api('/configuracion')
    ]);
    const valor = function (i) { return resultados[i].status === 'fulfilled' ? (resultados[i].value || []) : []; };

    estado.asignaturas = valor(0);
    estado.tipos = valor(1);
    estado.formatos = valor(2);
    estado.programas = valor(3);
    estado.usuarios = valor(4);
    estado.tiposCertificado = valor(5);
    if (resultados[6].status === 'fulfilled' && resultados[6].value) {
        estado.configuracion = resultados[6].value;
    }

    const fallido = resultados.find(function (r) { return r.status === 'rejected'; });
    if (fallido) {
        mostrarMensaje(fallido.reason.message, 'error');
    }
    pintarCatalogos();
}

function textoAsignatura(a) {
    return a.codigoMateria + ' ' + (a.codigoCurso || '') + ' — ' + a.nombre;
}

function pintarCatalogos() {
    llenarSelect(document.getElementById('carga-asignatura'), estado.asignaturas,
        function (a) { return a.id; }, textoAsignatura, 'Seleccione una asignatura');

    llenarSelect(document.getElementById('carga-tipo'), estado.tipos,
        function (t) { return t.id; }, function (t) { return t.nombre; }, 'Seleccione un tipo de documento');

    llenarSelect(document.getElementById('carga-programa'), estado.programas,
        function (p) { return p.id; }, function (p) { return (p.codigo ? p.codigo + ' - ' : '') + p.nombre; },
        '(Opcional)');

    /* Formatos: los del catalogo + "Otro" para escribir codigo/version */
    const selectFormato = document.getElementById('carga-formato');
    llenarSelect(selectFormato, estado.formatos,
        function (f) { return f.codigo + '|' + f.version; },
        function (f) { return f.codigo + ' v' + f.version + ' — ' + f.nombre + (f.vigente ? ' (vigente)' : ''); },
        'Seleccione el formato del documento');
    const otro = document.createElement('option');
    otro.value = FORMATO_OTRO;
    otro.textContent = 'Otro formato (escribir código y versión)';
    selectFormato.appendChild(otro);

    llenarSelect(document.getElementById('sol-tipo'), estado.tiposCertificado,
        function (t) { return t.id; }, function (t) { return t.nombre; }, 'Seleccione un tipo');

    const activos = estado.usuarios.filter(function (u) { return u.activo !== false; });
    const selectEncargado = document.getElementById('sol-encargado');
    llenarSelect(selectEncargado, activos,
        function (u) { return u.id; }, function (u) { return u.nombreCompleto + ' (' + u.usuario + ')'; },
        'Seleccione el encargado');
    if (!selectEncargado.value && estado.usuario) {
        selectEncargado.value = String(estado.usuario.id);
    }

    /* Tipos de archivo permitidos (los define el backend) */
    const extensiones = estado.configuracion.extensionesPermitidas || ['pdf'];
    const aceptar = extensiones.map(function (e) { return '.' + e; }).join(',');
    document.getElementById('carga-archivo').accept = aceptar;
    document.getElementById('reemplazar-archivo').accept = aceptar;
    document.getElementById('carga-archivo-ayuda').textContent =
        'Arrastra el archivo aquí o haz clic para buscarlo. Se aceptan: ' +
        extensiones.join(', ').toUpperCase() + ' (máx. ' + estado.configuracion.tamanoMaximoMb + ' MB).';

    pintarChecklistAsignaturas();
    pintarProgramas();
}

/* =====================================================================
   6. DASHBOARD, ACTIVIDAD RECIENTE E HISTORIAL
   ===================================================================== */

async function cargarDashboard() {
    cargarActividad();
    try {
        const [asignaturas, documentos, solicitudes] = await Promise.all([
            api('/asignaturas'),
            api('/documentos-academicos/resumen'),
            api('/solicitudes-certificados')
        ]);
        estado.asignaturas = asignaturas || [];
        estado.documentos = documentos || [];
        estado.solicitudes = solicitudes || [];
        pintarKpis();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

function pintarKpis() {
    const docs = estado.documentos;
    const desactualizados = docs.filter(function (d) { return !d.vigente; });
    const pendientes = estado.solicitudes.filter(function (s) {
        return s.estado === 'PENDIENTE' || s.estado === 'ESPERANDO_DOCUMENTOS';
    });
    const realizadas = estado.solicitudes.filter(function (s) { return s.estado === 'REALIZADO'; });

    document.getElementById('kpi-documentos').textContent = docs.length;
    document.getElementById('kpi-documentos-detalle').textContent =
        (docs.length - desactualizados.length) + ' vigentes · ' + desactualizados.length + ' desactualizados';
    document.getElementById('kpi-asignaturas').textContent = estado.asignaturas.length;
    document.getElementById('kpi-solicitudes').textContent = estado.solicitudes.length;
    document.getElementById('kpi-solicitudes-detalle').textContent = realizadas.length + ' realizadas';
    document.getElementById('kpi-pendientes').textContent = pendientes.length + desactualizados.length;

    const alerta = document.getElementById('alerta-formatos');
    if (desactualizados.length) {
        document.getElementById('alerta-formatos-texto').textContent =
            desactualizados.length + ' documento(s) usan un formato que ya no es el vigente. ' +
            'Carga la versión actualizada de la carta descriptiva.';
        alerta.hidden = false;
    } else {
        alerta.hidden = true;
    }
}

const ICONOS_NIVEL = { success: '✓', warning: '!', danger: '✕', info: 'i' };

function htmlActividad(a, claseFila, claseMarcador) {
    const nivel = ICONOS_NIVEL[a.nivel] ? a.nivel : 'success';
    const detalle = [a.detalle, a.usuario ? 'por ' + a.usuario : '', a.estado === 'EN_CURSO' ? '(en curso)' : '']
        .filter(Boolean).join(' · ');
    return '<div class="' + claseFila + '">' +
        '<div class="' + claseMarcador + ' ' + nivel + '">' + ICONOS_NIVEL[nivel] + '</div>' +
        '<div><strong>' + escaparTexto(a.titulo) + '</strong><span>' + escaparTexto(detalle) + '</span></div>' +
        '<time datetime="' + escaparTexto(a.fechaInicio) + '" title="' + escaparTexto(formatearFecha(a.fechaInicio)) + '">' +
        escaparTexto(tiempoRelativo(a.fechaInicio)) + '</time>' +
        '</div>';
}

async function cargarActividad() {
    const lista = document.getElementById('lista-actividad');
    const modulo = document.getElementById('filtro-actividad').value;
    try {
        const actividades = await api('/actividades?limite=' + ACTIVIDADES_DASHBOARD +
            '&modulo=' + encodeURIComponent(modulo));
        if (!actividades.length) {
            lista.innerHTML = '<p class="empty-state">Aún no hay actividad registrada. Todo lo que se haga en el ' +
                'panel (cargar documentos, registrar asignaturas, solicitudes…) aparecerá aquí.</p>';
            return;
        }
        lista.innerHTML = actividades.map(function (a) {
            return htmlActividad(a, 'activity-item', 'activity-dot');
        }).join('');
    } catch (e) {
        lista.innerHTML = '<p class="empty-state">' + escaparTexto(e.message) + '</p>';
    }
}

async function mostrarHistorial(modulo) {
    const clave = TITULOS_HISTORIAL[modulo] ? modulo : 'todos';
    mostrarVista('historial');
    document.getElementById('history-title').textContent = TITULOS_HISTORIAL[clave];
    const lista = document.getElementById('history-list');
    lista.innerHTML = '<p class="empty-state">Cargando…</p>';
    try {
        const actividades = await api('/actividades?limite=' + ACTIVIDADES_HISTORIAL +
            '&modulo=' + encodeURIComponent(clave));
        document.getElementById('history-total').textContent = actividades.length;
        document.getElementById('history-ultima').textContent =
            actividades.length ? tiempoRelativo(actividades[0].fechaInicio) : '—';
        document.getElementById('history-alertas').textContent =
            actividades.filter(function (a) { return a.nivel === 'warning' || a.nivel === 'danger'; }).length;
        lista.innerHTML = actividades.length
            ? actividades.map(function (a) { return htmlActividad(a, 'history-row', 'history-marker'); }).join('')
            : '<p class="empty-state">No hay registros para este módulo.</p>';
    } catch (e) {
        lista.innerHTML = '<p class="empty-state">' + escaparTexto(e.message) + '</p>';
    }
}

/* =====================================================================
   7. DATOS DEL CURSO (campos que se escriben a mano)

   Los mismos campos se usan en dos lugares:
     - al cargar un documento nuevo        (prefijo "carga")
     - al corregir una carga ya existente  (prefijo "editar")
   ===================================================================== */

function crearCampoCurso(prefijo, campo, obligatorio) {
    const grupo = document.createElement('div');
    grupo.className = 'form-group' + (campo.ancho ? ' full-width' : '');

    const etiqueta = document.createElement('label');
    etiqueta.htmlFor = prefijo + '-' + campo.id;
    etiqueta.textContent = campo.etiqueta + (obligatorio && !campo.opcional ? ' *' : '');
    grupo.appendChild(etiqueta);

    const control = document.createElement(campo.tipo === 'area' ? 'textarea' : 'input');
    control.id = prefijo + '-' + campo.id;
    control.dataset.campoCurso = campo.id;
    if (campo.tipo === 'area') {
        control.rows = campo.filas || 3;
    } else {
        control.type = 'text';
        if (campo.tipo === 'numero') {
            control.inputMode = 'decimal';
            control.maxLength = 7;
            control.autocomplete = 'off';
        }
    }
    if (campo.maximo) { control.maxLength = campo.maximo; }
    if (campo.ejemplo) { control.placeholder = campo.ejemplo; }
    grupo.appendChild(control);

    if (campo.ayuda) {
        const ayuda = document.createElement('small');
        ayuda.className = 'field-help';
        ayuda.textContent = campo.ayuda;
        grupo.appendChild(ayuda);
    }
    return grupo;
}

/** Construye los campos dentro de un contenedor (una sola vez). */
function construirCamposCurso(idContenedor, prefijo) {
    const contenedor = document.getElementById(idContenedor);
    contenedor.textContent = '';

    const principales = document.createElement('div');
    principales.className = 'form-grid';
    CAMPOS_CURSO.obligatorios.forEach(function (campo) {
        principales.appendChild(crearCampoCurso(prefijo, campo, true));
    });
    contenedor.appendChild(principales);

    const plegable = document.createElement('details');
    plegable.className = 'optional-fields';
    const resumen = document.createElement('summary');
    resumen.textContent = 'Más datos de la carta descriptiva (opcional)';
    plegable.appendChild(resumen);
    const opcionales = document.createElement('div');
    opcionales.className = 'form-grid';
    CAMPOS_CURSO.opcionales.forEach(function (campo) {
        opcionales.appendChild(crearCampoCurso(prefijo, campo, false));
    });
    plegable.appendChild(opcionales);
    contenedor.appendChild(plegable);
}

function todosLosCamposCurso() {
    return CAMPOS_CURSO.obligatorios.concat(CAMPOS_CURSO.opcionales);
}

/**
 * Lee y valida los campos. Devuelve { datos, errores }.
 * datos trae solo los campos con valor (los numeros ya con punto decimal).
 */
function leerCamposCurso(prefijo) {
    const datos = {};
    const errores = [];

    todosLosCamposCurso().forEach(function (campo) {
        const control = document.getElementById(prefijo + '-' + campo.id);
        let valor = control.value.trim();
        control.classList.remove('input-error');
        if (!valor) {
            return;
        }
        if (campo.tipo === 'numero') {
            valor = valor.replace(',', '.');
            if (!/^\d{1,4}(\.\d{1,2})?$/.test(valor)) {
                errores.push(campo.etiqueta + ': escribe un número (ej. 3 o 2.5).');
                control.classList.add('input-error');
                return;
            }
        }
        datos[campo.id] = valor;
    });

    const exigir = function (id, mensaje) {
        if (!datos[id]) {
            errores.push(mensaje);
            document.getElementById(prefijo + '-' + id).classList.add('input-error');
        }
    };
    exigir('descripcion', 'Escribe la descripción del curso.');
    exigir('detalleContenido', 'Escribe el detalle de contenido (un tema por línea).');
    exigir('creditos', 'Escribe el número de créditos.');
    if (datos.creditos && Number(datos.creditos) <= 0) {
        errores.push('Los créditos deben ser mayores que cero.');
    }
    const horas = ['horasTeoricas', 'horasPracticas', 'horasLaboratorio', 'horasIndependientes'];
    if (!horas.some(function (h) { return Number(datos[h]) > 0; })) {
        errores.push('Registra al menos un valor de horas mayor que cero.');
        document.getElementById(prefijo + '-horasTeoricas').classList.add('input-error');
    }
    return { datos: datos, errores: errores };
}

function ponerCamposCurso(prefijo, datos) {
    todosLosCamposCurso().forEach(function (campo) {
        const control = document.getElementById(prefijo + '-' + campo.id);
        const valor = datos ? datos[campo.id] : null;
        control.value = valor === null || valor === undefined ? '' : String(valor);
        control.classList.remove('input-error');
    });
}

/* =====================================================================
   8. CARGA DE INFORMACION (documentos, borrador y reintento)
   ===================================================================== */

async function cargarDocumentos() {
    try {
        estado.documentos = await api('/documentos-academicos/resumen') || [];
    } catch (e) {
        estado.documentos = [];
        mostrarMensaje(e.message, 'error');
    }
    pintarDocumentos();
    pintarCursos();
}

function pintarDocumentos() {
    const tbody = document.getElementById('tabla-documentos');
    const texto = document.getElementById('buscar-documentos').value.trim().toLowerCase();
    const filtro = document.getElementById('filtro-formato').value;

    const lista = estado.documentos.filter(function (d) {
        const coincideTexto = !texto || [d.codigoMateria, d.codigoCurso, d.nombreAsignatura, d.tipoDocumento]
            .join(' ').toLowerCase().includes(texto);
        const coincideFiltro = filtro === 'TODOS' || d.estadoFormato === filtro;
        return coincideTexto && coincideFiltro;
    });

    if (!lista.length) {
        filaVacia(tbody, 10, estado.documentos.length
            ? 'Ningún documento coincide con la búsqueda.'
            : 'Aún no hay documentos cargados. Usa el formulario de arriba.');
        return;
    }

    tbody.innerHTML = lista.map(function (d) {
        const ultima = d.ultimaVersion;
        const acciones = ultima
            ? '<div class="table-actions">' + enlaceArchivo(ultima) +
              '<button type="button" class="outline-button small" data-editar-version="' + idSeguro(ultima.id) +
              '">Corregir</button></div>'
            : '—';
        return '<tr>' +
            '<td><span class="code-chip">' + escaparTexto(d.codigoMateria) + '</span></td>' +
            '<td><span class="code-chip course">' + escaparTexto(d.codigoCurso || '—') + '</span></td>' +
            '<td>' + escaparTexto(d.nombreAsignatura) + '</td>' +
            '<td>' + escaparTexto(d.tipoDocumento) + '</td>' +
            '<td>' + escaparTexto(d.codigoFormato) + ' v' + escaparTexto(d.versionFormato) +
            '<small class="cell-help">' + escaparTexto(d.nombreFormato) + '</small></td>' +
            '<td>' + badgeFormato(d.vigente) + '</td>' +
            '<td>' + (ultima ? badgeDatos(ultima) : '—') + '</td>' +
            '<td>' + escaparTexto(d.totalVersiones) + '</td>' +
            '<td>' + escaparTexto(ultima ? formatearFecha(ultima.fechaCarga) : '—') +
            (ultima && ultima.periodo ? '<small class="cell-help">Periodo ' + escaparTexto(ultima.periodo) + '</small>' : '') +
            '</td>' +
            '<td>' + acciones + '</td>' +
            '</tr>';
    }).join('');
}

/** Codigo y version del formato elegido (del catalogo o escritos a mano). */
function formatoElegido() {
    const valor = document.getElementById('carga-formato').value;
    if (valor === FORMATO_OTRO) {
        return {
            codigo: document.getElementById('carga-formato-codigo').value.trim().toUpperCase(),
            version: document.getElementById('carga-formato-version').value.trim()
        };
    }
    if (!valor) {
        return null;
    }
    const partes = valor.split('|');
    return { codigo: partes[0], version: partes[1] };
}

/** Misma regla que FormatoService.esVigente del backend (solo para la vista previa). */
function esFormatoVigente(codigo, version) {
    const normalizarVersion = function (v) {
        const limpio = String(v || '').trim().toUpperCase().replace(/^V(ERSION)?\s*/, '');
        return /^\d+$/.test(limpio) ? String(parseInt(limpio, 10)) : limpio;
    };
    return estado.formatos.some(function (f) {
        return f.vigente &&
            f.codigo.toUpperCase().replace(/\s+/g, '') === String(codigo || '').toUpperCase().replace(/\s+/g, '') &&
            normalizarVersion(f.version) === normalizarVersion(version);
    });
}

function actualizarVistaPreviaFormato() {
    const esOtro = document.getElementById('carga-formato').value === FORMATO_OTRO;
    document.getElementById('grupo-formato-codigo').hidden = !esOtro;
    document.getElementById('grupo-formato-version').hidden = !esOtro;

    const destino = document.getElementById('carga-estado-formato');
    const formato = formatoElegido();
    if (!formato || !formato.codigo || !formato.version) {
        destino.innerHTML = '<span class="badge neutral">Seleccione un formato</span>';
        return;
    }
    const vigente = esFormatoVigente(formato.codigo, formato.version);
    const actual = estado.formatos.find(function (f) { return f.vigente; });
    destino.innerHTML = badgeFormato(vigente) + (vigente
        ? ' <small>Es el formato actual.</small>'
        : ' <small>Formato viejo' + (actual ? '; el vigente es ' + escaparTexto(actual.codigo) + ' v' +
        escaparTexto(actual.version) : '') + '.</small>');
}

function mostrarNombreArchivo() {
    const archivo = document.getElementById('carga-archivo').files[0];
    document.getElementById('carga-archivo-nombre').textContent = archivo
        ? archivo.name + ' (' + (archivo.size / 1024 / 1024).toFixed(2) + ' MB)'
        : 'Ningún archivo seleccionado';
}

/** Comprueba extension y tamano antes de enviar (el backend vuelve a validar). */
function validarArchivo(archivo) {
    const config = estado.configuracion;
    if (!archivo) {
        return 'Selecciona el archivo del documento.';
    }
    if (!config.extensionesPermitidas.includes(extensionDe(archivo.name))) {
        return 'Tipo de archivo no permitido. Se aceptan: ' + config.extensionesPermitidas.join(', ') + '.';
    }
    if (archivo.size === 0) {
        return 'El archivo está vacío.';
    }
    if (archivo.size > config.tamanoMaximoMb * 1024 * 1024) {
        return 'El archivo supera ' + config.tamanoMaximoMb + ' MB.';
    }
    return null;
}

/* ---------------------------------------------------------------------
   BORRADOR: lo que la auxiliar escribe se guarda en este navegador
   mientras llena el formulario, para que no se pierda si la carga falla,
   se cae el internet o se cierra la pagina. Se borra al guardar bien.
   (El archivo NO se guarda en el borrador: hay que volver a elegirlo.)
   --------------------------------------------------------------------- */

const CONTROLES_BORRADOR = ['carga-asignatura', 'carga-tipo', 'carga-formato', 'carga-formato-codigo',
    'carga-formato-version', 'carga-periodo', 'carga-programa'];

function claveBorrador() {
    return 'borrador-carga-v1-' + (estado.usuario ? estado.usuario.id : 'anonimo');
}

function guardarBorrador() {
    try {
        const borrador = {};
        CONTROLES_BORRADOR.forEach(function (id) { borrador[id] = document.getElementById(id).value; });
        todosLosCamposCurso().forEach(function (campo) {
            borrador['curso:' + campo.id] = document.getElementById('carga-' + campo.id).value;
        });
        localStorage.setItem(claveBorrador(), JSON.stringify(borrador));
    } catch (e) {
        /* navegador sin almacenamiento (modo privado): el formulario sigue funcionando */
    }
}

function restaurarBorrador() {
    try {
        const texto = localStorage.getItem(claveBorrador());
        if (!texto) {
            return;
        }
        const borrador = JSON.parse(texto);
        let hayDatos = false;
        CONTROLES_BORRADOR.forEach(function (id) {
            if (typeof borrador[id] === 'string' && borrador[id]) {
                document.getElementById(id).value = borrador[id];
            }
        });
        todosLosCamposCurso().forEach(function (campo) {
            const valor = borrador['curso:' + campo.id];
            if (typeof valor === 'string' && valor) {
                document.getElementById('carga-' + campo.id).value = valor;
                hayDatos = true;
            }
        });
        actualizarVistaPreviaFormato();
        if (hayDatos) {
            mostrarMensaje('Se recuperó un borrador de carga sin terminar.', 'warning');
        }
    } catch (e) {
        /* borrador ilegible: se ignora */
    }
}

function borrarBorrador() {
    try {
        localStorage.removeItem(claveBorrador());
    } catch (e) {
        /* sin almacenamiento */
    }
}

function mostrarErrorCarga(mensaje) {
    document.getElementById('carga-error-texto').textContent = mensaje +
        ' Tus datos siguen en el formulario: revisa el archivo y pulsa Reintentar.';
    document.getElementById('carga-error').hidden = false;
}

function ocultarErrorCarga() {
    document.getElementById('carga-error').hidden = true;
}

async function guardarDocumento(evento) {
    if (evento) {
        evento.preventDefault();
    }
    ocultarErrorCarga();

    const idAsignatura = document.getElementById('carga-asignatura').value;
    const idTipo = document.getElementById('carga-tipo').value;
    const formato = formatoElegido();
    const periodo = document.getElementById('carga-periodo').value.trim();
    const idPrograma = document.getElementById('carga-programa').value;
    const archivo = document.getElementById('carga-archivo').files[0];
    const curso = leerCamposCurso('carga');

    /* Validaciones rapidas en el navegador (el backend vuelve a validar todo) */
    const errores = [];
    if (!idAsignatura) { errores.push('Seleccione la asignatura.'); }
    if (!idTipo) { errores.push('Seleccione el tipo de documento.'); }
    if (!formato || !formato.codigo || !formato.version) { errores.push('Indique el formato (código y versión).'); }
    if (periodo && !/^\d{4}-\d{1,2}$/.test(periodo)) { errores.push('El periodo debe ser AAAA-N (ej. 2026-2).'); }
    curso.errores.forEach(function (e) { errores.push(e); });
    const errorArchivo = validarArchivo(archivo);
    if (errorArchivo) { errores.push(errorArchivo); }
    if (errores.length) {
        mostrarMensaje(errores.join(' '), 'error');
        return;
    }

    const datos = new FormData();
    datos.append('idAsignatura', idAsignatura);
    datos.append('idTipoDocumentoAcademico', idTipo);
    datos.append('codigoFormato', formato.codigo);
    datos.append('versionFormato', formato.version);
    if (periodo) { datos.append('periodo', periodo); }
    if (idPrograma) { datos.append('idPrograma', idPrograma); }
    Object.keys(curso.datos).forEach(function (campo) { datos.append(campo, curso.datos[campo]); });
    datos.append('archivo', archivo);

    const boton = document.getElementById('btn-guardar-documento');
    boton.disabled = true;
    boton.textContent = 'Guardando…';
    try {
        const resultado = await api('/documentos-academicos/cargar', { method: 'POST', body: datos });
        mostrarMensaje(resultado && !resultado.vigente
            ? 'Documento guardado, pero su formato está DESACTUALIZADO.'
            : 'Documento guardado correctamente.', resultado && !resultado.vigente ? 'warning' : 'success');
        borrarBorrador();
        document.getElementById('form-carga').reset();
        await cargarDocumentos();
        cargarActividad();
    } catch (e) {
        /* La carga fallo: NO se limpia el formulario; el borrador conserva lo escrito */
        guardarBorrador();
        mostrarErrorCarga(e.message);
        mostrarMensaje(e.message, 'error');
    } finally {
        boton.disabled = false;
        boton.textContent = 'Guardar documento';
    }
}

function configurarZonaArchivo() {
    const zona = document.getElementById('zona-archivo');
    const input = document.getElementById('carga-archivo');
    ['dragenter', 'dragover'].forEach(function (ev) {
        zona.addEventListener(ev, function (e) { e.preventDefault(); zona.classList.add('dragging'); });
    });
    ['dragleave', 'drop'].forEach(function (ev) {
        zona.addEventListener(ev, function (e) { e.preventDefault(); zona.classList.remove('dragging'); });
    });
    zona.addEventListener('drop', function (e) {
        if (e.dataTransfer.files.length) {
            input.files = e.dataTransfer.files;
            mostrarNombreArchivo();
        }
    });
    /* El aviso de "la carga fallo" se deja visible al cambiar el archivo,
       para que el boton Reintentar quede a la mano. */
    input.addEventListener('change', mostrarNombreArchivo);
}

/* =====================================================================
   9. CORREGIR UNA CARGA (editar datos / reemplazar archivo)
   ===================================================================== */

/** Busca una version (y su documento) dentro de lo que ya esta en memoria. */
function buscarVersion(idVersion) {
    for (const documento of estado.documentos) {
        const version = documento.versiones.find(function (v) { return v.id === idVersion; });
        if (version) {
            return { documento: documento, version: version };
        }
    }
    return null;
}

async function abrirEditor(idVersion) {
    const encontrado = buscarVersion(idVersion);
    if (!encontrado) {
        mostrarMensaje('No se encontró esa versión. Actualiza la página.', 'error');
        return;
    }
    estado.versionEnEdicion = idVersion;
    mostrarVista('carga');

    const d = encontrado.documento;
    document.getElementById('editar-titulo').textContent =
        d.codigoMateria + ' ' + (d.codigoCurso || '') + ' — ' + d.nombreAsignatura;
    document.getElementById('editar-subtitulo').textContent =
        d.tipoDocumento + ' · archivo: ' + encontrado.version.nombreArchivo +
        (encontrado.version.periodo ? ' · periodo ' + encontrado.version.periodo : '');

    const panel = document.getElementById('panel-editar');
    panel.hidden = false;
    ponerCamposCurso('editar', null);
    document.getElementById('form-reemplazar').reset();
    panel.scrollIntoView({ behavior: 'smooth' });

    try {
        ponerCamposCurso('editar', await api('/versiones-documentos/' + idSeguro(idVersion) + '/datos'));
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

function cerrarEditor() {
    estado.versionEnEdicion = null;
    document.getElementById('panel-editar').hidden = true;
}

async function guardarDatosCurso(evento) {
    evento.preventDefault();
    if (!estado.versionEnEdicion) {
        return;
    }
    const curso = leerCamposCurso('editar');
    if (curso.errores.length) {
        mostrarMensaje(curso.errores.join(' '), 'error');
        return;
    }
    /* Los numeros viajan como numeros en el JSON */
    const datos = Object.assign({}, curso.datos);
    ['creditos', 'horasTeoricas', 'horasPracticas', 'horasLaboratorio', 'horasIndependientes'].forEach(function (c) {
        if (datos[c] !== undefined) { datos[c] = Number(datos[c]); }
    });
    try {
        await apiJson('/versiones-documentos/' + idSeguro(estado.versionEnEdicion) + '/datos', 'PUT', datos);
        mostrarMensaje('Datos del curso guardados.', 'success');
        await cargarDocumentos();
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

async function reemplazarArchivo(evento) {
    evento.preventDefault();
    if (!estado.versionEnEdicion) {
        return;
    }
    const archivo = document.getElementById('reemplazar-archivo').files[0];
    const error = validarArchivo(archivo);
    if (error) {
        mostrarMensaje(error, 'error');
        return;
    }
    const datos = new FormData();
    datos.append('archivo', archivo);
    try {
        await api('/versiones-documentos/' + idSeguro(estado.versionEnEdicion) + '/archivo',
            { method: 'POST', body: datos });
        mostrarMensaje('Archivo reemplazado.', 'success');
        document.getElementById('form-reemplazar').reset();
        await cargarDocumentos();
        const encontrado = buscarVersion(estado.versionEnEdicion);
        if (encontrado) {
            document.getElementById('editar-subtitulo').textContent =
                encontrado.documento.tipoDocumento + ' · archivo: ' + encontrado.version.nombreArchivo;
        }
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message + ' Puedes elegir el archivo otra vez e intentarlo de nuevo.', 'error');
    }
}

/* =====================================================================
   10. CONTENIDO DE CURSOS (asignaturas y programas)
   ===================================================================== */

/** Resumen de una asignatura con sus documentos: VIGENTE | DESACTUALIZADO | SIN_DOCUMENTOS. */
function estadoAsignatura(idAsignatura) {
    const docs = estado.documentos.filter(function (d) { return d.idAsignatura === idAsignatura; });
    if (!docs.length) {
        return { docs: docs, estado: 'SIN_DOCUMENTOS' };
    }
    return { docs: docs, estado: docs.some(function (d) { return !d.vigente; }) ? 'DESACTUALIZADO' : 'VIGENTE' };
}

function pintarCursos() {
    const grid = document.getElementById('grid-cursos');
    const texto = document.getElementById('buscar-cursos').value.trim().toLowerCase();
    const filtro = document.getElementById('filtro-cursos').value;

    const lista = estado.asignaturas.map(function (a) {
        return Object.assign({ resumen: estadoAsignatura(a.id) }, a);
    }).filter(function (a) {
        const coincideTexto = !texto || [a.codigoMateria, a.codigoCurso, a.nombre].join(' ').toLowerCase().includes(texto);
        return coincideTexto && (filtro === 'TODOS' || a.resumen.estado === filtro);
    });

    if (!lista.length) {
        grid.innerHTML = '<p class="empty-state">' + (estado.asignaturas.length
            ? 'Ninguna asignatura coincide con la búsqueda.'
            : 'Aún no hay asignaturas. Regístrala con su código de materia y de curso.') + '</p>';
        return;
    }

    const badges = {
        VIGENTE: '<span class="badge success">Vigente</span>',
        DESACTUALIZADO: '<span class="badge danger">Desactualizado</span>',
        SIN_DOCUMENTOS: '<span class="badge neutral">Sin documentos</span>'
    };

    grid.innerHTML = lista.map(function (a) {
        const docs = a.resumen.docs.map(function (d) {
            return '<li>' + escaparTexto(d.tipoDocumento) + ' · ' + escaparTexto(d.codigoFormato) + ' v' +
                escaparTexto(d.versionFormato) + ' ' + badgeFormato(d.vigente) + '</li>';
        }).join('');
        return '<div class="course-card">' +
            '<div class="course-header"><span>' + codigoAsignatura(a) + '</span>' + badges[a.resumen.estado] + '</div>' +
            '<h3>' + escaparTexto(a.nombre) + '</h3>' +
            (docs ? '<ul class="course-docs">' + docs + '</ul>' : '<p>Sin documentos cargados.</p>') +
            '<button class="outline-button" type="button" data-ver-curso="' + idSeguro(a.id) + '">Ver versiones →</button>' +
            '</div>';
    }).join('');
}

function verDetalleCurso(idAsignatura) {
    const asignatura = estado.asignaturas.find(function (a) { return a.id === idAsignatura; });
    if (!asignatura) {
        return;
    }
    const docs = estado.documentos.filter(function (d) { return d.idAsignatura === idAsignatura; });
    document.getElementById('detalle-curso-titulo').textContent = asignatura.nombre;
    document.getElementById('detalle-curso-codigos').innerHTML =
        '<div><strong>Código de materia</strong><span>' + escaparTexto(asignatura.codigoMateria) + '</span></div>' +
        '<div><strong>Código de curso</strong><span>' + escaparTexto(asignatura.codigoCurso || '—') + '</span></div>' +
        '<div><strong>Documentos</strong><span>' + docs.length + '</span></div>';

    const tbody = document.getElementById('detalle-curso-versiones');
    const filas = [];
    docs.forEach(function (d) {
        d.versiones.forEach(function (v, indice) {
            filas.push('<tr>' +
                '<td>' + escaparTexto(d.tipoDocumento) + (indice === 0 ? ' <span class="badge processing">Última</span>' : '') + '</td>' +
                '<td>' + escaparTexto(d.codigoFormato) + ' v' + escaparTexto(d.versionFormato) + '</td>' +
                '<td>' + badgeFormato(d.vigente) + '</td>' +
                '<td>' + escaparTexto(v.periodo || '—') + '</td>' +
                '<td>' + escaparTexto(v.nombreArchivo) + '<small class="cell-help">Datos: ' +
                (versionTieneDatos(v) ? 'completos' : 'por completar') + '</small></td>' +
                '<td>' + escaparTexto(formatearFecha(v.fechaCarga)) + '</td>' +
                '<td><div class="table-actions">' + enlaceArchivo(v) +
                '<button type="button" class="outline-button small" data-editar-version="' + idSeguro(v.id) +
                '">Corregir</button></div></td>' +
                '</tr>');
        });
    });
    if (filas.length) {
        tbody.innerHTML = filas.join('');
    } else {
        filaVacia(tbody, 7, 'Esta asignatura aún no tiene documentos cargados.');
    }
    const detalle = document.getElementById('detalle-curso');
    detalle.hidden = false;
    detalle.scrollIntoView({ behavior: 'smooth' });
}

function actualizarVistaPreviaAsignatura() {
    const materia = document.getElementById('asig-materia').value.trim().toUpperCase();
    const curso = document.getElementById('asig-curso').value.trim().toUpperCase();
    document.getElementById('asig-vista-previa').textContent = materia || curso ? (materia + ' ' + curso).trim() : '—';
}

async function registrarAsignatura(evento) {
    evento.preventDefault();
    const datos = {
        codigoMateria: document.getElementById('asig-materia').value.trim().toUpperCase(),
        codigoCurso: document.getElementById('asig-curso').value.trim().toUpperCase(),
        nombre: document.getElementById('asig-nombre').value.trim()
    };
    if (!/^[A-Z0-9]{2,6}$/.test(datos.codigoMateria)) {
        mostrarMensaje('Código de materia: de 2 a 6 letras o dígitos (ej. FION).', 'error');
        return;
    }
    if (!/^[A-Z0-9]{2,8}$/.test(datos.codigoCurso)) {
        mostrarMensaje('Código de curso: de 2 a 8 letras o dígitos (ej. 0001).', 'error');
        return;
    }
    if (!datos.nombre) {
        mostrarMensaje('Escriba el nombre de la asignatura.', 'error');
        return;
    }
    try {
        const creada = await apiJson('/asignaturas', 'POST', datos);
        mostrarMensaje('Asignatura ' + creada.codigo + ' registrada.', 'success');
        document.getElementById('form-asignatura').reset();
        actualizarVistaPreviaAsignatura();
        await cargarCatalogos();
        pintarCursos();
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

function pintarProgramas() {
    const lista = document.getElementById('lista-programas');
    lista.innerHTML = estado.programas.map(function (p) {
        return '<li class="chip">' + escaparTexto(p.codigo ? p.codigo + ' · ' : '') + escaparTexto(p.nombre) + '</li>';
    }).join('');
}

async function registrarPrograma(evento) {
    evento.preventDefault();
    const datos = {
        codigo: document.getElementById('prog-codigo').value.trim().toUpperCase(),
        nombre: document.getElementById('prog-nombre').value.trim()
    };
    if (!datos.codigo || !datos.nombre) {
        mostrarMensaje('Código y nombre del programa son obligatorios.', 'error');
        return;
    }
    try {
        await apiJson('/programas', 'POST', datos);
        mostrarMensaje('Programa registrado.', 'success');
        document.getElementById('form-programa').reset();
        await cargarCatalogos();
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

/* =====================================================================
   11. CERTIFICACIONES (solicitudes y vista previa)
   ===================================================================== */

function pintarChecklistAsignaturas() {
    const contenedor = document.getElementById('sol-asignaturas');
    const texto = document.getElementById('sol-buscar-asignatura').value.trim().toLowerCase();
    const marcadas = new Set(Array.from(contenedor.querySelectorAll('input:checked')).map(function (i) { return i.value; }));

    const lista = estado.asignaturas.filter(function (a) {
        return !texto || textoAsignatura(a).toLowerCase().includes(texto) || marcadas.has(String(a.id));
    });
    contenedor.innerHTML = lista.length ? lista.map(function (a) {
        const id = idSeguro(a.id);
        return '<label class="checkbox-item"><input type="checkbox" value="' + id + '"' +
            (marcadas.has(String(id)) ? ' checked' : '') + '> ' + codigoAsignatura(a) + ' ' +
            escaparTexto(a.nombre) + '</label>';
    }).join('') : '<p class="empty-state">No hay asignaturas registradas.</p>';
}

async function cargarSolicitudes() {
    try {
        estado.solicitudes = await api('/solicitudes-certificados') || [];
    } catch (e) {
        estado.solicitudes = [];
        mostrarMensaje(e.message, 'error');
    }
    pintarSolicitudes();
    if (estado.solicitudSeleccionada) {
        verDetalleSolicitud(estado.solicitudSeleccionada);
    }
}

function nombreTipoCertificado(id) {
    const tipo = estado.tiposCertificado.find(function (t) { return t.id === id; });
    return tipo ? tipo.nombre : 'Tipo #' + id;
}

function nombreUsuario(id) {
    const usuario = estado.usuarios.find(function (u) { return u.id === id; });
    return usuario ? usuario.nombreCompleto : 'Usuario #' + id;
}

/** Acciones disponibles segun el estado (mismas transiciones que el backend). */
function accionesSolicitud(s) {
    const id = idSeguro(s.id);
    const boton = function (accion, texto, clase) {
        return '<button type="button" class="' + (clase || 'outline-button') + ' small" data-accion="' + accion +
            '" data-id="' + id + '">' + texto + '</button>';
    };
    const acciones = [boton('ver', 'Ver')];
    if (s.estado === 'PENDIENTE' || s.estado === 'ESPERANDO_DOCUMENTOS') {
        acciones.push(boton('procesar', 'Generar PDF', 'primary-button'));
    }
    if (s.estado === 'PENDIENTE') {
        acciones.push(boton('esperar', 'Esperar documentos'));
    }
    if (s.estado === 'PROCESANDO') {
        acciones.push(boton('esperar', 'Esperar documentos'));
        acciones.push(boton('realizar', 'Marcar realizada'));
    }
    if (s.estado !== 'REALIZADO' && s.estado !== 'ERROR') {
        acciones.push(boton('error', 'Marcar error', 'danger-button'));
    }
    if (s.estado === 'REALIZADO') {
        acciones.push('<a class="link-button" target="_blank" rel="noopener" href="' + API_BASE +
            '/certificados-generados/solicitud/' + id + '/archivo">Ver PDF</a>');
    }
    return '<div class="table-actions">' + acciones.join('') + '</div>';
}

function pintarSolicitudes() {
    const tbody = document.getElementById('tabla-solicitudes');
    const filtro = document.getElementById('filtro-estado-solicitudes').value;
    const todas = estado.solicitudes.slice().sort(function (a, b) { return b.id - a.id; });
    const lista = filtro === 'TODOS' ? todas : todas.filter(function (s) { return s.estado === filtro; });

    document.getElementById('stat-total').textContent = todas.length;
    document.getElementById('stat-pendientes').textContent = todas.filter(function (s) {
        return s.estado === 'PENDIENTE' || s.estado === 'ESPERANDO_DOCUMENTOS' || s.estado === 'PROCESANDO';
    }).length;
    document.getElementById('stat-realizadas').textContent = todas.filter(function (s) { return s.estado === 'REALIZADO'; }).length;
    document.getElementById('stat-error').textContent = todas.filter(function (s) { return s.estado === 'ERROR'; }).length;

    if (!lista.length) {
        filaVacia(tbody, 7, todas.length ? 'No hay solicitudes con ese estado.' : 'Aún no hay solicitudes registradas.');
        return;
    }
    tbody.innerHTML = lista.map(function (s) {
        return '<tr>' +
            '<td>#' + escaparTexto(s.id) + '</td>' +
            '<td>' + escaparTexto(formatearIdEstudiante(s.idEstudiante)) + '</td>' +
            '<td>' + escaparTexto(nombreTipoCertificado(s.idTipoCertificado)) + '</td>' +
            '<td>' + escaparTexto(nombreUsuario(s.idUsuarioEncargado)) + '</td>' +
            '<td>' + escaparTexto(formatearFecha(s.fechaSolicitud)) + '</td>' +
            '<td>' + badgeSolicitud(s.estado) + '</td>' +
            '<td>' + accionesSolicitud(s) + '</td>' +
            '</tr>';
    }).join('');
}

async function crearSolicitud(evento) {
    evento.preventDefault();
    const estudiante = document.getElementById('sol-estudiante').value.trim();
    const idTipo = document.getElementById('sol-tipo').value;
    /* El administrador elige el encargado; la auxiliar es siempre ella misma */
    const idEncargado = esAdmin() ? document.getElementById('sol-encargado').value : String(estado.usuario.id);
    const asignaturas = Array.from(document.querySelectorAll('#sol-asignaturas input:checked'))
        .map(function (i) { return idSeguro(i.value); });

    if (!/^\d{1,9}$/.test(estudiante) || Number(estudiante) <= 0) {
        mostrarMensaje('El ID del estudiante debe ser numérico (ej. 000269307).', 'error');
        return;
    }
    if (!idTipo || !idEncargado) {
        mostrarMensaje('Seleccione el tipo de certificado y el usuario encargado.', 'error');
        return;
    }
    if (!asignaturas.length) {
        mostrarMensaje('Marque al menos una asignatura para el certificado.', 'error');
        return;
    }
    try {
        const creada = await apiJson('/solicitudes-certificados', 'POST', {
            idEstudiante: Number(estudiante),
            idTipoCertificado: idSeguro(idTipo),
            idUsuarioEncargado: idSeguro(idEncargado)
        });
        for (const idAsignatura of asignaturas) {
            await api('/solicitudes-certificados/' + idSeguro(creada.id) + '/asignaturas/' + idAsignatura, { method: 'POST' });
        }
        mostrarMensaje('Solicitud #' + creada.id + ' creada con ' + asignaturas.length + ' asignatura(s).', 'success');
        document.getElementById('form-solicitud').reset();
        document.getElementById('sol-encargado').value = String(estado.usuario.id);
        pintarChecklistAsignaturas();
        estado.solicitudSeleccionada = creada.id;
        await cargarSolicitudes();
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
        cargarSolicitudes();
    }
}

const RUTAS_ACCION = {
    procesar: { ruta: 'procesar', ok: 'Certificado generado correctamente.' },
    esperar: { ruta: 'esperar-documentos', ok: 'Solicitud en espera de documentos.' },
    realizar: { ruta: 'realizar', ok: 'Solicitud marcada como realizada.' },
    error: { ruta: 'error', ok: 'Solicitud marcada con error.' }
};

async function ejecutarAccionSolicitud(accion, id) {
    if (accion === 'ver') {
        estado.solicitudSeleccionada = id;
        verDetalleSolicitud(id);
        document.getElementById('detalle-solicitud').scrollIntoView({ behavior: 'smooth' });
        return;
    }
    const info = RUTAS_ACCION[accion];
    if (!info) {
        return;
    }
    if (accion === 'error' && !window.confirm('¿Marcar la solicitud #' + id + ' con error? No se puede deshacer.')) {
        return;
    }
    try {
        await apiJson('/solicitudes-certificados/' + idSeguro(id) + '/' + info.ruta, 'PATCH');
        mostrarMensaje(info.ok, 'success');
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
    estado.solicitudSeleccionada = id;
    await cargarSolicitudes();
    cargarActividad();
}

/** "T 3 · P 0 · L 0 · Indep. 6" (solo las horas registradas). */
function textoHoras(a) {
    const partes = [];
    const agregar = function (nombre, valor) {
        if (valor !== null && valor !== undefined) { partes.push(nombre + ' ' + formatearNumero(valor)); }
    };
    agregar('T', a.horasTeoricas);
    agregar('P', a.horasPracticas);
    agregar('L', a.horasLaboratorio);
    agregar('Indep.', a.horasIndependientes);
    return partes.join(' · ');
}

function recortar(texto, maximo) {
    const limpio = String(texto || '').replace(/\s+/g, ' ').trim();
    return limpio.length > maximo ? limpio.slice(0, maximo - 1) + '…' : limpio;
}

async function verDetalleSolicitud(id) {
    const solicitud = estado.solicitudes.find(function (s) { return s.id === id; });
    const panel = document.getElementById('detalle-solicitud');
    if (!solicitud) {
        panel.hidden = true;
        return;
    }
    panel.hidden = false;
    document.getElementById('detalle-solicitud-titulo').textContent = 'Solicitud #' + solicitud.id;
    document.getElementById('detalle-solicitud-tipo').textContent = nombreTipoCertificado(solicitud.idTipoCertificado);
    document.getElementById('detalle-solicitud-estudiante').textContent = formatearIdEstudiante(solicitud.idEstudiante);
    document.getElementById('detalle-solicitud-numero').textContent = '#' + solicitud.id + ' · ' + formatearFecha(solicitud.fechaSolicitud);
    document.getElementById('detalle-solicitud-encargado').textContent = nombreUsuario(solicitud.idUsuarioEncargado);

    const estadoBadge = document.getElementById('detalle-solicitud-estado');
    const info = ESTADOS_SOLICITUD[solicitud.estado] || { texto: solicitud.estado, clase: 'neutral' };
    estadoBadge.className = 'badge ' + info.clase;
    estadoBadge.textContent = info.texto;

    /* Solo se pueden agregar asignaturas mientras la solicitud no este cerrada */
    const editable = solicitud.estado !== 'REALIZADO' && solicitud.estado !== 'ERROR';
    document.getElementById('detalle-solicitud-agregar').hidden = !editable;

    const tbody = document.getElementById('detalle-solicitud-asignaturas');
    const aviso = document.getElementById('detalle-solicitud-aviso');
    aviso.hidden = true;
    filaVacia(tbody, 6, 'Cargando asignaturas…');
    try {
        /* Lo mismo que usara el backend para armar el PDF */
        const asignaturas = await api('/solicitudes-certificados/' + idSeguro(id) + '/contenido') || [];
        const ids = new Set(asignaturas.map(function (a) { return a.idAsignatura; }));
        llenarSelect(document.getElementById('detalle-agregar-asignatura'),
            estado.asignaturas.filter(function (a) { return !ids.has(a.id); }),
            function (a) { return a.id; }, textoAsignatura, 'Seleccione una asignatura para agregar');

        if (!asignaturas.length) {
            filaVacia(tbody, 6, 'La solicitud no tiene asignaturas. Agrégalas para incluirlas en el certificado.');
            return;
        }

        const incompletas = asignaturas.filter(function (a) { return !a.completo; });
        if (incompletas.length && editable) {
            document.getElementById('detalle-solicitud-aviso-texto').textContent =
                incompletas.length + ' asignatura(s) no tienen todo lo necesario. Complétalas en «Carga de información» ' +
                'antes de generar el PDF.';
            aviso.hidden = false;
        }

        tbody.innerHTML = asignaturas.map(function (a) {
            const contenido = a.completo || a.descripcion
                ? '<strong>' + escaparTexto(a.tipoDocumento || '') + ' ' + escaparTexto(a.formato || '') + '</strong> ' +
                  (a.formato ? badgeFormato(a.formatoVigente) : '') +
                  '<small class="cell-help">' + escaparTexto(recortar(a.descripcion, 180)) + '</small>' +
                  '<small class="cell-help">' + a.contenidos.length + ' tema(s) de contenido</small>'
                : '<span class="cell-help">Sin información registrada</span>';
            const estadoFila = a.completo
                ? '<span class="badge success">Completa</span>'
                : '<span class="badge warning">Falta: ' + escaparTexto(a.faltantes.join(', ')) + '</span>' +
                  (a.idVersionDocumento
                      ? ' <button type="button" class="outline-button small" data-editar-version="' +
                        idSeguro(a.idVersionDocumento) + '">Completar</button>'
                      : '');
            return '<tr><td><span class="code-chip">' + escaparTexto(a.codigoMateria) + '</span></td>' +
                '<td><span class="code-chip course">' + escaparTexto(a.codigoCurso || '—') + '</span></td>' +
                '<td>' + escaparTexto(a.nombre) + '</td>' +
                '<td>' + (a.creditos !== null && a.creditos !== undefined
                    ? escaparTexto(formatearNumero(a.creditos)) + ' créd.' : '—') +
                '<small class="cell-help">' + escaparTexto(textoHoras(a)) + '</small></td>' +
                '<td>' + contenido + '</td><td>' + estadoFila + '</td></tr>';
        }).join('');
    } catch (e) {
        filaVacia(tbody, 6, e.message);
    }
}

async function agregarAsignaturaASolicitud() {
    const id = estado.solicitudSeleccionada;
    const idAsignatura = document.getElementById('detalle-agregar-asignatura').value;
    if (!id || !idAsignatura) {
        mostrarMensaje('Seleccione una asignatura para agregar.', 'error');
        return;
    }
    try {
        await api('/solicitudes-certificados/' + idSeguro(id) + '/asignaturas/' + idSeguro(idAsignatura), { method: 'POST' });
        mostrarMensaje('Asignatura agregada a la solicitud #' + id + '.', 'success');
        verDetalleSolicitud(id);
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

/* =====================================================================
   12. USUARIOS (solo administrador) Y MI CUENTA
   ===================================================================== */

/** Mismas reglas que UsuarioService.validarContrasena del backend. */
function errorContrasena(contrasena, repetida) {
    if (contrasena.length < 8 || contrasena.length > 72) {
        return 'La contraseña debe tener entre 8 y 72 caracteres.';
    }
    if (!/\p{L}/u.test(contrasena) || !/\d/.test(contrasena)) {
        return 'La contraseña debe incluir letras y números.';
    }
    if (repetida !== undefined && contrasena !== repetida) {
        return 'Las dos contraseñas no coinciden.';
    }
    return null;
}

async function cargarUsuarios() {
    const tbody = document.getElementById('tabla-usuarios');
    try {
        const [usuarios, limites] = await Promise.all([api('/usuarios'), api('/usuarios/limites')]);
        estado.usuarios = usuarios || [];
        const auxiliares = estado.usuarios.filter(function (u) { return u.rol === 'AUXILIAR' && u.activo !== false; }).length;
        document.getElementById('usuarios-cupo').textContent = limites && limites.maxAuxiliares > 0
            ? 'Auxiliares activas: ' + auxiliares + ' de ' + limites.maxAuxiliares + '.'
            : 'Auxiliares activas: ' + auxiliares + '.';
    } catch (e) {
        filaVacia(tbody, 5, e.message);
        return;
    }

    if (!estado.usuarios.length) {
        filaVacia(tbody, 5, 'No hay usuarios.');
        return;
    }
    tbody.innerHTML = estado.usuarios.map(function (u) {
        const id = idSeguro(u.id);
        const activo = u.activo !== false;
        const esYo = estado.usuario && estado.usuario.id === u.id;
        return '<tr>' +
            '<td>' + escaparTexto(u.nombreCompleto) + (esYo ? ' <span class="badge processing">Tú</span>' : '') + '</td>' +
            '<td>' + escaparTexto(u.usuario) + '</td>' +
            '<td>' + escaparTexto(NOMBRES_ROL[u.rol] || u.rol) + '</td>' +
            '<td>' + (activo ? '<span class="badge success">Activo</span>' : '<span class="badge neutral">Inactivo</span>') + '</td>' +
            '<td><div class="table-actions">' +
            /* La contrasena propia se cambia en "Mi cuenta" (pide la actual) */
            (esYo
                ? '<button type="button" class="outline-button small" data-vista="cuenta">Cambiar mi contraseña</button>'
                : '<button type="button" class="outline-button small" data-usuario-contrasena="' + id + '">Cambiar contraseña</button>') +
            (esYo ? '' : '<button type="button" class="' + (activo ? 'danger-button' : 'outline-button') +
                ' small" data-usuario-estado="' + id + '" data-activar="' + (activo ? 'false' : 'true') + '">' +
                (activo ? 'Desactivar' : 'Activar') + '</button>') +
            '</div></td></tr>';
    }).join('');
}

async function crearUsuario(evento) {
    evento.preventDefault();
    const contrasena = document.getElementById('usr-contrasena').value;
    const datos = {
        nombreCompleto: document.getElementById('usr-nombre').value.trim(),
        usuario: document.getElementById('usr-usuario').value.trim().toLowerCase(),
        correo: document.getElementById('usr-correo').value.trim(),
        rol: document.getElementById('usr-rol').value,
        contrasena: contrasena
    };
    if (datos.nombreCompleto.length < 3) {
        mostrarMensaje('Escribe el nombre completo.', 'error');
        return;
    }
    if (!/^[a-z0-9][a-z0-9._-]{2,29}$/.test(datos.usuario)) {
        mostrarMensaje('El usuario debe tener de 3 a 30 caracteres: minúsculas, números, punto o guion.', 'error');
        return;
    }
    const error = errorContrasena(contrasena, document.getElementById('usr-contrasena2').value);
    if (error) {
        mostrarMensaje(error, 'error');
        return;
    }
    try {
        const creado = await apiJson('/usuarios', 'POST', datos);
        mostrarMensaje('Usuario ' + creado.usuario + ' creado.', 'success');
        document.getElementById('form-usuario').reset();
        await cargarUsuarios();
        pintarCatalogos();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

async function cambiarEstadoUsuario(id, activar) {
    const usuario = estado.usuarios.find(function (u) { return u.id === id; });
    if (!activar && !window.confirm('¿Desactivar a ' + (usuario ? usuario.nombreCompleto : 'este usuario') +
        '? No podrá iniciar sesión hasta que lo actives de nuevo.')) {
        return;
    }
    try {
        await api('/usuarios/' + idSeguro(id) + '/estado?activo=' + (activar ? 'true' : 'false'), { method: 'PATCH' });
        mostrarMensaje(activar ? 'Usuario activado.' : 'Usuario desactivado.', 'success');
        await cargarUsuarios();
        pintarCatalogos();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

function abrirCambioContrasena(id) {
    const usuario = estado.usuarios.find(function (u) { return u.id === id; });
    if (!usuario) {
        return;
    }
    estado.usuarioContrasena = id;
    document.getElementById('contrasena-titulo').textContent = 'Nueva contraseña para ' + usuario.nombreCompleto;
    document.getElementById('form-contrasena').reset();
    const panel = document.getElementById('panel-contrasena');
    panel.hidden = false;
    panel.scrollIntoView({ behavior: 'smooth' });
}

async function asignarContrasena(evento) {
    evento.preventDefault();
    const nueva = document.getElementById('contrasena-nueva').value;
    const error = errorContrasena(nueva);
    if (error) {
        mostrarMensaje(error, 'error');
        return;
    }
    try {
        await apiJson('/usuarios/' + idSeguro(estado.usuarioContrasena) + '/contrasena', 'PATCH', { nueva: nueva });
        mostrarMensaje('Contraseña asignada.', 'success');
        document.getElementById('form-contrasena').reset();
        document.getElementById('panel-contrasena').hidden = true;
        estado.usuarioContrasena = null;
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

async function cambiarMiContrasena(evento) {
    evento.preventDefault();
    const actual = document.getElementById('mi-actual').value;
    const nueva = document.getElementById('mi-nueva').value;
    if (!actual) {
        mostrarMensaje('Escribe tu contraseña actual.', 'error');
        return;
    }
    const error = errorContrasena(nueva, document.getElementById('mi-nueva2').value);
    if (error) {
        mostrarMensaje(error, 'error');
        return;
    }
    try {
        await apiJson('/auth/cambiar-contrasena', 'POST', { actual: actual, nueva: nueva });
        mostrarMensaje('Tu contraseña se cambió correctamente.', 'success');
        document.getElementById('form-mi-contrasena').reset();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

/* =====================================================================
   13. INICIO: conexion de eventos (NO usar onclick en el HTML)
   ===================================================================== */

document.addEventListener('DOMContentLoaded', async function () {

    /* Primero la sesion: sin usuario autenticado no se muestra nada */
    try {
        await cargarSesion();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
        return;
    }

    construirCamposCurso('carga-campos-curso', 'carga');
    construirCamposCurso('editar-campos-curso', 'editar');

    /* Un solo "escucha" para todos los botones marcados con data-... */
    document.addEventListener('click', function (e) {
        const destino = e.target.closest('[data-vista]');
        if (destino) {
            e.preventDefault();
            mostrarVista(destino.dataset.vista);
            return;
        }
        const historial = e.target.closest('[data-historial]');
        if (historial) {
            mostrarHistorial(historial.dataset.historial);
            return;
        }
        const curso = e.target.closest('[data-ver-curso]');
        if (curso) {
            verDetalleCurso(Number(curso.dataset.verCurso));
            return;
        }
        const editar = e.target.closest('[data-editar-version]');
        if (editar) {
            abrirEditor(Number(editar.dataset.editarVersion));
            return;
        }
        const contrasena = e.target.closest('[data-usuario-contrasena]');
        if (contrasena) {
            abrirCambioContrasena(Number(contrasena.dataset.usuarioContrasena));
            return;
        }
        const estadoUsuario = e.target.closest('[data-usuario-estado]');
        if (estadoUsuario) {
            cambiarEstadoUsuario(Number(estadoUsuario.dataset.usuarioEstado), estadoUsuario.dataset.activar === 'true');
            return;
        }
        const accion = e.target.closest('[data-accion]');
        if (accion) {
            ejecutarAccionSolicitud(accion.dataset.accion, Number(accion.dataset.id));
        }
    });

    document.getElementById('btn-cerrar-sesion').addEventListener('click', cerrarSesion);

    /* Dashboard */
    document.getElementById('filtro-actividad').addEventListener('change', cargarActividad);
    document.getElementById('btn-actualizar-actividad').addEventListener('click', cargarDashboard);

    /* Carga de informacion */
    const formCarga = document.getElementById('form-carga');
    formCarga.addEventListener('submit', guardarDocumento);
    formCarga.addEventListener('input', guardarBorrador);
    formCarga.addEventListener('change', guardarBorrador);
    formCarga.addEventListener('reset', function () {
        setTimeout(function () {
            borrarBorrador();
            ocultarErrorCarga();
            ponerCamposCurso('carga', null);
            mostrarNombreArchivo();
            actualizarVistaPreviaFormato();
            document.getElementById('carga-periodo').value = periodoActual();
        });
    });
    document.getElementById('btn-reintentar-carga').addEventListener('click', function () { guardarDocumento(); });
    document.getElementById('carga-formato').addEventListener('change', actualizarVistaPreviaFormato);
    document.getElementById('carga-formato-codigo').addEventListener('input', actualizarVistaPreviaFormato);
    document.getElementById('carga-formato-version').addEventListener('input', actualizarVistaPreviaFormato);
    document.getElementById('buscar-documentos').addEventListener('input', pintarDocumentos);
    document.getElementById('filtro-formato').addEventListener('change', pintarDocumentos);
    document.getElementById('carga-periodo').value = periodoActual();
    configurarZonaArchivo();

    /* Corregir una carga */
    document.getElementById('form-editar').addEventListener('submit', guardarDatosCurso);
    document.getElementById('form-reemplazar').addEventListener('submit', reemplazarArchivo);
    document.getElementById('btn-cerrar-editar').addEventListener('click', cerrarEditor);

    /* Contenido de cursos */
    document.getElementById('form-asignatura').addEventListener('submit', registrarAsignatura);
    document.getElementById('asig-materia').addEventListener('input', actualizarVistaPreviaAsignatura);
    document.getElementById('asig-curso').addEventListener('input', actualizarVistaPreviaAsignatura);
    document.getElementById('form-programa').addEventListener('submit', registrarPrograma);
    document.getElementById('buscar-cursos').addEventListener('input', pintarCursos);
    document.getElementById('filtro-cursos').addEventListener('change', pintarCursos);
    document.getElementById('btn-cerrar-detalle').addEventListener('click', function () {
        document.getElementById('detalle-curso').hidden = true;
    });

    /* Certificaciones */
    document.getElementById('form-solicitud').addEventListener('submit', crearSolicitud);
    document.getElementById('sol-buscar-asignatura').addEventListener('input', pintarChecklistAsignaturas);
    document.getElementById('filtro-estado-solicitudes').addEventListener('change', pintarSolicitudes);
    document.getElementById('btn-actualizar-solicitudes').addEventListener('click', cargarSolicitudes);
    document.getElementById('btn-agregar-asignatura').addEventListener('click', agregarAsignaturaASolicitud);

    /* Usuarios y mi cuenta */
    document.getElementById('form-usuario').addEventListener('submit', crearUsuario);
    document.getElementById('form-contrasena').addEventListener('submit', asignarContrasena);
    document.getElementById('btn-cerrar-contrasena').addEventListener('click', function () {
        document.getElementById('panel-contrasena').hidden = true;
    });
    document.getElementById('form-mi-contrasena').addEventListener('submit', cambiarMiContrasena);

    /* Datos iniciales */
    await cargarCatalogos();
    restaurarBorrador();
    await Promise.all([cargarDashboard(), cargarDocumentos(), cargarSolicitudes()]);
});
