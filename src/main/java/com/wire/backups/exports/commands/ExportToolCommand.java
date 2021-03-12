package com.wire.backups.exports.commands;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
        name = "base",
        description = "Export Wire client backups.",
        mixinStandardHelpOptions = true
)
public class ExportToolCommand implements Callable<Integer> {
    @Override
    public Integer call() {
        System.out.println("Pass client name - web, android or ios.");
        return 1;
    }
}
