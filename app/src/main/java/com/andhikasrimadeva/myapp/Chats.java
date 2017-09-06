package com.andhikasrimadeva.myapp;

/**
 * Created by Andhika on 06/09/2017.
 */

public class Chats {

    public Messages getMessages() {
        return messages;
    }

    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    public Chats(Messages messages) {
        this.messages = messages;
    }

    public Chats(){}

    private Messages messages;


}
