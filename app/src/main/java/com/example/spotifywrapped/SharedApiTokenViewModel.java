package com.example.spotifywrapped;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedApiTokenViewModel extends ViewModel {
    private MutableLiveData<String> apiTokenLiveData = new MutableLiveData<>();

    public void setApiToken(String token) {
        apiTokenLiveData.setValue(token);
    }

    public LiveData<String> getApiToken() {
        return apiTokenLiveData;
    }
}