package com.hcmus.detectionwithai.utils;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DialogHelper {
    public static void showImageSourceDialog(AppCompatActivity activity,
                                             ImageSourceSelectionListener listener) {
        new AlertDialog.Builder(activity)
                .setTitle("Select Image Source")
                .setItems(new String[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                    if (listener != null) {
                        if (which == 0) {
                            listener.onTakePhotoSelected();
                        } else {
                            listener.onGallerySelected();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public interface ImageSourceSelectionListener {
        void onTakePhotoSelected();
        void onGallerySelected();
    }
}