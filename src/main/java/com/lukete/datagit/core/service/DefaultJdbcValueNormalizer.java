package com.lukete.datagit.core.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukete.datagit.config.exception.DatabaseNormalizationException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultJdbcValueNormalizer implements JdbcValueNormalizer {
    private final ObjectMapper objectMapper;

    @Override
    public Object normalize(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof PGobject pg) {
            return normalizePGObject(pg);
        }

        if (value instanceof Timestamp ts) {
            return ts.toInstant().toString();
        }

        if (value instanceof UUID uuid) {
            return uuid.toString();
        }

        if (value instanceof BigDecimal bd) {
            return bd.stripTrailingZeros().toPlainString();
        }

        if (value instanceof Map<?, ?> map) {
            return normalizeMap(map);
        }

        if (value instanceof List<?> list) {
            return normalizeList(list);
        }

        return value;
    }

    private Object normalizePGObject(PGobject pgObject) {
        try {
            return objectMapper.readTree(pgObject.getValue());
        } catch (JsonProcessingException exception) {
            throw new DatabaseNormalizationException(
                    "Failed to normalize PostgreSQL object.",
                    exception);
        }
    }

    private Map<String, Object> normalizeMap(Map<?, ?> map) {
        Map<String, Object> normalized = new LinkedHashMap<>();

        for (var entry : map.entrySet()) {
            normalized.put(
                    String.valueOf(entry.getKey()),
                    normalize(entry.getValue()));
        }

        return normalized;
    }

    private List<Object> normalizeList(List<?> list) {
        return list.stream()
                .map(this::normalize)
                .toList();
    }
}
