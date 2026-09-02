package com.certificados.app;

import com.certificados.app.controller.FileStorageController;
import com.certificados.app.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileStorageController.class)
class DocumentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileStorageService fileStorageService;

    @Test
    void deberiaAsociarArchivoADocumento() throws Exception {
        MockMultipartFile archivoModalidad = new MockMultipartFile(
                "archivo",
                "documento_original.pdf",
                "application/pdf",
                "Contenido del documento original".getBytes()
        );

        mockMvc.perform(multipart("/api/archivos/asociar")
                        .file(archivoModalidad)
                        .param("documentoId", "100"))
                .andExpect(status().isOk());
    }
}