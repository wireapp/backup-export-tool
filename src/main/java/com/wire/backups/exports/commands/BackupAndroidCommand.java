package com.wire.backups.exports.commands;

import com.wire.backups.exports.exporters.AndroidExporter;
import com.wire.backups.exports.model.ExportConfig;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;

import javax.ws.rs.client.Client;

public class BackupAndroidCommand extends EncryptedBackupCommandBase {

    public BackupAndroidCommand() {
        super("android-pdf", "Convert Wire Android backup file into PDF");
    }

    @Override
    public void run(Bootstrap<ExportConfig> bootstrap, Namespace namespace, ExportConfig configuration) throws Exception {
        final Client client = getClient(bootstrap, configuration);
        final AndroidExporter exporter = new AndroidExporter(parseExportConfiguration(namespace), client);
        exporter.run();
    }
}
