// Wire
// Copyright (C) 2016 Wire Swiss GmbH
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see http://www.gnu.org/licenses/.
//

package com.wire.backups.exports;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.wire.backups.exports.commands.EncryptedBackupCommand;
import com.wire.backups.exports.commands.ExporterProducer;
import com.wire.backups.exports.commands.OpenBackupCommand;
import com.wire.backups.exports.exporters.AndroidExporter;
import com.wire.backups.exports.exporters.DesktopExporter;
import com.wire.backups.exports.exporters.IosExporter;
import com.wire.backups.exports.model.Config;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.Server;
import io.dropwizard.Application;
import io.dropwizard.cli.Command;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Service extends Server<Config> {
    public static Service instance;

    public static void main(String[] args) throws Exception {
        instance = new Service();
        instance.run(args);
    }

    @Override
    protected MessageHandlerBase createHandler(Config config, Environment environment) {
        return new DummyMessageHandler();
    }

    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        super.initialize(bootstrap);
        // because of the Java > 8 support
        bootstrap.setObjectMapper(Jackson.newMinimalObjectMapper().registerModule(new Jdk8Module()));

        bootstrap.addCommand(openBackups("Desktop", DesktopExporter::new));
        bootstrap.addCommand(encryptedBackups("iOS", IosExporter::new));
        bootstrap.addCommand(encryptedBackups("Android", AndroidExporter::new));

        Application<Config> application = bootstrap.getApplication();
        instance = (Service) application;
    }

    @SuppressWarnings("SameParameterValue") // don't care
    private Command openBackups(String client, ExporterProducer exporterProducer) {
        return new OpenBackupCommand(
                client.toLowerCase() + "-pdf",
                String.format("Convert Wire %s backup file into PDF", client),
                exporterProducer
        );
    }

    private Command encryptedBackups(String client, ExporterProducer exporterProducer) {
        return new EncryptedBackupCommand(
                client.toLowerCase() + "-pdf",
                String.format("Convert Wire %s backup file into PDF", client),
                exporterProducer
        );
    }
}
