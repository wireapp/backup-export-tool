package com.wire.bots.recording.model;


import java.util.ArrayList;
import java.util.UUID;

public class Sender {
    public UUID senderId;
    public String avatar;
    public String name;
    public String accent;
    public ArrayList<Message> messages = new ArrayList<>();

    public boolean equals(Sender s) {
        return senderId.equals(s.senderId);
    }
}
