package com.lukete.datagit.cli;

import com.lukete.datagit.core.service.DiffService;
import com.lukete.datagit.core.service.ReferenceResolver;

import static com.lukete.datagit.core.util.JsonUtils.toJson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

// TODO Defaults to latest and previous.
@Command(name = "diff", description = "Compare two snapshots.")
@RequiredArgsConstructor
@Slf4j
public class DiffCommand implements Runnable {
    @Parameters(index = "0")
    private String oldId;

    @Parameters(index = "1")
    private String newId;

    private final DiffService service;
    private final ReferenceResolver refResolver;

    @Override
    public void run() {
        var oldSnap = refResolver.resolve(oldId);
        var newSnap = refResolver.resolve(newId);

        var diff = service.compare(oldSnap, newSnap);

        log.info(toJson(diff));
    }
}
