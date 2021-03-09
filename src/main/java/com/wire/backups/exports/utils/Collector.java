package com.wire.backups.exports.utils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.wire.xenon.backend.models.Asset;
import com.wire.xenon.backend.models.User;
import com.wire.xenon.models.*;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Collector {
    private static final String regex = "http(?:s)?://(?:www\\.)?youtu(?:\\.be/|be\\.com/(?:watch\\?v=|v/|embed/" +
            "|user/(?:[\\w#]+/)+))([^&#?\\n]+)";
    private static final Pattern p = Pattern.compile(regex);
    public final String root;
    private final MustacheFactory mf = new DefaultMustacheFactory();
    private final Cache cache;
    private final LinkedList<Day> days = new LinkedList<>();
    private final HashMap<UUID, Message> messagesHashMap = new HashMap<>();
    public Details details;
    private String convName;
    private UUID conversationId;

    public Collector(Cache cache, String root) {
        this.cache = cache;
        this.root = root;
    }

    public Collector(Cache cache) {
        this(cache, "recording");
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
        message.youTube = extractYouTube(event.getText());

        message.timeStamp = event.getTime();
        message.quotedMessage = toQuotedMessage(event);
        Sender sender = sender(event.getUserId());
        sender.add(message);

        return append(sender, message, event.getTime());
    }

    private String extractYouTube(String text) {
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public Sender addEdit(EditedTextMessage event) throws ParseException {
        addSystem("✏ Edited", event.getTime(), "", event.getMessageId());
        return add(event);
    }

    public Sender add(ImageMessage event) throws ParseException {
        Message message = new Message();
        message.id = event.getMessageId();
        message.timeStamp = event.getTime();

        File file = cache.getAssetFile(event);
        message.image = getFilename(file);

        Sender sender = sender(event.getUserId());
        sender.add(message);

        return append(sender, message, event.getTime());
    }

    public void add(VideoMessage event) throws ParseException {
        Message message = new Message();
        message.id = event.getMessageId();
        message.timeStamp = event.getTime();

        File file = cache.getAssetFile(event);
        message.video = new Video();
        message.video.url = getFilename(file);
        message.video.width = event.getWidth();
        message.video.height = event.getHeight();
        message.video.mimeType = event.getMimeType();

        Sender sender = sender(event.getUserId());
        sender.add(message);

        append(sender, message, event.getTime());
    }

    public Sender add(AttachmentMessage event) throws ParseException {
        Message message = new Message();
        message.id = event.getMessageId();
        message.timeStamp = event.getTime();

        File file = cache.getAssetFile(event);

        message.attachment = new Attachment();
        message.attachment.name = String.format("%s (%s)", event.getName(), event.getAssetKey());
        try {
            message.attachment.url = "file://" + file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            message.attachment.url = "file://" + file.getAbsolutePath();
        }

        Sender sender = sender(event.getUserId());
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
        message.text = Helper.markdown2Html(event.getText());

        Link link = new Link();
        link.title = event.getTitle();
        link.summary = event.getSummary();
        link.url = event.getUrl();

        File file = cache.getAssetFile(event);
        if (file.exists())
            link.preview = getFilename(file);

        message.link = link;

        Sender sender = sender(event.getUserId());
        sender.add(message);

        append(sender, message, event.getTime());
    }

    /**
     * Adds new message with _name_ `system` and avatar based on _type_. If the last message has the same timestamp as
     * this one then this message will not be added and FALSE is returned
     *
     * @return true if the message was added
     * @throws ParseException when it was not possible to parse
     */
    public boolean addSystem(String text, String dateTime, String type, UUID msgId) throws ParseException {
//        if (lastMessage != null && lastMessage.timeStamp.equals(dateTime))
//            return false;

        Message message = new Message();
        message.id = msgId;
        message.text = Helper.markdown2Html(text);
        message.timeStamp = dateTime;

        Sender sender = system(type);
        sender.add(message);

        append(sender, message, dateTime);
        return true;
    }

    private String getText(TextMessage event) {
        String text = event.getText();
        return Helper.markdown2Html(text);
    }

    @Nullable
    private Message toQuotedMessage(TextMessage event) {
        UUID id = event.getQuotedMessageId();
        return id != null ? messagesHashMap.get(id) : null;
    }

    private Sender sender(UUID userId) {
        User user = cache.getUser(userId);

        Sender sender = new Sender();
        sender.senderId = userId;
        sender.name = cache.getUserName(user);
        sender.accent = toColor(user.accent);
        sender.avatar = getAvatar(user.id);
        return sender;
    }

    private Sender system(String type) {
        Sender sender = new Sender();
        sender.system = "system";
        sender.senderId = UUID.randomUUID();
        sender.avatar = systemIcon(type);
        return sender;
    }

    private String systemIcon(String type) {
        final String base = String.format("/%s/assets/", root);
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
            case "conversation.otr-message-add.new-ping":
                return base + "icons8-sun-50.png";
            default:
                return "";
        }
    }

    private Sender append(Sender sender, Message message, String dateTime) throws ParseException {
        messagesHashMap.put(message.id, message);

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
        return String.format("/%s/%s/%s", root, "assets", file.getName());
    }

    @Nullable
    private String getAvatar(UUID userId) {
        User user = cache.getProfile(userId);
        String profileAssetKey = getProfileAssetKey(user);
        if (profileAssetKey != null) {
            File file = cache.getProfileImage(profileAssetKey);
            return String.format("/%s/%s/%s", root, "avatars", file.getName());
        }
        return null;
    }

    @Nullable
    private String getProfileAssetKey(User user) {
        if (user.assets == null) {
            return null;
        }

        for (Asset asset : user.assets) {
            if (asset.size.equals("preview")) {
                return asset.key;
            }
        }
        return null;
    }

    public Conversation getConversation() {
        Conversation ret = new Conversation();
        ret.days = days;
        ret.title = convName;
        ret.details = details;
        return ret;
    }

    @Nullable
    public UUID getConversationId() {
        return this.conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public String getConvName() {
        return convName;
    }

    public void setConvName(String convName) {
        this.convName = convName;
    }

    public String getUserName(UUID userId) {
        return cache.getUserName(userId);
    }

    public Cache getCache() {
        return cache;
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

    public static class Conversation {
        LinkedList<Day> days = new LinkedList<>();
        String title;
        Details details;

        public String getTitle() {
            return title;
        }
    }

    public static class Details {
        public String device;
        public String date;
        public String platform;
        public String handle;
        public String id;
        public String name;
        public String version;
    }

    public static class Day {
        String date;
        LinkedList<Sender> senders = new LinkedList<>();

        boolean equals(Day d) {
            return Objects.equals(date, d.date);
        }
    }

    public static class Message {
        private final HashSet<UUID> likers = new HashSet<>();
        UUID id;
        String name;
        String text;
        String youTube;
        String image;
        Video video;
        Link link;
        Attachment attachment;
        String timeStamp;
        String likes;
        Message quotedMessage;

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

    public static class Attachment {
        String name;
        String url;
    }

    public static class Video {
        int width;
        int height;
        String mimeType;
        String url;
    }

    public static class Sender {
        private final ArrayList<Message> messages = new ArrayList<>();
        UUID senderId;
        String avatar;
        String name;
        String accent;
        String system;

        boolean equals(Sender s) {
            return Objects.equals(senderId, s.senderId);
        }

        void add(Message message) {
            message.name = name;
            getMessages().add(message);
        }

        List<Message> getMessages() {
            return messages;
        }
    }
}
