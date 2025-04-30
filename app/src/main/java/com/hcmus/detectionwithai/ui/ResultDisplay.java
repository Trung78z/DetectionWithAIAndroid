package com.hcmus.detectionwithai.ui;

import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

public class ResultDisplay {
    private final TextView resultTextView;
    private final ScrollView scrollView;

    public ResultDisplay(TextView resultTextView, ScrollView scrollView) {
        this.resultTextView = resultTextView;
        this.scrollView = scrollView;
    }

    public void showResult(String text) {
        resultTextView.setText(text);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_UP));
    }

    public void showError(String message) {
        resultTextView.setText("Error: " + message);
    }

    public void clear() {
        resultTextView.setText("");
    }
}