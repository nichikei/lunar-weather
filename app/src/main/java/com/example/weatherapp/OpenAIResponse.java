package com.example.weatherapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OpenAIResponse {
    @SerializedName("choices")
    private List<Choice> choices;

    public List<Choice> getChoices() {
        return choices;
    }

    public static class Choice {
        @SerializedName("message")
        private Message message;

        public Message getMessage() {
            return message;
        }
    }

    public static class Message {
        @SerializedName("role")
        private String role;

        @SerializedName("content")
        private String content;

        public String getContent() {
            return content;
        }
    }
}

