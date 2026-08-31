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
    void deberiaCargarSyllabusYCartaDescriptiva() throws Exception {
        MockMultipartFile archivoSyllabus = new MockMultipartFile(
                "archivo",
                "syllabus_sistemas.pdf",
                "application/pdf",
                "Contenido del syllabus".getBytes()
        );

        MockMultipartFile archivoCarta = new MockMultipartFile(
                "archivo",
                "carta_descriptiva.pdf",
                "application/pdf",
                "Contenido de la carta".getBytes()
        );

        mockMvc.perform(multipart("/api/archivos/cargar")
                        .file(archivoSyllabus))
                .andExpect(status().isOk());

        mockMvc.perform(multipart("/api/archivos/cargar")
                        .file(archivoCarta))
                .andExpect(status().isOk());
    }
}