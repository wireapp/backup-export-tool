package com.wire.backups.exports.commands;

import com.wire.backups.exports.exporters.IosExporter;
import com.wire.backups.exports.model.ExportConfig;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;

import javax.ws.rs.client.Client;


public class BackupIosCommand extends EncryptedBackupCommandBase {

    public BackupIosCommand() {
        super("ios-pdf", "Convert Wire iOS backup file into PDF");
    }

    @Override
    protected void run(Bootstrap<ExportConfig> bootstrap, Namespace namespace, ExportConfig configuration) throws Exception {
        final Client client = getClient(bootstrap, configuration);
        final IosExporter exporter = new IosExporter(parseExportConfiguration(namespace), client);
        exporter.run();
    }
}
