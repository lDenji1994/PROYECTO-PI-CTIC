-- =====================================================================
--  USUARIO DE BASE DE DATOS PARA LA APLICACION (solo SERVIDOR / produccion)
--
--  Ejecutar UNA vez en el servidor de base de datos, como administrador
--  de MySQL, DESPUES de 01 y 02 (y de las migraciones 04...).
--
--  Por que: la aplicacion NO debe conectarse con root. Este usuario solo
--  puede leer y escribir datos (no borrar tablas ni cambiar el esquema)
--  y solo desde la IP del servidor de aplicacion, con conexion cifrada.
--
--  ANTES de ejecutar, reemplazar:
--    IP_SERVIDOR_APP         -> IP del servidor de aplicacion
--    CONTRASENA_SEGURA_AQUI  -> una contrasena larga y aleatoria
--  La misma contrasena va en /etc/certificados/certificados.properties
--  del servidor de aplicacion. NO guardar este archivo ya editado en git.
--
--  En tu PC (desarrollo) NO hace falta: alli se sigue usando tu usuario.
-- =====================================================================

CREATE USER IF NOT EXISTS 'certificados_app'@'IP_SERVIDOR_APP'
    IDENTIFIED BY 'CONTRASENA_SEGURA_AQUI'
    REQUIRE SSL;

GRANT SELECT, INSERT, UPDATE, DELETE
    ON CertificadosCursosAcademicosUPB.*
    TO 'certificados_app'@'IP_SERVIDOR_APP';

FLUSH PRIVILEGES;

-- Comprobar:
-- SHOW GRANTS FOR 'certificados_app'@'IP_SERVIDOR_APP';
