package com.lukete.datagit.cli.render.renderer;

import com.lukete.datagit.core.domain.diff.SchemaDiffResult;

public interface SchemaDiffRenderer {
    void render(String leftRef, String rightRef, SchemaDiffResult diffResult);
}
