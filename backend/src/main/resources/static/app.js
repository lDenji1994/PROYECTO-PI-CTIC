/* =====================================================================
   PANEL DE GESTION ACADEMICA - UPB (CTIC)
   Logica del panel: navegacion + conexion con el backend Spring Boot.

   ORGANIZACION DEL ARCHIVO
     1. Configuracion y estado
     2. Utilidades (API, seguridad, formato de fechas, mensajes)
     3. Navegacion entre vistas
     4. Catalogos (asignaturas, tipos, formatos, programas, usuarios)
     5. Dashboard + Actividad reciente + Historial
     6. Carga de informacion (documentos)
     7. Contenido de cursos (asignaturas y programas)
     8. Certificaciones (solicitudes)
     9. Inicio

   REGLAS DE SEGURIDAD (NO MODIFICAR)
   - Todo texto que venga del servidor se pinta con escaparTexto() o
     textContent: evita inyeccion de HTML/JS (XSS).
   - Los IDs que van en URLs se validan como numeros (idSeguro()).
   - No hay datos de ejemplo quemados: si una lista esta vacia es
     porque la base de datos esta vacia.
   ===================================================================== */

'use strict';

/* =====================================================================
   1. CONFIGURACION Y ESTADO
   ===================================================================== */

/* SE PUEDE MODIFICAR: si el panel se abre desde otro servidor (p. ej.
   Live Server), cambia esto por 'http://localhost:8080/api'. */
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
    historial: 'Historial'
};

const TITULOS_HISTORIAL = {
    todos: 'Toda la actividad',
    documentos: 'Historial de documentos académicos',
    cursos: 'Historial de asignaturas y programas',
    certificaciones: 'Historial de certificaciones'
};

/* Valor especial del select de formatos para escribir uno a mano. */
const FORMATO_OTRO = '__OTRO__';

/* Cache en memoria de lo que devolvio el backend (se recarga tras cada cambio). */
const estado = {
    asignaturas: [],
    tipos: [],
    formatos: [],
    programas: [],
    usuarios: [],
    tiposCertificado: [],
    documentos: [],
    solicitudes: [],
    solicitudSeleccionada: null
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
    return 'Error ' + respuesta.status + ' al comunicarse con el servidor';
}

/**
 * Llamada al backend. Devuelve el JSON (o null si no hay cuerpo).
 * Lanza Error con el mensaje del servidor si la respuesta no es 2xx.
 */
