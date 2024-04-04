package com.example.spotifywrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class LLMFragment extends Fragment {

    private EditText userMusicInput;
    private EditText friendMusicInput;
    private TextView descriptionOutput;

    public LLMFragment() {
        // Required empty public constructor
    }

    public static LLMFragment newInstance() {
        return new LLMFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_llm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userMusicInput = view.findViewById(R.id.userMusicInput);
        friendMusicInput = view.findViewById(R.id.friendMusicInput);
        Button generateDescriptionBtn = view.findViewById(R.id.generateDescriptionBtn);
        Button compareWithFriendBtn = view.findViewById(R.id.compareWithFriendBtn);
        descriptionOutput = view.findViewById(R.id.descriptionOutput);

        OpenAIAPIService apiService = new OpenAIAPIService();

        generateDescriptionBtn.setOnClickListener(v -> {
            String userTaste = userMusicInput.getText().toString();
            apiService.generateText("Describe a person who listens to a lot of " + userTaste + ".", new OpenAIAPIService.ApiServiceCallback() {
                @Override
                public void onSuccess(String result) {
                    updateTextView(result);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    updateTextView("Failed to generate description. Please try again.");
                }
            });
        });

        compareWithFriendBtn.setOnClickListener(v -> {
            String userTaste = userMusicInput.getText().toString();
            String friendTaste = friendMusicInput.getText().toString();
            // This is a placeholder. You'll need to implement a method in your API service
            // that can handle a comparison between two inputs.
            apiService.compareTastes(userTaste, friendTaste, new OpenAIAPIService.ApiServiceCallback() {
                @Override
                public void onSuccess(String result) {
                    updateTextView(result);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    updateTextView("Failed to compare tastes. Please try again.");
                }
            });
        });
    }

    private void updateTextView(String text) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> descriptionOutput.setText(text));
    }
}
