package com.wire.bots.recording.utils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.wire.bots.sdk.models.MessageAssetBase;
import com.wire.bots.sdk.models.ReactionMessage;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.User;

import javax.annotation.Nullable;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CollectorV2 {
    private static MustacheFactory mf = new DefaultMustacheFactory();
    private final CacheV2 cache;
    private LinkedList<Day> days = new LinkedList<>();
    private String convName;

    public CollectorV2(CacheV2 cache) {
        this.cache = cache;
    }

    private static Day newDay(Sender sender, String dateTime) throws ParseException {
        Day day = new Day();
        day.date = toDate(dateTime);
        day.senders.add(sender);
        return day;
    }

    private static String toColor(int accent) {
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

    static String toTime(String timestamp) throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(timestamp);
        DateFormat df = new SimpleDateFormat("HH:mm");
        return df.format(date);
    }

    static String toDate(String timestamp) throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(timestamp);
        DateFormat df = new SimpleDateFormat("dd MMM, yyyy");
        return df.format(date);
    }

    public void add(TextMessage event) throws ParseException {
        Message message = new Message();
        message.id = event.getMessageId();
        message.text = HelperV2.markdown2Html(event.getText(), true);
        message.time = toTime(event.getTime());

        User user = cache.getUser(event.getUserId());
        Sender sender = sender(user);
        sender.messages.add(message);

        append(sender, message, event.getTime());
    }

    public void add(MessageAssetBase event) throws ParseException {
        File file = cache.getAssetFile(event);
        if (file.exists()) {
            Message message = new Message();
            message.id = event.getMessageId();
            message.time = toTime(event.getTime());

            String assetFilename = getFilename(file, "images");

            String mimeType = event.getMimeType();
            if (mimeType.startsWith("image")) {
                message.image = assetFilename;
            } else {
                String url = String.format("<a href=\"%s\">%s</a>",
                        assetFilename,
                        event.getName());
                message.text = Helper.markdown2Html(url, false);
            }

            User user = cache.getUser(event.getUserId());

            Sender sender = sender(user);
            sender.messages.add(message);
            append(sender, message, event.getTime());
        }
    }

    public void add(ReactionMessage event) {
        String userName = getUserName(event.getUserId());
        UUID reactionMessageId = event.getReactionMessageId();
        String emoji = event.getEmoji();
        for (Day day : days) {
            for (Sender sender : day.senders) {
                for (Message msg : sender.messages) {
                    if (Objects.equals(msg.id, reactionMessageId)) {
                        if (msg.likes == null)
                            msg.likes = emoji;
                        msg.likes = String.format("%s %s ", msg.likes, userName);
                    }
                }
            }
        }
    }

    public void addSystem(String text, String dateTime, String type) throws ParseException {
        Message message = new Message();
        message.text = HelperV2.markdown2Html(text, true);
        message.time = toTime(dateTime);

        Sender sender = system(type);
        sender.messages.add(message);

        append(sender, message, dateTime);
    }

    private Sender sender(User user) {
        Sender sender = new Sender();
        sender.senderId = user.id;
        sender.name = user.name;
        sender.accent = toColor(user.accent);
        sender.avatar = getAvatar(user);
        return sender;
    }

    private Sender system(String type) {
        Sender sender = new Sender();
        sender.system = "system";
        sender.senderId = UUID.randomUUID();
        sender.avatar = systemIcon(type);
        return sender;
    }

    @Nullable
    private String systemIcon(String type) {
        final String base = "/recording/assets/";
        switch (type) {
            case "conversation.create":
            case "conversation.member-join":
                return base + "icons8-plus-24.png";
            case "conversation.member-leave":
                return base + "icons8-minus-24.png";
            case "conversation.rename":
            case "conversation.otr-message-add.edit-text":
                return base + "icons8-edit-30.png";
            case "conversation.otr-message-add.call":
                return base + "icons8-end-call-30.png";
            case "conversation.otr-message-add.delete-text":
                return base + "icons8-delete.png";
            default:
                return null;
        }
    }

    private void append(Sender sender, Message message, String dateTime) throws ParseException {
        Day day = newDay(sender, dateTime);

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

    private String getFilename(File file, String dir) {
        return String.format("/recording/%s/%s", dir, file.getName());
    }

    private String getAvatar(User user) {
        File file = cache.getProfileImage(user);
        String ret = String.format("/recording/%s/%s", "avatars", file.getName());
        return file.exists() ? ret : null;
    }

    public Conversation getConversation() {
        Conversation ret = new Conversation();
        ret.days = days;
        ret.title = convName;
        return ret;
    }

    public void setConvName(String convName) {
        this.convName = convName;
    }

    public String getUserName(UUID userId) {
        return cache.getUser(userId).name;
    }

    public CacheV2 getCache() {
        return cache;
    }

    public static class Conversation {
        LinkedList<Day> days = new LinkedList<>();
        String title;

        public String getTitle() {
            return title;
        }
    }

    private Mustache compileTemplate() {
        String path = "templates/conversation.html";
        return mf.compile(path);
    }


    public File executeFile(String filename) throws IOException {
        File file = new File(filename);
        try (FileWriter sw = new FileWriter(file)) {
            Mustache mustache = compileTemplate();
            Conversation conversation = getConversation();
            mustache.execute(new PrintWriter(sw), conversation).flush();
        }
        return file;
    }

    public String execute() throws IOException {
        Mustache mustache = compileTemplate();
        try (StringWriter sw = new StringWriter()) {
            Conversation conversation = getConversation();
            mustache.execute(new PrintWriter(sw), conversation).flush();
            return sw.toString();
        }
    }

    public static class Day {
        String date;
        LinkedList<Sender> senders = new LinkedList<>();

        boolean equals(Day d) {
            return Objects.equals(date, d.date);
        }
    }

    public static class Message {
        UUID id;
        String text;
        String image;
        String time;
        String likes;
    }

    public static class Sender {
        UUID senderId;
        String avatar;
        String name;
        String accent;
        String system;
        ArrayList<Message> messages = new ArrayList<>();

        boolean equals(Sender s) {
            return Objects.equals(senderId, s.senderId);
        }
    }
}
