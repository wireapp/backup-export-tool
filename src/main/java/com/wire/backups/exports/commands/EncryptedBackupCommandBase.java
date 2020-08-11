package com.wire.backups.exports.commands;

import com.wire.backups.exports.exporters.ExportConfiguration;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public abstract class EncryptedBackupCommandBase extends OpenBackupCommandBase {

    protected EncryptedBackupCommandBase(String name, String description) {
        super(name, description);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-u", "--username")
                .dest("username")
                .type(String.class)
                .required(true)
                .help("Username");

        subparser.addArgument("-bp", "--backup-password")
                .dest("dbPassword")
                .type(String.class)
                .required(true)
                .help("Backup password");
    }


    @Override
    protected ExportConfiguration parseExportConfiguration(Namespace namespace) {
        final ExportConfiguration conf = super.parseExportConfiguration(namespace);
        conf.setUserName(namespace.getString("username"));
        conf.setDatabasePassword(namespace.getString("dbPassword"));
        return conf;
    }
}
