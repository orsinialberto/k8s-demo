package com.example.demo.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static JsonNode convertToJsonNode(final String content) {
        try {
            return OBJECT_MAPPER.readTree(content);
        } catch (JsonProcessingException e) {
            throw new JsonConverterException(e);
        }
    }

    public static String convertToString(final Object content) {
        try {
            return OBJECT_MAPPER.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new JsonConverterException(e);
        }
    }

    public static <T> T convertToObject(final String content, final Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(content, valueType);
        } catch (final JsonProcessingException e) {
            throw new JsonConverterException(e);
        }
    }
}
