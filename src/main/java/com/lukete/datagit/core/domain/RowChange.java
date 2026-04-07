package com.lukete.datagit.core.domain;

import java.util.Map;

/**
 * Represents a row-level change
 */
public record RowChange(
                Map<String, Object> before,
                Map<String, Object> after) {

}
