package com.wire.bots.recording;

import com.wire.bots.recording.model.DBRecord;
import com.wire.bots.recording.model.Day;
import org.junit.Test;

import java.util.LinkedList;
import java.util.UUID;

public class CollectorTest {

    private static final UUID dejan = UUID.fromString("40b96378-951d-11e9-bc42-526af7764f64");
    private static final UUID lipis = UUID.fromString("40b96896-951d-11e9-bc42-526af7764f64");
    private static final String DEJAN = "Dejan";
    private static final String LIPIS = "Lipis";

    @Test
    public void collect() {
        int thursday = 1552596670;
        int friday = 1552683070;
        int saturday = 1552769470;

        Collector collector = new Collector();
        collector.add(newRecord(dejan, DEJAN, thursday, "1"));
        collector.add(newRecord(lipis, LIPIS, thursday, "2"));
        collector.add(newRecord(dejan, DEJAN, thursday, "3"));
        collector.add(newRecord(dejan, DEJAN, thursday, "4"));
        collector.add(newRecord(lipis, LIPIS, thursday, "5"));
        collector.add(newRecord(lipis, LIPIS, thursday, "6"));
        collector.add(newRecord(dejan, DEJAN, friday, "7"));
        collector.add(newRecord(dejan, DEJAN, saturday, "8"));
        collector.add(newRecord(dejan, DEJAN, saturday, "9"));
        collector.add(newRecord(dejan, DEJAN, saturday, "10"));
        collector.add(newRecord(lipis, LIPIS, saturday, "11"));
        collector.add(newRecord(dejan, DEJAN, saturday, "12"));
        collector.add(newRecord(lipis, LIPIS, saturday, "13"));
        collector.add(newRecord(lipis, LIPIS, saturday, "14"));
        collector.add(newRecord(lipis, LIPIS, saturday, "15"));

        LinkedList<Day> days = collector.getConversation("Test").days;
        assert days.size() == 3;
        assert days.getFirst().senders.size() == 4;
        assert days.getFirst().senders.getFirst().senderId.equals(dejan);
        assert days.getFirst().senders.getLast().senderId.equals(lipis);

        assert days.getLast().senders.size() == 4;
        assert days.getLast().senders.getFirst().senderId.equals(dejan);
        assert days.getLast().senders.getLast().senderId.equals(lipis);
    }

    private DBRecord newRecord(UUID id, String name, int timestamp, String text) {
        DBRecord record = new DBRecord();
        record.senderId = id;
        record.sender = name;
        record.timestamp = timestamp;
        record.text = text;
        record.mimeType = "txt";
        return record;
    }
}
