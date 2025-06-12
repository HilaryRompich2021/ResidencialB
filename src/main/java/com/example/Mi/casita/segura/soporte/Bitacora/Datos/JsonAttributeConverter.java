package com.example.Mi.casita.segura.soporte.Bitacora.Datos;

import com.example.Mi.casita.segura.pagos.Bitacora.CapturaDatos.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
// org.postgresql.util.PGobject;

@Converter(autoApply = false)
public class JsonAttributeConverter implements AttributeConverter<Object, String> {

    // Mapper configurado para Java 8 dates, indentado, etc.
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error serializando a JSON", e);
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        // Si no lo necesitas deserializar, deja esto en null o devuelve dbData
        return dbData;
    }
}
