package com.belobina.app.utils;

public interface Callback<T> {
    void onSuccess(T result);
    void onError(String errorMessage);
}
