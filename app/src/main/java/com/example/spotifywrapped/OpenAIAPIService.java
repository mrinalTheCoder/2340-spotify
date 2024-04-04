package com.example.spotifywrapped;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LLMFragment extends Fragment {

    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
    OkHttpClient client = new OkHttpClient();

    String jsonBody = "{\"prompt\": \"Describe a person who listens to a lot of indie rock.\", \"max_tokens\": 100}";
    RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
    Request request = new Request.Builder()
            .url("https://api.openai.com/v1/engines/davinci/completions")
            .addHeader("Authorization", "Bearer YOUR_API_KEY")
            .post(body)
            .build();

    client.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            // Handle failure
            e.printStackTrace();
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            // Handle success
            String responseBody = response.body().string();
            // Update your UI using the main thread
        }
    });

    private static final String SERVER_URL = "https://yourserver.com/generate-text";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private OkHttpClient client = new OkHttpClient();
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public void generateText(String prompt, Callback callback) {
        executorService.execute(() -> {
            String json = "{\"prompt\": \"" + prompt + "\", \"max_tokens\": 100}";
            RequestBody body = RequestBody.create(json, JSON);
            Request request = new Request.Builder()
                    .url(SERVER_URL)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String result = response.body().string();
                mainThreadHandler.post(() -> callback.onSuccess(result));
            } catch (IOException e) {
                e.printStackTrace();
                mainThreadHandler.post(() -> callback.onError(e));
            }
        });
    }

    public interface Callback {
        void onSuccess(String result);
        void onError(Exception e);
    }
}