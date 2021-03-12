package com.wire.backups.exports.commands;

import com.wire.backups.exports.exporters.DesktopExporter;
import com.wire.backups.exports.exporters.ExportConfiguration;
import com.wire.backups.exports.exporters.Exporter;
import picocli.CommandLine.Command;

import javax.ws.rs.client.Client;

@Command(name = "web",
        description = "Exports Backup made in Desktop or Web Wire client.")
public class WebClientCommand extends OpenClientCommand {

    @Override
    protected Exporter buildExporter(ExportConfiguration configuration, Client httpClient) {
        return new DesktopExporter(configuration, httpClient);
    }
}

