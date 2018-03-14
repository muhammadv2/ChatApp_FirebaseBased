package com.muhammadv2.pm_me;


public class Message {

    private String senderName;
    private String messageBody;
    private String imageUrl;

    public Message(String senderName, String messageBody, String imageUrl) {
        this.senderName = senderName;
        this.messageBody = messageBody;
        this.imageUrl = imageUrl;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
