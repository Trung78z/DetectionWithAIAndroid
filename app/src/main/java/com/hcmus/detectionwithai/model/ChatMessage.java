package com.hcmus.detectionwithai.model;

public class ChatMessage {
    public static final int SENDER_USER = 0;
    public static final int SENDER_AI = 1;

    private String message;
    private int sender;

    public ChatMessage(String message, int sender) {
        this.message = message;
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public int getSender() {
        return sender;
    }
}