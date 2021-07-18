package com.wire.backups.exports.commands;

import com.wire.backups.exports.exporters.ExportConfiguration;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command
abstract public class EncryptedClientCommand extends OpenClientCommand implements Callable<Integer> {

    @Option(
            names = {"-u", "--username"},
            description = "Username of the user who created the backup.",
            required = true
    )
    protected String userName;

    @Option(names = {"-bp", "--backup-password"}, description = "Password used during backup creation.", defaultValue = "")
    protected String backupPassword;

    @Override
    public Integer call() throws Exception {
        final ExportConfiguration ec = buildFromInput();
        ec.setDatabasePassword(backupPassword);
        ec.setUserName(userName);

        buildExporter(ec, buildClient()).run();
        return 0;
    }
}
