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

package com.wire.bots.recording;

import com.wire.bots.recording.DAO.HistoryDAO;
import com.wire.bots.recording.model.Config;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.Server;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

public class Service extends Server<Config> {
    public static void main(String[] args) throws Exception {
        new Service().run(args);
    }

    @Override
    protected MessageHandlerBase createHandler(Config config, Environment env) {
        final DBIFactory factory = new DBIFactory();
        final DBI jdbi = factory.build(environment, config.database, "postgresql");
        final HistoryDAO historyDAO = jdbi.onDemand(HistoryDAO.class);

        return new MessageHandler(historyDAO);
    }

    @Override
    protected void onRun(Config config, Environment env) {
        Helper.client = getClient();
        env.healthChecks().register("User login", new UserHealthCheck());
    }
}
