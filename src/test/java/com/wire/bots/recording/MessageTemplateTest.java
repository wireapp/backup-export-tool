package com.wire.bots.recording;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.wire.bots.recording.model.Conversation;
import com.wire.bots.recording.model.DBRecord;
import com.wire.bots.recording.utils.Collector;
import com.wire.bots.recording.utils.PdfGenerator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.UUID;

public class MessageTemplateTest {

    private static final String DEJAN = "Dejan";
    private static final String LIPIS = "Lipis";
    private static final UUID dejan = UUID.fromString("40b96378-951d-11e9-bc42-526af7764f64");
    private static final UUID lipis = UUID.fromString("40b96896-951d-11e9-bc42-526af7764f64");

    private static DBRecord newTxtRecord(UUID id, String name, int timestamp, String text) {
        DBRecord record = new DBRecord();
        record.sender = name;
        record.senderId = id;
        record.timestamp = timestamp;
        record.text = text;
        record.mimeType = "txt";
        record.accent = id.equals(dejan) ? 3 : 1;
        return record;
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

    private static DBRecord newImageRecord(UUID id, String name, int timestamp, String key, String type) {
        DBRecord record = new DBRecord();
        record.senderId = id;
        record.sender = name;
        record.timestamp = timestamp;
        record.assetKey = key;
        record.mimeType = type;
        record.accent = id.equals(dejan) ? 3 : 1;
        return record;
    }

    // ------------------- Tests -------------------
    @Test
    public void templateTest() throws Exception {
        Mustache mustache = compileTemplate("conversation.html");

        final int thursday = 1552596670;
        final int friday = 1552683070;
        final int saturday = 1552769470;

        TestWireClient client = new TestWireClient();
        Collector collector = new Collector(client);
        collector.add(newTxtRecord(dejan, DEJAN, thursday, "1😃👍"));
        collector.add(newTxtRecord(lipis, LIPIS, thursday, "<head>"));
        collector.add(newTxtRecord(dejan, DEJAN, thursday, "😃🐠😴🤧✍️👉👨‍🚒👨‍🏫👩‍👦👨‍👧‍👦🐥🐧🐾🐁🐕🎋🐲🐉"));
        collector.add(newTxtRecord(dejan, DEJAN, thursday, "4"));
        collector.add(newTxtRecord(lipis, LIPIS, thursday, "5 👍"));
        collector.add(newTxtRecord(lipis, LIPIS, thursday, "😃Lorem ipsum **dolor** sit amet, consectetur adipiscing elit, sed " +
                "do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam," +
                " quis nostrud exercitation ullamco _laboris_ nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum"));
        collector.add(newTxtRecord(dejan, DEJAN, friday, "7"));
        collector.add(newTxtRecord(lipis, LIPIS, saturday, "8"));
        collector.add(newImageRecord(lipis, LIPIS, saturday, "ognjiste2", "image/png"));
        collector.add(newImageRecord(lipis, LIPIS, saturday, "small", "image/png"));
        collector.add(newTxtRecord(dejan, DEJAN, saturday, "9"));
        collector.add(newTxtRecord(dejan, DEJAN, saturday, "10"));
        collector.add(newTxtRecord(lipis, LIPIS, saturday, "```collector.add(newImageRecord(dejan, DEJAN, friday," +
                " \"SP\", \"image/jpeg\"));\n" +
                "        collector.add(newTxtRecord(dejan, DEJAN, friday, \"7\"));\n" +
                "        collector.add(newTxtRecord(lipis, LIPIS, saturday, \"8\"));\n" +
                "        collector.add(newImageRecord(lipis, LIPIS, saturday, \"ognjiste2\", \"image/png\"));\n" +
                "        collector.add(newImageRecord(lipis, LIPIS, saturday, \"small\", \"image/png\"));\n" +
                "```"));
        collector.add(newTxtRecord(dejan, DEJAN, saturday, "12"));
        collector.add(newTxtRecord(lipis, LIPIS, saturday, "13"));
        collector.add(newImageRecord(dejan, DEJAN, saturday, "ognjiste", "image/png"));
        collector.add(newTxtRecord(lipis, LIPIS, saturday, "14"));
        collector.add(newTxtRecord(lipis, LIPIS, saturday, "15"));
        collector.add(newTxtRecord(dejan, DEJAN, saturday, "Lorem ipsum **dolor** sit amet, consectetur adipiscing elit, sed " +
                "do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam," +
                " quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
                "Excepteur sint occaecat cupidatat non _proident_, sunt in culpa qui officia deserunt mollit anim id est" +
                " laborum."));
        collector.add(newTxtRecord(lipis, LIPIS, saturday, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed " +
                "do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam," +
                " quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est" +
                " laborum."));
        collector.add(newTxtRecord(dejan, DEJAN, saturday, "This is some url [google](https://google.com)"));
        collector.add(newTxtRecord(dejan, DEJAN, saturday, "https://google.com"));
        collector.add(newTxtRecord(dejan, DEJAN, saturday, "This is some url https://google.com and some text"));
        collector.add(newTxtRecord(dejan, DEJAN, saturday, "These two urls https://google.com https://wire.com"));

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

    //@Test
    public void test() {
        try (PDDocument doc = new PDDocument()) {
            PDFont font = PDType0Font.load(doc, new File("src/main/resources/fonts/NotoEmoji-Regular.ttf"));
            PDPage page = new PDPage();
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            cs.beginText();
            cs.newLineAtOffset(0, 700);
            cs.setFont(font, 20);
            String s = "😃🐠😴🤧✍️👉👨‍🚒👨‍🏫👩‍👦👨‍👧‍👦🐥🐧🐾🐁🐕🎋🐲🐉";
            for (int i = 0; i < s.length() - 1; ++i) {
                String s1 = new String(new int[]{s.codePointAt(i)}, 0, 1);
                try {
                    cs.showText(s1);
                } catch (IllegalArgumentException ex) {
                    //cs.showText(" ");
                }
            }
            cs.endText();
            cs.close();
            doc.save(new File("emojis.pdf"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
