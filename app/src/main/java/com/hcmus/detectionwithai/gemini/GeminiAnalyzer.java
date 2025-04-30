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

import java.util.concurrent.Executor;

public class GeminiAnalyzer {
    private final GenerativeModelFutures model;
    private final Executor mainExecutor;

    private final Context context;

    public GeminiAnalyzer(Context context, Executor mainExecutor) {
        this.context = context;
        GenerativeModel generativeModel = new GenerativeModel(GeminiConfig.MODEL_NAME, GeminiConfig.API_KEY);
        this.model = GenerativeModelFutures.from(generativeModel);
        this.mainExecutor = mainExecutor;
    }

    public void analyzeImage(Bitmap imageBitmap, String languageCode, AnalysisCallback callback) {
        Log.d("Main", "onImageCaptured: " + languageCode);
        String languageSuffix = getLanguagePromptSuffix(languageCode);

        String prompt = "Analyze this image thoroughly. Describe all important objects, " +
                "their spatial relationships, colors, and any text present. " +
                "Provide detailed insights. " + languageSuffix;
        Content content = new Content.Builder()
                .addText(prompt)
                .addImage(imageBitmap)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(
                response,
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        if (result != null && result.getText() != null) {
                            callback.onSuccess(result.getText());
                        } else {
                            callback.onFailure(new Exception("No analysis results"));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        callback.onFailure(t);
                    }
                },
                mainExecutor
        );
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

    public interface AnalysisCallback {
        void onSuccess(String result);

        void onFailure(Throwable t);
    }
}