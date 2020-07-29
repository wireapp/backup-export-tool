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

import com.wire.backups.exports.commands.BackupAndroidCommand;
import com.wire.backups.exports.commands.BackupDesktopCommand;
import com.wire.backups.exports.commands.BackupIosCommand;
import com.wire.backups.exports.model.Config;
import com.wire.backups.exports.utils.ImagesBundle;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.Server;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
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

        bootstrap.addBundle(new AssetsBundle("/recording/assets"));
        bootstrap.addBundle(new AssetsBundle("/recording/scripts", "/recording/scripts", "index.htm", "scripts"));
        bootstrap.addBundle(new ImagesBundle("/opt/recording/avatars", "/recording/avatars", "avatars"));
        bootstrap.addBundle(new ImagesBundle("/opt/recording/html", "/recording/channel", "channels"));

        bootstrap.addCommand(new BackupDesktopCommand());
        bootstrap.addCommand(new BackupAndroidCommand());
        bootstrap.addCommand(new BackupIosCommand());

        Application<Config> application = bootstrap.getApplication();
        instance = (Service) application;
    }
}
