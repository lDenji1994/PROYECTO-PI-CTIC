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
│   └── 02_datos_iniciales.sql                   ← usuario admin, tipos, plantilla PDF mínima, ejemplos
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
│       │   └── config/       ← CORS, nombres de tablas, redirección del panel
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

El paso 2 crea el usuario `admin` (id 1), que es a nombre de quien se registra la
**bitácora / actividad reciente** mientras no exista login. Sin ningún usuario en la
tabla de usuarios la bitácora no se guarda (la tabla Logs exige un usuario).

### 3.2 Tus credenciales (sin subirlas a GitHub)

```bash
cd backend/src/main/resources
copy application-local.properties.example application-local.properties     # Windows
# cp application-local.properties.example application-local.properties     # Mac/Linux
```

Edita `application-local.properties` con tu puerto (3306 o 3307), usuario y contraseña.
Ese archivo está en `.gitignore`: **nunca se sube**.

Alternativa sin archivo: variables de entorno `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

### 3.3 Levantar el backend

```bash
cd backend
gradle bootRun
```

`application-local.properties` se carga automáticamente si existe (no hace falta activar perfiles).

### 3.4 Abrir el panel

<http://localhost:8080> — el punto de la barra superior se pone rojo si el panel no logra
comunicarse con el backend.

### 3.5 Prueba rápida de extremo a extremo

1. **Contenido de cursos** → registrar asignatura `ISIS` + `0202` + "Bases de Datos".
2. **Carga de información** → elegir la asignatura, tipo "Carta Descriptiva", formato
   `DA-FO-085N v03` (vigente) y subir un PDF. Repetir con otra asignatura usando
   `SYLLABUS v1` → aparece como **Desactualizado**.
3. **Certificaciones** → ID estudiante, tipo de certificado, encargado, marcar
   asignaturas → *Crear solicitud* → *Generar PDF* → *Ver PDF*.
4. **Dashboard** → los contadores y la *Actividad reciente* muestran todo lo anterior.

---

## 4. Qué hace cada módulo del panel

| Módulo | Qué hace | API que usa |
|---|---|---|
| **Dashboard** | Contadores reales, aviso de formatos desactualizados y **actividad reciente** (bitácora). Clic en una tarjeta → historial del módulo. | `/api/actividades`, `/api/documentos-academicos/resumen`, `/api/solicitudes-certificados` |
| **Carga de información** | Sube el PDF de una asignatura (materia + curso), tipo y formato. Muestra en vivo si el formato es **vigente** o **desactualizado**. Tabla con todas las cargas. | `POST /api/documentos-academicos/cargar`, `/api/formatos` |
| **Contenido de cursos** | Registrar asignaturas (código de **materia** + código de **curso**) y programas. Tarjetas por asignatura con el estado de sus formatos e historial de versiones. | `/api/asignaturas`, `/api/programas` |
| **Certificaciones** | Crear solicitudes con asignaturas, cambiar su estado, generar y ver el PDF. Vista previa con los documentos y la vigencia de cada formato. | `/api/solicitudes-certificados`, `/api/certificados-generados` |

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
SYLLABUS / CONTENIDOS / PROGRAMA  → formatos antiguos
```

Cuando salga una versión nueva de la carta descriptiva: agregarla como `VIGENTE` y pasar
la anterior a `ANTIGUO`. Todo documento con otro formato pasa a verse como **Desactualizado**.

### Actividad reciente (bitácora)

Se guarda en la tabla `..._LogS` del esquema (IP, usuario, tabla, proceso, fechas).
Se registra automáticamente al: crear programa/asignatura, crear documento, cambiar su
formato, cargar versión (en amarillo si el formato es viejo), crear solicitud, agregar
asignaturas, cambiar estado y generar el certificado. Ver `service/ActividadService.java`.

---

## 5. Endpoints nuevos o modificados en esta versión

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/actividades?modulo=&limite=` | Actividad reciente legible (módulos: documentos, cursos, certificaciones) |
| GET | `/api/formatos` | Catálogo de formatos con cuál es el vigente |
| GET | `/api/documentos-academicos/resumen` | Documentos con materia/curso, tipo, vigencia y versiones (sin binarios) |
| POST | `/api/documentos-academicos/cargar` | Multipart: crea/reutiliza el documento y guarda el PDF como versión nueva |
| GET/POST | `/api/asignaturas` | Ahora con `codigoMateria` y `codigoCurso` (acepta el `codigo` anterior) |
| GET | `/api/usuarios` | Usuarios activos (sin contraseña) para elegir encargado |
| GET | `/api/certificados-generados/solicitud/{id}/archivo` | Abre el PDF del certificado |
| GET | `/api/solicitudes-certificados/{id}/asignaturas` | Ahora devuelve materia/curso separados |

Los demás endpoints del equipo (plantillas, secciones, campos, logs, etc.) no cambiaron.

---

## 6. Seguridad aplicada (no quitar)

* **Sin credenciales en el código**: `application.properties` usa variables / perfil `local`.
* **SQL injection**: todo acceso es por Spring Data JPA (consultas parametrizadas), sin SQL armado a mano.
* **XSS**: el panel escapa todo texto del servidor (`escaparTexto`) y no usa `onclick` con datos.
* **Archivos**: solo PDF reales (se revisa la firma `%PDF`, no solo la extensión), máx. 20 MB,
  nombre de archivo limpiado y cabecera de descarga escapada (evita inyección de cabeceras).
* **Errores**: el navegador recibe mensajes claros; las trazas internas solo salen en la consola.
* **Usuarios**: `/api/usuarios` nunca devuelve contraseñas.

---

## 7. Notas importantes para el equipo

1. **Contraseña expuesta**: la versión anterior de `application.properties` en GitHub tenía la
   contraseña de MySQL de un integrante. Ya se quitó, pero **sigue en el historial de git**:
   esa persona debe **cambiar su contraseña de MySQL**.
2. **Nombres de tablas en Mac/Linux**: `LowerCaseTableNamingStrategy` pasa los nombres de
   tabla a minúsculas (en Windows MySQL ya los guarda así). En Mac/Linux, MySQL distingue
   mayúsculas: hay que crear la BD con `lower_case_table_names=1` o no funcionará.
3. `ddl-auto=validate`: Hibernate **no** crea ni cambia tablas; el esquema manda. Si se cambia
   el SQL, hay que ajustar la entidad correspondiente en `model/`.
4. `backend/bin/` (compilación del IDE) se eliminó del repo y quedó en `.gitignore`.
5. El login (`login.html`) sigue siendo solo visual: falta Spring Security + JWT. Mientras
   tanto, la bitácora usa `app.auditoria.id-usuario-por-defecto`.

---

## 8. Próximos pasos sugeridos

* Login real (Spring Security + BCrypt + JWT) y usar el usuario autenticado en la bitácora.
* Extraer automáticamente los datos de la carta descriptiva (horas, créditos, CINE…) al cargarla
  (las columnas ya existen en `VersionesDocumentosS`).
* Pantalla para administrar plantillas de certificado desde el panel.
