package com.wire.backups.exports.exporters;

import com.wire.backups.exports.utils.Collector;
import com.wire.backups.exports.utils.PdfGenerator;

import javax.ws.rs.client.Client;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;

abstract class ExporterBase {
    protected static final String VERSION = "1.1.0";

    protected final HashMap<UUID, Collector> collectorHashMap = new HashMap<>();
    protected String logicalRoot;
    protected Client client;

    protected ExporterBase(Client client) {
        this.client = client;
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
        System.out.printf("Directories had to be created: %b\n", dirsCreated);
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
