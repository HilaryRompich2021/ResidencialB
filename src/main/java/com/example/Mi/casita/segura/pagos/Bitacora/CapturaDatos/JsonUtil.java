package com.example.Mi.casita.segura.pagos.Bitacora.CapturaDatos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

@Component
public class JsonUtil {
    //Para la serialización de objetos a JSON. Capturando datos nuevos y datos Anteriores como txt Json

    private final ObjectMapper mapper;

    public JsonUtil() {
        this.mapper = new ObjectMapper();

        // Registro del módulo para Java 8 fechas
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        //Para que genere Json legible
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public String toJson(Object o){
        try{
            return mapper.writeValueAsString(o);
        } catch (Exception e){
            //e.printStackTrace();
            System.out.println("ERROR SERIALIZANDO: " + o.getClass().getSimpleName());
            return "{\\\"error\\\":\\\"no pudo serializar a JSON\\\"}";
        }

    }

    /**
     * Deserializa un JSON a una clase concreta.
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            System.err.println("ERROR DESERIALIZANDO a " + clazz.getSimpleName());
            return null;
        }
    }

    /**
     * Deserializa un JSON a un tipo genérico (por ejemplo, Map o List).
     */
    public <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return mapper.readValue(json, typeRef);
        } catch (Exception e) {
            System.err.println("ERROR DESERIALIZANDO a tipo genérico");
            return null;
        }
    }

}
