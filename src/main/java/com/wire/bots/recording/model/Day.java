package com.wire.bots.recording.model;

import java.util.LinkedList;

public class Day {
    public String date;
    public LinkedList<Sender> senders = new LinkedList<>();

    public boolean equals(Day d) {
        return date.equals(d.date);
    }
}
