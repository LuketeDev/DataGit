package com.lukete.datagit.core.domain;

/**
 * Represents a field-level change in an updated row.
 *
 * @param field  the changed field name
 * @param before the value before the change
 * @param after  the value after the change
 */
public record FieldChange(
                String field,
                Object before,
                Object after) {
}