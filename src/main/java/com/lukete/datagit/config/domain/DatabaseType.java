package com.lukete.datagit.config.domain;

import com.lukete.datagit.config.exception.UnsupportedDatabaseTypeException;

/**
 * Supported database types
 */
public enum DatabaseType {
    POSTGRES;

    public static DatabaseType from(String value) {
        if (value == null || value.isBlank()) {
            throw new UnsupportedDatabaseTypeException("null or blank");
        }

        return switch (value.trim().toUpperCase()) {
            case "POSTGRES" -> POSTGRES;
            default -> throw new UnsupportedDatabaseTypeException(value);
        };
    }
}
