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
    REALIZADO: { texto: 'Realizado (sin entregar)', clase: 'success' },
    ENTREGADO: { texto: 'Entregado', clase: 'delivered' },
    ERROR: { texto: 'Error', clase: 'danger' }
};

/* Contenido del certificado y alcance de la solicitud (enums del backend). SE PUEDE MODIFICAR: textos. */
const TIPOS_CONTENIDO = { RESUMIDO: 'Resumido', COMPLETO: 'Completo' };
const ALCANCES = {
    ASIGNATURAS: 'Asignaturas',
    SEMESTRES: 'Semestres',
    PROGRAMA: 'Programa completo'
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
        { id: 'resumen', etiqueta: 'Resumen para el certificado resumido', tipo: 'area', filas: 3, maximo: 1500, ancho: true,
          ayuda: 'Un párrafo breve sobre la asignatura. Si lo dejas vacío, el certificado resumido usa la descripción del curso.' },
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
    programaEditado: null,       // programa abierto en "Editar programa"
    versionEnEdicion: null,
    usuarioContrasena: null,
    planes: [],                  // todos los planes de estudio (lista con buscador)
    planElegido: null,           // plan con las asignaturas abiertas
    planEditando: null,          // plan cargado en el formulario para editar nombre / estado
    planPrograma: [],            // asignaturas por semestre del plan abierto
    planSolicitud: []            // plan de estudios del programa elegido en la solicitud
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
/**
 * Token CSRF que el servidor deja en la cookie XSRF-TOKEN. Se devuelve en la
 * cabecera X-XSRF-TOKEN en cada POST/PUT/PATCH/DELETE para demostrar que la
 * peticion sale de este panel y no de otra pagina. NO MODIFICAR.
 */
function tokenCsrf() {
    const coincidencia = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
    return coincidencia ? decodeURIComponent(coincidencia[1]) : '';
}

/** Agrega la cabecera CSRF a las peticiones que cambian datos. */
function conCsrf(opciones) {
    const resultado = Object.assign({}, opciones);
    const metodo = (resultado.method || 'GET').toUpperCase();
    if (metodo !== 'GET' && metodo !== 'HEAD') {
        resultado.headers = Object.assign({}, resultado.headers, { 'X-XSRF-TOKEN': tokenCsrf() });
    }
    return resultado;
}

async function api(ruta, opciones) {
    let respuesta;
    try {
        respuesta = await fetch(API_BASE + ruta, conCsrf(opciones));
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
        await fetch(API_BASE + '/auth/logout', conCsrf({ method: 'POST' }));
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
    if (vista === 'cursos') { cargarDocumentos(); cargarPlanes(); }
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

/* ---------------------------------------------------------------------
   BUSCAR ASIGNATURA POR CODIGO: las listas de asignaturas tienen un
   cuadro de busqueda encima. Se puede escribir el codigo de materia, el de
   curso, los dos juntos o separados ("FION 0001", "fion0001") o el nombre.
   --------------------------------------------------------------------- */

function coincideAsignatura(a, texto) {
    const t = String(texto || '').trim().toLowerCase();
    if (!t) {
        return true;
    }
    const clave = claveCodigo(t);
    return textoAsignatura(a).toLowerCase().includes(t) ||
        (clave.length > 0 && claveCodigo((a.codigoMateria || '') + (a.codigoCurso || '')).includes(clave));
}

/** Llena la lista con las asignaturas que coinciden con el buscador (la elegida siempre se conserva). */
function filtrarSelectAsignaturas(idSelect, idBuscar) {
    const select = document.getElementById(idSelect);
    const texto = document.getElementById(idBuscar).value;
    const actual = select.value;
    const coincidencias = estado.asignaturas.filter(function (a) { return coincideAsignatura(a, texto); });
    /* La que ya estaba escogida se conserva en la lista aunque no coincida */
    const lista = coincidencias.slice();
    const elegida = estado.asignaturas.find(function (a) { return String(a.id) === actual; });
    if (elegida && lista.indexOf(elegida) < 0) {
        lista.push(elegida);
    }
    llenarSelect(select, lista, function (a) { return a.id; }, textoAsignatura,
        texto.trim() ? coincidencias.length + ' coincidencia(s) — seleccione una' : 'Seleccione una asignatura');
    if (texto.trim() && !coincidencias.length) {
        select.options[0].textContent = 'Ninguna asignatura coincide con «' + texto.trim() + '»';
    }
    /* Si solo una coincide, se escoge sola */
    if (texto.trim() && coincidencias.length === 1 && select.value !== String(coincidencias[0].id)) {
        select.value = String(coincidencias[0].id);
        select.dispatchEvent(new Event('change', { bubbles: true }));
    }
}

function textoPrograma(p) {
    return (p.codigo ? p.codigo + ' - ' : '') + p.nombre;
}

function nombrePrograma(id) {
    const programa = estado.programas.find(function (p) { return p.id === id; });
    return programa ? textoPrograma(programa) : 'Programa #' + id;
}

function pintarCatalogos() {
    filtrarSelectAsignaturas('carga-asignatura', 'carga-buscar-asignatura');
    filtrarSelectAsignaturas('ident-asignatura', 'ident-buscar-asignatura');
    filtrarSelectAsignaturas('plan-asignatura', 'plan-buscar-asignatura');

    llenarSelect(document.getElementById('ident-tipo'), estado.tipos,
        function (t) { return t.id; }, function (t) { return t.nombre; }, 'Seleccione un tipo de documento');
    llenarSelect(document.getElementById('ident-programa'), estado.programas,
        function (p) { return p.id; }, textoPrograma, '(Sin programa)');
    llenarSelect(document.getElementById('plan-programa'), estado.programas,
        function (p) { return p.id; }, textoPrograma, 'Seleccione un programa');
    llenarSelect(document.getElementById('sol-programa'), estado.programas,
        function (p) { return p.id; }, textoPrograma, 'Seleccione un programa');
    /* Codigos de formato conocidos, como sugerencias al corregir una carga */
    document.getElementById('ident-formatos').innerHTML = estado.formatos.map(function (f) {
        return '<option value="' + escaparTexto(f.codigo) + '">' + escaparTexto(f.nombre + ' (v' + f.version + ')') + '</option>';
    }).join('');

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

    /* Largo minimo de contrasena (politica de CTIC, la define el backend) */
    const minimo = estado.configuracion.contrasenaMinima || 12;
    ['usr-contrasena', 'usr-contrasena2', 'contrasena-nueva', 'mi-nueva', 'mi-nueva2'].forEach(function (id) {
        document.getElementById(id).minLength = minimo;
    });
    document.querySelectorAll('.field-help').forEach(function (ayuda) {
        /* Solo los textos de contrasena (los demas pueden tener enlaces) */
        if (ayuda.children.length === 0 && /Mínimo \d+ caracteres/.test(ayuda.textContent)) {
            ayuda.textContent = ayuda.textContent.replace(/Mínimo \d+ caracteres/, 'Mínimo ' + minimo + ' caracteres');
        }
    });

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
    const entregadas = estado.solicitudes.filter(function (s) { return s.estado === 'ENTREGADO'; });

    document.getElementById('kpi-documentos').textContent = docs.length;
    document.getElementById('kpi-documentos-detalle').textContent =
        (docs.length - desactualizados.length) + ' vigentes · ' + desactualizados.length + ' desactualizados';
    document.getElementById('kpi-asignaturas').textContent = estado.asignaturas.length;
    document.getElementById('kpi-solicitudes').textContent = estado.solicitudes.length;
    document.getElementById('kpi-solicitudes-detalle').textContent =
        realizadas.length + ' sin entregar · ' + entregadas.length + ' entregadas';
    document.getElementById('kpi-pendientes').textContent = pendientes.length + desactualizados.length;

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
    pintarExistentesCarga();
    pintarCursos();
}

/**
 * Todos los archivos cargados (uno por fila), del mas reciente al mas antiguo.
 * Cada fila: { documento, version }.
 */
function archivosCargados(idAsignatura) {
    const filas = [];
    estado.documentos.forEach(function (d) {
        if (idAsignatura && d.idAsignatura !== idAsignatura) {
            return;
        }
        d.versiones.forEach(function (v) { filas.push({ documento: d, version: v }); });
    });
    filas.sort(function (a, b) {
        return String(b.version.fechaCarga || '').localeCompare(String(a.version.fechaCarga || '')) ||
            b.version.id - a.version.id;
    });
    return filas;
}

function pintarDocumentos() {
    const tbody = document.getElementById('tabla-documentos');
    const texto = document.getElementById('buscar-documentos').value.trim().toLowerCase();
    const clave = claveCodigo(texto);
    const filtro = document.getElementById('filtro-formato').value;
    const todas = archivosCargados(null);

    /* Una fila por ARCHIVO cargado: asi se ven todos los documentos de cada asignatura */
    const lista = todas.filter(function (f) {
        const d = f.documento;
        const v = f.version;
        const coincideTexto = !texto ||
            [d.codigoMateria, d.codigoCurso, d.nombreAsignatura, d.tipoDocumento, v.periodo, v.nombreArchivo]
                .join(' ').toLowerCase().includes(texto) ||
            (clave.length > 0 && claveCodigo((d.codigoMateria || '') + (d.codigoCurso || '')).includes(clave));
        const coincideFiltro = filtro === 'TODOS' || d.estadoFormato === filtro;
        return coincideTexto && coincideFiltro;
    });

    if (!lista.length) {
        filaVacia(tbody, 10, todas.length
            ? 'Ningún documento coincide con la búsqueda.'
            : 'Aún no hay documentos cargados. Usa el formulario de arriba.');
        return;
    }

    tbody.innerHTML = lista.map(function (f) {
        const d = f.documento;
        const v = f.version;
        return '<tr>' +
            '<td><span class="code-chip">' + escaparTexto(d.codigoMateria) + '</span></td>' +
            '<td><span class="code-chip course">' + escaparTexto(d.codigoCurso || '—') + '</span></td>' +
            '<td>' + escaparTexto(d.nombreAsignatura) + '</td>' +
            '<td>' + escaparTexto(d.tipoDocumento) + '</td>' +
            '<td>' + escaparTexto(d.codigoFormato) + ' v' + escaparTexto(d.versionFormato) +
            '<small class="cell-help">' + escaparTexto(d.nombreFormato) + '</small></td>' +
            '<td>' + badgeFormato(d.vigente) + '</td>' +
            '<td>' + badgeDatos(v) + '</td>' +
            '<td>' + escaparTexto(v.periodo || '—') + '</td>' +
            '<td>' + escaparTexto(v.nombreArchivo) +
            '<small class="cell-help">' + escaparTexto(formatearFecha(v.fechaCarga)) + '</small></td>' +
            '<td><div class="table-actions">' + enlaceArchivo(v) +
            '<button type="button" class="outline-button small" data-editar-version="' + idSeguro(v.id) +
            '">Corregir</button>' + botonEliminarVersion(v.id) + '</div></td>' +
            '</tr>';
    }).join('');
}

/** Debajo del formulario de carga: lo que ya tiene cargado la asignatura escogida. */
function pintarExistentesCarga() {
    const caja = document.getElementById('carga-existentes');
    const idAsignatura = Number(document.getElementById('carga-asignatura').value);
    if (!idAsignatura) {
        caja.hidden = true;
        return;
    }
    const filas = archivosCargados(idAsignatura);
    caja.hidden = false;
    if (!filas.length) {
        caja.innerHTML = '<strong>Esta asignatura aún no tiene documentos cargados.</strong>';
        return;
    }
    caja.innerHTML = '<strong>Ya cargado para esta asignatura (' + filas.length + '):</strong><ul>' +
        filas.map(function (f) {
            return '<li>' + escaparTexto(f.documento.tipoDocumento) + ' · ' +
                escaparTexto(f.documento.codigoFormato) + ' v' + escaparTexto(f.documento.versionFormato) + ' ' +
                badgeFormato(f.documento.vigente) + ' · periodo ' + escaparTexto(f.version.periodo || '—') + ' · ' +
                escaparTexto(f.version.nombreArchivo) + ' ' + enlaceArchivo(f.version) +
                '<button type="button" class="outline-button small" data-editar-version="' + idSeguro(f.version.id) +
                '">Corregir</button></li>';
        }).join('') + '</ul>';
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

    /* Identificacion del archivo (asignatura, tipo, formato, periodo, programa) */
    document.getElementById('ident-buscar-asignatura').value = '';
    filtrarSelectAsignaturas('ident-asignatura', 'ident-buscar-asignatura');
    document.getElementById('ident-asignatura').value = String(d.idAsignatura);
    document.getElementById('ident-tipo').value = String(d.idTipoDocumento);
    document.getElementById('ident-formato-codigo').value = d.codigoFormato || '';
    document.getElementById('ident-formato-version').value = d.versionFormato || '';
    document.getElementById('ident-periodo').value = encontrado.version.periodo || '';
    document.getElementById('ident-programa').value =
        encontrado.version.idProgramaDisena ? String(encontrado.version.idProgramaDisena) : '';

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

/** Guarda asignatura, tipo, formato, periodo y programa del archivo en edicion. */
async function guardarIdentificacion(evento) {
    evento.preventDefault();
    if (!estado.versionEnEdicion) {
        return;
    }
    const idAsignatura = document.getElementById('ident-asignatura').value;
    const idTipo = document.getElementById('ident-tipo').value;
    if (!idAsignatura || !idTipo) {
        mostrarMensaje('Seleccione la asignatura y el tipo de documento.', 'error');
        return;
    }
    const datos = {
        idAsignatura: idSeguro(idAsignatura),
        idTipoDocumentoAcademico: idSeguro(idTipo),
        codigoFormato: document.getElementById('ident-formato-codigo').value.trim().toUpperCase(),
        versionFormato: document.getElementById('ident-formato-version').value.trim(),
        periodo: document.getElementById('ident-periodo').value.trim() || null,
        idPrograma: document.getElementById('ident-programa').value
            ? idSeguro(document.getElementById('ident-programa').value) : null
    };
    const errores = [];
    if (!datos.idAsignatura) { errores.push('Seleccione la asignatura.'); }
    if (!datos.idTipoDocumentoAcademico) { errores.push('Seleccione el tipo de documento.'); }
    if (!datos.codigoFormato || !datos.versionFormato) { errores.push('Indique el código y la versión del formato.'); }
    if (datos.periodo && !/^\d{4}-\d{1,2}$/.test(datos.periodo)) { errores.push('El periodo debe ser AAAA-N (ej. 2026-2).'); }
    if (errores.length) {
        mostrarMensaje(errores.join(' '), 'error');
        return;
    }
    try {
        await apiJson('/documentos-academicos/cargas/' + idSeguro(estado.versionEnEdicion), 'PUT', datos);
        mostrarMensaje('Identificación del documento corregida.', 'success');
        const idVersion = estado.versionEnEdicion;
        await cargarDocumentos();
        cargarActividad();
        abrirEditor(idVersion);
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

/* ---------------------------------------------------------------------
   ELIMINAR lo que se subio o registro por error (sin ir a la base de datos).
   Siempre se pide confirmacion: lo eliminado NO se puede recuperar.
   Las reglas (que se puede borrar y que no) las decide el backend
   (EliminacionService); aqui solo se pregunta y se muestra el resultado.
   --------------------------------------------------------------------- */

function botonEliminarVersion(idVersion) {
    return '<button type="button" class="danger-button small" data-eliminar-version="' + idSeguro(idVersion) +
        '">Eliminar</button>';
}

async function eliminarVersion(idVersion) {
    const encontrado = buscarVersion(idVersion);
    if (!encontrado) {
        mostrarMensaje('No se encontró ese documento. Actualiza la página.', 'error');
        return;
    }
    const d = encontrado.documento;
    const otras = d.versiones.length - 1;
    const pregunta = '¿Eliminar el archivo "' + encontrado.version.nombreArchivo + '" de ' +
        d.codigoMateria + ' ' + (d.codigoCurso || '') + ' — ' + d.nombreAsignatura + '?\n\n' +
        'Se borran el archivo y los datos del curso registrados con él. ' +
        (otras > 0
            ? 'Las otras ' + otras + ' versión(es) de este documento se conservan.'
            : 'Es el único archivo de este documento, así que desaparecerá de la lista.') +
        '\n\nEsta acción NO se puede deshacer.';
    if (!window.confirm(pregunta)) {
        return;
    }
    try {
        await api('/versiones-documentos/' + idSeguro(idVersion), { method: 'DELETE' });
        mostrarMensaje('Documento eliminado.', 'success');
        if (estado.versionEnEdicion === idVersion) {
            cerrarEditor();
        }
        document.getElementById('detalle-curso').hidden = true;
        await cargarDocumentos();
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

async function eliminarAsignatura(idAsignatura) {
    const asignatura = estado.asignaturas.find(function (a) { return a.id === idAsignatura; });
    if (!asignatura) {
        return;
    }
    if (!window.confirm('¿Eliminar la asignatura ' + asignatura.codigoMateria + ' ' + (asignatura.codigoCurso || '') +
        ' — ' + asignatura.nombre + '?\n\nSolo se puede eliminar si no tiene documentos cargados ni está en ' +
        'una solicitud de certificado.\n\nEsta acción NO se puede deshacer.')) {
        return;
    }
    try {
        await api('/asignaturas/' + idSeguro(idAsignatura), { method: 'DELETE' });
        mostrarMensaje('Asignatura eliminada.', 'success');
        document.getElementById('detalle-curso').hidden = true;
        await cargarCatalogos();
        pintarCursos();
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

async function eliminarPrograma(idPrograma) {
    const programa = estado.programas.find(function (p) { return p.id === idPrograma; });
    if (!programa) {
        return;
    }
    if (!window.confirm('¿Eliminar el programa ' + (programa.codigo ? programa.codigo + ' — ' : '') + programa.nombre +
        '?\n\nSolo se puede eliminar si ningún documento cargado lo usa.\n\nEsta acción NO se puede deshacer.')) {
        return;
    }
    try {
        await api('/programas/' + idSeguro(idPrograma), { method: 'DELETE' });
        mostrarMensaje('Programa eliminado.', 'success');
        await cargarCatalogos();
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

async function eliminarSolicitud(idSolicitud) {
    const solicitud = estado.solicitudes.find(function (s) { return s.id === idSolicitud; });
    if (!solicitud) {
        return;
    }
    const conCertificado = solicitud.estado === 'REALIZADO' || solicitud.estado === 'ENTREGADO';
    if (!window.confirm('¿Eliminar la solicitud #' + idSolicitud + ' del estudiante ' +
        formatearIdEstudiante(solicitud.idEstudiante) + '?\n\n' +
        (conCertificado
            ? 'OJO: esta solicitud YA tiene un certificado generado. También se borrará el PDF.'
            : 'Se borra la solicitud con las asignaturas que tenía marcadas.') +
        '\n\nEsta acción NO se puede deshacer.')) {
        return;
    }
    try {
        await api('/solicitudes-certificados/' + idSeguro(idSolicitud), { method: 'DELETE' });
        mostrarMensaje('Solicitud #' + idSolicitud + ' eliminada.', 'success');
        if (estado.solicitudSeleccionada === idSolicitud) {
            estado.solicitudSeleccionada = null;
            document.getElementById('detalle-solicitud').hidden = true;
        }
        await cargarSolicitudes();
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

async function eliminarUsuario(idUsuario) {
    const usuario = estado.usuarios.find(function (u) { return u.id === idUsuario; });
    if (!usuario) {
        return;
    }
    if (!window.confirm('¿Eliminar la cuenta de ' + usuario.nombreCompleto + ' (' + usuario.usuario + ')?\n\n' +
        'Solo se puede eliminar una cuenta que todavía no ha hecho nada en el sistema. ' +
        'Si ya tiene actividad, usa «Desactivar».\n\nEsta acción NO se puede deshacer.')) {
        return;
    }
    try {
        await api('/usuarios/' + idSeguro(idUsuario), { method: 'DELETE' });
        mostrarMensaje('Usuario eliminado.', 'success');
        await cargarUsuarios();
        pintarCatalogos();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
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
        const coincideTexto = coincideAsignatura(a, texto);
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
        /* Todo lo cargado de la asignatura: un renglon por archivo, con su tipo y periodo */
        const docs = archivosCargados(a.id).map(function (f) {
            return '<li>' + escaparTexto(f.documento.tipoDocumento) + ' · ' + escaparTexto(f.documento.codigoFormato) +
                ' v' + escaparTexto(f.documento.versionFormato) +
                (f.version.periodo ? ' · ' + escaparTexto(f.version.periodo) : '') + ' ' +
                badgeFormato(f.documento.vigente) + '</li>';
        }).join('');
        return '<div class="course-card">' +
            '<div class="course-header"><span>' + codigoAsignatura(a) + '</span>' + badges[a.resumen.estado] + '</div>' +
            '<h3>' + escaparTexto(a.nombre) + '</h3>' +
            (docs ? '<ul class="course-docs">' + docs + '</ul>' : '<p>Sin documentos cargados.</p>') +
            '<div class="table-actions">' +
            '<button class="outline-button" type="button" data-ver-curso="' + idSeguro(a.id) + '">Ver documentos →</button>' +
            '<button class="danger-button small" type="button" data-eliminar-asignatura="' + idSeguro(a.id) + '">Eliminar</button>' +
            '</div></div>';
    }).join('');
}

function verDetalleCurso(idAsignatura) {
    const asignatura = estado.asignaturas.find(function (a) { return a.id === idAsignatura; });
    if (!asignatura) {
        return;
    }
    const archivos = archivosCargados(idAsignatura);
    document.getElementById('detalle-curso-titulo').textContent = asignatura.nombre;
    document.getElementById('detalle-curso-codigos').innerHTML =
        '<div><strong>Código de materia</strong><span>' + escaparTexto(asignatura.codigoMateria) + '</span></div>' +
        '<div><strong>Código de curso</strong><span>' + escaparTexto(asignatura.codigoCurso || '—') + '</span></div>' +
        '<div><strong>Documentos cargados</strong><span>' + archivos.length + '</span></div>';

    const tbody = document.getElementById('detalle-curso-versiones');
    const filas = [];
    archivos.forEach(function (f) {
        const d = f.documento;
        const v = f.version;
        filas.push('<tr>' +
            '<td>' + escaparTexto(d.tipoDocumento) + '</td>' +
            '<td>' + escaparTexto(d.codigoFormato) + ' v' + escaparTexto(d.versionFormato) + '</td>' +
            '<td>' + badgeFormato(d.vigente) + '</td>' +
            '<td>' + escaparTexto(v.periodo || '—') + '</td>' +
            '<td>' + escaparTexto(v.nombreArchivo) + '<small class="cell-help">Datos: ' +
            (versionTieneDatos(v) ? 'completos' : 'por completar') + '</small></td>' +
            '<td>' + escaparTexto(formatearFecha(v.fechaCarga)) + '</td>' +
            '<td><div class="table-actions">' + enlaceArchivo(v) +
            '<button type="button" class="outline-button small" data-editar-version="' + idSeguro(v.id) +
            '">Corregir</button>' + botonEliminarVersion(v.id) + '</div></td>' +
            '</tr>');
        filas.push('<tr class="fila-datos"><td colspan="7" data-datos-version="' + idSeguro(v.id) + '">' +
            '<small class="cell-help">Cargando datos del documento…</small></td></tr>');
    });
    if (archivos.length) {
        tbody.innerHTML = filas.join('');
        archivos.forEach(function (f) {
            cargarDatosVersion(f.version.id);
        });
    } else {
        filaVacia(tbody, 7, 'Esta asignatura aún no tiene documentos cargados.');
    }
    const detalle = document.getElementById('detalle-curso');
    detalle.hidden = false;
    detalle.scrollIntoView({ behavior: 'smooth' });
}

/** Pide los datos del curso de una version y los pinta en su fila de detalle. */
async function cargarDatosVersion(idVersion) {
    const celda = document.querySelector('[data-datos-version="' + idSeguro(idVersion) + '"]');
    if (!celda) {
        return;
    }
    try {
        const datos = await api('/versiones-documentos/' + idSeguro(idVersion) + '/datos');
        celda.innerHTML = htmlDatosVersion(datos);
    } catch (error) {
        celda.innerHTML = '<small class="cell-help">No se pudieron cargar los datos de este documento.</small>';
    }
}

/** Arma la vista de datos del curso: campos escritos y lista de contenidos. */
function htmlDatosVersion(d) {
    const campo = function (etiqueta, valor, ancho) {
        if (valor === null || valor === undefined || String(valor).trim() === '') {
            return '';
        }
        const clase = ancho ? ' class="datos-ancho"' : '';
        return '<div' + clase + '><dt>' + escaparTexto(etiqueta) + '</dt><dd>' +
            escaparTexto(String(valor)).replace(/\n/g, '<br>') + '</dd></div>';
    };
    const temas = String(d.detalleContenido || '').split(/\r?\n/)
        .map(function (t) { return t.trim(); }).filter(function (t) { return t !== ''; });
    const campos = [
        campo('Descripción', d.descripcion, true),
        campo('Propósito', d.proposito, true),
        campo('Requisitos / competencias previas', d.requisitos, true),
        campo('Justificación', d.justificacion, true),
        campo('Resumen para certificado resumido', d.resumen, true),
        campo('Créditos', d.creditos),
        campo('Horas teóricas', d.horasTeoricas),
        campo('Horas prácticas', d.horasPracticas),
        campo('Horas de laboratorio', d.horasLaboratorio),
        campo('Horas independientes', d.horasIndependientes),
        campo('Ciclo', d.ciclo),
        campo('Nivel de formación', d.nivelFormacion),
        campo('Modo de calificación', d.modoCalificacion),
        campo('Modalidades', d.modalidades),
        campo('Clasificación CINE', d.clasificacionCINE),
        campo('Núcleo básico de conocimiento', d.nucleoBasicoConocimiento),
        campo('Escuela', d.escuela),
        campo('Facultad', d.facultad),
        campo('Bibliografía y observaciones', d.observaciones, true)
    ].join('');
    const lista = temas.length
        ? '<details class="datos-temas"><summary>Contenidos (' + temas.length + ' temas)</summary><ol>' +
          temas.map(function (t) { return '<li>' + escaparTexto(t) + '</li>'; }).join('') + '</ol></details>'
        : '';
    if (!campos && !lista) {
        return '<small class="cell-help">Este documento no tiene datos del curso registrados.</small>';
    }
    return '<div class="datos-version">' + (campos ? '<dl class="datos-grid">' + campos + '</dl>' : '') + lista + '</div>';
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
        return '<li class="chip">' + escaparTexto(p.codigo ? p.codigo + ' · ' : '') + escaparTexto(p.nombre) +
            '<button type="button" class="chip-edit" data-editar-programa="' + idSeguro(p.id) +
                '" title="Editar nombre, escuela y facultad" aria-label="Editar programa">✎</button>' +
            (esAdmin() ? '<button type="button" class="chip-remove" data-eliminar-programa="' + idSeguro(p.id) +
                '" title="Eliminar programa" aria-label="Eliminar programa">×</button>' : '') + '</li>';
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

/** Abre el formulario para cambiar nombre, escuela y facultad del programa. */
function abrirEditorPrograma(idPrograma) {
    const programa = estado.programas.find(function (p) { return p.id === idPrograma; });
    if (!programa) {
        return;
    }
    const config = estado.configuracion || {};
    estado.programaEditado = programa.id;
    document.getElementById('editar-programa-titulo').textContent = 'Editar programa ' + (programa.codigo || '');
    document.getElementById('edit-prog-nombre').value = programa.nombre || '';
    document.getElementById('edit-prog-escuela').value = programa.escuela || '';
    document.getElementById('edit-prog-facultad').value = programa.facultad || '';
    document.getElementById('edit-prog-escuela').placeholder = config.membreteEscuela || '';
    document.getElementById('edit-prog-facultad').placeholder = config.membreteFacultad || '';
    const form = document.getElementById('form-editar-programa');
    form.hidden = false;
    form.scrollIntoView({ behavior: 'smooth', block: 'center' });
    document.getElementById('edit-prog-nombre').focus();
}

function cerrarEditorPrograma() {
    estado.programaEditado = null;
    document.getElementById('form-editar-programa').hidden = true;
}

async function guardarPrograma(evento) {
    evento.preventDefault();
    const programa = estado.programas.find(function (p) { return p.id === estado.programaEditado; });
    if (!programa) {
        return;
    }
    const limpiar = function (id) { return document.getElementById(id).value.trim().replace(/\s+/g, ' '); };
    const datos = {
        codigo: programa.codigo,
        nombre: limpiar('edit-prog-nombre'),
        escuela: limpiar('edit-prog-escuela'),
        facultad: limpiar('edit-prog-facultad')
    };
    if (!datos.nombre) {
        mostrarMensaje('El nombre del programa es obligatorio.', 'error');
        return;
    }
    try {
        await apiJson('/programas/' + idSeguro(programa.id), 'PUT', datos);
        mostrarMensaje('Programa ' + (programa.codigo || '') + ' actualizado.', 'success');
        cerrarEditorPrograma();
        await cargarCatalogos();
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

/* ---------------------------------------------------------------------
   PLAN DE ESTUDIOS (US-09): en que semestre de cada programa va cada
   asignatura. Lo usan las solicitudes por semestres o por programa.
   Lo consultan y lo cambian auxiliares y administrador.
   --------------------------------------------------------------------- */

/** Agrupa filas del plan por semestre: { 1: [filas], 2: [filas] ... } */
function agruparPorSemestre(filas) {
    const grupos = {};
    filas.forEach(function (f) {
        (grupos[f.semestre] = grupos[f.semestre] || []).push(f);
    });
    return grupos;
}

const ESTADOS_PLAN = {
    VIGENTE: { texto: 'Vigente', clase: 'success', orden: 0 },
    ANTIGUO: { texto: 'Antiguo', clase: 'neutral', orden: 1 },
    DESACTUALIZADO: { texto: 'Desactualizado', clase: 'warning', orden: 2 }
};

function badgeEstadoPlan(estadoPlan) {
    const info = ESTADOS_PLAN[estadoPlan] || ESTADOS_PLAN.VIGENTE;
    return '<span class="badge ' + info.clase + '">' + escaparTexto(info.texto) + '</span>';
}

/** Mismas reglas que PlanEstudioService.nombrePlan */
function nombrePlanValido(nombre) {
    return /^[\p{L}\p{N} .,:\-/()]{1,100}$/u.test(nombre);
}

/** Todos los planes de estudio (lista con buscador). */
async function cargarPlanes() {
    try {
        estado.planes = await api('/planes') || [];
    } catch (e) {
        estado.planes = [];
        mostrarMensaje(e.message, 'error');
    }
    pintarTablaPlanes();
}

function normalizarBusqueda(texto) {
    return String(texto || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim();
}

/** Tabla de planes filtrada por nombre (vigentes primero). */
function pintarTablaPlanes() {
    const tbody = document.getElementById('tabla-planes');
    const texto = normalizarBusqueda(document.getElementById('plan-buscar').value);
    const lista = estado.planes.filter(function (p) {
        return !texto || normalizarBusqueda(p.nombre).indexOf(texto) >= 0;
    });
    if (!estado.planes.length) {
        filaVacia(tbody, 4, 'Aún no hay planes de estudio. Crea el primero con el formulario de arriba.');
        return;
    }
    if (!lista.length) {
        filaVacia(tbody, 4, 'Ningún plan se llama «' + document.getElementById('plan-buscar').value.trim() + '».');
        return;
    }
    tbody.innerHTML = lista.map(function (p) {
        return '<tr' + (p.id === estado.planElegido ? ' class="fila-activa"' : '') + '>' +
            '<td><strong>' + escaparTexto(p.nombre) + '</strong></td>' +
            '<td>' + escaparTexto(p.programa || '') + '</td>' +
            '<td>' + badgeEstadoPlan(p.estado) + '</td>' +
            '<td class="acciones-plan">' +
            '<button type="button" class="outline-button small" data-editar-plan-datos="' + idSeguro(p.id) + '">Editar</button>' +
            '<button type="button" class="outline-button small" data-asignaturas-plan="' + idSeguro(p.id) + '">Asignaturas (' +
                escaparTexto(p.asignaturas) + ')</button>' +
            '<button type="button" class="danger-button small" data-eliminar-plan="' + idSeguro(p.id) + '">Eliminar</button>' +
            '</td></tr>';
    }).join('');
}

/** Lleva un plan al formulario de arriba para cambiarle el nombre o el estado. */
function editarDatosPlan(idPlan) {
    const plan = estado.planes.find(function (p) { return p.id === idPlan; });
    if (!plan) {
        return;
    }
    estado.planEditando = plan.id;
    document.getElementById('plan-form-titulo').textContent = 'Editar plan de estudios';
    document.getElementById('plan-programa').value = String(plan.idPrograma);
    document.getElementById('plan-programa').disabled = true;
    document.getElementById('plan-titulo').value = plan.nombre;
    document.getElementById('plan-estado-editar').value = plan.estado;
    document.getElementById('btn-guardar-plan').textContent = 'Guardar cambios';
    document.getElementById('btn-cancelar-plan').hidden = false;
    document.getElementById('tarjeta-plan').scrollIntoView({ behavior: 'smooth', block: 'start' });
    document.getElementById('plan-titulo').focus();
}

function limpiarFormularioPlan() {
    estado.planEditando = null;
    document.getElementById('form-plan-datos').reset();
    document.getElementById('plan-programa').disabled = false;
    document.getElementById('plan-form-titulo').textContent = 'Crear plan de estudios';
    document.getElementById('btn-guardar-plan').textContent = 'Guardar plan';
    document.getElementById('btn-cancelar-plan').hidden = true;
}

/** Abre las asignaturas por semestre del plan. */
async function abrirAsignaturasPlan(idPlan) {
    const plan = estado.planes.find(function (p) { return p.id === idPlan; });
    if (!plan) {
        return;
    }
    estado.planElegido = plan.id;
    document.getElementById('plan-asignaturas-titulo').textContent = plan.nombre + ' · ' + (plan.programa || '');
    document.getElementById('plan-asignaturas').hidden = false;
    pintarTablaPlanes();
    await cargarPlanPrograma();
    document.getElementById('plan-asignaturas').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function cerrarAsignaturasPlan() {
    estado.planElegido = null;
    estado.planPrograma = [];
    document.getElementById('plan-asignaturas').hidden = true;
    pintarTablaPlanes();
}

async function cargarPlanPrograma() {
    const idPlan = estado.planElegido;
    const contenedor = document.getElementById('plan-contenido');
    if (!idPlan) {
        estado.planPrograma = [];
        contenedor.innerHTML = '';
        return;
    }
    let filas = [];
    try {
        filas = await api('/planes/' + idSeguro(idPlan) + '/asignaturas') || [];
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
    if (estado.planElegido !== idPlan) {
        return;
    }
    estado.planPrograma = filas;
    pintarPlanPrograma();
}

function pintarPlanPrograma() {
    const contenedor = document.getElementById('plan-contenido');
    const idPlan = estado.planElegido;
    if (!estado.planPrograma.length) {
        contenedor.innerHTML = '<p class="empty-state">Este plan aún no tiene asignaturas. Agrégalas con el formulario de arriba.</p>';
        return;
    }
    const grupos = agruparPorSemestre(estado.planPrograma);
    contenedor.innerHTML = Object.keys(grupos).map(Number).sort(function (a, b) { return a - b; }).map(function (semestre) {
        return '<div class="plan-semestre"><h4>Semestre ' + escaparTexto(semestre) + ' <span class="badge neutral">' +
            grupos[semestre].length + '</span></h4><ul class="chip-list">' +
            grupos[semestre].map(function (f) {
                return '<li class="chip">' + escaparTexto(f.codigoMateria + ' ' + (f.codigoCurso || '')) + ' · ' +
                    escaparTexto(f.nombre) +
                    '<button type="button" class="chip-edit" data-editar-plan="' + idSeguro(f.idAsignatura) +
                        '" title="Cambiar semestre" aria-label="Cambiar semestre">✎</button>' +
                    '<button type="button" class="chip-remove" data-quitar-plan="' + idSeguro(f.idAsignatura) +
                        '" data-plan="' + idSeguro(idPlan) + '" title="Quitar del plan" aria-label="Quitar del plan">×</button>' +
                    '</li>';
            }).join('') + '</ul></div>';
    }).join('');
}

/** Crea el plan o guarda los cambios de nombre / estado. */
async function guardarPlan(evento) {
    evento.preventDefault();
    const idPrograma = document.getElementById('plan-programa').value;
    const idPlan = estado.planEditando;
    const nombre = document.getElementById('plan-titulo').value.trim().replace(/\s+/g, ' ');
    const estadoPlan = document.getElementById('plan-estado-editar').value;
    if (!idPlan && !idPrograma) {
        mostrarMensaje('Seleccione el programa académico.', 'error');
        return;
    }
    if (!nombrePlanValido(nombre)) {
        mostrarMensaje('Escribe el nombre del plan (hasta 100 caracteres: letras, números, espacios y . , : - / ( )).', 'error');
        return;
    }
    try {
        if (idPlan) {
            await apiJson('/planes/' + idSeguro(idPlan), 'PUT', { nombre: nombre, estado: estadoPlan });
            mostrarMensaje('Plan «' + nombre + '» guardado (' + ESTADOS_PLAN[estadoPlan].texto.toLowerCase() + ').', 'success');
        } else {
            await apiJson('/programas/' + idSeguro(idPrograma) + '/planes', 'POST', { nombre: nombre, estado: estadoPlan });
            mostrarMensaje('Plan «' + nombre + '» creado.', 'success');
        }
        limpiarFormularioPlan();
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
    cargarPlanes();
}

async function eliminarPlan(idPlan) {
    const plan = estado.planes.find(function (p) { return p.id === idPlan; });
    if (!plan || !window.confirm('¿Eliminar el plan «' + plan.nombre + '»? Las asignaturas no se borran del sistema.')) {
        return;
    }
    try {
        await api('/planes/' + idSeguro(plan.id), { method: 'DELETE' });
        mostrarMensaje('Plan «' + plan.nombre + '» eliminado.', 'success');
        if (estado.planElegido === plan.id) {
            cerrarAsignaturasPlan();
        }
        if (estado.planEditando === plan.id) {
            limpiarFormularioPlan();
        }
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
    cargarPlanes();
}

/** Lleva una asignatura del plan al formulario para cambiarle el semestre. */
function editarEnPlan(idAsignatura) {
    const fila = estado.planPrograma.find(function (f) { return f.idAsignatura === idAsignatura; });
    if (!fila) {
        return;
    }
    document.getElementById('plan-buscar-asignatura').value = '';
    filtrarSelectAsignaturas('plan-asignatura', 'plan-buscar-asignatura');
    document.getElementById('plan-asignatura').value = String(idSeguro(fila.idAsignatura));
    document.getElementById('plan-semestre').value = fila.semestre;
    document.getElementById('plan-codigos').value = '';
    document.getElementById('form-plan').scrollIntoView({ behavior: 'smooth', block: 'center' });
    mostrarMensaje('Editando ' + fila.codigo + ': cambia el semestre y pulsa «Guardar en el plan».', 'warning');
}

async function agregarAlPlan(evento) {
    evento.preventDefault();
    const idPlan = estado.planElegido;
    const semestre = Number(document.getElementById('plan-semestre').value);
    const idAsignatura = document.getElementById('plan-asignatura').value;
    const textoCodigos = document.getElementById('plan-codigos').value;

    if (!idPlan) {
        mostrarMensaje('Elige o crea un plan de estudios.', 'error');
        return;
    }
    if (!Number.isInteger(semestre) || semestre < 1 || semestre > 20) {
        mostrarMensaje('El semestre debe ser un número entre 1 y 20.', 'error');
        return;
    }
    /* Asignaturas a agregar: la escogida en la lista + las de los codigos pegados */
    const ids = new Set();
    if (idAsignatura) { ids.add(idSeguro(idAsignatura)); }
    let noEncontrados = [];
    if (textoCodigos.trim()) {
        const resultado = buscarCodigosEnTexto(textoCodigos);
        resultado.encontradas.forEach(function (a) { ids.add(a.id); });
        noEncontrados = resultado.desconocidos;
    }
    if (!ids.size) {
        mostrarMensaje('Escoja una asignatura o pegue códigos registrados.', 'error');
        return;
    }
    let agregadas = 0;
    try {
        for (const id of ids) {
            await apiJson('/planes/' + idSeguro(idPlan) + '/asignaturas/' + idSeguro(id), 'PUT', { semestre: semestre });
            agregadas++;
        }
        mostrarMensaje(agregadas + ' asignatura(s) en el semestre ' + semestre + '.' +
            (noEncontrados.length ? ' No se encontraron: ' + noEncontrados.join(', ') + '.' : ''),
            noEncontrados.length ? 'warning' : 'success');
        document.getElementById('plan-codigos').value = '';
        document.getElementById('plan-asignatura').value = '';
        document.getElementById('plan-buscar-asignatura').value = '';
        filtrarSelectAsignaturas('plan-asignatura', 'plan-buscar-asignatura');
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
    cargarPlanes();
    cargarPlanPrograma();
}

async function quitarDelPlan(idPlan, idAsignatura) {
    if (!window.confirm('¿Quitar la asignatura del plan de estudios? La asignatura no se borra.')) {
        return;
    }
    try {
        await api('/planes/' + idSeguro(idPlan) + '/asignaturas/' + idSeguro(idAsignatura), { method: 'DELETE' });
        mostrarMensaje('Asignatura quitada del plan de estudios.', 'success');
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
    cargarPlanes();
    cargarPlanPrograma();
}

/* =====================================================================
   11. CERTIFICACIONES (solicitudes y vista previa)
   ===================================================================== */

/**
 * Pinta la lista de asignaturas para marcar.
 * @param cambio (opcional) { agregar: [ids] } marca ademas esas asignaturas;
 *               { limpiar: true } las desmarca todas.
 */
function pintarChecklistAsignaturas(cambio) {
    const contenedor = document.getElementById('sol-asignaturas');
    const texto = document.getElementById('sol-buscar-asignatura').value.trim().toLowerCase();
    const marcadas = new Set(Array.from(contenedor.querySelectorAll('input:checked')).map(function (i) { return i.value; }));
    if (cambio && cambio.limpiar) { marcadas.clear(); }
    if (cambio && cambio.agregar) { cambio.agregar.forEach(function (id) { marcadas.add(String(id)); }); }

    const lista = estado.asignaturas.filter(function (a) {
        return !texto || textoAsignatura(a).toLowerCase().includes(texto) || marcadas.has(String(a.id));
    });
    contenedor.innerHTML = lista.length ? lista.map(function (a) {
        const id = idSeguro(a.id);
        return '<label class="checkbox-item"><input type="checkbox" value="' + id + '"' +
            (marcadas.has(String(id)) ? ' checked' : '') + '> ' + codigoAsignatura(a) + ' ' +
            escaparTexto(a.nombre) + '</label>';
    }).join('') : '<p class="empty-state">No hay asignaturas registradas.</p>';
    actualizarContadorAsignaturas();
}

function actualizarContadorAsignaturas() {
    const total = document.querySelectorAll('#sol-asignaturas input:checked').length;
    document.getElementById('sol-seleccionadas').textContent =
        total + (total === 1 ? ' seleccionada' : ' seleccionadas');
}

/* ---------------------------------------------------------------------
   KARDEX MANUAL (US-16 / US-12)
   La auxiliar copia del Kardex los codigos de las asignaturas que curso
   el estudiante y los pega en un cuadro; aqui se buscan en el catalogo y
   se marcan todas de una vez. No se lee ningun archivo y el texto pegado
   no sale del navegador (el Kardex no se guarda en ninguna parte).

   SE PUEDE MODIFICAR: PARECE_CODIGO, si los codigos de la universidad
   cambian de forma (hoy: letras + numeros, ej. FION 0001 o FION0001).
   --------------------------------------------------------------------- */

/** "fion-0001" / "FION 0001" -> "FION0001" (clave para comparar codigos). */
function claveCodigo(texto) {
    return String(texto || '').toUpperCase().replace(/[^A-Z0-9]/g, '');
}

const PARECE_MATERIA = /^[A-Z]{2,6}$/;
const PARECE_CURSO = /^\d[A-Z0-9]{1,7}$/;
const PARECE_CODIGO = /^[A-Z]{2,6}\d[A-Z0-9]{1,7}$/;

/**
 * Busca codigos de asignatura dentro de un texto pegado.
 * Acepta "FION 0001", "FION-0001", "FION0001", separados por coma, punto y
 * coma, espacios o saltos de linea; ignora el resto (nombres, notas...).
 * Devuelve { encontradas: [asignaturas], desconocidos: ["FION 0002", ...] }.
 */
function buscarCodigosEnTexto(texto) {
    const catalogo = new Map();
    estado.asignaturas.forEach(function (a) {
        catalogo.set(claveCodigo(a.codigoMateria + (a.codigoCurso || '')), a);
    });

    const palabras = String(texto || '').toUpperCase()
        .split(/[^A-Z0-9ÁÉÍÓÚÜÑ.]+/)
        .filter(Boolean);

    const encontradas = new Map();
    const desconocidos = new Set();
    let i = 0;
    while (i < palabras.length) {
        const una = palabras[i];
        const dos = i + 1 < palabras.length ? una + palabras[i + 1] : null;

        if (dos && catalogo.has(claveCodigo(dos))) {              /* "FION" "0001" */
            const a = catalogo.get(claveCodigo(dos));
            encontradas.set(a.id, a);
            i += 2;
        } else if (catalogo.has(claveCodigo(una))) {               /* "FION0001" */
            const a = catalogo.get(claveCodigo(una));
            encontradas.set(a.id, a);
            i += 1;
        } else if (dos && PARECE_MATERIA.test(una) && PARECE_CURSO.test(palabras[i + 1])) {
            desconocidos.add(una + ' ' + palabras[i + 1]);         /* parece codigo, no esta registrado */
            i += 2;
        } else if (PARECE_CODIGO.test(una)) {
            desconocidos.add(una);
            i += 1;
        } else {
            i += 1;                                                /* cualquier otra palabra se ignora */
        }
    }
    return { encontradas: Array.from(encontradas.values()), desconocidos: Array.from(desconocidos) };
}

function marcarAsignaturasPorCodigos() {
    const resultado = document.getElementById('sol-codigos-resultado');
    const texto = document.getElementById('sol-codigos').value;
    if (!texto.trim()) {
        mostrarMensaje('Pega primero los códigos de las asignaturas.', 'error');
        return;
    }
    const busqueda = buscarCodigosEnTexto(texto);
    document.getElementById('sol-buscar-asignatura').value = '';
    pintarChecklistAsignaturas({ agregar: busqueda.encontradas.map(function (a) { return a.id; }) });

    /* Resumen (con textContent: nada de lo pegado se interpreta como HTML) */
    resultado.textContent = '';
    resultado.classList.toggle('warning', busqueda.desconocidos.length > 0);
    const titulo = document.createElement('strong');
    titulo.textContent = busqueda.encontradas.length + ' asignatura(s) marcada(s)' +
        (busqueda.encontradas.length ? ': ' + busqueda.encontradas.map(function (a) {
            return (a.codigoMateria + ' ' + (a.codigoCurso || '')).trim();
        }).join(', ') : '.');
    resultado.appendChild(titulo);
    if (busqueda.desconocidos.length) {
        const aviso = document.createElement('span');
        aviso.textContent = busqueda.desconocidos.length + ' código(s) no están registrados y NO se incluyeron: ' +
            busqueda.desconocidos.join(', ') + '. Regístralos en «Contenido de cursos» y vuelve a pulsar el botón.';
        resultado.appendChild(aviso);
    } else if (!busqueda.encontradas.length) {
        const aviso = document.createElement('span');
        aviso.textContent = 'No se reconoció ningún código en el texto pegado.';
        resultado.appendChild(aviso);
    }
    resultado.hidden = false;
}

function desmarcarAsignaturas() {
    pintarChecklistAsignaturas({ limpiar: true });
    document.getElementById('sol-codigos-resultado').hidden = true;
}

/* ---------------------------------------------------------------------
   ALCANCE DE LA SOLICITUD: asignaturas sueltas, uno o varios semestres de
   un programa, o el programa completo. En semestres / programa las
   asignaturas salen del plan de estudios (no se marcan a mano).
   --------------------------------------------------------------------- */

function alcanceElegido() {
    return document.getElementById('sol-alcance').value || 'ASIGNATURAS';
}

function cambiarAlcanceSolicitud() {
    const alcance = alcanceElegido();
    const porPlan = alcance !== 'ASIGNATURAS';
    document.getElementById('grupo-sol-programa').hidden = !porPlan;
    document.getElementById('grupo-sol-plan-version').hidden = !porPlan;
    document.getElementById('grupo-sol-semestres').hidden = alcance !== 'SEMESTRES';
    document.getElementById('grupo-sol-plan').hidden = !porPlan;
    document.getElementById('grupo-sol-kardex').hidden = porPlan;
    document.getElementById('grupo-sol-asignaturas').hidden = porPlan;
    if (porPlan) {
        cargarPlanesSolicitud();
    }
}

/** Planes del programa elegido en la solicitud (primero el vigente). */
async function cargarPlanesSolicitud() {
    const idPrograma = document.getElementById('sol-programa').value;
    const select = document.getElementById('sol-plan');
    let planes = [];
    if (idPrograma) {
        try {
            planes = await api('/programas/' + idSeguro(idPrograma) + '/planes') || [];
        } catch (e) {
            mostrarMensaje(e.message, 'error');
        }
    }
    if (document.getElementById('sol-programa').value !== idPrograma) {
        return;
    }
    llenarSelect(select, planes, function (p) { return p.id; }, function (p) {
        return p.nombre + ' · ' + (ESTADOS_PLAN[p.estado] || ESTADOS_PLAN.VIGENTE).texto;
    }, planes.length ? 'Seleccione el plan de estudios' : (idPrograma ? 'El programa no tiene planes' : 'Seleccione un programa'));
    const vigente = planes.find(function (p) { return p.estado === 'VIGENTE'; });
    if (vigente) {
        select.value = String(vigente.id);
    }
    cargarPlanSolicitud();
}

async function cargarPlanSolicitud() {
    const idPlan = document.getElementById('sol-plan').value;
    let filas = [];
    if (idPlan) {
        try {
            filas = await api('/planes/' + idSeguro(idPlan) + '/asignaturas') || [];
        } catch (e) {
            mostrarMensaje(e.message, 'error');
        }
    }
    /* Si mientras tanto se escogio otro plan, esta respuesta ya no sirve */
    if (document.getElementById('sol-plan').value !== idPlan) {
        return;
    }
    estado.planSolicitud = filas;
    /* Casillas: solo los semestres que tienen asignaturas en el plan */
    const grupos = agruparPorSemestre(estado.planSolicitud);
    const semestres = Object.keys(grupos).map(Number).sort(function (a, b) { return a - b; });
    document.getElementById('sol-semestres').innerHTML = semestres.length ? semestres.map(function (n) {
        return '<label><input type="checkbox" value="' + idSeguro(n) + '"> Semestre ' + escaparTexto(n) +
            ' <small class="cell-help">(' + grupos[n].length + ')</small></label>';
    }).join('') : '<p class="empty-state">' + (idPlan
        ? 'Este plan no tiene asignaturas. Regístralas en «Contenido de cursos» > Planes de estudio.'
        : 'Seleccione el programa y el plan de estudios.') + '</p>';
    actualizarResumenPlan();
}

function semestresElegidos() {
    return Array.from(document.querySelectorAll('#sol-semestres input:checked')).map(function (i) { return Number(i.value); });
}

/** "Se incluirán 12 asignaturas: FION 0001, ..." (lo que pondra el backend en la solicitud). */
function actualizarResumenPlan() {
    const caja = document.getElementById('sol-plan-resumen');
    const alcance = alcanceElegido();
    if (alcance === 'ASIGNATURAS') {
        return;
    }
    const semestres = semestresElegidos();
    const filas = estado.planSolicitud.filter(function (f) {
        return alcance === 'PROGRAMA' || semestres.indexOf(f.semestre) >= 0;
    });
    caja.className = 'paste-result' + (filas.length ? '' : ' warning');
    caja.innerHTML = filas.length
        ? '<strong>Se incluirán ' + filas.length + ' asignatura(s):</strong>' +
          escaparTexto(filas.map(function (f) { return f.codigoMateria + ' ' + (f.codigoCurso || ''); }).join(', '))
        : '<strong>' + (alcance === 'SEMESTRES' ? 'Marque al menos un semestre.' : 'El programa no tiene asignaturas en su plan.') + '</strong>';
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
    if (s.estado === 'REALIZADO') {
        acciones.push(boton('entregar', 'Marcar entregado', 'primary-button'));
    }
    if (s.estado === 'ENTREGADO') {
        acciones.push(boton('deshacer-entrega', 'Deshacer entrega'));
    }
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
    if (s.estado !== 'REALIZADO' && s.estado !== 'ENTREGADO' && s.estado !== 'ERROR') {
        acciones.push(boton('error', 'Marcar error', 'danger-button'));
    }
    if (s.estado === 'REALIZADO' || s.estado === 'ENTREGADO') {
        acciones.push('<a class="link-button" target="_blank" rel="noopener" href="' + API_BASE +
            '/certificados-generados/solicitud/' + id + '/archivo">Ver PDF</a>');
    }
    /* Eliminar: una solicitud con certificado ya generado solo la borra el administrador */
    if (esAdmin() || (s.estado !== 'REALIZADO' && s.estado !== 'ENTREGADO')) {
        acciones.push('<button type="button" class="danger-button small" data-eliminar-solicitud="' + id +
            '">Eliminar</button>');
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
    document.getElementById('stat-entregadas').textContent = todas.filter(function (s) { return s.estado === 'ENTREGADO'; }).length;
    document.getElementById('stat-error').textContent = todas.filter(function (s) { return s.estado === 'ERROR'; }).length;

    if (!lista.length) {
        filaVacia(tbody, 7, todas.length ? 'No hay solicitudes con ese estado.' : 'Aún no hay solicitudes registradas.');
        return;
    }
    tbody.innerHTML = lista.map(function (s) {
        return '<tr>' +
            '<td>#' + escaparTexto(s.id) + '</td>' +
            '<td>' + escaparTexto(s.nombreEstudiante || 'Sin nombre') +
            '<small class="cell-help">ID ' + escaparTexto(formatearIdEstudiante(s.idEstudiante)) + '</small></td>' +
            '<td>' + escaparTexto(nombreTipoCertificado(s.idTipoCertificado)) +
            '<small class="cell-help">' + escaparTexto(textoContenidoAlcance(s)) + '</small></td>' +
            '<td>' + escaparTexto(nombreUsuario(s.idUsuarioEncargado)) + '</td>' +
            '<td>' + escaparTexto(formatearFecha(s.fechaSolicitud)) + '</td>' +
            '<td>' + badgeSolicitud(s.estado) +
            (s.estado === 'ENTREGADO' && s.fechaEntrega
                ? '<small class="cell-help">' + escaparTexto(formatearFecha(s.fechaEntrega)) + '</small>' : '') + '</td>' +
            '<td>' + accionesSolicitud(s) + '</td>' +
            '</tr>';
    }).join('');
}

async function crearSolicitud(evento) {
    evento.preventDefault();
    const estudiante = document.getElementById('sol-estudiante').value.trim();
    const nombre = document.getElementById('sol-nombre').value.trim().replace(/\s+/g, ' ');
    const tipoContenido = document.getElementById('sol-tipo-contenido').value;
    const alcance = alcanceElegido();
    const idPrograma = document.getElementById('sol-programa').value;
    const idPlan = document.getElementById('sol-plan').value;
    const semestres = semestresElegidos();
    const idTipo = document.getElementById('sol-tipo').value;
    /* El administrador elige el encargado; la auxiliar es siempre ella misma */
    const idEncargado = esAdmin() ? document.getElementById('sol-encargado').value : String(estado.usuario.id);
    const asignaturas = Array.from(document.querySelectorAll('#sol-asignaturas input:checked'))
        .map(function (i) { return idSeguro(i.value); });

    if (!/^\d{1,9}$/.test(estudiante) || Number(estudiante) <= 0) {
        mostrarMensaje('El ID del estudiante debe ser numérico (ej. 000269307).', 'error');
        return;
    }
    /* Mismas reglas que SolicitudCertificadoService.normalizarNombre */
    if (!/^[\p{L}][\p{L} .'-]{1,149}$/u.test(nombre)) {
        mostrarMensaje('Escriba el nombre del estudiante (solo letras, espacios, punto, guion y apóstrofe).', 'error');
        return;
    }
    if (!idTipo || !idEncargado) {
        mostrarMensaje('Seleccione el tipo de certificado y el usuario encargado.', 'error');
        return;
    }
    if (alcance === 'ASIGNATURAS' && !asignaturas.length) {
        mostrarMensaje('Marque al menos una asignatura para el certificado.', 'error');
        return;
    }
    if (alcance !== 'ASIGNATURAS' && !idPrograma) {
        mostrarMensaje('Seleccione el programa académico.', 'error');
        return;
    }
    if (alcance !== 'ASIGNATURAS' && !idPlan) {
        mostrarMensaje('Seleccione el plan de estudios.', 'error');
        return;
    }
    if (alcance === 'SEMESTRES' && !semestres.length) {
        mostrarMensaje('Marque al menos un semestre.', 'error');
        return;
    }
    try {
        const creada = await apiJson('/solicitudes-certificados', 'POST', {
            idEstudiante: Number(estudiante),
            idTipoCertificado: idSeguro(idTipo),
            idUsuarioEncargado: idSeguro(idEncargado),
            nombreEstudiante: nombre,
            tipoContenido: tipoContenido,
            alcance: alcance,
            idPrograma: alcance === 'ASIGNATURAS' ? null : idSeguro(idPrograma),
            idPlan: alcance === 'ASIGNATURAS' ? null : idSeguro(idPlan),
            semestres: alcance === 'SEMESTRES' ? semestres : null
        });
        /* Por semestres / programa las asignaturas ya las agrego el backend desde el plan */
        if (alcance === 'ASIGNATURAS') {
            for (const idAsignatura of asignaturas) {
                await api('/solicitudes-certificados/' + idSeguro(creada.id) + '/asignaturas/' + idAsignatura, { method: 'POST' });
            }
        }
        mostrarMensaje('Solicitud #' + creada.id + ' creada (' + textoContenidoAlcance(creada) + ').', 'success');
        document.getElementById('form-solicitud').reset();
        cambiarAlcanceSolicitud();
        document.getElementById('sol-encargado').value = String(estado.usuario.id);
        document.getElementById('sol-codigos-resultado').hidden = true;
        pintarChecklistAsignaturas({ limpiar: true });
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
    entregar: { ruta: 'entregar', ok: 'Certificado marcado como entregado al estudiante.' },
    'deshacer-entrega': { ruta: 'deshacer-entrega', ok: 'Se deshizo la entrega: la solicitud vuelve a Realizado.' },
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
    if (accion === 'entregar' && !window.confirm('¿Confirmas que el certificado de la solicitud #' + id +
        ' ya se le entregó al estudiante?')) {
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
    document.getElementById('detalle-solicitud-estudiante').textContent =
        (solicitud.nombreEstudiante ? solicitud.nombreEstudiante + ' · ' : 'Sin nombre · ') +
        'ID ' + formatearIdEstudiante(solicitud.idEstudiante);
    const completo = solicitud.tipoContenido === 'COMPLETO';
    document.getElementById('detalle-solicitud-contenido').textContent = completo
        ? 'Completo (todo el contenido de cada asignatura)'
        : 'Resumido (un párrafo por asignatura)';
    document.getElementById('detalle-solicitud-alcance').textContent = textoAlcance(solicitud);
    document.getElementById('detalle-solicitud-entrega').textContent = solicitud.estado === 'ENTREGADO'
        ? 'Entregado el ' + formatearFecha(solicitud.fechaEntrega)
        : (solicitud.estado === 'REALIZADO' ? 'Pendiente de entregar al estudiante' : '—');
    document.getElementById('detalle-solicitud-descripcion').textContent = 'Contenidos programáticos ' +
        (completo ? 'completos' : 'resumidos') + ' de las asignaturas de la solicitud.';
    document.getElementById('detalle-solicitud-numero').textContent = '#' + solicitud.id + ' · ' + formatearFecha(solicitud.fechaSolicitud);
    document.getElementById('detalle-solicitud-encargado').textContent = nombreUsuario(solicitud.idUsuarioEncargado);
    pintarMembrete(solicitud);

    const estadoBadge = document.getElementById('detalle-solicitud-estado');
    const info = ESTADOS_SOLICITUD[solicitud.estado] || { texto: solicitud.estado, clase: 'neutral' };
    estadoBadge.className = 'badge ' + info.clase;
    estadoBadge.textContent = info.texto;

    /* Solo se pueden agregar asignaturas mientras la solicitud no este cerrada */
    const editable = solicitud.estado !== 'REALIZADO' && solicitud.estado !== 'ENTREGADO' &&
        solicitud.estado !== 'ERROR';
    document.getElementById('detalle-solicitud-agregar').hidden = !editable;

    /* Nombre y tipo de contenido: se pueden corregir antes de generar el PDF */
    const datosEditables = solicitud.estado === 'PENDIENTE' || solicitud.estado === 'ESPERANDO_DOCUMENTOS';
    const formDatos = document.getElementById('form-detalle-solicitud');
    formDatos.hidden = !datosEditables;
    if (datosEditables) {
        document.getElementById('detalle-nombre').value = solicitud.nombreEstudiante || '';
        document.getElementById('detalle-tipo-contenido').value = solicitud.tipoContenido || 'RESUMIDO';
    }

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

        /* Quitar una asignatura agregada por error (siempre debe quedar al menos una) */
        const sePuedeQuitar = editable && asignaturas.length > 1;
        tbody.innerHTML = asignaturas.map(function (a) {
            const contenido = selectorVersion(solicitud, a, editable) + (a.completo || a.descripcion
                ? '<strong>' + escaparTexto(a.tipoDocumento || '') + ' ' + escaparTexto(a.formato || '') + '</strong> ' +
                  (a.formato ? badgeFormato(a.formatoVigente) : '') +
                  (a.periodo ? '<small class="cell-help">Periodo ' + escaparTexto(a.periodo) +
                      (a.versionElegidaManual ? ' · elegido a mano' : ' · elegido automáticamente') + '</small>' : '') +
                  '<small class="cell-help">' + escaparTexto(recortar(a.descripcion, 180)) + '</small>' +
                  '<small class="cell-help">' + a.contenidos.length + ' tema(s) de contenido</small>'
                : '<span class="cell-help">Sin información registrada</span>');
            const estadoFila = a.completo
                ? '<span class="badge success">Completa</span>'
                : '<span class="badge warning">Falta: ' + escaparTexto(a.faltantes.join(', ')) + '</span>' +
                  (a.idVersionDocumento
                      ? ' <button type="button" class="outline-button small" data-editar-version="' +
                        idSeguro(a.idVersionDocumento) + '">Completar</button>'
                      : '');
            const quitar = sePuedeQuitar
                ? '<button type="button" class="danger-button small quitar-asignatura" data-quitar-asignatura-solicitud="' +
                  idSeguro(a.idAsignatura) + '" data-solicitud="' + idSeguro(solicitud.id) + '" data-codigo="' +
                  escaparTexto((a.codigoMateria || '') + ' ' + (a.codigoCurso || '')) + '">Quitar</button>'
                : '';
            return '<tr><td><span class="code-chip">' + escaparTexto(a.codigoMateria) + '</span></td>' +
                '<td><span class="code-chip course">' + escaparTexto(a.codigoCurso || '—') + '</span></td>' +
                '<td>' + escaparTexto(a.nombre) +
                (a.semestre ? '<small class="cell-help">Semestre ' + escaparTexto(a.semestre) + '</small>' : '') + '</td>' +
                '<td>' + (a.creditos !== null && a.creditos !== undefined
                    ? escaparTexto(formatearNumero(a.creditos)) + ' créd.' : '—') +
                '<small class="cell-help">' + escaparTexto(textoHoras(a)) + '</small></td>' +
                '<td>' + contenido + '</td><td>' + estadoFila + quitar + '</td></tr>';
        }).join('');
    } catch (e) {
        filaVacia(tbody, 6, e.message);
    }
}

/* ---------------------------------------------------------------------
   SELECTOR DE VERSION: que version del documento (periodo) usa el
   certificado para cada asignatura. Por defecto la elige el sistema
   (la mas reciente con datos completos y formato vigente); la auxiliar
   puede escoger otra, por ejemplo la del semestre en que el estudiante
   curso la materia.
   --------------------------------------------------------------------- */

function textoOpcionVersion(v) {
    return (v.periodo || 'Sin periodo') + ' · ' + (v.tipoDocumento || 'Documento') + ' ' + (v.formato || '') +
        (v.formatoVigente ? '' : ' (formato desactualizado)') +
        (v.datosCompletos ? '' : ' — sin datos del curso');
}

function selectorVersion(solicitud, a, editable) {
    const versiones = a.versionesDisponibles || [];
    if (!editable || versiones.length === 0) {
        return '';
    }
    const opciones = ['<option value=""' + (a.versionElegidaManual ? '' : ' selected') +
        '>Automática (recomendada)</option>'];
    versiones.forEach(function (v) {
        const elegida = a.versionElegidaManual && v.idVersionDocumento === a.idVersionDocumento;
        opciones.push('<option value="' + idSeguro(v.idVersionDocumento) + '"' + (elegida ? ' selected' : '') + '>' +
            escaparTexto(textoOpcionVersion(v)) + '</option>');
    });
    return '<select class="version-select" aria-label="Versión del documento para el certificado"' +
        ' data-version-solicitud="' + idSeguro(solicitud.id) + '" data-version-asignatura="' +
        idSeguro(a.idAsignatura) + '">' + opciones.join('') + '</select>';
}

async function elegirVersion(select) {
    const idSolicitud = Number(select.dataset.versionSolicitud);
    const idAsignatura = Number(select.dataset.versionAsignatura);
    const idVersion = select.value ? idSeguro(select.value) : null;
    select.disabled = true;
    try {
        await apiJson('/solicitudes-certificados/' + idSeguro(idSolicitud) + '/asignaturas/' + idSeguro(idAsignatura) +
            '/version', 'PUT', { idVersionDocumento: idVersion });
        mostrarMensaje(idVersion ? 'Versión del documento actualizada.' : 'Se usará la versión automática.', 'success');
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
    verDetalleSolicitud(idSolicitud);
}

function textoAlcance(s) {
    if (s.alcance === 'SEMESTRES') {
        const lista = (s.semestres || []).join(', ');
        return 'Semestre' + ((s.semestres || []).length === 1 ? ' ' : 's ') + lista + ' · ' + nombrePrograma(s.idPrograma) +
            (s.nombrePlan ? ' · ' + s.nombrePlan : '');
    }
    if (s.alcance === 'PROGRAMA') {
        return 'Programa completo · ' + nombrePrograma(s.idPrograma) + (s.nombrePlan ? ' · ' + s.nombrePlan : '');
    }
    return 'Asignaturas';
}

/** "Resumido · Semestres 1, 2 · ISIS - Ing. de Sistemas" */
function textoContenidoAlcance(s) {
    return (TIPOS_CONTENIDO[s.tipoContenido] || 'Resumido') + ' · ' + textoAlcance(s);
}

/** Guarda nombre del estudiante y tipo de contenido (antes de generar el PDF). */
async function guardarDatosSolicitud(evento) {
    evento.preventDefault();
    const id = estado.solicitudSeleccionada;
    const nombre = document.getElementById('detalle-nombre').value.trim().replace(/\s+/g, ' ');
    if (!id) {
        return;
    }
    if (!/^[\p{L}][\p{L} .'-]{1,149}$/u.test(nombre)) {
        mostrarMensaje('Escriba el nombre del estudiante (solo letras, espacios, punto, guion y apóstrofe).', 'error');
        return;
    }
    try {
        await apiJson('/solicitudes-certificados/' + idSeguro(id) + '/datos', 'PATCH', {
            nombreEstudiante: nombre,
            tipoContenido: document.getElementById('detalle-tipo-contenido').value
        });
        mostrarMensaje('Datos de la solicitud #' + id + ' actualizados.', 'success');
        await cargarSolicitudes();
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
}

/** Membrete y pie de la vista previa (mismos textos que el PDF, segun el programa). */
async function pintarMembrete(solicitud) {
    const config = estado.configuracion || {};
    document.getElementById('membrete-escuela').textContent = config.membreteEscuela || '';
    document.getElementById('membrete-facultad').textContent = config.membreteFacultad || '';
    /* Escoger escuela y facultad: mientras el certificado no se haya entregado */
    const form = document.getElementById('form-membrete-solicitud');
    form.hidden = solicitud.estado === 'ENTREGADO';
    llenarSugerenciasMembrete();
    try {
        const textos = await api('/solicitudes-certificados/' + idSeguro(solicitud.id) + '/membrete');
        if (textos && estado.solicitudSeleccionada === solicitud.id) {
            document.getElementById('membrete-escuela').textContent = textos.escuela || '';
            document.getElementById('membrete-facultad').textContent = textos.facultad || '';
            document.getElementById('membrete-elegir-escuela').value = textos.escuela || '';
            document.getElementById('membrete-elegir-facultad').value = textos.facultad || '';
        }
    } catch (e) {
        /* Si falla, quedan los textos generales */
    }
    document.getElementById('membrete-titulo').textContent = solicitud.tipoContenido === 'COMPLETO'
        ? 'CONTENIDOS PROGRAMÁTICOS' : 'RESUMEN CONTENIDOS PROGRAMÁTICOS';
    document.getElementById('membrete-estudiante').textContent =
        (solicitud.nombreEstudiante ? solicitud.nombreEstudiante + ', ' : '') +
        'ID. ' + formatearIdEstudiante(solicitud.idEstudiante);
    document.getElementById('pie-pagina-texto').textContent = config.piePagina || '';
}

/** Sugerencias de escuela y facultad: las de los programas y las generales. */
function llenarSugerenciasMembrete() {
    const config = estado.configuracion || {};
    const unicos = function (valores) {
        return valores.filter(function (v, i) { return v && valores.indexOf(v) === i; });
    };
    /* SE PUEDE MODIFICAR: escuelas y facultades que salen como sugerencia */
    const escuelas = unicos([config.membreteEscuela, 'ESCUELA DE INGENIERÍAS',
        'ESCUELA DE ECONOMÍA, ADMINISTRACIÓN Y NEGOCIOS']
        .concat(estado.programas.map(function (p) { return p.escuela; })));
    const facultades = unicos([config.membreteFacultad, 'FACULTAD DE INGENIERÍA DE SISTEMAS E INFORMÁTICA',
        'FACULTAD DE ADMINISTRACIÓN DE NEGOCIOS INTERNACIONALES']
        .concat(estado.programas.map(function (p) { return p.facultad; })));
    document.getElementById('lista-escuelas').innerHTML = escuelas.map(function (v) {
        return '<option value="' + escaparTexto(v) + '"></option>';
    }).join('');
    document.getElementById('lista-facultades').innerHTML = facultades.map(function (v) {
        return '<option value="' + escaparTexto(v) + '"></option>';
    }).join('');
}

/** Guarda la escuela y la facultad del membrete de la solicitud abierta. */
async function guardarMembreteSolicitud(evento) {
    evento.preventDefault();
    const id = estado.solicitudSeleccionada;
    if (!id) {
        return;
    }
    const limpiar = function (idCampo) { return document.getElementById(idCampo).value.trim().replace(/\s+/g, ' '); };
    const boton = evento.target.querySelector('button[type="submit"]');
    boton.disabled = true;
    try {
        const solicitud = await apiJson('/solicitudes-certificados/' + idSeguro(id) + '/membrete', 'PATCH', {
            escuela: limpiar('membrete-elegir-escuela'),
            facultad: limpiar('membrete-elegir-facultad')
        });
        mostrarMensaje('Membrete de la solicitud #' + id + ' guardado' +
            (solicitud && solicitud.estado === 'REALIZADO' ? '. El PDF se volvió a generar.' : '.'), 'success');
        cargarActividad();
        await cargarSolicitudes();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
    boton.disabled = false;
}

/** Quita de la solicitud una asignatura agregada por error (no la borra del sistema). */
async function quitarAsignaturaDeSolicitud(idSolicitud, idAsignatura, codigo) {
    if (!window.confirm('¿Quitar ' + (codigo.trim() || 'la asignatura') + ' de la solicitud #' + idSolicitud +
        '? La asignatura no se borra del sistema.')) {
        return;
    }
    try {
        await api('/solicitudes-certificados/' + idSeguro(idSolicitud) + '/asignaturas/' + idSeguro(idAsignatura),
            { method: 'DELETE' });
        mostrarMensaje('Asignatura quitada de la solicitud #' + idSolicitud + '.', 'success');
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    }
    verDetalleSolicitud(idSolicitud);
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

/** Mismas reglas que UsuarioService.validarContrasena del backend.
 *  El minimo lo define el backend (app.seguridad.contrasena-minima) y llega
 *  en /api/configuracion; 12 es solo el valor por si no llegara. */
function errorContrasena(contrasena, repetida) {
    const minimo = estado.configuracion.contrasenaMinima || 12;
    if (contrasena.length < minimo || new TextEncoder().encode(contrasena).length > 72) {
        return 'La contraseña debe tener entre ' + minimo + ' y 72 caracteres.';
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
                (activo ? 'Desactivar' : 'Activar') + '</button>' +
                '<button type="button" class="danger-button small" data-eliminar-usuario="' + id + '">Eliminar</button>') +
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
        const eliminarDoc = e.target.closest('[data-eliminar-version]');
        if (eliminarDoc) {
            eliminarVersion(Number(eliminarDoc.dataset.eliminarVersion));
            return;
        }
        const eliminarAsig = e.target.closest('[data-eliminar-asignatura]');
        if (eliminarAsig) {
            eliminarAsignatura(Number(eliminarAsig.dataset.eliminarAsignatura));
            return;
        }
        const editarProg = e.target.closest('[data-editar-programa]');
        if (editarProg) {
            abrirEditorPrograma(Number(editarProg.dataset.editarPrograma));
            return;
        }
        const eliminarProg = e.target.closest('[data-eliminar-programa]');
        if (eliminarProg) {
            eliminarPrograma(Number(eliminarProg.dataset.eliminarPrograma));
            return;
        }
        const eliminarSol = e.target.closest('[data-eliminar-solicitud]');
        if (eliminarSol) {
            eliminarSolicitud(Number(eliminarSol.dataset.eliminarSolicitud));
            return;
        }
        const eliminarUsr = e.target.closest('[data-eliminar-usuario]');
        if (eliminarUsr) {
            eliminarUsuario(Number(eliminarUsr.dataset.eliminarUsuario));
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
        const datosPlan = e.target.closest('[data-editar-plan-datos]');
        if (datosPlan) {
            editarDatosPlan(Number(datosPlan.dataset.editarPlanDatos));
            return;
        }
        const asigPlan = e.target.closest('[data-asignaturas-plan]');
        if (asigPlan) {
            abrirAsignaturasPlan(Number(asigPlan.dataset.asignaturasPlan));
            return;
        }
        const borrarPlan = e.target.closest('[data-eliminar-plan]');
        if (borrarPlan) {
            eliminarPlan(Number(borrarPlan.dataset.eliminarPlan));
            return;
        }
        const editarPlan = e.target.closest('[data-editar-plan]');
        if (editarPlan) {
            editarEnPlan(Number(editarPlan.dataset.editarPlan));
            return;
        }
        const quitarSol = e.target.closest('[data-quitar-asignatura-solicitud]');
        if (quitarSol) {
            quitarAsignaturaDeSolicitud(Number(quitarSol.dataset.solicitud),
                Number(quitarSol.dataset.quitarAsignaturaSolicitud), quitarSol.dataset.codigo || '');
            return;
        }
        const quitarPlan = e.target.closest('[data-quitar-plan]');
        if (quitarPlan) {
            quitarDelPlan(Number(quitarPlan.dataset.plan), Number(quitarPlan.dataset.quitarPlan));
            return;
        }
        const accion = e.target.closest('[data-accion]');
        if (accion) {
            ejecutarAccionSolicitud(accion.dataset.accion, Number(accion.dataset.id));
        }
    });

    document.getElementById('btn-cerrar-sesion').addEventListener('click', cerrarSesion);

    /* Selector de version en la vista previa de la solicitud */
    document.addEventListener('change', function (e) {
        const select = e.target.closest('select[data-version-solicitud]');
        if (select) {
            elegirVersion(select);
        }
    });

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
            filtrarSelectAsignaturas('carga-asignatura', 'carga-buscar-asignatura');
            pintarExistentesCarga();
        });
    });
    document.getElementById('btn-reintentar-carga').addEventListener('click', function () { guardarDocumento(); });
    document.getElementById('carga-formato').addEventListener('change', actualizarVistaPreviaFormato);
    document.getElementById('carga-formato-codigo').addEventListener('input', actualizarVistaPreviaFormato);
    document.getElementById('carga-formato-version').addEventListener('input', actualizarVistaPreviaFormato);
    document.getElementById('buscar-documentos').addEventListener('input', pintarDocumentos);
    /* Buscar la asignatura por codigo de materia / curso / nombre */
    document.getElementById('carga-buscar-asignatura').addEventListener('input', function () {
        filtrarSelectAsignaturas('carga-asignatura', 'carga-buscar-asignatura');
        pintarExistentesCarga();
    });
    document.getElementById('carga-asignatura').addEventListener('change', pintarExistentesCarga);
    document.getElementById('filtro-formato').addEventListener('change', pintarDocumentos);
    document.getElementById('carga-periodo').value = periodoActual();
    configurarZonaArchivo();

    /* Corregir una carga */
    document.getElementById('form-editar').addEventListener('submit', guardarDatosCurso);
    document.getElementById('form-identificacion').addEventListener('submit', guardarIdentificacion);
    document.getElementById('ident-buscar-asignatura').addEventListener('input', function () {
        filtrarSelectAsignaturas('ident-asignatura', 'ident-buscar-asignatura');
    });
    document.getElementById('form-reemplazar').addEventListener('submit', reemplazarArchivo);
    document.getElementById('btn-cerrar-editar').addEventListener('click', cerrarEditor);

    /* Contenido de cursos */
    document.getElementById('form-asignatura').addEventListener('submit', registrarAsignatura);
    document.getElementById('asig-materia').addEventListener('input', actualizarVistaPreviaAsignatura);
    document.getElementById('asig-curso').addEventListener('input', actualizarVistaPreviaAsignatura);
    document.getElementById('form-programa').addEventListener('submit', registrarPrograma);
    document.getElementById('form-plan-datos').addEventListener('submit', guardarPlan);
    document.getElementById('form-membrete-solicitud').addEventListener('submit', guardarMembreteSolicitud);
    document.getElementById('btn-cancelar-plan').addEventListener('click', limpiarFormularioPlan);
    document.getElementById('btn-cerrar-asignaturas-plan').addEventListener('click', cerrarAsignaturasPlan);
    document.getElementById('plan-buscar').addEventListener('input', pintarTablaPlanes);
    document.getElementById('form-editar-programa').addEventListener('submit', guardarPrograma);
    document.getElementById('btn-cancelar-programa').addEventListener('click', cerrarEditorPrograma);
    document.getElementById('form-plan').addEventListener('submit', agregarAlPlan);
    document.getElementById('plan-buscar-asignatura').addEventListener('input', function () {
        filtrarSelectAsignaturas('plan-asignatura', 'plan-buscar-asignatura');
    });
    document.getElementById('buscar-cursos').addEventListener('input', pintarCursos);
    document.getElementById('filtro-cursos').addEventListener('change', pintarCursos);
    document.getElementById('btn-cerrar-detalle').addEventListener('click', function () {
        document.getElementById('detalle-curso').hidden = true;
    });

    /* Certificaciones */
    document.getElementById('form-solicitud').addEventListener('submit', crearSolicitud);
    document.getElementById('sol-alcance').addEventListener('change', cambiarAlcanceSolicitud);
    document.getElementById('sol-programa').addEventListener('change', cargarPlanesSolicitud);
    document.getElementById('sol-plan').addEventListener('change', cargarPlanSolicitud);
    document.getElementById('sol-semestres').addEventListener('change', actualizarResumenPlan);
    document.getElementById('form-detalle-solicitud').addEventListener('submit', guardarDatosSolicitud);
    document.getElementById('sol-buscar-asignatura').addEventListener('input', function () { pintarChecklistAsignaturas(); });
    document.getElementById('sol-asignaturas').addEventListener('change', actualizarContadorAsignaturas);
    document.getElementById('btn-marcar-codigos').addEventListener('click', marcarAsignaturasPorCodigos);
    document.getElementById('btn-desmarcar-todas').addEventListener('click', desmarcarAsignaturas);
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
    cambiarAlcanceSolicitud();   /* por si el navegador recordo el alcance al recargar */
    restaurarBorrador();
    await Promise.all([cargarDashboard(), cargarDocumentos(), cargarSolicitudes()]);
});
