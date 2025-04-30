package com.hcmus.detectionwithai.interfaces;

public interface AnalysisCallback {
    void onSuccess(String result);

    void onFailure(Throwable t);
}