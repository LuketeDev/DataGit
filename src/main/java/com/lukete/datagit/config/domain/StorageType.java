package com.lukete.datagit.config.domain;

import com.lukete.datagit.config.exception.UnsupportedStorageTypeException;

public enum StorageType {
    FILESYSTEM;

    public static StorageType from(String value) {
        if (value == null || value.isBlank()) {
            throw new UnsupportedStorageTypeException("null or blank");
        }

        return switch (value.trim().toUpperCase()) {
            case "FILESYSTEM" -> FILESYSTEM;
            default -> throw new UnsupportedStorageTypeException(value);
        };
    }
}
