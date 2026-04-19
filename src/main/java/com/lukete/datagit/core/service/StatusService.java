package com.lukete.datagit.core.service;

import com.lukete.datagit.core.domain.DiffResult;
import com.lukete.datagit.core.ports.DataSourceAdapter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatusService {
    private final DataSourceAdapter adapter;
    private final ReferenceResolver resolver;
    private final DiffService diffService;

    public DiffResult getStatus() {
        // extract resolve compare
        var current = adapter.extract();
        var head = resolver.resolve("HEAD");
        return diffService.compare(head, current);
    }
}
