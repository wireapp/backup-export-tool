package com.wire.backups.exports;

import com.wire.backups.exports.utils.Collector;
import com.wire.backups.exports.utils.PdfGenerator;
import com.wire.backups.exports.utils.TestCache;
import com.wire.xenon.models.*;
import com.wire.xenon.tools.Logger;
import com.wire.xenon.tools.Util;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.UUID;

public class ConversationTemplateTest {
    public static final UUID dejan = UUID.fromString("40b96378-951d-11e9-bc42-526af7764f64");
    private static final UUID lipis = UUID.fromString("40b96896-951d-11e9-bc42-526af7764f64");
    private static final String CONV_NAME = "Recording Test";
    private static final String SRC_TEST_OUT = "src/test/resources";

    private static TextMessage txt(UUID userId, String time, String text) {
        TextMessage ret = new TextMessage(UUID.randomUUID(), UUID.randomUUID(), "", userId);
        ret.setText(text);
        ret.setTime(time);
        return ret;
    }

    private static ReactionMessage like(UUID userId, String emoji, String time, UUID msgId) {
        ReactionMessage ret = new ReactionMessage(UUID.randomUUID(), UUID.randomUUID(), "", userId);
        ret.setReactionMessageId(msgId);
        ret.setEmoji(emoji);
        ret.setTime(time);
        return ret;
    }

    private static TextMessage quote(UUID userId, String text, String time, UUID msgId) {
        TextMessage ret = new TextMessage(UUID.randomUUID(), UUID.randomUUID(), "", userId);
        ret.setQuotedMessageId(msgId);
        ret.setText(text);
        ret.setTime(time);
        return ret;
    }

    private static EditedTextMessage edit(UUID userId, String text, String time) {
        EditedTextMessage ret = new EditedTextMessage(UUID.randomUUID(), UUID.randomUUID(), "", userId);
        ret.setText(text);
        ret.setTime(time);
        return ret;
    }

    private static ImageMessage img(UUID userId, String time, String key, String mimeType) {
        ImageMessage ret = new ImageMessage(UUID.randomUUID(), UUID.randomUUID(), "", userId);
        ret.setAssetKey(key);
        ret.setMimeType(mimeType);
        ret.setTime(time);
        return ret;
    }

    private static VideoMessage vid(UUID userId, String time, String key, String mimeType) {
        VideoMessage ret = new VideoMessage(UUID.randomUUID(), UUID.randomUUID(), "", userId);
        ret.setAssetKey(key);
        ret.setMimeType(mimeType);
        ret.setTime(time);
        ret.setHeight(568);
        ret.setWidth(320);
        return ret;
    }

    private static AttachmentMessage attachment(UUID userId, String time, String key, String name, String mimeType) {
        AttachmentMessage ret = new AttachmentMessage(UUID.randomUUID(), UUID.randomUUID(), "", userId);
        ret.setAssetKey(key);
        ret.setMimeType(mimeType);
        ret.setTime(time);
        ret.setName(name);
        return ret;
    }

    private static LinkPreviewMessage link(UUID userId, String time, String text, String title, String url, String preview) {
        LinkPreviewMessage ret = new LinkPreviewMessage(UUID.randomUUID(), UUID.randomUUID(), "", userId);
        ret.setTime(time);
        ret.setTitle(title);
        ret.setText(text);
        ret.setUrl(url);
        ret.setAssetKey(preview);
        ret.setMimeType("image/png");
        return ret;
    }

    //@Before
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void clean() {
        String pdf = getFilename(CONV_NAME, "pdf");
        String html = getFilename(CONV_NAME, "html");

        new File(pdf).delete();
        new File(html).delete();
    }

