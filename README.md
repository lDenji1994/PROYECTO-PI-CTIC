# Certificados de Cursos Académicos UPB (PROYECTO-PI-CTIC)

Panel web (HTML/CSS/JS) + backend Spring Boot + MySQL, todo en **un solo servidor**:
Spring Boot se conecta a MySQL y además sirve el panel. No hace falta Node.js ni Angular.

```
Navegador ──► http://localhost:8080  (Spring Boot)
                 ├── /            → panel (backend/src/main/resources/static)
                 └── /api/...     → API REST  ──►  MySQL (CertificadosCursosAcademicosUPB)
```

---

## 1. Estructura

```
PROYECTO-PI-CTIC/
├── database/
│   ├── 01_esquema_BASE_DE_DATOS_PI_TRACK1.sql   ← esquema oficial del equipo (sin cambios)
│   └── 02_datos_iniciales.sql                   ← usuario admin (sin contraseña), tipos, plantilla PDF mínima, ejemplos
├── backend/
│   ├── build.gradle
│   └── src/main/
│       ├── java/com/certificados/app/
│       │   ├── controller/   ← endpoints REST (/api/...)
│       │   ├── service/      ← lógica de negocio y validaciones
│       │   ├── repository/   ← acceso a datos (Spring Data JPA)
│       │   ├── model/        ← entidades = tablas del script SQL
│       │   ├── dto/          ← lo que se envía/recibe en JSON
│       │   ├── exception/    ← errores en JSON uniforme {"messages": [...]}
│       │   ├── security/     ← login: usuario de la sesión, admin inicial, límite de intentos
│       │   └── config/       ← reglas de seguridad por rol, CORS, nombres de tablas
│       └── resources/
│           ├── application.properties                 ← configuración (SIN contraseñas)
│           ├── application-local.properties.example   ← copia para tus credenciales
│           └── static/        ← EL PANEL (index.html, app.js, styles.css, login.*)
├── frontend-panel/           ← copia del panel para GitHub Pages (solo visual, sin backend)
└── build-monolito.sh         ← genera el JAR final
```

> **La fuente del panel es `backend/src/main/resources/static/`.** Si editas `frontend-panel/`,
> copia los cambios a `static/` (o al revés) para que no se desincronicen.

---

## 2. Requisitos

| Herramienta | Versión |
|---|---|
| JDK | **21** (lo exige `build.gradle`) |
| Gradle | 8.x (o el wrapper `gradlew` del proyecto) |
| MySQL | 8.x |
| IDE sugerido | IntelliJ IDEA o VS Code con Extension Pack for Java |

---

## 3. Paso a paso para correrlo en local

### 3.1 Base de datos

En MySQL Workbench (o consola), ejecutar **en este orden**:

1. `database/01_esquema_BASE_DE_DATOS_PI_TRACK1.sql` (crea la BD y las tablas).
   Si la BD ya existe, sáltalo.
2. `database/02_datos_iniciales.sql` (se puede correr varias veces sin duplicar).

El paso 2 crea el usuario `admin` **sin contraseña**. La contraseña se la asigna el
servidor la primera vez que arranca (paso 3.2). Esta versión **no cambia el esquema**:
si ya tenías la base de datos creada no hay que volver a correr nada.

### 3.2 Tus credenciales (sin subirlas a GitHub)

```bash
cd backend/src/main/resources
copy application-local.properties.example application-local.properties     # Windows
# cp application-local.properties.example application-local.properties     # Mac/Linux
```

Edita `application-local.properties` con:

* tu puerto de MySQL (3306 o 3307), usuario y contraseña;
* la **contraseña del administrador del panel**:

```properties
app.seguridad.admin-inicial.usuario=admin
app.seguridad.admin-inicial.contrasena=   ← escribe aquí la tuya (mín. 8, con letras y números)
```

Ese archivo está en `.gitignore`: **nunca se sube**. `application.properties` (el que sí se
sube) ya **no trae ninguna contraseña por defecto**, ni de MySQL ni del panel.

