package com.wire.bots.recording;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.wire.bots.recording.utils.CollectorV2;
import com.wire.bots.recording.utils.PdfGenerator;
import com.wire.bots.recording.utils.TestCacheV2;
import com.wire.bots.sdk.models.EditedTextMessage;
import com.wire.bots.sdk.models.MessageAssetBase;
import com.wire.bots.sdk.models.ReactionMessage;
import com.wire.bots.sdk.models.TextMessage;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.UUID;

public class ConversationTemplateTest {
    public static final UUID dejan = UUID.fromString("40b96378-951d-11e9-bc42-526af7764f64");
    private static final UUID lipis = UUID.fromString("40b96896-951d-11e9-bc42-526af7764f64");

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

    private static EditedTextMessage edit(UUID userId, String edit, String time) {
        EditedTextMessage ret = new EditedTextMessage(UUID.randomUUID(), UUID.randomUUID(), "", userId);
        ret.setText(edit);
        ret.setTime(time);
        return ret;
    }

    private static MessageAssetBase img(UUID userId, String time, String key, String mimeType) {
        MessageAssetBase ret = new MessageAssetBase(UUID.randomUUID(), UUID.randomUUID(), "", userId);
        ret.setAssetKey(key);
        ret.setMimeType(mimeType);
        ret.setTime(time);
        return ret;
    }

    private Mustache compileTemplate(String template) {
        MustacheFactory mf = new DefaultMustacheFactory();
        String path = String.format("templates/%s", template);
        Mustache mustache = mf.compile(path);
        Assert.assertNotNull(path, mustache);
        return mustache;
    }

    private String execute(Mustache mustache, Object model) {
        try (StringWriter sw = new StringWriter()) {
            mustache.execute(new PrintWriter(sw), model).flush();
            return sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void templateTest() throws Exception {
        Mustache mustache = compileTemplate("conversation.html");

        final String thursday = "2019-07-17T14:43:33.179Z";
        final String friday = "2019-07-18T21:12:03.149Z";
        final String saturday = "2019-07-19T03:57:01.279Z";

        TestCacheV2 cache = new TestCacheV2();
        CollectorV2 collector = new CollectorV2(cache);
        collector.setConvName("Recording Test");

        collector.addSystem("**Dejo** started recording in **Recording test** with:\n- **Lipis**", thursday, "conversation.create");
        collector.addSystem("**Dejo** added **Lipis**", thursday, "conversation.member-join");
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

        collector.add(txt(lipis, thursday, "😃Lorem ipsum **dolor** sit amet, consectetur adipiscing elit, sed " +
                "do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam," +
                " quis nostrud exercitation ullamco _laboris_ nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum"));
        TextMessage seven = txt(lipis, friday, "7");
        collector.add(seven);
        collector.addSystem("**Dejo** deleted something", friday, "conversation.otr-message-add.delete-text");
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
        collector.add(txt(lipis, saturday, "14"));
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
        collector.add(txt(dejan, saturday, "https://google.com"));
        collector.add(txt(dejan, saturday, "This is some url https://google.com and some text"));
        collector.add(txt(dejan, saturday, "These two urls https://google.com https://wire.com"));
        collector.addSystem("**Dejo** removed **Lipis**", saturday, "conversation.member-leave");

        CollectorV2.Conversation conversation = collector.getConversation();
        String html = execute(mustache, conversation);
        assert html != null;

        String pdfFilename = String.format("src/test/out/%s.pdf", conversation.getTitle());
        PdfGenerator.save(pdfFilename, html, "file:src/test/resources");

        File file = new File(String.format("src/test/out/%s.html", conversation.getTitle()));
        try (DataOutputStream os = new DataOutputStream(new FileOutputStream(file))) {
            os.write(html.getBytes());
        }
    }
}
