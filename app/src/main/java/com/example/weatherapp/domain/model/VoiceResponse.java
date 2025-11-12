package com.example.weatherapp.domain.model;

import java.io.Serializable;

/**
 * Response model for Voice Weather Assistant
 * Contains both text and spoken response with metadata
 */
public class VoiceResponse implements Serializable {
    
    private final String spokenText;        // What the assistant will say
    private final String displayText;       // What to show on screen (optional, can be longer)
    private final boolean success;
    private final String errorMessage;
    
    // Constructor for success response
    public VoiceResponse(String spokenText, String displayText) {
        this.spokenText = spokenText;
        this.displayText = displayText != null ? displayText : spokenText;
        this.success = true;
        this.errorMessage = null;
    }
    
    // Constructor for simple response
    public VoiceResponse(String spokenText) {
        this(spokenText, spokenText);
    }
    
    // Constructor for error response
    public static VoiceResponse error(String errorMessage) {
        VoiceResponse response = new VoiceResponse();
        return response;
    }
    
    private VoiceResponse() {
        this.spokenText = "Sorry, I couldn't process that request.";
        this.displayText = "Error occurred";
        this.success = false;
        this.errorMessage = "Unknown error";
    }
    
    private VoiceResponse(String errorMsg, boolean isError) {
        this.spokenText = "Sorry, " + errorMsg;
        this.displayText = "Error: " + errorMsg;
        this.success = false;
        this.errorMessage = errorMsg;
    }
    
    public static VoiceResponse errorWithMessage(String errorMessage) {
        return new VoiceResponse(errorMessage, true);
    }
    
    // Getters
    public String getSpokenText() { return spokenText; }
    public String getDisplayText() { return displayText; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    
    @Override
    public String toString() {
        return "VoiceResponse{" +
                "spoken='" + spokenText + '\'' +
                ", success=" + success +
                '}';
    }
}
