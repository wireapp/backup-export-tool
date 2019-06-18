package com.wire.bots.recording;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.wire.bots.recording.model.Conversation;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class MessageTemplateTest {
    // ------------------- Tests -------------------
    @Test
    public void templateTest() throws Exception {
        Mustache mustache = compileTemplate("conversation.html");

        int thursday = 1552596670;
        int friday = 1552683070;
        int saturday = 1552769470;

        Collector collector = new Collector();
        collector.add(newTxtRecord("Dejan", thursday, "1"));
        collector.add(newTxtRecord("Lipis", thursday, "2"));
        collector.add(newTxtRecord("Dejan", thursday, "3"));
        collector.add(newTxtRecord("Dejan", thursday, "4"));
        collector.add(newTxtRecord("Lipis", thursday, "5"));
        collector.add(newTxtRecord("Lipis", thursday, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed " +
                "do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam," +
                " quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum"));
        collector.add(newImageRecord("Dejan", friday, "SP", "image/jpeg"));
        collector.add(newTxtRecord("Dejan", friday, "7"));
        collector.add(newTxtRecord("Lipis", saturday, "8"));
        collector.add(newImageRecord("Lipis", saturday, "ognjiste2", "image/png"));
        collector.add(newImageRecord("Lipis", saturday, "small", "image/png"));
        collector.add(newTxtRecord("Dejan", saturday, "9"));
        collector.add(newTxtRecord("Dejan", saturday, "10"));
        collector.add(newTxtRecord("Lipis", saturday, "11"));
        collector.add(newTxtRecord("Dejan", saturday, "12"));
        collector.add(newTxtRecord("Lipis", saturday, "13"));
        collector.add(newImageRecord("Dejan", saturday, "ognjiste", "image/png"));
        collector.add(newTxtRecord("Lipis", saturday, "14"));
        collector.add(newTxtRecord("Lipis", saturday, "15"));
        collector.add(newTxtRecord("Dejan", saturday, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed " +
                "do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam," +
                " quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est" +
                " laborum."));
        collector.add(newTxtRecord("Lipis", saturday, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed " +
                "do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam," +
                " quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est" +
                " laborum."));
        collector.add(newImageRecord("Dejan", saturday, "Praha", "image/jpeg"));
        collector.add(newTxtRecord("Dejan", saturday, "This is some url https://google.com"));
        collector.add(newTxtRecord("Dejan", saturday, "https://google.com"));
        collector.add(newTxtRecord("Dejan", saturday, "This is some url https://google.com and some text"));
        collector.add(newTxtRecord("Dejan", saturday, "These two urls https://google.com https://wire.com"));

        Conversation conversation = collector.getConversation("export");
        String html = execute(mustache, conversation);
        assert html != null;

        String pdfFilename = String.format("%s.pdf", conversation.title);
        PdfGenerator.save(pdfFilename, html);

        File file = new File(String.format("%s.html", conversation.title));
        try (DataOutputStream os = new DataOutputStream(new FileOutputStream(file))) {
            os.write(html.getBytes());
        }
    }

    // ------------------- Tests -------------------

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
            Assert.assertTrue(mustache.getName(), false);
            return null;
        }
    }

    private Database.Record newTxtRecord(String name, int timestamp, String text) {
        Database.Record record = new Database.Record();
        record.sender = name;
        record.senderId = name;
        record.timestamp = timestamp;
        record.text = text;
        record.type = "txt";
        record.accent = name.equalsIgnoreCase("Dejan") ? 3 : 1;
        return record;
    }

    private Database.Record newImageRecord(String name, int timestamp, String key, String type) {
        Database.Record record = new Database.Record();
        record.sender = name;
        record.senderId = name;
        record.timestamp = timestamp;
        record.assetKey = key;
        record.type = type;
        record.accent = name.equalsIgnoreCase("Dejan") ? 3 : 1;
        return record;
    }
}
