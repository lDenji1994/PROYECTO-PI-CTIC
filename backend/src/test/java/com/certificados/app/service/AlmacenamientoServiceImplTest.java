package com.certificados.app.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.certificados.app.model.Documento;
import com.certificados.app.model.TipoDocumento;
import com.certificados.app.service.impl.AlmacenamientoServiceImpl;

public class AlmacenamientoServiceImplTest {

    @Test
    public void probarGuardadoArchivo() {
        AlmacenamientoServiceImpl servicio = new AlmacenamientoServiceImpl("uploads_test");
        servicio.inicializarEstructura();

        String contenidoPrueba = "Prueba de almacenamiento de documentos.";
        InputStream stream = new ByteArrayInputStream(contenidoPrueba.getBytes());

        Documento doc = servicio.guardarArchivo(stream, "syllabus_prueba.pdf", "CERTIFICADO");

        Assertions.assertNotNull(doc.getId());
        Assertions.assertEquals("syllabus_prueba.pdf", doc.getNombreOriginal());
        Assertions.assertEquals(TipoDocumento.CERTIFICADO, doc.getTipoDocumento());
        Assertions.assertTrue(Files.exists(Paths.get(doc.getRutaArchivo())));
    }
}