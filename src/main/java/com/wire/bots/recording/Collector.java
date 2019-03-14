package com.wire.bots.recording;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.wire.bots.sdk.WireClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

class Collector {

    private LinkedList<Day> days = new LinkedList<>();

    void add(Database.Record record) {
        Message message = newMessage(record);
        Sender sender = newSender(record, message);
        Day day = newDay(record, sender);

        if (days.isEmpty() || !days.getLast().equals(day)) {
            days.add(day);
            return;
        }

        Day lastDay = days.getLast();
        Sender lastSender = lastDay.senders.getLast();
        if (lastSender.equals(sender)) {
            lastSender.messages.add(message);
        } else {
            lastDay.senders.add(sender);
        }
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
        sender.avatar = "todo";//todo
        sender.messages.add(message);
        return sender;
    }

    private Message newMessage(Database.Record record) {
        Message message = new Message();
        message.text = record.text;
        message.time = toTime(record.timestamp);
        return message;
    }

    private String toTime(long timestamp) {
        DateFormat df = new SimpleDateFormat("HH:mm");
        return df.format(new Date(timestamp * 1000L));
    }

    private String toDate(long timestamp) {
        DateFormat df = new SimpleDateFormat("dd MMM, yyyy");
        return df.format(new Date(timestamp * 1000L));
    }

    public Conversation getConversation() {
        Conversation ret = new Conversation();
        ret.days = days;
        return ret;
    }

    void send(WireClient client, String userId) throws Exception {
        Mustache mustache = compileTemplate("conversation.html");
        String html = execute(mustache, getConversation());
        String filename = "export.html";
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8))) {
            writer.write(html);
        }
        client.sendDirectFile(new File(filename), "text/html", userId);
    }

    private Mustache compileTemplate(String template) {
        MustacheFactory mf = new DefaultMustacheFactory();
        String path = String.format("templates/%s", template);
        return mf.compile(path);
    }

    private String execute(Mustache mustache, Object model) throws IOException {
        try (StringWriter sw = new StringWriter()) {
            mustache.execute(new PrintWriter(sw), model).flush();
            return sw.toString();
        }
    }

    static class Conversation {
        public LinkedList<Day> days = new LinkedList<>();
    }

    static class Day {
        public String date;
        public LinkedList<Sender> senders = new LinkedList<>();

        public boolean equals(Day d) {
            return date.equals(d.date);
        }
    }

    static class Sender {
        public String avatar;
        public String name;
        public ArrayList<Message> messages = new ArrayList<>();

        public boolean equals(Sender s) {
            return name.equals(s.name);
        }
    }

    static class Message {
        public String text;
        public String time;
    }
}
