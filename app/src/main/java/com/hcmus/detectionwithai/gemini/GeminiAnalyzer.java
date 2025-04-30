package com.hcmus.detectionwithai.gemini;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hcmus.detectionwithai.R;
import com.hcmus.detectionwithai.interfaces.AnalysisCallback;
import com.hcmus.detectionwithai.interfaces.ChatCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class GeminiAnalyzer {
    private final GenerativeModelFutures model;
    private final Executor mainExecutor;
    private final List<Content> chatHistory;
    private final Context context;

    public GeminiAnalyzer(Context context, Executor mainExecutor) {
        this.mainExecutor = mainExecutor;
        this.context = context;
        GenerativeModel generativeModel = new GenerativeModel(
                GeminiConfig.MODEL_NAME,
                GeminiConfig.API_KEY);
        this.model = GenerativeModelFutures.from(generativeModel);
        this.chatHistory = new ArrayList<>();
    }

    public void analyzeImage(Bitmap imageBitmap, String languageCode, AnalysisCallback callback) {
        try {
            // 1. Validate input parameters
            if (imageBitmap == null || imageBitmap.isRecycled()) {
                callback.onFailure(new IllegalArgumentException("Invalid image bitmap"));
                return;
            }

            if (languageCode == null || languageCode.isEmpty()) {
                languageCode = "en"; // Default to English
            }



            // 2. Process image (resize if too large)
            Bitmap processedBitmap = optimizeBitmapSize(imageBitmap, 2048); // Max width 2048px

            // 3. Build the prompt with language support
            String languageSuffix = getLanguagePromptSuffix(languageCode);
            String prompt = buildAnalysisPrompt(languageSuffix);

            // 4. Create content with error handling
            Content content;
            try {
                content = new Content.Builder()
                        .addText(prompt)
                        .addImage(processedBitmap)
                        .build();
            } catch (Exception e) {
                callback.onFailure(new Exception("Failed to create content: " + e.getMessage()));
                return;
            }

            // 5. Execute the request with timeout handling
            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    try {
                        if (result == null) {
                            callback.onFailure(new Exception("Null response from API"));
                            return;
                        }

                        String analysisResult = result.getText();
                        if (analysisResult == null || analysisResult.isEmpty()) {
                            callback.onFailure(new Exception("Empty analysis results"));
                        } else {
                
                            callback.onSuccess(analysisResult);
                        }
                    } catch (Exception e) {
                        callback.onFailure(new Exception("Result processing error: " + e.getMessage()));
                    }
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    String errorMsg = "Analysis failed: " + t.getMessage();
                    Log.e("GeminiAnalyzer", errorMsg, t);

                    // Classify different error types
                    if (t.getMessage() != null) {
                        if (t.getMessage().contains("image size")) {
                            errorMsg = "Image too large. Please try a smaller image.";
                        } else if (t.getMessage().contains("timeout")) {
                            errorMsg = "Request timed out. Please try again.";
                        }
                    }

                    callback.onFailure(new Exception(errorMsg));
                }
            }, mainExecutor);

        } catch (Exception e) {
            Log.e("GeminiAnalyzer", "Unexpected error in analyzeImage", e);
            callback.onFailure(new Exception("Unexpected error: " + e.getMessage()));
        }
    }

    // Helper method to optimize bitmap size
    private Bitmap optimizeBitmapSize(Bitmap src, int maxWidth) {
        if (src.getWidth() <= maxWidth)
            return src;

        float aspectRatio = (float) src.getHeight() / src.getWidth();
        int newHeight = Math.round(maxWidth * aspectRatio);
        return Bitmap.createScaledBitmap(src, maxWidth, newHeight, true);
    }

    // Helper method to build the prompt
    private String buildAnalysisPrompt(String languageSuffix) {
        return "Analyze this image thoroughly. Describe:\n" +
                "1. All important objects\n" +
                "2. Their spatial relationships\n" +
                "3. Colors and visual characteristics\n" +
                "4. Any readable text\n\n" +
                "Provide detailed insights in a structured format. " +
                languageSuffix;
    }

    public void chatWithAI(String userMessage, ChatCallback callback) {
        try {
            // Create user content
            Content.Builder userContentBuilder = new Content.Builder();
            userContentBuilder.addText(userMessage);
            userContentBuilder.setRole("user");
            Content userContent = userContentBuilder.build();
            chatHistory.add(userContent);

            // Convert chat history to array
            Content[] contentArray = chatHistory.toArray(new Content[0]);

            // Generate response
            ListenableFuture<GenerateContentResponse> response = model.generateContent(contentArray);

            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    if (result != null && result.getText() != null) {
                        // Create AI response content
                        Content.Builder aiContentBuilder = new Content.Builder();
                        aiContentBuilder.addText(result.getText());
                        aiContentBuilder.setRole("model");
                        Content aiContent = aiContentBuilder.build();
                        chatHistory.add(aiContent);
                        callback.onSuccess(result.getText());
                    } else {
                        callback.onFailure(new Exception("Empty response from AI"));
                    }
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    callback.onFailure(t);
                }
            }, mainExecutor);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    public void clearChatHistory() {
        chatHistory.clear();
    }

    private String getLanguagePromptSuffix(String languageCode) {
        Resources res = context.getResources();
        String[] languageCodes = res.getStringArray(R.array.language_codes);
        String[] languageNames = res.getStringArray(R.array.language_names);

        // Default to English if language not found
        String languageName = "English";

        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(languageCode)) {
                languageName = languageNames[i];
                break;
            }
        }

        return "Reply in " + languageName + " language";
    }
}