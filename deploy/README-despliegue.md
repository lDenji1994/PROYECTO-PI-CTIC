# Despliegue en producción (Red Hat Enterprise Linux 10)

Dos servidores: **aplicación** (Java + Nginx) y **base de datos** (MySQL 8.4).
Todo lo de esta carpeta son plantillas: reemplazar `DOMINIO_DE_LA_WEB`, `IP_SERVIDOR_BD`,
`IP_SERVIDOR_APP` y las contraseñas. **Nunca** subir a GitHub los archivos ya editados.

## 1. Servidor de base de datos

```bash
sudo dnf install mysql8.4-server        # o el paquete MySQL 8.4 que indique CTIC
```

En `/etc/my.cnf.d/certificados.cnf` (ANTES de iniciar MySQL por primera vez):

```ini
[mysqld]
lower_case_table_names = 1          # obligatorio: sin esto la app no encuentra las tablas
max_allowed_packet      = 32M       # documentos de hasta 20 MB
binlog_expire_logs_seconds = 259200 # binary logs de 3 días, para no llenar el disco
bind-address            = IP_SERVIDOR_BD
require_secure_transport = ON       # solo conexiones cifradas
```

```bash
sudo systemctl enable --now mysqld
mysql -u root -p < database/01_base_certificados.sql
mysql -u root -p < database/02_datos_iniciales.sql
# Editar IP_SERVIDOR_APP y la contraseña en una COPIA de este archivo y ejecutarla:
mysql -u root -p < 03_usuario_aplicacion_produccion.sql

# Firewall: MySQL solo para el servidor de aplicación
sudo firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="IP_SERVIDOR_APP" port port="3306" protocol="tcp" accept'
sudo firewall-cmd --reload
```

Respaldo diario (cron) con `mysqldump --single-transaction`, guardado **fuera** de este disco.

## 2. Servidor de aplicación

```bash
sudo dnf install java-21-openjdk-headless nginx
sudo useradd --system --home /opt/certificados --shell /sbin/nologin certificados
sudo mkdir -p /opt/certificados /etc/certificados /var/log/certificados
sudo chown certificados:certificados /var/log/certificados
```

1. **Programa:** en un PC del equipo, `cd backend` y `./gradlew bootJar`
   (o `.\gradlew.bat bootJar`). Copiar `backend/build/libs/certificados-app.jar`
   a `/opt/certificados/`. El JAR no lleva las credenciales locales de quien lo compiló.
2. **Credenciales:** copiar `deploy/certificados.properties.example` a
   `/etc/certificados/certificados.properties`, completarlo y protegerlo:
   ```bash
   sudo chown root:certificados /etc/certificados/certificados.properties
   sudo chmod 640 /etc/certificados/certificados.properties
   ```
3. **Servicio:** copiar `deploy/certificados.service` a `/etc/systemd/system/` y:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable --now certificados
   sudo journalctl -u certificados -f      # ver que arranque (Started CertificadosAppApplication)
   ```
4. **Nginx + HTTPS:** copiar `deploy/nginx-certificados.conf` y `deploy/certificados-proxy.inc`
   a `/etc/nginx/conf.d/`, poner el dominio y el certificado SSL, y:
   ```bash
   sudo setsebool -P httpd_can_network_connect 1   # SELinux: Nginx -> aplicación
   sudo nginx -t && sudo systemctl enable --now nginx
   sudo firewall-cmd --permanent --add-service=http --add-service=https
   sudo firewall-cmd --reload
   ```
5. Entrar a `https://DOMINIO_DE_LA_WEB` con el administrador inicial, **cambiar su
   contraseña en Mi cuenta** y borrar la línea `app.seguridad.admin-inicial.contrasena`
   del archivo de credenciales.

## Qué hace el perfil `prod` (application-prod.properties)

| Ajuste | Para qué |
|---|---|
| `server.address=127.0.0.1` | Solo Nginx puede hablar con la aplicación |
| `server.forward-headers-strategy=native` | IP real del usuario (bloqueo por intentos y bitácora) y detección de HTTPS |
| `server.servlet.session.cookie.secure=true` | Cookies de sesión y CSRF solo por HTTPS |
| `spring.web.error.include-message=never` | No mostrar detalles internos en errores |
| Actuator solo `health` | No exponer información del servidor |
| Logs en `/var/log/certificados` con rotación | Que los logs no llenen el disco |

La conexión a MySQL va cifrada (`sslMode=REQUIRED`) con un usuario que solo puede
leer y escribir datos (`03_usuario_aplicacion_produccion.sql`).

## Actualizar a una versión nueva

```bash
# 1. Si la versión trae migraciones nuevas (database/0X_migracion_*.sql), ejecutarlas en el servidor de BD
# 2. Reemplazar el JAR y reiniciar
sudo cp certificados-app.jar /opt/certificados/
sudo systemctl restart certificados
```
