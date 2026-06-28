package com.example.demo.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusDoacaoConverter implements AttributeConverter<StatusDoacao, String> {

    @Override
    public String convertToDatabaseColumn(StatusDoacao attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name(); // Saves to DB as uppercase (e.g. 'APROVADA')
    }

    @Override
    public StatusDoacao convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        String clean = dbData.trim().toUpperCase();
        switch (clean) {
            case "APROVADA":
            case "APROVADO":
                return StatusDoacao.APROVADA;
            case "REPROVADA":
            case "REPROVADO":
                return StatusDoacao.REPROVADA;
            case "PENDENTE":
                return StatusDoacao.PENDENTE;
            case "AGUARDANDO_CORRECAO":
            case "AGUARDANDO CORREÇÃO":
            case "AGUARDANDO_CORREÇÃO":
                return StatusDoacao.AGUARDANDO_CORRECAO;
            default:
                throw new IllegalArgumentException("Unknown database value for StatusDoacao: " + dbData);
        }
    }
}
