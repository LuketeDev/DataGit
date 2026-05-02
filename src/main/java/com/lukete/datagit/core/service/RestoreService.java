package com.lukete.datagit.core.service;

import com.lukete.datagit.core.domain.Snapshot;
import com.lukete.datagit.core.ports.DataSourceAdapter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestoreService {
    private final DataSourceAdapter adapter;

    public void restore(Snapshot snapshot) {
        adapter.restore(snapshot);
    }
}
