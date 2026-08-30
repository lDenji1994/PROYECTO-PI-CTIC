package com.certificados.app;

import com.certificados.app.dto.CargaArchivoResponseDTO;
import com.certificados.app.exception.AlmacenamientoException;
import com.certificados.app.model.Documento;
import com.certificados.app.repository.DocumentoRepository;
import com.certificados.app.service.AlmacenamientoService;
import com.certificados.app.service.impl.CargarArchivoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargarArchivoServiceImplTest {

    @Mock
    private AlmacenamientoService almacenamientoService;

    @Mock
    private DocumentoRepository documentoRepository;

    @InjectMocks
    private CargarArchivoServiceImpl cargarArchivoService;

    private MockMultipartFile archivoPdfValido;
    private MockMultipartFile archivoTxtInvalido;

    @BeforeEach
    void setUp() {
        archivoPdfValido = new MockMultipartFile(
                "archivo",
                "syllabus_calculo.pdf",
                "application/pdf",
                "%PDF-1.4 contenido de prueba".getBytes()
        );

        archivoTxtInvalido = new MockMultipartFile(
                "archivo",
                "syllabus.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "contenido docx".getBytes()
        );
    }

    @Test
    void cargarArchivo_DeberiaCargarExitosamente_CuandoEsPdfValido() {
        when(almacenamientoService.guardar(any())).thenReturn("uuid_syllabus_calculo.pdf");
        when(documentoRepository.save(any(Documento.class))).thenAnswer(invocation -> {
            Documento doc = invocation.getArgument(0);
            doc.setId("1");
            return doc;
        });

        CargaArchivoResponseDTO respuesta = cargarArchivoService.cargarArchivo(archivoPdfValido, "SYLLABUS");

        assertNotNull(respuesta);
        assertEquals("1", respuesta.getId());
        assertEquals("syllabus_calculo.pdf", respuesta.getNombreOriginal());
        assertEquals("SYLLABUS", respuesta.getTipoDocumento());
        verify(almacenamientoService, times(1)).guardar(archivoPdfValido);
        verify(documentoRepository, times(1)).save(any(Documento.class));
    }

    @Test
    void cargarArchivo_DeberiaLanzarExcepcion_CuandoFormatoNoEsPdf() {
        AlmacenamientoException excepcion = assertThrows(
                AlmacenamientoException.class,
                () -> cargarArchivoService.cargarArchivo(archivoTxtInvalido, "SYLLABUS")
        );

        assertTrue(excepcion.getMessage().contains("Solo se aceptan archivos en formato PDF"));
        verifyNoInteractions(almacenamientoService);
        verifyNoInteractions(documentoRepository);
    }
}