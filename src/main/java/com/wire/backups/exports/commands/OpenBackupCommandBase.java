package com.wire.backups.exports.commands;

import com.wire.backups.exports.exporters.ExportConfiguration;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public abstract class OpenBackupCommandBase extends CommandBase {

    public OpenBackupCommandBase(String name, String description) {
        super(name, description);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-e", "--email")
                .dest("email")
                .type(String.class)
                .required(true)
                .help("Email address");

        subparser.addArgument("-p", "--password")
                .dest("password")
                .type(String.class)
                .required(true)
                .help("Password");

        subparser.addArgument("-in", "--input")
                .dest("in")
                .type(String.class)
                .required(true)
                .help("Backup file");

        subparser.addArgument("-out", "--output")
                .dest("out")
                .type(String.class)
                .required(false)
                .help("Output directory");
    }

    protected ExportConfiguration parseExportConfiguration(Namespace namespace) {
        final ExportConfiguration conf = new ExportConfiguration();
        conf.setEmail(namespace.getString("email"));
        conf.setPassword(namespace.getString("password"));
        conf.setIn(namespace.getString("in"));

        String out = namespace.getString("out");
        if (out != null && out.endsWith("/")) {
            out = out.substring(0, out.length() - 1);
        }
        conf.setOut(out);
        return conf;
    }

}
