# Parte comun del proxy (lo incluye nginx-certificados.conf).
# Copiar a /etc/nginx/conf.d/certificados-proxy.inc
# NO MODIFICAR las cabeceras X-Forwarded-*: la aplicacion las usa para
# saber la IP real del usuario y que la conexion es HTTPS.
proxy_pass http://127.0.0.1:8080;
proxy_set_header Host              $host;
proxy_set_header X-Real-IP         $remote_addr;
proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
proxy_set_header X-Forwarded-Proto $scheme;
proxy_set_header X-Forwarded-Port  $server_port;
proxy_read_timeout 120s;
