package com.wire.backups.exports.commands;

import com.wire.backups.exports.exporters.ExportConfiguration;
import com.wire.backups.exports.exporters.Exporter;

import javax.ws.rs.client.Client;

@FunctionalInterface
public interface ExporterProducer {
    Exporter build(ExportConfiguration one, Client two);
}

