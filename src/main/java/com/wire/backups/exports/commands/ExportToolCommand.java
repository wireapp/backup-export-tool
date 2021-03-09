package com.wire.backups.exports.commands;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
        name = "base",
        description = "Export Wire client backups."
)
public class ExportToolCommand implements Callable<Integer> {
    @Override
    public Integer call() {
        return 0;
    }
}
