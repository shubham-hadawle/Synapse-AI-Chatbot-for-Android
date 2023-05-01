package com.shubhamslab.brainshopai;

public class ChatsModel {
    public static String SENT_BY_USER = "user";
    public static String SENT_BY_CHATBOT = "chatbot";

    String message;
    String sentBy;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public ChatsModel(String message, String sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }
}
