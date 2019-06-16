package com.wire.bots.recording.model;


import java.util.ArrayList;

public class Sender {
    public String senderId;
    public String avatar;
    public String name;
    public String accent;
    public ArrayList<Message> messages = new ArrayList<>();

    public boolean equals(Sender s) {
        return name.equals(s.name);
    }
}
