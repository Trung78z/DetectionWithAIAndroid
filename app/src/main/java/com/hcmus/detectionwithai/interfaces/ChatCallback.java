package com.hcmus.detectionwithai.interfaces;

public interface ChatCallback {
    void onSuccess(String response);

    void onFailure(Throwable t);
}