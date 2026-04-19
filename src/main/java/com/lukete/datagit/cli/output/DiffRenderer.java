package com.lukete.datagit.cli.output;

import com.lukete.datagit.core.domain.DiffResult;

public interface DiffRenderer {
    void render(String leftRef, String rightRef, DiffResult diffResult);
}
