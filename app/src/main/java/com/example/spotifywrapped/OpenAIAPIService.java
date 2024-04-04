package com.example.spotifywrapped;

import android.os.Handler;
import android.os.Looper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpenAIAPIService {

    private static final String SERVER_URL = "http://10.0.2.2:3000/generate-text";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private OkHttpClient client = new OkHttpClient();
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public void generateText(String prompt, ApiServiceCallback callback) {
        executorService.execute(() -> {
            String json = "{\"prompt\": \"" + prompt + "\", \"max_tokens\": 100}";
            RequestBody body = RequestBody.create(json, JSON);
            Request request = new Request.Builder()
                    .url(SERVER_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mainThreadHandler.post(() -> callback.onError(e));
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError(new IOException("Unexpected code " + response));
                        return;
                    }
                    String result = response.body().string();
                    mainThreadHandler.post(() -> callback.onSuccess(result));
                }
            });
        });
    }
    public void compareTastes(String userTaste, String friendTaste, ApiServiceCallback callback) {
        executorService.execute(() -> {
            // Assuming the server expects a JSON with two fields: userPrompt and friendPrompt
            String json = "{\"userPrompt\": \"" + userTaste + "\", \"friendPrompt\": \"" + friendTaste + "\"}";
            RequestBody body = RequestBody.create(json, JSON);
            // Update SERVER_URL to the URL of the new endpoint
            String compareUrl = SERVER_URL.replace("generate-text", "compare-tastes");
            Request request = new Request.Builder()
                    .url(compareUrl)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mainThreadHandler.post(() -> callback.onError(e));
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError(new IOException("Unexpected code " + response));
                        return;
                    }
                    String result = response.body().string();
                    mainThreadHandler.post(() -> callback.onSuccess(result));
                }
            });
        });
    }

    public interface ApiServiceCallback {
        void onSuccess(String result);
        void onError(Exception e);
    }
}
