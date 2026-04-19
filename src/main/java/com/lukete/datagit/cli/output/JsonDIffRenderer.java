package com.lukete.datagit.cli.output;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukete.datagit.core.domain.DiffResult;
import com.lukete.datagit.core.exception.DiffRenderingException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JsonDIffRenderer implements DiffRenderer {
    private final CliPrinter printer;
    private final ObjectMapper objectMapper;

    @Override
    public void render(String leftRef, String rightRef, DiffResult diffResult) {
        /**
         * LinkedHashMap preserves insertion order, so output will always be:
         * {
         * "leftRef":"..."
         * "rightRef":"..."
         * "diff":{...}
         * }
         */
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("leftRef", leftRef);
        payload.put("rightRef", rightRef);
        payload.put("diff", diffResult);
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
            printer.info(json);
        } catch (Exception e) {
            throw new DiffRenderingException("Failed to render diff as JSON", e);
        }
    }

}
