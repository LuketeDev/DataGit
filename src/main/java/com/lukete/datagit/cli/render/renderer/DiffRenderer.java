package com.lukete.datagit.cli.render.renderer;

import com.lukete.datagit.core.domain.diff.DiffResult;

public interface DiffRenderer {
    void render(String leftRef, String rightRef, DiffResult diffResult);
}
