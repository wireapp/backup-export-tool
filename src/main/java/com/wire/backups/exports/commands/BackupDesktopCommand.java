package com.wire.backups.exports.commands;

import com.wire.backups.exports.exporters.DesktopExporter;
import com.wire.backups.exports.model.ExportConfig;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;

import javax.ws.rs.client.Client;

public class BackupDesktopCommand extends OpenBackupCommandBase {

    public BackupDesktopCommand() {
        super("desktop-pdf", "Convert Wire Desktop backup file into PDF");
    }

    @Override
    public void run(Bootstrap<ExportConfig> bootstrap, Namespace namespace, ExportConfig configuration) throws Exception {
        final Client client = getClient(bootstrap, configuration);
        final DesktopExporter exporter = new DesktopExporter(parseExportConfiguration(namespace), client);
        exporter.run();
    }
}
