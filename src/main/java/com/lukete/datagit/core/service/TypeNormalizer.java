package com.lukete.datagit.core.service;

import java.util.Map;

/**
 * Normalizes database type aliases into stable names for schema comparisons.
 */
public final class TypeNormalizer {
    private static final Map<String, String> POSTGRES_ALIASES = Map.of(
            "int4", "integer",
            "int8", "bigint",
            "varchar", "character varying",
            "bool", "boolean",
            "timestamptz", "timestamp with time zone");

    private TypeNormalizer() {
    }

    public static String normalize(String type) {
        if (type == null) {
            return null;
        }

        String normalized = type.trim().toLowerCase();
        return POSTGRES_ALIASES.getOrDefault(normalized, normalized);
    }
}
