package com.hcmus.detectionwithai;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.hcmus.detectionwithai.activity.ChatWithAI;
import com.hcmus.detectionwithai.activity.DetectionAnalytics;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        (findViewById(R.id.chatButton)).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatWithAI.class);
            startActivity(intent);
        });
        (findViewById(R.id.analyticsButton)).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DetectionAnalytics.class);
            startActivity(intent);
        });
    }
}