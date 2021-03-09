package com.wire.backups.exports.commands;

import com.wire.backups.exports.exporters.DesktopExporter;
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

@Command(name = "web",
        mixinStandardHelpOptions = true,
        version = "checksum 4.0",
        description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
public class PicocliExample implements Callable<Integer> {

    @Parameters(index = "0", description = "Wire Client backup.")
    private File file;

    @Option(names = {"-e", "--email"}, description = "An email address of the Wire account.", required = true)
    private String email;

    @Option(names = {"-p", "--password"}, description = "For Wire account.", required = true)
    private String password;

    @Option(names = {"-o", "--output"}, description = "Where should be the result created.")
    private File output = null;

    @Override
    public Integer call() throws Exception { // your business logic goes here...
        final ExportConfiguration ec = new ExportConfiguration();
        ec.setEmail(email);
        ec.setPassword(password);
        ec.setIn(file.getAbsolutePath());
        if (output != null) {
            ec.setOut(output.getAbsolutePath());
        }
        final Client client = ClientBuilder.newClient(new ClientConfig());
        final Exporter exporter = new DesktopExporter(ec, client);
        exporter.run();
        return 0;
    }
}

