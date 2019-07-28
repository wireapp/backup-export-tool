package com.wire.bots.recording.utils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.wire.bots.sdk.models.*;
import com.wire.bots.sdk.server.model.User;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CollectorV2 {
    private static final MustacheFactory mf = new DefaultMustacheFactory();
    private final CacheV2 cache;
    private final LinkedList<Day> days = new LinkedList<>();
    private final HashMap<UUID, Message> messagesHashMap = new HashMap<>();
    private Message lastMessage = null;
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

    private static String toTime(String timestamp) throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(timestamp);
        DateFormat df = new SimpleDateFormat("HH:mm");
        return df.format(date);
    }

    private static String toDate(String timestamp) throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(timestamp);
        DateFormat df = new SimpleDateFormat("dd MMM, yyyy");
        return df.format(date);
    }

    public Sender add(TextMessage event) throws ParseException {
        Message message = new Message();
        message.id = event.getMessageId();
        message.text = getText(event);
        message.timeStamp = event.getTime();
        message.quotedMessage = toQuotedMessage(event);
        User user = cache.getUser(event.getUserId());
        Sender sender = sender(user);
        sender.add(message);

        return append(sender, message, event.getTime());
    }

    public Sender addEdit(EditedTextMessage event) throws ParseException {
        Sender sender = add(event);
        sender.name += " ✏️";
        return sender;
    }

    public Sender add(MessageAssetBase event) throws ParseException {
        File file = cache.getAssetFile(event);
        Message message = new Message();
        message.id = event.getMessageId();
        message.timeStamp = event.getTime();
        String assetFilename = getFilename(file);

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
        sender.add(message);

        return append(sender, message, event.getTime());
    }

    public void add(ReactionMessage event) {
        UUID userId = event.getUserId();
        UUID reactionMessageId = event.getReactionMessageId();
        Message message = messagesHashMap.get(reactionMessageId);
        if (message != null) {
            if (event.getEmoji().isEmpty())
                message.likers.remove(userId);
            else
                message.likers.add(userId);

            ArrayList<String> names = new ArrayList<>();
            for (UUID id : message.likers)
                names.add(getUserName(id));

            message.likes = String.join(", ", names);
        }
    }


    public void addLink(LinkPreviewMessage event) throws ParseException {
        Message message = new Message();
        message.id = event.getMessageId();
        message.timeStamp = event.getTime();
        message.text = HelperV2.markdown2Html(event.getText());

        message.link = new Link();
        message.link.title = event.getTitle();
        message.link.summary = event.getSummary();
        message.link.url = event.getUrl();

        File file = cache.getAssetFile(event);
        if (file.exists())
            message.link.preview = getFilename(file);

        User user = cache.getUser(event.getUserId());

        Sender sender = sender(user);
        sender.add(message);

        append(sender, message, event.getTime());
    }

    /**
     * Adds new message with _name_ `system` and avatar based on _type_. If the last message has the same timestamp as
     * this one then this message will not be added and FALSE is returned
     *
     * @param text
     * @param dateTime
     * @param type
     * @param msgId
     * @return true if the message was added
     * @throws ParseException
     */
    public boolean addSystem(String text, String dateTime, String type, UUID msgId) throws ParseException {
        if (lastMessage != null && lastMessage.timeStamp.equals(dateTime))
            return false;

        Message message = new Message();
        message.id = msgId;
        message.text = HelperV2.markdown2Html(text);
        message.timeStamp = dateTime;

        Sender sender = system(type);
        sender.add(message);

        append(sender, message, dateTime);
        return true;
    }

    private String getText(TextMessage event) {
        String text = event.getText();
        return HelperV2.markdown2Html(text);
    }

    @Nullable
    private Message toQuotedMessage(TextMessage event) {
        UUID id = event.getQuotedMessageId();
        return id != null ? messagesHashMap.get(id) : null;
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
                return base + "icons8-record-48.png";
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
            case "conversation.member-leave.bot-removed":
                return base + "icons8-stop-squared-48.png";
            default:
                return null;
        }
    }

    private Sender append(Sender sender, Message message, String dateTime) throws ParseException {
        messagesHashMap.put(message.id, message);
        lastMessage = message;

        Day day = newDay(sender, dateTime);

        if (days.isEmpty()) {
            days.add(day);
            return days.getLast().senders.getLast();
        }

        Day lastDay = days.getLast();
        Sender lastSender = lastDay.senders.getLast();

        if (!lastDay.equals(day)) {
            days.add(day);
            return days.getLast().senders.getLast();
        }

        if (lastSender.equals(sender)) {
            lastSender.messages.add(message);
        } else {
            lastDay.senders.add(sender);
        }

        return days.getLast().senders.getLast();
    }

    private String getFilename(File file) {
        return String.format("/recording/%s/%s", "images", file.getName());
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
        try (OutputStreamWriter sw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            Mustache mustache = compileTemplate();
            Conversation conversation = getConversation();
            mustache.execute(new PrintWriter(sw), conversation).flush();
        }
        return file;
    }

    public String execute() throws IOException {
        try (StringWriter sw = new StringWriter()) {
            Mustache mustache = compileTemplate();
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
        String name;
        String text;
        String image;
        Link link;
        String timeStamp;
        String likes;
        Message quotedMessage;
        private HashSet<UUID> likers = new HashSet<>();

        String getTime() throws ParseException {
            return toTime(timeStamp);
        }

        String getDate() throws ParseException {
            return toDate(timeStamp);
        }
    }

    public static class Link {
        String title;
        String summary;
        String url;
        String preview;
    }

    public static class Sender {
        UUID senderId;
        String avatar;
        String name;
        String accent;
        String system;
        private ArrayList<Message> messages = new ArrayList<>();

        boolean equals(Sender s) {
            return Objects.equals(senderId, s.senderId);
        }

        public void add(Message message) {
            message.name = name;
            getMessages().add(message);
        }

        List<Message> getMessages() {
            return messages;
        }
    }
}
