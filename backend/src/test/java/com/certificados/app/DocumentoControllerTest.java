package com.certificados.app;

import com.certificados.app.controller.DocumentoController;
import com.certificados.app.model.Documento;
import com.certificados.app.service.DocumentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentoController.class)
class DocumentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentoService documentoService;

    @Test
    void deberiaRegistrarDocumento() throws Exception {
        MockMultipartFile archivoMock = new MockMultipartFile(
                "archivo",
                "acta_grado.pdf",
                "application/pdf",
                "Contenido de prueba".getBytes()
        );

        Documento documentoMock = new Documento(
                "uuid-1234-5678",
                "acta_grado.pdf",
                "uploads/uuid-1234-5678.pdf",
                "application/pdf",
                1024L,
                LocalDateTime.now()
        );

        given(documentoService.registrarDocumento(any())).willReturn(documentoMock);

        mockMvc.perform(multipart("/api/documentos/registrar")
                        .file(archivoMock))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("uuid-1234-5678"))
                .andExpect(jsonPath("$.nombreOriginal").value("acta_grado.pdf"))
                .andExpect(jsonPath("$.tamanoBytes").value(1024));
    }
}