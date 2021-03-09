package com.wire.backups.exports.commands;

import com.wire.backups.exports.exporters.ExportConfiguration;
import com.wire.backups.exports.exporters.Exporter;
import com.wire.backups.exports.exporters.IosExporter;
import picocli.CommandLine.Command;

import javax.ws.rs.client.Client;

@Command(name = "ios",
        description = "Decrypt backup from iOS Wire client.")
public class IosClientCommand extends EncryptedClientCommand {
    @Override
    protected Exporter buildExporter(ExportConfiguration configuration, Client httpClient) {
        return new IosExporter(configuration, httpClient);
    }
}

