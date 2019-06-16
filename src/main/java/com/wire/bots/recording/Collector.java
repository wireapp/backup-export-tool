package com.wire.bots.recording;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.wire.bots.recording.model.Conversation;
import com.wire.bots.recording.model.Day;
import com.wire.bots.recording.model.Message;
import com.wire.bots.recording.model.Sender;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.tools.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

class Collector {
    private static MustacheFactory mf = new DefaultMustacheFactory();

    private LinkedList<Day> days = new LinkedList<>();

    void add(Database.Record record) {
        Message message = newMessage(record);
        if (message.text == null && message.image == null)
            return;

        Sender sender = newSender(record, message);
        Day day = newDay(record, sender);

        if (days.isEmpty()) {
            days.add(day);
            return;
        }

        Day lastDay = days.getLast();
        if (!lastDay.equals(day)) {
            days.add(day);
            return;
        }

        Sender lastSender = lastDay.senders.getLast();
        if (lastSender.equals(sender)) {
            lastSender.messages.add(message);
        } else {
            lastDay.senders.add(sender);
        }
    }

    private Message newMessage(Database.Record record) {
        Message message = new Message();
        message.text = record.text;
        message.time = toTime(record.timestamp);
        if (record.type.startsWith("image")) {
            File file = UrlUtil.getFile(record.assetKey, record.type);
            if (file.exists())
                message.image = String.format("file://%s", file.getAbsolutePath());
        }
        return message;
    }

    private Day newDay(Database.Record record, Sender sender) {
        Day day = new Day();
        day.date = toDate(record.timestamp);
        day.senders.add(sender);
        return day;
    }

    private Sender newSender(Database.Record record, Message message) {
        Sender sender = new Sender();
        sender.name = record.sender;
        sender.senderId = record.senderId;
        sender.avatar = toAvatar(record.senderId);
        sender.accent = toColor(record.accent);
        sender.messages.add(message);
        return sender;
    }

    @Nullable
    private String toAvatar(String senderId) {
        if (senderId == null)
            return null;

        String filename = avatarPath(senderId);
        File file = new File(filename);
        return String.format("file://%s", file.getAbsolutePath());
    }

    private String avatarPath(String senderId) {
        return String.format("avatars/%s.png", senderId);
    }

    private String toColor(int accent) {
        switch (accent) {
            case 0:
                return "blue";
            case 1:
                return "green";
            case 2:
                return "red";
            case 3:
                return "orange";
            case 4:
                return "pink";
            default:
                return "purple";
        }
    }

    private String toTime(long timestamp) {
        DateFormat df = new SimpleDateFormat("HH:mm");
        return df.format(new Date(timestamp * 1000L));
    }

    private String toDate(long timestamp) {
        DateFormat df = new SimpleDateFormat("dd MMM, yyyy");
        return df.format(new Date(timestamp * 1000L));
    }

    Conversation getConversation(String convName) {
        Conversation ret = new Conversation();
        ret.days = days;
        ret.title = convName;
        return ret;
    }

    void send(WireClient client, String userId) throws Exception {
        downloadProfiles();

        String convName = client.getConversation().name;
        Conversation conversation = getConversation(convName);
        String html = execute(conversation);
        String pdfFilename = String.format("pdf/%s.pdf", convName);
        File pdfFile = PdfGenerator.save(pdfFilename, html);
        client.sendDirectFile(pdfFile, "application/pdf", userId);
    }

    private void downloadProfiles() {
        for (Day day : days) {
            for (Sender sender : day.senders) {
                if (sender.senderId == null) {
                    Logger.warning("downloadProfiles: senderId=null. Day: %s, sender: %s, accent: %s",
                            day.date, sender.name, sender.accent);
                    continue;
                }
                try {
                    String filename = avatarPath(sender.senderId);
                    File file = new File(filename);
                    if (!file.exists()) {
                        byte[] profile = Helper.getProfile(UUID.fromString(sender.senderId));
                        try (DataOutputStream os = new DataOutputStream(new FileOutputStream(file))) {
                            if (profile != null)
                                os.write(profile);
                        }
                    }
                } catch (Exception e) {
                    Logger.warning("downloadProfiles: %s", e);
                }
            }
        }
    }

    private Mustache compileTemplate() {
        String path = "templates/conversation.html";
        return mf.compile(path);
    }

    private String execute(Object model) throws IOException {
        Mustache mustache = compileTemplate();
        try (StringWriter sw = new StringWriter()) {
            mustache.execute(new PrintWriter(sw), model).flush();
            return sw.toString();
        }
    }
}