    @Test
    public void templateTest() throws Exception {
        final String thursday = "2019-07-17T14:43:33.271Z";
        final String thursday2 = "2019-07-17T14:43:34.489Z";
        final String friday = "2019-07-18T21:12:03.144Z";
        final String friday2 = "2019-07-18T21:12:05.159Z";
        final String saturday = "2019-07-19T03:57:01.275Z";
        final String saturday2 = "2019-07-19T03:58:01.289Z";

        TestCache cache = new TestCache();
        Collector collector = new Collector(cache);
        collector.setConvName(CONV_NAME);

        collector.addSystem("**Dejo** started recording in **Recording test** with:\n- **Lipis**", thursday,
                "conversation.create", UUID.randomUUID());
        collector.addSystem("**Dejo** added **Lipis**", thursday2, "conversation.member-join", UUID.randomUUID());
        collector.addSystem("**Dejo** added **Lipis**", thursday2, "conversation.member-join", UUID.randomUUID());
        collector.add(txt(dejan, thursday, "Privet! Kak dela? 😃👍"));
        TextMessage normalna = txt(lipis, thursday, "Normalna");
        collector.add(normalna);
        collector.add(like(dejan, "❤️", thursday, normalna.getMessageId()));
        collector.add(like(lipis, "❤️", thursday, normalna.getMessageId()));

        collector.add(txt(lipis, thursday, "<head>"));
        collector.add(txt(dejan, thursday, "😃🐠😴🤧✍️👉👨‍🚒👨‍🏫👩‍👦👨‍👧‍👦🐥🐧🐾🐁🐕🎋🐲🐉"));
        collector.add(txt(dejan, thursday, "4"));
        TextMessage five = txt(lipis, thursday, "5 👍");
        collector.add(five);
        collector.add(like(dejan, "❤️", thursday, five.getMessageId()));
        collector.add(like(dejan, "", thursday, five.getMessageId()));
        collector.addEdit(edit(dejan, "This was an edit", thursday));

        collector.add(txt(lipis, thursday, "😃Lorem  \uD83D\uDC81\uD83C\uDFFB ipsum **dolor** sit amet, consectetur adipiscing elit, sed " +
                "do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam," +
                " quis nostrud exercitation ullamco _laboris_ nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum"));
        TextMessage seven = txt(lipis, friday, "7");
        collector.add(seven);
        collector.addSystem("**Dejo** deleted something", friday2, "conversation.otr-message-add.delete-text", UUID.randomUUID());
        collector.add(txt(lipis, saturday, "8"));
        collector.add(quote(dejan, "This was a quote", saturday, seven.getMessageId()));
        collector.add(img(lipis, saturday, "ognjiste2", "image/png"));
        collector.add(img(lipis, saturday, "small", "image/png"));
        collector.add(txt(dejan, saturday, "9"));
        collector.add(txt(dejan, saturday, "10"));
        collector.add(txt(lipis, saturday, "```This is some cool Java code here```"));
        collector.add(txt(dejan, saturday, "12"));
        collector.add(txt(lipis, saturday, "13"));
        collector.add(img(dejan, saturday, "ognjiste", "image/png"));
        collector.add(attachment(lipis, saturday, "Wire+Security+Whitepaper", "Wire Security Paper.pdf", "pdf"));
        collector.add(txt(lipis, saturday, "15"));
        collector.add(txt(dejan, saturday, "Lorem ipsum **dolor** sit amet, consectetur adipiscing elit, sed " +
                "do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam," +
                " quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
                "Excepteur sint occaecat cupidatat non _proident_, sunt in culpa qui officia deserunt mollit anim id est" +
                " laborum."));
        collector.add(txt(lipis, saturday, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed " +
                "do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam," +
                " quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est" +
                " laborum."));
        collector.add(txt(dejan, saturday, "This is some url [google](https://google.com)"));
        collector.add(txt(dejan, saturday, "https://wire.com"));
        collector.addLink(link(dejan,
                saturday,
                "Yo, check this link preview: https://wire.com. Totally without bugs!",
                "The most secure collaboration platform · Wire",
                "wire.com",
                "logo"));
        collector.add(txt(dejan, saturday, "This is some url https://google.com and some text"));
        collector.add(txt(dejan, saturday, "These two urls https://google.com https://wire.com"));
        collector.addSystem("**Dejo** removed **Lipis**", saturday2, "conversation.member-leave", UUID.randomUUID());
        collector.add(txt(dejan, saturday, "https://www.youtube.com/watch?v=rlR4PJn8b8I"));
        collector.add(vid(dejan, saturday, "panormos", "video/mp4"));

        Collector.Conversation conversation = collector.getConversation();
        File htmlFile = collector.executeFile(getFilename(conversation.getTitle(), "html"));
        Logger.info("Generated file: %s", htmlFile.getAbsolutePath());

        String html = Util.readFile(htmlFile);

        String pdfFilename = getFilename(conversation.getTitle(), "pdf");
        File pdfFile = PdfGenerator.save(pdfFilename, html, "file:src/test/resources");
        Logger.info("Generated file: %s", pdfFile.getAbsolutePath());
    }

    private String getFilename(String name, String extension) {
        return String.format("%s/%s.%s", SRC_TEST_OUT, name, extension);
    }
}
