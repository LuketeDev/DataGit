package com.lukete.datagit.cli.output;

import com.lukete.datagit.core.domain.SchemaDiffResult;

public interface SchemaDiffRenderer {
    void render(String leftRef, String rightRef, SchemaDiffResult diffResult);
}
