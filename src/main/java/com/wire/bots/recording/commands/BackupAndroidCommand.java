package com.wire.bots.recording.commands;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wire.bots.recording.utils.Collector;
import com.wire.bots.recording.utils.Helper;
import com.wire.bots.recording.utils.InstantCache;
import com.wire.bots.recording.utils.PdfGenerator;
import com.wire.bots.sdk.models.TextMessage;
import io.dropwizard.cli.Command;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.client.ssl.TlsConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.apache.xmlgraphics.image.loader.cache.ExpirationPolicy;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import pw.forst.wire.android.backups.database.dto.*;
import pw.forst.wire.android.backups.steps.DecryptionResult;
import pw.forst.wire.android.backups.steps.ExportMetadata;

import javax.ws.rs.client.Client;
import java.io.File;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static pw.forst.wire.android.backups.database.converters.DatabaseConverterKt.extractDatabase;
import static pw.forst.wire.android.backups.steps.DecryptKt.initSodium;
import static pw.forst.wire.android.backups.steps.OrchestrateKt.decryptAndExtract;

public class BackupAndroidCommand extends Command {
    private static final String VERSION = "0.1.3";
    private final HashMap<UUID, Conversation> conversationHashMap = new HashMap<>();
    private final HashMap<UUID, Collector> collectorHashMap = new HashMap<>();

    private DatabaseMetadata databaseMetadata;
    private ExportMetadata exportMetadata;

    public BackupAndroidCommand() {
        super("android-pdf", "Convert Wire Desktop backup file into PDF");
    }


    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("-e", "--email")
                .dest("email")
                .type(String.class)
                .required(true)
                .help("Email address");

        subparser.addArgument("-p", "--password")
                .dest("password")
                .type(String.class)
                .required(true)
                .help("Password");

        subparser.addArgument("-in", "--input")
                .dest("in")
                .type(String.class)
                .required(true)
                .help("Backup file");

        subparser.addArgument("-u", "--user-id")
                .dest("userId")
                .type(String.class)
                .required(true)
                .help("User Id");

        subparser.addArgument("-bp", "--backup-password")
                .dest("dbPassword")
                .type(String.class)
                .required(true)
                .help("Backup password");
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        System.out.printf("Backup to PDF converter version: %s\n\n", VERSION);

        final String email = namespace.getString("email");
        final String password = namespace.getString("password");
        final String in = namespace.getString("in");
        final String userId = namespace.getString("userId");
        final String databasePassword = namespace.getString("dbPassword");

        makeDirs(userId);
        Helper.root = userId;
        Collector.root = userId;
        InstantCache cache = new InstantCache(email, password, getClient(bootstrap));

        final DecryptionResult decryptionResult = decryptAndExtract(in, databasePassword, userId);
        if (decryptionResult == null) {
            throw new IllegalArgumentException("It was not possible to decrypt the database!");
        }

        final DatabaseDto databaseDto = extractDatabase(UUID.fromString(userId), decryptionResult.getDatabaseFile());
        exportMetadata = decryptionResult.getMetadata();
        databaseMetadata = databaseDto.getMetaData();

        processConversations(databaseDto, cache);

        processMessages(databaseDto, cache);

