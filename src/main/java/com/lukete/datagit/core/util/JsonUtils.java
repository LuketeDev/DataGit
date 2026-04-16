package com.lukete.datagit.core.util;

import org.springframework.core.serializer.support.SerializationFailedException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Utility class for JSON serialization.
 */
public class JsonUtils {
    private JsonUtils() {
        /* This utility class should not be instantiated */
    }

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT); // pretty print

    /**
     * Serializes the provided object into an indented JSON string.
     *
     * @param obj the object to serialize
     * @return the serialized JSON representation
     */
    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new SerializationFailedException("Failed to serialize object to JSON", e);
        }
    }
}
