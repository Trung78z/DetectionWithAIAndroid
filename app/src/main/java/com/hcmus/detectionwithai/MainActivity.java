package com.hcmus.detectionwithai;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.hcmus.detectionwithai.gemini.GeminiAnalyzer;
import com.hcmus.detectionwithai.ui.ImageHandler;
import com.hcmus.detectionwithai.ui.ResultDisplay;
import com.hcmus.detectionwithai.utils.DialogHelper;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements
        ImageHandler.ImageCallback,
        GeminiAnalyzer.AnalysisCallback,
        DialogHelper.ImageSourceSelectionListener,
        AdapterView.OnItemSelectedListener {

    private ImageView imageView;
    private ProgressBar progressBar;
    private CardView imageCardView;
    private Spinner languageSpinner;

    private ResultDisplay resultDisplay;
    private ImageHandler imageHandler;
    private GeminiAnalyzer geminiAnalyzer;

    private Button analyticsButton;
    private String selectedLanguageCode = "en"; // Default to Vietnamese

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeComponents();
        setupLanguageSpinner();
        findViewById(R.id.captureButton).setOnClickListener(v ->
                DialogHelper.showImageSourceDialog(this, this));
        analyticsButton = findViewById(R.id.analyticsButton);
        analyticsButton.setOnClickListener(v -> onAnalytics());
    }

    private void initializeViews() {
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);
        imageCardView = findViewById(R.id.imageCardView);
        languageSpinner = findViewById(R.id.languageSpinner);

        TextView resultTextView = findViewById(R.id.resultTextView);
        ScrollView scrollView = findViewById(R.id.scrollView);
        resultDisplay = new ResultDisplay(resultTextView, scrollView);
    }

    private void initializeComponents() {
        imageHandler = new ImageHandler(this, this);
        geminiAnalyzer = new GeminiAnalyzer(this, getMainExecutor());
    }

    private void setupLanguageSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.language_names,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        // Set default selection to English ("en")
        String[] languageCodes = getResources().getStringArray(R.array.language_codes);
        int englishPosition = -1;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals("en")) {
                englishPosition = i;
                break;
            }
        }

        if (englishPosition != -1) {
            languageSpinner.setSelection(englishPosition);
            selectedLanguageCode = "en";
        }

        languageSpinner.setOnItemSelectedListener(this);
    }

    private Bitmap bitmapImage;

    private void onAnalytics() {
        analyticsButton.setEnabled(false);
        if (bitmapImage == null || bitmapImage.isRecycled()) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            resultDisplay.showResult("No image available for analysis. Please select an image first.");
            return;
        }

        // Show loading state
        showLoading(true);
        resultDisplay.showResult("Analyzing image...");

        // Perform analysis
        geminiAnalyzer.analyzeImage(bitmapImage, selectedLanguageCode, new GeminiAnalyzer.AnalysisCallback() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    analyticsButton.setEnabled(true);
                    showLoading(false);
                    resultDisplay.showResult(result);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    showLoading(false);
                    analyticsButton.setEnabled(true);
                    resultDisplay.showResult("Analysis failed: " + t.getMessage());
                    Toast.makeText(MainActivity.this, "Analysis failed", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onImageCaptured(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        imageCardView.setVisibility(View.VISIBLE);
        bitmapImage = bitmap;
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        resultDisplay.showError(message);
    }

    @Override
    public void onSuccess(String result) {
        showLoading(false);
        resultDisplay.showResult(result);
    }

    @Override
    public void onFailure(Throwable t) {
        showLoading(false);
        resultDisplay.showError("Analysis failed: " + t.getMessage());
    }

    @Override
    public void onTakePhotoSelected() {
        imageHandler.takePicture();
    }

    @Override
    public void onGallerySelected() {
        imageHandler.pickImageFromGallery();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String[] languageCodes = getResources().getStringArray(R.array.language_codes);
        selectedLanguageCode = languageCodes[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectedLanguageCode = "vi"; // Fallback to Vietnamese
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        imageView.setAlpha(isLoading ? 0.5f : 1.0f);
    }
}