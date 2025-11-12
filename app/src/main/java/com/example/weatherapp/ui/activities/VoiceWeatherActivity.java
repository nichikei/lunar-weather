package com.example.weatherapp.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.weatherapp.R;
import com.example.weatherapp.domain.model.VoiceQuery;
import com.example.weatherapp.domain.model.VoiceResponse;
import com.example.weatherapp.domain.model.WeatherData;
import com.example.weatherapp.domain.services.VoiceWeatherService;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Voice Weather Assistant Activity
 * Allows users to ask weather questions using voice and get spoken responses
 */
public class VoiceWeatherActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    
    private static final String TAG = "VoiceWeatherActivity";
    private static final int REQUEST_RECORD_PERMISSION = 100;
    
    // UI Components
    private ImageButton btnMicrophone;
    private ImageView ivVoiceAnimation;
    private TextView tvListening;
    private TextView tvYouSaid;
    private TextView tvResponse;
    private ProgressBar progressBar;
    private View layoutListening;
    private View layoutResponse;
    
    // Voice Services
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private VoiceWeatherService voiceService;
    
    // Weather data (passed from MainActivity)
    private WeatherData currentWeatherData;
    
    // State
    private boolean isListening = false;
    private boolean isTTSReady = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_weather);
        
        initializeViews();
        initializeServices();
        setupListeners();
        checkPermissions();
        
        // Show initial hint
        showHint();
    }
    
    private void initializeViews() {
        btnMicrophone = findViewById(R.id.btnMicrophone);
        ivVoiceAnimation = findViewById(R.id.ivVoiceAnimation);
        tvListening = findViewById(R.id.tvListening);
        tvYouSaid = findViewById(R.id.tvYouSaid);
        tvResponse = findViewById(R.id.tvResponse);
        progressBar = findViewById(R.id.progressBar);
        layoutListening = findViewById(R.id.layoutListening);
        layoutResponse = findViewById(R.id.layoutResponse);
        
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void initializeServices() {
        // Get weather data from Intent (passed from MainActivity)
        currentWeatherData = (WeatherData) getIntent().getSerializableExtra("weather_data");
        
        if (currentWeatherData == null) {
            // If no data passed, show error and close
            Toast.makeText(this, "Weather data not available. Please wait for weather to load.", 
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Initialize Voice Service
        voiceService = new VoiceWeatherService(this);
        
        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(this, this);
        
        // Initialize Speech Recognizer
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new VoiceRecognitionListener());
            Log.d(TAG, "Speech recognizer initialized successfully");
        } else {
            // Speech recognition not available - show helpful message
            Log.e(TAG, "Speech recognition not available on this device");
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Speech Recognition Not Available")
                    .setMessage("Your device doesn't have Google Speech Recognition.\n\n" +
                            "To use Voice Assistant:\n" +
                            "1. Install 'Google' app from Play Store\n" +
                            "2. Update Google app to latest version\n" +
                            "3. Enable voice input in system settings\n\n" +
                            "For now, you can type your questions instead.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Enable typing mode as fallback
                        enableTypingMode();
                    })
                    .setNegativeButton("Exit", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
    }
    
    private void setupListeners() {
        btnMicrophone.setOnClickListener(v -> {
            if (isListening) {
                stopListening();
            } else {
                startListening();
            }
        });
    }
    
    private void showHint() {
        tvResponse.setText("🎙️ Tap the microphone and ask me about the weather!\n\n" +
                "Try asking:\n" +
                "• \"What's the weather like?\"\n" +
                "• \"Should I bring an umbrella?\"\n" +
                "• \"What should I wear?\"\n" +
                "• \"What's the temperature?\"\n" +
                "• \"Is it going to rain?\"");
    }
    
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_PERMISSION);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission required for voice assistant",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    
    /**
     * Start listening for voice input
     */
    private void startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
            return;
        }
        
        if (speechRecognizer == null) {
            // If speech recognizer not available, use typing mode
            Toast.makeText(this, "Speech recognition not available. Using typing mode.", Toast.LENGTH_SHORT).show();
            enableTypingMode();
            return;
        }
        
        isListening = true;
        updateUI(UIState.LISTENING);
        
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask me about the weather...");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5); // Increased for better accuracy
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true); // Enable partial results
        
        speechRecognizer.startListening(intent);
        Log.d(TAG, "Started listening...");
        Toast.makeText(this, "🎙️ Listening... Speak clearly!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Stop listening
     */
    private void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            updateUI(UIState.IDLE);
        }
    }
    
    /**
     * Process recognized speech
     */
    private void processVoiceInput(String voiceText) {
        Log.d(TAG, "Processing voice input: " + voiceText);
        
        tvYouSaid.setText("You: " + voiceText);
        tvYouSaid.setVisibility(View.VISIBLE);
        
        updateUI(UIState.PROCESSING);
        
        // Parse voice query
        VoiceQuery query = voiceService.parseVoiceInput(voiceText);
        Log.d(TAG, "Parsed query: " + query);
        
        // Use the weather data passed from MainActivity
        if (currentWeatherData == null) {
            showError("Weather data not available. Please wait for weather to load.");
            return;
        }
        
        // Generate response
        voiceService.generateVoiceResponse(query, currentWeatherData, new VoiceWeatherService.VoiceResponseCallback() {
            @Override
            public void onSuccess(VoiceResponse response) {
                runOnUiThread(() -> {
                    displayResponse(response);
                    speakResponse(response.getSpokenText());
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> showError(error));
            }
        });
    }
    
    /**
     * Display response on screen
     */
    private void displayResponse(VoiceResponse response) {
        updateUI(UIState.RESPONSE);
        tvResponse.setText(response.getDisplayText());
        tvResponse.setVisibility(View.VISIBLE);
    }
    
    /**
     * Speak response using TTS
     */
    private void speakResponse(String text) {
        if (isTTSReady && textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "response");
            Log.d(TAG, "Speaking: " + text);
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String error) {
        updateUI(UIState.ERROR);
        tvResponse.setText("❌ " + error);
        tvResponse.setVisibility(View.VISIBLE);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Enable typing mode as fallback when speech recognition not available
     */
    private void enableTypingMode() {
        // Show input dialog for typing
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Type your weather question...");
        input.setPadding(40, 40, 40, 40);
        
        new android.app.AlertDialog.Builder(this)
                .setTitle("💬 Type Your Question")
                .setMessage("Ask me anything about the weather:")
                .setView(input)
                .setPositiveButton("Ask", (dialog, which) -> {
                    String question = input.getText().toString().trim();
                    if (!question.isEmpty()) {
                        processVoiceInput(question);
                    } else {
                        Toast.makeText(this, "Please type a question", Toast.LENGTH_SHORT).show();
                        enableTypingMode(); // Show again
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
    
    /**
     * Update UI based on state
     */
    private enum UIState {
        IDLE, LISTENING, PROCESSING, RESPONSE, ERROR
    }
    
    private void updateUI(UIState state) {
        runOnUiThread(() -> {
            switch (state) {
                case IDLE:
                    btnMicrophone.setImageResource(R.drawable.ic_microphone);
                    btnMicrophone.setEnabled(true);
                    layoutListening.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    ivVoiceAnimation.setVisibility(View.GONE);
                    break;
                    
                case LISTENING:
                    btnMicrophone.setImageResource(R.drawable.ic_microphone_active);
                    layoutListening.setVisibility(View.VISIBLE);
                    tvListening.setText("🎤 Listening...");
                    ivVoiceAnimation.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    layoutResponse.setVisibility(View.GONE);
                    // TODO: Add voice wave animation
                    break;
                    
                case PROCESSING:
                    btnMicrophone.setEnabled(false);
                    layoutListening.setVisibility(View.VISIBLE);
                    tvListening.setText("🤔 Thinking...");
                    ivVoiceAnimation.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                    
                case RESPONSE:
                    btnMicrophone.setImageResource(R.drawable.ic_microphone);
                    btnMicrophone.setEnabled(true);
                    layoutListening.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    layoutResponse.setVisibility(View.VISIBLE);
                    break;
                    
                case ERROR:
                    btnMicrophone.setImageResource(R.drawable.ic_microphone);
                    btnMicrophone.setEnabled(true);
                    layoutListening.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    layoutResponse.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }
    
    /**
     * TextToSpeech initialization callback
     */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "TTS language not supported");
                isTTSReady = false;
            } else {
                isTTSReady = true;
                Log.d(TAG, "TTS initialized successfully");
            }
        } else {
            Log.e(TAG, "TTS initialization failed");
            isTTSReady = false;
        }
    }
    
    /**
     * Speech Recognition Listener
     */
    private class VoiceRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "Ready for speech");
        }
        
        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "Speech began");
        }
        
        @Override
        public void onRmsChanged(float rmsdB) {
            // Volume level changed - could animate mic icon
        }
        
        @Override
        public void onBufferReceived(byte[] buffer) {}
        
        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "Speech ended");
            isListening = false;
        }
        
        @Override
        public void onError(int error) {
            isListening = false;
            String errorMessage = getErrorText(error);
            Log.e(TAG, "Speech recognition error: " + errorMessage);
            
            runOnUiThread(() -> {
                // For "No match found" error, give helpful feedback
                if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                    updateUI(UIState.IDLE);
                    Toast.makeText(VoiceWeatherActivity.this,
                            "🎙️ Couldn't hear you clearly. Try again!\n\nTips:\n• Speak clearly and slowly\n• Check your internet connection\n• Tap mic and speak immediately",
                            Toast.LENGTH_LONG).show();
                } else if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    updateUI(UIState.IDLE);
                    Toast.makeText(VoiceWeatherActivity.this,
                            "⏱️ Didn't hear anything. Tap mic and speak right away!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    updateUI(UIState.ERROR);
                    Toast.makeText(VoiceWeatherActivity.this,
                            "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                String recognizedText = matches.get(0);
                processVoiceInput(recognizedText);
            }
        }
        
        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                String partialText = matches.get(0);
                runOnUiThread(() -> {
                    tvYouSaid.setText("Hearing: " + partialText + "...");
                    tvYouSaid.setVisibility(View.VISIBLE);
                });
            }
        }
        
        @Override
        public void onEvent(int eventType, Bundle params) {}
        
        private String getErrorText(int errorCode) {
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    return "Audio recording error";
                case SpeechRecognizer.ERROR_CLIENT:
                    return "Client error";
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    return "Insufficient permissions";
                case SpeechRecognizer.ERROR_NETWORK:
                    return "Network error";
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    return "Network timeout";
                case SpeechRecognizer.ERROR_NO_MATCH:
                    return "No match found";
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    return "Recognition service busy";
                case SpeechRecognizer.ERROR_SERVER:
                    return "Server error";
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    return "No speech input";
                default:
                    return "Unknown error";
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
