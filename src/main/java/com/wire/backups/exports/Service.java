package com.wire.backups.exports;

import com.wire.backups.exports.commands.AndroidClientCommand;
import com.wire.backups.exports.commands.ExportToolCommand;
import com.wire.backups.exports.commands.IosClientCommand;
import com.wire.backups.exports.commands.WebClientCommand;
import picocli.CommandLine;

public class Service {
    public static void main(String... args) {
        int exitCode = new CommandLine(new ExportToolCommand())
                .addSubcommand(new WebClientCommand())
                .addSubcommand(new AndroidClientCommand())
                .addSubcommand(new IosClientCommand())
                .execute(args);
        System.exit(exitCode);
    }
}
