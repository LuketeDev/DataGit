package com.lukete.datagit.cli.command;

import com.lukete.datagit.cli.output.CliPrinter;
import com.lukete.datagit.cli.output.TextDiffRenderer;
import com.lukete.datagit.core.service.StatusService;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;

@Command(name = "status", description = "Compare current database state against the latest snapshot.")
@RequiredArgsConstructor
public class StatusCommand implements Runnable {
    private final TextDiffRenderer renderer;
    private final StatusService statusService;
    private final CliPrinter printer;

    @Override
    public void run() {
        printer.info("Comparing current database against HEAD");
        printer.blankLine();

        var diffResult = statusService.getStatus();

        renderer.render("HEAD", "CURRENT", diffResult);
    }

}
