package com.wire.bots.recording.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wire.bots.recording.utils.Collector;
import com.wire.bots.recording.utils.PdfGenerator;
import io.dropwizard.cli.Command;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.client.ssl.TlsConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

abstract class BackupCommandBase extends Command {
    protected final HashMap<UUID, Collector> collectorHashMap = new HashMap<>();

    protected BackupCommandBase(String name, String description) {
        super(name, description);
    }

    protected void makeDirs(String root) {
        final File imagesDir = new File(String.format("%s/assets", root));
        final File avatarsDir = new File(String.format("%s/avatars", root));
        final File outDir = new File(String.format("%s/out", root));
        final File inDir = new File(String.format("%s/in", root));

        boolean dirsCreated = imagesDir.mkdirs()
                & avatarsDir.mkdirs()
                & outDir.mkdirs()
                & inDir.mkdirs();
        System.out.printf("All directories created: %b\n", dirsCreated);
    }

    protected Client getClient(Bootstrap<?> bootstrap) {
        final Environment environment = new Environment(getName(),
                new ObjectMapper(),
                bootstrap.getValidatorFactory().getValidator(),
                bootstrap.getMetricRegistry(),
                bootstrap.getClassLoader());

        JerseyClientConfiguration jerseyCfg = new JerseyClientConfiguration();
        jerseyCfg.setChunkedEncodingEnabled(false);
        jerseyCfg.setGzipEnabled(false);
        jerseyCfg.setGzipEnabledForRequests(false);
        jerseyCfg.setTimeout(Duration.seconds(40));
        jerseyCfg.setConnectionTimeout(Duration.seconds(20));
        jerseyCfg.setConnectionRequestTimeout(Duration.seconds(20));
        jerseyCfg.setRetries(3);
        jerseyCfg.setKeepAlive(Duration.milliseconds(0));

        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setProtocol("TLSv1.2");
        tlsConfiguration.setProvider("SunJSSE");
        tlsConfiguration.setSupportedProtocols(Arrays.asList("TLSv1.2", "TLSv1.1"));
        jerseyCfg.setTlsConfiguration(tlsConfiguration);

        return new JerseyClientBuilder(environment)
                .using(jerseyCfg)
                .withProvider(MultiPartFeature.class)
                .withProvider(JacksonJsonProvider.class)
                .build(getName());
    }

    protected void createPDFs(String root, String htmlAssetsRoot) {
        for (Collector collector : collectorHashMap.values()) {
            try {
                final String html = collector.execute();

                final UUID conversationId = collector.getConversationId();
                final String fileNameBase = conversationId != null ?
                        String.format("%s-%s", collector.getConvName(), conversationId.toString())
                        : collector.getConvName();

                final String filename = URLEncoder.encode(
                        fileNameBase,
                        StandardCharsets.UTF_8.toString());
                final String out = String.format("%s/out/%s.pdf", root, filename);
                PdfGenerator.save(out, html, "file:" + htmlAssetsRoot);
                System.out.printf("Generated pdf: %s\n", out);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
