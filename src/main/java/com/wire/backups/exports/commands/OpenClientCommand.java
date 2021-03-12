package com.wire.backups.exports.commands;


import com.wire.backups.exports.exporters.ExportConfiguration;
import com.wire.backups.exports.exporters.Exporter;
import com.wire.backups.exports.utils.JerseyClientBuilder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import javax.ws.rs.client.Client;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(mixinStandardHelpOptions = true)
abstract class OpenClientCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Wire Client backup.")
    protected File file;

    @Option(
            names = {"-e", "--email"},
            description = "An email address of the Wire account.",
            required = true
    )
    protected String email;

    @Option(
            names = {"-p", "--password"},
            description = "For Wire account.",
            required = true
    )
    protected String password;

    @Option(
            names = {"-o", "--output"},
            description = "Where should be the result created."
    )
    protected File output = null;

    @Option(
            names = {"--use-proxy"},
            description = "Use proxy to route client requests.",
            negatable = true,
            defaultValue = "false"
    )
    protected boolean useProxy = false;

    @Option(names = {"--proxy-host"},
            description = "Proxy host, mandatory if [useProxy] is set."
    )
    protected String proxyHost = null;

    @Option(
            names = {"--proxy-port"},
            description = "Proxy port, mandatory if [useProxy] is set.",
            defaultValue = "8080"
    )
    protected int proxyPort = 8080;

    @Option(
            names = {"--proxy-non-proxy-hosts"},
            description = "List of hosts for which is the proxy not applied. By default no hosts."
    )
    protected String[] nonProxyHosts = new String[0];

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
        Client client;
        if (!useProxy) {
            client = JerseyClientBuilder.build();
        } else {
            if (proxyHost == null) {
                throw new IllegalArgumentException("Proxy host is not set even though useProxy is enabled. Specify --proxy-host.");
            }
            final Set<String> hosts = new HashSet<>(Arrays.asList(nonProxyHosts));
            client = JerseyClientBuilder.buildWithProxy(proxyHost, proxyPort, hosts);
        }
        return client;
    }

    protected abstract Exporter buildExporter(ExportConfiguration configuration, Client httpClient);

    @Override
    public Integer call() throws Exception {
        final ExportConfiguration ec = buildFromInput();
        buildExporter(ec, buildClient()).run();
        return 0;
    }
}
