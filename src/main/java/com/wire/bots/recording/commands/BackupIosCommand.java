package com.wire.bots.recording.commands;

import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import pw.forst.wire.backups.ios.GenericMessageDto;

import java.util.Arrays;
import java.util.List;

import static pw.forst.wire.backups.ios.GenericMessageConverterKt.obtainProtobufsForDatabase;

public class BackupIosCommand extends BackupCommandBase {

    private static final String VERSION = "0.2.0";

    public BackupIosCommand() {
        super("ios-pdf", "Convert Wire iOS backup file into PDF");
    }

    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("-in", "--input")
                .dest("in")
                .type(String.class)
                .required(true)
                .help("Extracted iOS database");
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) {
        System.out.printf("Backup to PDF converter version: %s\n\n", VERSION);

        final String in = namespace.getString("in");
        List<GenericMessageDto> messages = obtainProtobufsForDatabase(in);
        messages.forEach(m -> System.out.println(Arrays.toString(m.getProtobuf())));
    }
}