async function api(ruta, opciones) {
    let respuesta;
    try {
        respuesta = await fetch(API_BASE + ruta, opciones);
    } catch (e) {
        marcarServidor(false);
        throw new Error('No se pudo conectar con el servidor. ¿Está corriendo el backend?');
    }
    marcarServidor(true);
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
    setTimeout(function () { aviso.remove(); }, tipo === 'error' ? 7000 : 4000);
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

function badgeFormato(vigente) {
    return vigente
        ? '<span class="badge success">Vigente</span>'
        : '<span class="badge danger">Desactualizado</span>';
}

function badgeSolicitud(estadoSolicitud) {
    const info = ESTADOS_SOLICITUD[estadoSolicitud] || { texto: estadoSolicitud, clase: 'neutral' };
    return '<span class="badge ' + info.clase + '">' + escaparTexto(info.texto) + '</span>';
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
   3. NAVEGACION ENTRE VISTAS
   ===================================================================== */

function mostrarVista(vista) {
    if (!TITULOS_VISTA[vista]) {
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
}

/* =====================================================================
   4. CATALOGOS
   ===================================================================== */

async function cargarCatalogos() {
    const resultados = await Promise.allSettled([
        api('/asignaturas'),
        api('/tipos-documentos-academicos'),
        api('/formatos'),
        api('/programas'),
        api('/usuarios'),
        api('/tipos-certificados')
    ]);
    const valor = function (i) { return resultados[i].status === 'fulfilled' ? (resultados[i].value || []) : []; };

    estado.asignaturas = valor(0);
    estado.tipos = valor(1);
    estado.formatos = valor(2);
    estado.programas = valor(3);
    estado.usuarios = valor(4);
    estado.tiposCertificado = valor(5);

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

    llenarSelect(document.getElementById('sol-encargado'), estado.usuarios,
        function (u) { return u.id; }, function (u) { return u.nombreCompleto + ' (' + u.usuario + ')'; },
        'Seleccione el encargado');

    pintarChecklistAsignaturas();
    pintarProgramas();
}

/* =====================================================================
   5. DASHBOARD, ACTIVIDAD RECIENTE E HISTORIAL
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
   6. CARGA DE INFORMACION (documentos academicos)
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
        filaVacia(tbody, 9, estado.documentos.length
            ? 'Ningún documento coincide con la búsqueda.'
            : 'Aún no hay documentos cargados. Usa el formulario de arriba.');
        return;
    }

    tbody.innerHTML = lista.map(function (d) {
        const ultima = d.ultimaVersion;
        const accion = ultima
            ? '<a class="link-button" target="_blank" rel="noopener" href="' + API_BASE + '/versiones-documentos/' +
              idSeguro(ultima.id) + '/archivo">Ver PDF</a>'
            : '—';
        return '<tr>' +
            '<td><span class="code-chip">' + escaparTexto(d.codigoMateria) + '</span></td>' +
            '<td><span class="code-chip course">' + escaparTexto(d.codigoCurso || '—') + '</span></td>' +
            '<td>' + escaparTexto(d.nombreAsignatura) + '</td>' +
            '<td>' + escaparTexto(d.tipoDocumento) + '</td>' +
            '<td>' + escaparTexto(d.codigoFormato) + ' v' + escaparTexto(d.versionFormato) +
            '<small class="cell-help">' + escaparTexto(d.nombreFormato) + '</small></td>' +
            '<td>' + badgeFormato(d.vigente) + '</td>' +
            '<td>' + escaparTexto(d.totalVersiones) + '</td>' +
            '<td>' + escaparTexto(ultima ? formatearFecha(ultima.fechaCarga) : '—') +
            (ultima && ultima.periodo ? '<small class="cell-help">Periodo ' + escaparTexto(ultima.periodo) + '</small>' : '') +
            '</td>' +
            '<td>' + accion + '</td>' +
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

async function guardarDocumento(evento) {
    evento.preventDefault();
    const idAsignatura = document.getElementById('carga-asignatura').value;
    const idTipo = document.getElementById('carga-tipo').value;
    const formato = formatoElegido();
    const periodo = document.getElementById('carga-periodo').value.trim();
    const idPrograma = document.getElementById('carga-programa').value;
    const archivo = document.getElementById('carga-archivo').files[0];

    /* Validaciones rapidas en el navegador (el backend vuelve a validar todo) */
    const errores = [];
    if (!idAsignatura) { errores.push('Seleccione la asignatura.'); }
    if (!idTipo) { errores.push('Seleccione el tipo de documento.'); }
    if (!formato || !formato.codigo || !formato.version) { errores.push('Indique el formato (código y versión).'); }
    if (periodo && !/^\d{4}-\d{1,2}$/.test(periodo)) { errores.push('El periodo debe ser AAAA-N (ej. 2026-2).'); }
    if (!archivo) { errores.push('Seleccione el archivo PDF.'); }
    if (archivo && !/\.pdf$/i.test(archivo.name)) { errores.push('Solo se permiten archivos PDF.'); }
    if (archivo && archivo.size > 20 * 1024 * 1024) { errores.push('El archivo supera 20 MB.'); }
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
    datos.append('archivo', archivo);

    const boton = document.getElementById('btn-guardar-documento');
    boton.disabled = true;
    try {
        const resultado = await api('/documentos-academicos/cargar', { method: 'POST', body: datos });
        mostrarMensaje(resultado && !resultado.vigente
            ? 'Documento guardado, pero su formato está DESACTUALIZADO.'
            : 'Documento guardado correctamente.', resultado && !resultado.vigente ? 'warning' : 'success');
        document.getElementById('form-carga').reset();
        document.getElementById('carga-periodo').value = periodoActual();
        mostrarNombreArchivo();
        actualizarVistaPreviaFormato();
        await cargarDocumentos();
        cargarActividad();
    } catch (e) {
        mostrarMensaje(e.message, 'error');
    } finally {
        boton.disabled = false;
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
    input.addEventListener('change', mostrarNombreArchivo);
}

/* =====================================================================
   7. CONTENIDO DE CURSOS (asignaturas y programas)
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
                '<td>' + escaparTexto(v.nombreArchivo) + '</td>' +
                '<td>' + escaparTexto(formatearFecha(v.fechaCarga)) + '</td>' +
                '<td><a class="link-button" target="_blank" rel="noopener" href="' + API_BASE +
                '/versiones-documentos/' + idSeguro(v.id) + '/archivo">Ver PDF</a></td>' +
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
   8. CERTIFICACIONES (solicitudes de certificado)
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
    const idEncargado = document.getElementById('sol-encargado').value;
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
    try {
        const creada = await apiJson('/solicitudes-certificados', 'POST', {
            idEstudiante: Number(estudiante),
            idTipoCertificado: idSeguro(idTipo),
            idUsuarioEncargado: idSeguro(idEncargado)
        });
        for (const idAsignatura of asignaturas) {
            await api('/solicitudes-certificados/' + idSeguro(creada.id) + '/asignaturas/' + idAsignatura, { method: 'POST' });
        }
        mostrarMensaje('Solicitud #' + creada.id + ' creada' +
            (asignaturas.length ? ' con ' + asignaturas.length + ' asignatura(s).' : '.'), 'success');
        document.getElementById('form-solicitud').reset();
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
    filaVacia(tbody, 4, 'Cargando asignaturas…');
    try {
        const asignaturas = await api('/solicitudes-certificados/' + idSeguro(id) + '/asignaturas') || [];
        const ids = new Set(asignaturas.map(function (a) { return a.id; }));
        llenarSelect(document.getElementById('detalle-agregar-asignatura'),
            estado.asignaturas.filter(function (a) { return !ids.has(a.id); }),
            function (a) { return a.id; }, textoAsignatura, 'Seleccione una asignatura para agregar');

        if (!asignaturas.length) {
            filaVacia(tbody, 4, 'La solicitud no tiene asignaturas. Agrégalas para incluirlas en el certificado.');
            return;
        }
        tbody.innerHTML = asignaturas.map(function (a) {
            const docs = estado.documentos.filter(function (d) { return d.idAsignatura === a.id; });
            const textoDocs = docs.length ? docs.map(function (d) {
                return escaparTexto(d.tipoDocumento) + ' (' + escaparTexto(d.codigoFormato) + ' v' +
                    escaparTexto(d.versionFormato) + ') ' + badgeFormato(d.vigente);
            }).join('<br>') : '<span class="badge warning">Sin documentos cargados</span>';
            return '<tr><td><span class="code-chip">' + escaparTexto(a.codigoMateria) + '</span></td>' +
                '<td><span class="code-chip course">' + escaparTexto(a.codigoCurso || '—') + '</span></td>' +
                '<td>' + escaparTexto(a.nombre) + '</td><td>' + textoDocs + '</td></tr>';
        }).join('');
    } catch (e) {
        filaVacia(tbody, 4, e.message);
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
   9. INICIO: conexion de eventos (NO usar onclick en el HTML)
   ===================================================================== */

document.addEventListener('DOMContentLoaded', async function () {

    /* Navegacion: cualquier elemento con data-vista="..." cambia de vista */
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
        const accion = e.target.closest('[data-accion]');
        if (accion) {
            ejecutarAccionSolicitud(accion.dataset.accion, Number(accion.dataset.id));
        }
    });

    /* Dashboard */
    document.getElementById('filtro-actividad').addEventListener('change', cargarActividad);
    document.getElementById('btn-actualizar-actividad').addEventListener('click', cargarDashboard);

    /* Carga de informacion */
    document.getElementById('form-carga').addEventListener('submit', guardarDocumento);
    document.getElementById('form-carga').addEventListener('reset', function () {
        setTimeout(function () {
            mostrarNombreArchivo();
            actualizarVistaPreviaFormato();
            document.getElementById('carga-periodo').value = periodoActual();
        });
    });
    document.getElementById('carga-formato').addEventListener('change', actualizarVistaPreviaFormato);
    document.getElementById('carga-formato-codigo').addEventListener('input', actualizarVistaPreviaFormato);
    document.getElementById('carga-formato-version').addEventListener('input', actualizarVistaPreviaFormato);
    document.getElementById('buscar-documentos').addEventListener('input', pintarDocumentos);
    document.getElementById('filtro-formato').addEventListener('change', pintarDocumentos);
    document.getElementById('carga-periodo').value = periodoActual();
    configurarZonaArchivo();

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

    /* Datos iniciales */
    await cargarCatalogos();
    await Promise.all([cargarDashboard(), cargarDocumentos(), cargarSolicitudes()]);
});
