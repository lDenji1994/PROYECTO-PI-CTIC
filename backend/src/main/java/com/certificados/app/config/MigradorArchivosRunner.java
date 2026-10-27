package com.certificados.app.config;

import com.certificados.app.service.AlmacenamientoArchivosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * PASA A LA CARPETA LOS ARCHIVOS QUE TODAVIA ESTAN DENTRO DE MYSQL.
 *
 * Antes de la migracion 09 los documentos cargados y los PDF de los
 * certificados se guardaban en la columna bi_archivo. Al arrancar, este
 * proceso busca los que aun no tienen ruta, los escribe en la carpeta de
 * archivos (AlmacenamientoArchivosService), guarda la ruta y deja
 * bi_archivo en NULL. Se hace uno por uno, asi que si se interrumpe, al
 * siguiente arranque continua donde quedo.
 *
 * Si la columna bi_archivo ya no existe (base nueva o despues del script
 * 10_quitar_archivos_de_la_base.sql) no hace nada.
 */
@Component
public class MigradorArchivosRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(MigradorArchivosRunner.class);

    private final JdbcTemplate jdbc;
    private final AlmacenamientoArchivosService almacenamiento;

    public MigradorArchivosRunner(JdbcTemplate jdbc, AlmacenamientoArchivosService almacenamiento) {
        this.jdbc = jdbc;
        this.almacenamiento = almacenamiento;
    }

    @Override
    public void run(ApplicationArguments args) {
        migrarDocumentos();
        migrarCertificados();
    }

    private void migrarDocumentos() {
        if (!tieneBinarios("versiones_documento")) {
            return;
        }
        List<Map<String, Object>> filas = jdbc.queryForList(
                "SELECT n_idVersionDocumento AS id, t_nombreArchivo AS nombre, c_periodo AS periodo "
                        + "FROM versiones_documento "
                        + "WHERE bi_archivo IS NOT NULL AND (t_rutaArchivo IS NULL OR t_rutaArchivo = '')");
        int movidos = 0;
        for (Map<String, Object> fila : filas) {
            Integer id = ((Number) fila.get("id")).intValue();
            byte[] contenido = jdbc.queryForObject(
                    "SELECT bi_archivo FROM versiones_documento WHERE n_idVersionDocumento = ?", byte[].class, id);
            if (contenido == null) {
                continue;
            }
            String ruta = almacenamiento.guardarDocumento((String) fila.get("periodo"),
                    (String) fila.get("nombre"), contenido);
            jdbc.update("UPDATE versiones_documento SET t_rutaArchivo = ?, bi_archivo = NULL "
                    + "WHERE n_idVersionDocumento = ?", ruta, id);
            movidos++;
        }
        if (movidos > 0) {
            LOG.info("ARCHIVOS: {} documento(s) cargado(s) pasaron de MySQL a la carpeta {}",
                    movidos, almacenamiento.raiz());
        }
    }

    private void migrarCertificados() {
        if (!tieneBinarios("certificados_generados")) {
            return;
        }
        List<Map<String, Object>> filas = jdbc.queryForList(
                "SELECT n_idCertificadoGenerado AS id, t_nombreArchivo AS nombre, dt_fechaGeneracion AS fecha "
                        + "FROM certificados_generados "
                        + "WHERE bi_archivo IS NOT NULL AND (t_rutaArchivo IS NULL OR t_rutaArchivo = '')");
        int movidos = 0;
        for (Map<String, Object> fila : filas) {
            Integer id = ((Number) fila.get("id")).intValue();
            byte[] contenido = jdbc.queryForObject(
                    "SELECT bi_archivo FROM certificados_generados WHERE n_idCertificadoGenerado = ?", byte[].class, id);
            if (contenido == null) {
                continue;
            }
            Object fecha = fila.get("fecha");
            String anio = fecha instanceof Timestamp t
                    ? String.valueOf(t.toLocalDateTime().getYear())
                    : fecha instanceof java.time.LocalDateTime l ? String.valueOf(l.getYear()) : "sin-fecha";
            String ruta = almacenamiento.guardar(AlmacenamientoArchivosService.CERTIFICADOS + "/" + anio,
                    (String) fila.get("nombre"), contenido);
            jdbc.update("UPDATE certificados_generados SET t_rutaArchivo = ?, bi_archivo = NULL "
                    + "WHERE n_idCertificadoGenerado = ?", ruta, id);
            movidos++;
        }
        if (movidos > 0) {
            LOG.info("ARCHIVOS: {} certificado(s) PDF pasaron de MySQL a la carpeta {}",
                    movidos, almacenamiento.raiz());
        }
    }

    /** ¿La tabla todavia tiene la columna bi_archivo? */
    private boolean tieneBinarios(String tabla) {
        Integer columnas = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = 'bi_archivo'",
                Integer.class, tabla);
        return columnas != null && columnas > 0;
    }
}
