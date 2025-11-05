package com.example.weatherapp;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface OpenAIService {
    @POST("v1/chat/completions")
    Call<OpenAIResponse> getChatCompletion(
            @Header("Authorization") String authorization,
            @Body OpenAIRequest request
    );
}