Alternativa sin archivo: variables de entorno `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

### 3.3 Levantar el backend

```bash
cd backend
.\gradlew.bat bootRun        # Windows
# ./gradlew bootRun          # Mac/Linux
```

`application-local.properties` se carga automáticamente si existe. En la consola debe
aparecer `LOGIN: administrador inicial 'admin' listo para iniciar sesion.` (solo la primera vez).

### 3.4 Entrar al panel

<http://localhost:8080> → pide iniciar sesión → usuario `admin` y la contraseña del paso 3.2.

Después, en el menú **Usuarios**, el administrador crea las cuentas de las auxiliares
(máximo 4 activas; se cambia con `app.seguridad.max-auxiliares`). Cada quien puede cambiar
su contraseña en **Mi cuenta**.

> ¿El administrador olvidó su contraseña? Pon una nueva en `application-local.properties`,
> agrega `app.seguridad.admin-inicial.forzar=true`, reinicia el servidor y luego quita esa línea.

### 3.5 Prueba rápida de extremo a extremo

1. **Contenido de cursos** → registrar asignatura `ISIS` + `0202` + "Bases de Datos".
2. **Carga de información** → elegir la asignatura, tipo "Carta Descriptiva", formato
   `DA-FO-085N v03`, **pegar la descripción del curso y el contenido (un tema por línea)**,
   escribir créditos y horas, y subir el archivo (PDF, Word o Excel).
3. **Certificaciones** → ID estudiante, tipo de certificado, marcar asignaturas →
   *Crear solicitud*. La vista previa muestra lo que dirá el certificado y qué asignaturas
   están incompletas → *Generar PDF* → *Ver PDF*.
4. **Dashboard** → los contadores y la *Actividad reciente* muestran todo lo anterior.

---

## 4. Qué hace cada módulo del panel

| Módulo | Qué hace | API que usa |
|---|---|---|
| **Login** | Usuario y contraseña contra la tabla de usuarios (hash BCrypt). Sesión por cookie. | `/api/auth/*` |
| **Dashboard** | Contadores reales, aviso de formatos desactualizados y **actividad reciente** (la auxiliar ve la suya; el administrador, la de todos). | `/api/actividades`, `/api/documentos-academicos/resumen`, `/api/solicitudes-certificados` |
| **Carga de información** | Sube el documento (PDF, Word, Excel) y registra **a mano** los datos del curso. Permite corregir los datos o reemplazar el archivo después. | `POST /api/documentos-academicos/cargar`, `/api/versiones-documentos/{id}/datos`, `/api/versiones-documentos/{id}/archivo` |
| **Contenido de cursos** | Registrar asignaturas (código de **materia** + código de **curso**) y programas (solo administrador). | `/api/asignaturas`, `/api/programas` |
| **Certificaciones** | Solicitudes con asignaturas, vista previa del contenido, generar y ver **un solo PDF** con todas las asignaturas. | `/api/solicitudes-certificados`, `/api/certificados-generados` |
| **Usuarios** (solo administrador) | Crear cuentas, activar/desactivar, asignar contraseña. | `/api/usuarios` |
| **Mi cuenta** | Cambiar la contraseña propia. | `POST /api/auth/cambiar-contrasena` |

### Quién puede hacer qué

| Acción | Auxiliar | Administrador |
|---|---|---|
| Cargar documentos y registrar / corregir datos del curso | ✔ | ✔ |
| Registrar asignaturas | ✔ | ✔ |
| Crear solicitudes y generar certificados | ✔ | ✔ |
| Ver la actividad | solo la suya | la de todos |
| Registrar programas | ✘ | ✔ |
| Crear usuarios, activar/desactivar, asignar contraseñas | ✘ | ✔ |
| Cambiar plantillas del certificado (API) | ✘ | ✔ |

Las reglas están en `config/SecurityConfig.java` (el panel solo oculta los botones; quien
decide es el backend).

### Datos del curso: todo manual (sin lectura automática de documentos)

El sistema **no lee** el contenido del PDF/Word/Excel. La auxiliar copia y pega desde la
carta descriptiva o el syllabus:

| Campo | ¿Obligatorio? | Dónde se guarda |
|---|---|---|
| Descripción del curso | Sí | `VersionesDocumentosS.t_descripcion` |
| Detalle de contenido (un tema por línea) | Sí | `ContenidosS` (una fila por tema, con orden) |
| Créditos | Sí | `VersionesDocumentosS` |
| Horas teóricas / prácticas / laboratorio / independientes | Al menos una > 0 | `VersionesDocumentosS` |
| Escuela, facultad, CINE, NBC, ciclo, nivel, requisitos, propósito, justificación, modalidades, observaciones | No (sección plegable) | `VersionesDocumentosS` |

La lista de campos del formulario está en `static/app.js` → `CAMPOS_CURSO`, y en el backend
en `dto/DatosCursoDTO.java` + `service/VersionDocumentoService.java`.

### Si la carga de un documento falla

* Lo escrito **no se pierde**: el formulario no se limpia y además se guarda un borrador en
  el navegador (se recupera aunque se recargue la página). El archivo sí hay que volver a elegirlo.
* Aparece un aviso rojo con el motivo y el botón **Reintentar**.
* Si el documento quedó cargado pero con el archivo equivocado o datos mal escritos: botón
  **Corregir** en la tabla → *Guardar datos del curso* o *Reemplazar archivo*.
* En el servidor la carga es una sola transacción: o queda todo guardado o no queda nada.

### Archivos aceptados

`pdf, docx, doc, xlsx, xlsm, xls` (propiedad `app.documentos.extensiones-permitidas`), máx. 20 MB.
Se valida la extensión **y** los primeros bytes del archivo (un `.exe` renombrado a `.docx`
se rechaza). Los PDF se abren en el navegador; Word y Excel se descargan.

### Certificado de contenidos resumidos

Un solo PDF por solicitud: encabezado de la plantilla (título, estudiante, solicitud) y,
debajo, un bloque por asignatura con código, nombre, créditos, horas, descripción y contenido;
al final, fecha de expedición y firma. Si a alguna asignatura le falta documento, descripción,
contenido o créditos, **no se genera** y se indica qué falta (la solicitud no cambia de estado).
De cada asignatura se toma la versión con datos completos, de formato vigente y más reciente
(`service/ContenidoCertificadoService.java`). El diseño del cuerpo está en
`service/CertificadoContenidosPdf.java`; ciudad y cargo de la firma en
`app.certificados.ciudad` / `app.certificados.cargo-firma`.

### Código de materia + código de curso

En las plantillas institucionales el código de una asignatura tiene dos partes, p. ej.
`FION 0001` → materia `FION`, curso `0001`. La BD **no cambió**: se guarda en la columna
`c_codigo` como `"MATERIA CURSO"` y el backend lo separa (`AsignaturaDTO.codigoMateria` /
`codigoCurso`). Asignaturas antiguas tipo `SIS00001` se separan como `SIS` + `00001`.

### Formato vigente / desactualizado

Se configura en `application.properties` → `app.formatos.catalogo` (no está quemado en código):

```
CODIGO:VERSION:Nombre visible:VIGENTE|ANTIGUO ; ...
DA-FO-085N:03:Carta descriptiva del curso:VIGENTE
DA-FO-763:2:Carta descriptiva del curso (version 2):ANTIGUO
```

Cuando salga una versión nueva de la carta descriptiva: agregarla como `VIGENTE` y pasar
la anterior a `ANTIGUO`.

### Actividad reciente (bitácora)

Se guarda en la tabla `..._LogS` (IP, usuario, tabla, proceso, fechas), **a nombre de quien
tiene la sesión iniciada**. Ver `service/ActividadService.java`.

---

## 5. Endpoints nuevos o modificados

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/login` | `{usuario, contrasena}` → inicia sesión (cookie) |
| GET | `/api/auth/yo` | Quién tiene la sesión (401 si nadie) |
| POST | `/api/auth/logout` | Cierra la sesión |
| POST | `/api/auth/cambiar-contrasena` | `{actual, nueva}` |
| GET | `/api/usuarios` | Administrador: todos. Auxiliar: solo activos (nunca contraseñas) |
| POST | `/api/usuarios` | *(admin)* crea usuario `{nombreCompleto, usuario, correo, contrasena, rol}` |
| PATCH | `/api/usuarios/{id}/estado?activo=` | *(admin)* activar / desactivar |
| PATCH | `/api/usuarios/{id}/contrasena` | *(admin)* `{nueva}` |
| GET | `/api/configuracion` | Extensiones permitidas y tamaño máximo |
| POST | `/api/documentos-academicos/cargar` | Multipart: archivo + datos del curso (campos de `DatosCursoDTO`) |
| GET / PUT | `/api/versiones-documentos/{id}/datos` | Leer / corregir los datos del curso |
| POST | `/api/versiones-documentos/{id}/archivo` | Reemplazar el archivo de una versión |
| GET | `/api/solicitudes-certificados/{id}/contenido` | Lo que dirá el certificado, por asignatura, y qué falta |
| GET | `/api/actividades?modulo=&limite=` | Actividad (módulos: documentos, cursos, certificaciones, usuarios) |
| GET | `/api/formatos` | Catálogo de formatos con cuál es el vigente |
| GET | `/api/documentos-academicos/resumen` | Documentos con materia/curso, vigencia y versiones (sin binarios) |

Todas las rutas `/api/**` (menos login/logout) exigen sesión: sin ella responden `401`.

---

## 6. Seguridad aplicada (no quitar)

* **Sin credenciales en el código ni en el repositorio**: ni la de MySQL ni la del panel.
  Los usuarios `{noop}` que había en `SecurityConfig` se eliminaron.
* **Contraseñas con hash BCrypt**; nunca se devuelven en la API ni se escriben en consola.
* **Bloqueo por intentos**: 5 fallos seguidos bloquean 5 minutos (`app.seguridad.*`).
* **Sesión**: cookie `HttpOnly` + `SameSite=Strict`, caduca a los 60 min sin uso; si el
  administrador desactiva a alguien o le cambia la contraseña, su sesión abierta se cierra.
* **Permisos por rol en el backend** (`SecurityConfig`), no solo en el panel.
* **SQL injection**: todo acceso es por Spring Data JPA (consultas parametrizadas).
* **XSS**: el panel escapa todo texto del servidor (`escaparTexto`) y no usa `onclick` con datos.
* **Archivos**: extensión + firma del archivo, máx. 20 MB, se guardan en la BD y nunca se
  ejecutan; se descargan con `X-Content-Type-Options: nosniff`.
* **Errores**: el navegador recibe mensajes claros; las trazas internas solo salen en la consola.

---

## 7. Notas importantes para el equipo

1. **`application.properties` ya no trae contraseña de MySQL por defecto.** Cada integrante
   debe tener su `application-local.properties` (paso 3.2) o no conectará.
2. **Contraseñas viejas en el historial de git** (MySQL de un integrante y `admin123` /
   `auxiliar123` del `SecurityConfig` anterior): no reutilizarlas en ningún lado.
3. **Nombres de tablas en Mac/Linux**: hay que crear la BD con `lower_case_table_names=1`.
4. `ddl-auto=validate`: Hibernate **no** crea ni cambia tablas; el esquema manda.
5. Si en tu BD existe el usuario `auxiliar` de una versión anterior del script 02, cuenta
   como una de las 4 auxiliares: asígnale contraseña desde **Usuarios** o desactívalo.
6. Documentos cargados antes de esta versión aparecen con datos *Por completar*: usar
   **Corregir** para registrarlos.

---

## 8. Próximos pasos sugeridos

* Plantilla institucional del certificado (logo, membrete, firma real).
* Kárdex del estudiante (qué asignaturas cursó) para armar la solicitud.
* Lectura automática de la carta descriptiva (se descartó en esta entrega por tiempo).
* Pantalla para administrar plantillas de certificado desde el panel.
* Token CSRF si el panel llega a servirse desde otro dominio distinto al del backend.
