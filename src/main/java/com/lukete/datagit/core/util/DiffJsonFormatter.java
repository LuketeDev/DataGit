package com.lukete.datagit.core.util;

import static com.lukete.datagit.core.util.JsonUtils.toJson;

import com.lukete.datagit.core.domain.DiffResult;

/**
 * Formats a diff result as JSON.
 */
public class DiffJsonFormatter {
    /**
     * Converts a diff result into a JSON string.
     *
     * @param diffResult the diff to format
     * @return the JSON representation of the diff
     */
    public String format(DiffResult diffResult) {
        return toJson(diffResult);
    }
}
