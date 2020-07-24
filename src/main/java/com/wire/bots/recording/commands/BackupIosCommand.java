package com.wire.bots.recording.commands;

import com.waz.model.Messages;
import com.wire.bots.recording.utils.Collector;
import com.wire.bots.recording.utils.InstantCache;
import com.wire.bots.sdk.exceptions.HttpException;
import com.wire.bots.sdk.models.*;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import pw.forst.wire.backups.ios.IosMessageDto;

import java.util.List;

import static pw.forst.wire.backups.ios.ConverterKt.obtainIosMessages;

public class BackupIosCommand extends BackupCommandBase {

    private static final String VERSION = "0.2.0";

    public BackupIosCommand() {
        super("ios-pdf", "Convert Wire iOS backup file into PDF");
    }

    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("-in", "--input")
                .dest("in")
                .type(String.class)
                .required(true)
                .help("Extracted iOS database");

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
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws HttpException {
        System.out.printf("Backup to PDF converter version: %s\n\n", VERSION);

        final String in = namespace.getString("in");
        final String email = namespace.getString("email");
        final String password = namespace.getString("password");

        InstantCache cache = new InstantCache(email, password, getClient(bootstrap));

        Collector collector = new Collector(cache);

        List<IosMessageDto> messages = obtainIosMessages(in);
        for (IosMessageDto msg : messages) {
            try {
                final Messages.GenericMessage genericMessage = Messages.GenericMessage.parseFrom(msg.getProtobuf());
                final MessageBase messageBase = GenericMessageConverter.convert(
                        msg.getSenderUUID(),
                        "",
                        msg.getConversationUUID(),
                        msg.getTime(),
                        genericMessage);

                if (messageBase instanceof TextMessage) {
                    collector.add((TextMessage) messageBase);
                }
                if (messageBase instanceof ImageMessage) {
                    collector.add((ImageMessage) messageBase);
                }
                if (messageBase instanceof VideoMessage) {
                    collector.add((VideoMessage) messageBase);
                }
                if (messageBase instanceof AttachmentMessage) {
                    collector.add((AttachmentMessage) messageBase);
                }
                if (messageBase instanceof ReactionMessage) {
                    collector.add((ReactionMessage) messageBase);
                }
                if (messageBase instanceof LinkPreviewMessage) {
                    collector.addLink((LinkPreviewMessage) messageBase);
                }
                if (messageBase instanceof EditedTextMessage) {
                    collector.addEdit((EditedTextMessage) messageBase);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
