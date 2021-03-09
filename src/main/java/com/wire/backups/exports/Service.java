package com.wire.backups.exports;

import com.wire.backups.exports.commands.PicocliExample;
import picocli.CommandLine;

public class Service {
    // this example implements Callable, so parsing, error handling and handling user
    // requests for usage help or version help can be done with one line of code.
    public static void main(String... args) {
        int exitCode = new CommandLine(new PicocliExample()).execute(args);
        System.exit(exitCode);
    }

}
