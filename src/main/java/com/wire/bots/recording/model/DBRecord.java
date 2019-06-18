package com.wire.bots.recording.model;

import java.util.UUID;

public class DBRecord {
    public int size;
    public int height;
    public int width;
    public int timestamp;
    public int accent;
    public String sender;
    public UUID senderId;
    public String text;
    public String mimeType;
    public String assetKey;
    public String assetToken;
    public byte[] sha256;
    public byte[] otrKey;
    public String filename;
}
