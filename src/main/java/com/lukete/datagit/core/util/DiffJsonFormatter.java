package com.lukete.datagit.core.util;

import static com.lukete.datagit.core.util.JsonUtils.toJson;

import com.lukete.datagit.core.domain.DiffResult;

public class DiffJsonFormatter {
    public String format(DiffResult diffResult) {
        return toJson(diffResult);
    }
}