        createPDFs(userId);
    }

    private void makeDirs(String root) {
        final File imagesDir = new File(String.format("%s/assets", root));
        final File avatarsDir = new File(String.format("%s/avatars", root));
        final File outDir = new File(String.format("%s/out", root));
        final File inDir = new File(String.format("%s/in", root));

        imagesDir.mkdirs();
        avatarsDir.mkdirs();
        outDir.mkdirs();
        inDir.mkdirs();
    }

    private Client getClient(Bootstrap<?> bootstrap) {
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

    private void createPDFs(String root) {
        for (Collector collector : collectorHashMap.values()) {
            try {
                final String html = collector.execute();
                final String filename = URLEncoder.encode(collector.getConvName(), StandardCharsets.UTF_8.toString());
                String out = String.format("%s/out/%s.pdf", root, filename);
                PdfGenerator.save(out, html, "file:./");
                System.out.printf("Generated pdf: %s\n", out);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processConversations(DatabaseDto db, InstantCache cache) {
        for (NamedConversationDto conversation : db.getNamedConversations()) {
            final Conversation conv = new Conversation(conversation.getId(), conversation.getName());
            conversationHashMap.put(conv.id, conv);
        }

        for (DirectConversationDto conversation : db.getDirectConversations()) {
            try {
                final String name = cache.getUser(conversation.getOtherUser()).name;
                final Conversation conv = new Conversation(conversation.getId(), name);

                conversationHashMap.put(conv.id, conv);

                System.out.printf("%s, id: %s\n",
                        conv.name,
                        conv.id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Conversation {
        @JsonProperty
        UUID id;
        @JsonProperty
        String name;

        public Conversation(UUID id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private void processMessages(DatabaseDto db, InstantCache cache) {
        db.getMessages().forEach(messageDto -> {
            try {
                processMessage(messageDto, getCollector(messageDto.getConversationId(), messageDto.getUserId(), cache));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void processMessage(MessageDto message, Collector collector) throws ParseException {
        final TextMessage txt = new TextMessage(message.getId(), message.getConversationId(), null, message.getUserId());
        txt.setTime(message.getTime());
        txt.setText(message.getContent());
        txt.setQuotedMessageId(message.getQuote());
        collector.add(txt);
    }
//    private void processEvents(Event[] events, InstantCache cache) {
//        System.out.println("\nEvents:");
//
//        for (Event event : events) {
//            if (event.type == null)
//                continue;
//
//            System.out.printf("Id: %s, time: %s, conv: %s, type: %s\n",
//                    event.id,
//                    event.time,
//                    event.conversation,
//                    event.type
//            );
//
//            try {
//                switch (event.type) {
//                    case "conversation.group-creation": {
//                        onGroupCreation(getCollector(event.conversation, cache), event);
//                    }
//                    break;
//                    case "conversation.message-add": {
//                        onMessageAdd(getCollector(event.conversation, cache), event);
//                    }
//                    break;
//                    case "conversation.asset-add": {
//                        onAssetAdd(getCollector(event.conversation, cache), event);
//                    }
//                    break;
//                    case "conversation.member-join": {
//                        onMemberJoin(getCollector(event.conversation, cache), event);
//                    }
//                    break;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private Collector getCollector(UUID convId, UUID userId, InstantCache cache) {
        return collectorHashMap.computeIfAbsent(convId, x -> {
            Collector collector = new Collector(cache);
            Conversation conversation = getConversation(convId);
            collector.setConvName(conversation.name);
            collector.details = new Collector.Details();
//           TODO obtain those from export info

            collector.details.name = databaseMetadata.getName();
            collector.details.handle = databaseMetadata.getHandle();
            collector.details.id = userId.toString();
//            collector.details.device = export.client_id;
            collector.details.platform = exportMetadata.getPlatform();
            collector.details.date = exportMetadata.getCreationTime();
            collector.details.version = exportMetadata.getVersion();

            return collector;
        });
    }

    private Conversation getConversation(UUID convId) {
        return conversationHashMap.get(convId);
    }

//    private void onMemberJoin(Collector collector, Event event) throws ParseException {
//        for (UUID userId : event.data.user_ids) {
//            String txt = String.format("**%s** %s **%s**",
//                    collector.getUserName(event.from),
//                    "added",
//                    collector.getUserName(userId));
//            collector.addSystem(txt, event.time, "conversation.member-join", event.id);
//        }
//    }

//    private void onAssetAdd(Collector collector, Event event) throws ParseException {
//        if (event.data.otrKey == null || event.data.sha256 == null)
//            return;
//        if (event.data.contentType.startsWith("image")) {
//            final ImageMessage img = new ImageMessage(event.id, event.conversation, null, event.from);
//            img.setTime(event.time);
//            img.setSize(event.data.contentLength);
//            img.setMimeType(event.data.contentType);
//            img.setAssetToken(event.data.token);
//            img.setAssetKey(event.data.key);
//            img.setOtrKey(toArray(event.data.otrKey));
//            img.setSha256(toArray(event.data.sha256));
//            collector.add(img);
//        } else {
//            final AttachmentMessage att = new AttachmentMessage(event.id, event.conversation, null, event.from);
//            att.setTime(event.time);
//            att.setSize(event.data.contentLength);
//            att.setMimeType(event.data.contentType);
//            att.setAssetToken(event.data.token);
//            att.setAssetKey(event.data.key);
//            att.setOtrKey(toArray(event.data.otrKey));
//            att.setSha256(toArray(event.data.sha256));
//            att.setName(event.data.info.name);
//            collector.add(att);
//        }
//    }
//
//    private void onMessageAdd(Collector collector, Mess) throws ParseException {
//        if (event.data.replacingMessageId != null) {
//            EditedTextMessage edit = new EditedTextMessage(event.id, event.conversation, null, event.from);
//            edit.setText(event.data.content);
//            edit.setTime(event.editedTime != null ? event.editedTime : event.time);
//            edit.setReplacingMessageId(event.data.replacingMessageId);
//            collector.addEdit(edit);
//        } else {
//            final TextMessage txt = new TextMessage(event.id, event.conversation, null, event.from);
//            txt.setTime(event.time);
//            txt.setText(event.data.content);
//            if (event.data.quote != null) {
//                txt.setQuotedMessageId(event.data.quote.messageId);
//            }
//            collector.add(txt);
//        }
//
//        if (event.reactions != null) {
//            for (UUID userId : event.reactions.keySet()) {
//                ReactionMessage like = new ReactionMessage(UUID.randomUUID(), event.conversation, null, userId);
//                like.setReactionMessageId(event.id);
//                like.setEmoji(event.reactions.get(userId));
//                like.setTime(event.time);
//                collector.add(like);
//            }
//        }
//    }

    private byte[] toArray(HashMap<String, Byte> otrKey) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(32);
        for (String key : otrKey.keySet()) {
            byteBuffer.put(Integer.parseInt(key), otrKey.get(key));
        }
        return byteBuffer.array();
    }

//    private void onGroupCreation(Collector collector, Event event) throws ParseException {
//        String txt = String.format("**%s** created conversation **%s** with:",
//                collector.getUserName(event.from),
//                event.data.name);
//        collector.addSystem(txt, event.time, "conversation.create", event.id);
//        for (UUID userId : event.data.userIds) {
//            final String userName = collector.getUserName(userId);
//            collector.addSystem(userName, event.time, "conversation.member-join", UUID.randomUUID());
//        }
//    }
}
