package com.wire.bots.recording;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MessageTemplateTest {
    // ------------------- Tests -------------------
    @Test
    public void templateTest() throws Exception {
        Mustache mustache = compileTemplate("conversation.html");

        int thursday = 1552596670;
        int friday = 1552683070;
        int saturday = 1552769470;

        Collector collector = new Collector();
        collector.add(newRecord("Dejan", thursday, "1"));
        collector.add(newRecord("Lipis", thursday, "2"));
        collector.add(newRecord("Dejan", thursday, "3"));
        collector.add(newRecord("Dejan", thursday, "4"));
        collector.add(newRecord("Lipis", thursday, "5"));
        collector.add(newRecord("Lipis", thursday, "6"));
        collector.add(newRecord("Dejan", friday, "7"));
        collector.add(newRecord("Dejan", saturday, "8"));
        collector.add(newRecord("Dejan", saturday, "9"));
        collector.add(newRecord("Dejan", saturday, "10"));
        collector.add(newRecord("Lipis", saturday, "11"));
        collector.add(newRecord("Dejan", saturday, "12"));
        collector.add(newRecord("Lipis", saturday, "13"));
        collector.add(newRecord("Lipis", saturday, "14"));
        collector.add(newRecord("Lipis", saturday, "15"));

        Collector.Conversation conversation = collector.getConversation("export");
        String html = execute(mustache, conversation);
        assert html != null;

        String pdfFilename = String.format("%s.pdf", conversation.name);
        PdfGenerator.save(pdfFilename, html);
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

    private Database.Record newRecord(String name, int timestamp, String text) {
        Database.Record record = new Database.Record();
        record.sender = name;
        record.timestamp = timestamp;
        record.text = text;
        record.type = "txt";
        return record;
    }
}
