package com.wire.backups.exports.commands;

import com.wire.backups.exports.exporters.AndroidExporter;
import com.wire.backups.exports.exporters.ExportConfiguration;
import com.wire.backups.exports.exporters.Exporter;
import picocli.CommandLine.Command;

import javax.ws.rs.client.Client;

@Command(name = "android",
        description = "Decrypt backup from Android Wire client.")
public class AndroidClientCommand extends EncryptedClientCommand {
    @Override
    protected Exporter buildExporter(ExportConfiguration configuration, Client httpClient) {
        return new AndroidExporter(configuration, httpClient);
    }
}
