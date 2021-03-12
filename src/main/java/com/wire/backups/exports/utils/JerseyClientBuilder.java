package com.wire.backups.exports.utils;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collections;
import java.util.Set;


public class JerseyClientBuilder {

    private static ClientConfig config() {
        return new ClientConfig();
    }

    public static Client build() {
        return setSharedProperties(ClientBuilder.newClient(config()));
    }

    public static Client buildWithProxy(String proxyHost, int proxyPort) {
        return buildWithProxy(proxyHost, proxyPort, Collections.emptySet());
    }

    public static Client buildWithProxy(String proxyHost, int proxyPort, Set<String> ignoredHosts) {
        // https://stackoverflow.com/a/56300006/7169288
        final Client jerseyClient = ClientBuilder.newClient(config().connectorProvider((client, runtimeConfig) -> {
            final HttpUrlConnectorProvider customConnProv = new HttpUrlConnectorProvider();
            customConnProv.connectionFactory(url -> {
                HttpURLConnection connection;
                if (ignoredHosts.contains(url.getHost())) {
                    connection = (HttpURLConnection) url.openConnection();
                } else {
                    final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                    connection = (HttpURLConnection) url.openConnection(proxy);
                }
                return connection;
            });
            return customConnProv.getConnector(client, runtimeConfig);
        }));
        return setSharedProperties(jerseyClient);
    }

    private static Client setSharedProperties(Client client) {
        final int timeoutSeconds = 40;
        client.property(ClientProperties.CONNECT_TIMEOUT, timeoutSeconds * 1_000);
        client.property(ClientProperties.READ_TIMEOUT, timeoutSeconds * 1_000);
        return client;
    }
}

