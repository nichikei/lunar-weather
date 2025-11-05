package com.example.weatherapp.data.api;

import com.example.weatherapp.data.responses.OpenAIRequest;
import com.example.weatherapp.data.responses.OpenAIResponse;

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

