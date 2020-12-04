package com.devlin.chatbot;

public class Request {
    private final String responseContent;

    public Request(String responseContent) {
        this.responseContent = responseContent;
    }

    public String getResponseContent() {
        return responseContent;
    }
}
