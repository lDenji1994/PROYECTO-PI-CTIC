package com.certificados.app.dto;

import java.util.List;

public class ConsultaKardexRequestDTO {

    private List<String> codigos;

    public ConsultaKardexRequestDTO() {
    }

    public ConsultaKardexRequestDTO(List<String> codigos) {
        this.codigos = codigos;
    }

    public List<String> getCodigos() {
        return codigos;
    }

    public void setCodigos(List<String> codigos) {
        this.codigos = codigos;
    }
}