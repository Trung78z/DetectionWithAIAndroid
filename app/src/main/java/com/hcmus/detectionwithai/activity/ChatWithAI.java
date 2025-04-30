package com.hcmus.detectionwithai.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmus.detectionwithai.R;
import com.hcmus.detectionwithai.adapter.ChatAdapter;
import com.hcmus.detectionwithai.gemini.GeminiAnalyzer;
import com.hcmus.detectionwithai.interfaces.ChatCallback;
import com.hcmus.detectionwithai.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatWithAI extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private GeminiAnalyzer geminiAnalyzer;
    private Executor mainExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize components
        mainExecutor = Executors.newSingleThreadExecutor();
        geminiAnalyzer = new GeminiAnalyzer(this, mainExecutor);

        // Setup back button
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Initialize chat UI components
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Setup chat messages list
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Send message when button is clicked
        sendButton.setOnClickListener(v -> sendMessage());

        // Send message when user presses enter
        messageInput.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (!message.isEmpty()) {
            // Add user message to chat
            ChatMessage userMessage = new ChatMessage(message, ChatMessage.SENDER_USER);
            chatMessages.add(userMessage);
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);

            // Clear input
            messageInput.setText("");

            // Show typing indicator
            ChatMessage typingIndicator = new ChatMessage("Typing...", ChatMessage.SENDER_AI);
            chatMessages.add(typingIndicator);
            int typingPosition = chatMessages.size() - 1;
            chatAdapter.notifyItemInserted(typingPosition);
            chatRecyclerView.smoothScrollToPosition(typingPosition);

            // Get AI response
            getAIResponse(message);
        }
    }

    private void getAIResponse(String userMessage) {
        geminiAnalyzer.chatWithAI(userMessage, new ChatCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    // Remove typing indicator
                    chatMessages.remove(chatMessages.size() - 1);

                    // Add AI response
                    ChatMessage aiMessage = new ChatMessage(response, ChatMessage.SENDER_AI);
                    chatMessages.add(aiMessage);
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    // Remove typing indicator
                    chatMessages.remove(chatMessages.size() - 1);

                    // Show error message
                    Toast.makeText(ChatWithAI.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    chatAdapter.notifyDataSetChanged();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear chat history when activity is destroyed
        geminiAnalyzer.clearChatHistory();
    }
}