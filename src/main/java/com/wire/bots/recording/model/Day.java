package com.wire.bots.recording.model;

import java.util.LinkedList;
import java.util.Objects;

public class Day {
    public String date;
    public LinkedList<Sender> senders = new LinkedList<>();

    public boolean equals(Day d) {
        return Objects.equals(date, d.date);
    }
}
