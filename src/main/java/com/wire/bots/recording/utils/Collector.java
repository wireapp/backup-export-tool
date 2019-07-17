package com.wire.bots.recording.utils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.wire.bots.recording.model.*;
import com.wire.bots.sdk.WireClient;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

import static com.wire.bots.recording.utils.Helper.markdown2Html;

public class Collector {
    private static MustacheFactory mf = new DefaultMustacheFactory();
    private final WireClient client;

    private LinkedList<Day> days = new LinkedList<>();

    public Collector(WireClient client) {

        this.client = client;
    }

    private static String toUrl(File file) {
        String url = null;
        if (file != null && file.exists())
            url = String.format("file://%s", file.getAbsolutePath());
        return url;
    }

    public void add(DBRecord record) {
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

    private Message newMessage(DBRecord record) {
        Message message = new Message();
        message.time = toTime(record.timestamp);

        if (record.mimeType.equals("txt")) {
            message.text = markdown2Html(record.text, true);
        }

        if (record.mimeType.startsWith("image")) {
            File file = Cache.downloadImage(client, record);
            message.image = toUrl(file);
        }
        return message;
    }

    private Day newDay(DBRecord record, Sender sender) {
        Day day = new Day();
        day.date = toDate(record.timestamp);
        day.senders.add(sender);
        return day;
    }

    private Sender newSender(DBRecord record, Message message) {
        Sender sender = new Sender();
        sender.name = record.sender;
        sender.senderId = record.senderId;
        sender.accent = toColor(record.accent);
        sender.messages.add(message);
        sender.avatar = getAvatar(record.senderId);
        return sender;
    }

    @Nullable
    private String getAvatar(@Nullable UUID senderId) {
        if (senderId != null) {
            File profile = Cache.getProfile(senderId);
            return toUrl(profile);
        }
        return null;
    }

    private String toColor(int accent) {
        switch (accent) {
            case 1:
                return "#2391d3";
            case 2:
                return "#00c800";
            case 3:
                return "#febf02";
            case 4:
                return "#fb0807";
            case 5:
                return "#ff8900";
            case 6:
                return "#fe5ebd";
            default:
                return "#9c00fe";
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

    public Conversation getConversation(String convName) {
        Conversation ret = new Conversation();
        ret.days = days;
        ret.title = convName;
        return ret;
    }

    public void sendPDF(UUID userId) throws Exception {
        String convName = client.getConversation().name;
        Conversation conversation = getConversation(convName);
        String html = execute(conversation);
        String filename = URLEncoder.encode(convName);
        String pdfFilename = String.format("pdf/%s.pdf", filename);
        File pdfFile = PdfGenerator.save(pdfFilename, html);
        client.sendDirectFile(pdfFile, "application/pdf", userId.toString());
    }

    public void sendHtml(UUID userId) throws Exception {
        String convName = client.getConversation().name;
        Conversation conversation = getConversation(convName);
        String clean = URLEncoder.encode(convName);
        String filename = String.format("html/%s.html", clean);
        executeFile(conversation, filename);
        File file = new File(filename);
        client.sendDirectFile(file, "application/html", userId.toString());
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

    private void executeFile(Object model, String filename) throws IOException {
        Mustache mustache = compileTemplate();
        try (FileWriter sw = new FileWriter(filename)) {
            mustache.execute(new PrintWriter(sw), model).flush();
        }
    }
}
