package com.wire.bots.recording;

import org.junit.Test;

import java.util.LinkedList;

public class CollectorTest {

    @Test
    public void collect() {
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

        LinkedList<Collector.Day> days = collector.getConversation().days;
        assert days.size() == 3;
        assert days.getFirst().senders.size() == 4;
        assert days.getFirst().senders.getFirst().name.equals("Dejan");
        assert days.getFirst().senders.getLast().name.equals("Lipis");

        assert days.getLast().senders.size() == 4;
        assert days.getLast().senders.getFirst().name.equals("Dejan");
        assert days.getLast().senders.getLast().name.equals("Lipis");
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
