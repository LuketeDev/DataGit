package com.lukete.datagit.cli.render.formatter;

import static com.lukete.datagit.cli.render.formatter.JsonUtils.toJson;

import com.lukete.datagit.core.domain.diff.DiffResult;

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
