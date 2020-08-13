package com.wire.backups.exports.exporters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class TimedMessagesExecutor {
    private final SortedMap<Long, List<Runnable>> timedMessages = new TreeMap<>();

    private static Long timeToMillis(String timestamp) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(timestamp).getTime();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void add(String timestamp, Runnable r) {
        final Long time = timeToMillis(timestamp);
        if (time == null) {
            // it was not possible to parse time
            return;
        }

        final List<Runnable> runnables = timedMessages.getOrDefault(time, new LinkedList<>());
        runnables.add(r);
        timedMessages.put(time, runnables);
    }

    public void execute() {
        timedMessages.forEach((timestamp, actions) -> actions.forEach(Runnable::run));
        timedMessages.clear();
    }
}
