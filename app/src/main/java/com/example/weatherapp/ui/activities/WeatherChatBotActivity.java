package com.example.weatherapp.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WeatherChatBotActivity extends AppCompatActivity {

    private static final String TAG = "WeatherChatBot";
    private static final String MODEL_NAME = "gemini-2.5-flash";
    private static final String GEMINI_API_URL_PREFIX = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=";

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatMessage> messages;
    private EditText etMessage;
    private ImageButton btnSend;
    private View loadingIndicator;
    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_chat_bot);

        recyclerView = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        ImageButton btnBack = findViewById(R.id.btnBack);

        messages = new ArrayList<>();
        adapter = new ChatAdapter(messages);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Configure OkHttpClient with longer timeouts (same as ActivitySuggestionService)
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        // Add welcome message
        addBotMessage("Hello! I'm your weather assistant powered by Gemini AI. Ask me anything about weather!");

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> sendMessage());

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }

        // Add user message
        addUserMessage(message);
        etMessage.setText("");

        // Show loading
        loadingIndicator.setVisibility(View.VISIBLE);

        // Call Gemini API
        callGeminiAPI(message);
    }

    private void addUserMessage(String text) {
        ChatMessage message = new ChatMessage(text, true, getCurrentTime());
        messages.add(message);
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.smoothScrollToPosition(messages.size() - 1);
    }

    private void addBotMessage(String text) {
        ChatMessage message = new ChatMessage(text, false, getCurrentTime());
        messages.add(message);
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.smoothScrollToPosition(messages.size() - 1);
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void callGeminiAPI(String userMessage) {
        try {
            // Create request body
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            
            // Add system context about weather
            String enhancedMessage = "You are a helpful weather assistant. Answer questions about weather, climate, and provide weather-related advice. User question: " + userMessage;
            part.put("text", enhancedMessage);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            requestBody.put("contents", contents);

            // Add generation config (same as ActivitySuggestionService)
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 2048);
            generationConfig.put("topP", 0.9);
            generationConfig.put("topK", 40);
            requestBody.put("generationConfig", generationConfig);

            RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                .url(GEMINI_API_URL_PREFIX + BuildConfig.GEMINI_API_KEY)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "API call failed", e);
                    runOnUiThread(() -> {
                        loadingIndicator.setVisibility(View.GONE);
                        addBotMessage("Sorry, I couldn't connect to the server. Please check your internet connection.");
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "API error: " + response.code());
                        runOnUiThread(() -> {
                            loadingIndicator.setVisibility(View.GONE);
                            addBotMessage("Sorry, I encountered an error. Please try again.");
                        });
                        return;
                    }

                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        
                        // Parse Gemini response
                        JSONArray candidates = jsonResponse.getJSONArray("candidates");
                        if (candidates.length() > 0) {
                            JSONObject candidate = candidates.getJSONObject(0);
                            JSONObject content = candidate.getJSONObject("content");
                            JSONArray parts = content.getJSONArray("parts");
                            if (parts.length() > 0) {
                                String botResponse = parts.getJSONObject(0).getString("text");
                                
                                runOnUiThread(() -> {
                                    loadingIndicator.setVisibility(View.GONE);
                                    addBotMessage(botResponse);
                                });
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response", e);
                        runOnUiThread(() -> {
                            loadingIndicator.setVisibility(View.GONE);
                            addBotMessage("Sorry, I couldn't understand the response.");
                        });
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error creating request", e);
            loadingIndicator.setVisibility(View.GONE);
            addBotMessage("Sorry, an error occurred. Please try again.");
        }
    }

    // Chat Message Model
    private static class ChatMessage {
        String text;
        boolean isUser;
        String time;

        ChatMessage(String text, boolean isUser, String time) {
            this.text = text;
            this.isUser = isUser;
            this.time = time;
        }
    }

    // Chat Adapter
    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_USER = 1;
        private static final int VIEW_TYPE_BOT = 2;

        private final List<ChatMessage> messages;

        ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).isUser ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_USER) {
                View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message_user, parent, false);
                return new UserMessageViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message_bot, parent, false);
                return new BotMessageViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ChatMessage message = messages.get(position);
            if (holder instanceof UserMessageViewHolder) {
                ((UserMessageViewHolder) holder).bind(message);
            } else {
                ((BotMessageViewHolder) holder).bind(message);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class UserMessageViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage, tvTime;

            UserMessageViewHolder(View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvTime = itemView.findViewById(R.id.tvTime);
            }

            void bind(ChatMessage message) {
                tvMessage.setText(message.text);
                tvTime.setText(message.time);
            }
        }

        class BotMessageViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage, tvTime;

            BotMessageViewHolder(View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvTime = itemView.findViewById(R.id.tvTime);
            }

            void bind(ChatMessage message) {
                tvMessage.setText(message.text);
                tvTime.setText(message.time);
            }
        }
    }
}
