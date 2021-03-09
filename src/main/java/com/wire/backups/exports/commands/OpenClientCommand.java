package com.wire.backups.exports.commands;


import com.wire.backups.exports.exporters.ExportConfiguration;
import com.wire.backups.exports.exporters.Exporter;
import org.glassfish.jersey.client.ClientConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.File;
import java.util.concurrent.Callable;

@Command(mixinStandardHelpOptions = true)
abstract class OpenClientCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Wire Client backup.")
    protected File file;

    @Option(names = {"-e", "--email"}, description = "An email address of the Wire account.", required = true)
    protected String email;

    @Option(names = {"-p", "--password"}, description = "For Wire account.", required = true)
    protected String password;

    @Option(names = {"-o", "--output"}, description = "Where should be the result created.")
    protected File output = null;

    protected ExportConfiguration buildFromInput() {
        final ExportConfiguration ec = new ExportConfiguration();
        ec.setEmail(email);
        ec.setPassword(password);
        ec.setIn(file.getAbsolutePath());
        if (output != null) {
            String path = output.getAbsolutePath();
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            ec.setOut(path);
        }
        return ec;
    }

    protected Client buildClient() {
        return ClientBuilder.newClient(new ClientConfig());
    }

    protected abstract Exporter buildExporter(ExportConfiguration configuration, Client httpClient);

    @Override
    public Integer call() throws Exception {
        final ExportConfiguration ec = buildFromInput();
        buildExporter(ec, buildClient()).run();
        return 0;
    }
}
