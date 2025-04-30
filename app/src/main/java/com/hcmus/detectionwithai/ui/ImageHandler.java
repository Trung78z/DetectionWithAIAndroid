package com.hcmus.detectionwithai.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class ImageHandler {
    private final AppCompatActivity activity;
    private final ActivityResultLauncher<Intent> takePictureLauncher;
    private final ActivityResultLauncher<Intent> pickImageLauncher;
    private final ImageCallback callback;

    public ImageHandler(AppCompatActivity activity, ImageCallback callback) {
        this.activity = activity;
        this.callback = callback;

        this.takePictureLauncher = activity.registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            if (imageBitmap != null) {
                                callback.onImageCaptured(imageBitmap);
                            } else {
                                callback.onError("Failed to capture image");
                            }
                        }
                    }
                });

        this.pickImageLauncher = activity.registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
                            callback.onImageCaptured(imageBitmap);
                        } catch (IOException e) {
                            callback.onError("Error loading image");
                        }
                    }
                });
    }

    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            takePictureLauncher.launch(takePictureIntent);
        } else {
            callback.onError("No camera app found");
        }
    }

    public void pickImageFromGallery() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(pickImageIntent);
    }

    public interface ImageCallback {
        void onImageCaptured(Bitmap bitmap);

        void onError(String message);

    }
}