package com.example.spotifywrapped;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;


import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import android.view.ViewGroup;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.view.View.OnClickListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WrappedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WrappedFragment extends Fragment {

    private ArrayList<String> topArtists = new ArrayList<>(5);
    private String mAccessToken, mAccessCode;
    private Call mCall;

    public static final String CLIENT_ID = "17c3cc6f018c42ce8e1f55bc13f61d99";
    public static final String REDIRECT_URI = "spotifywrapped://auth";

    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    private TextView infoTextView;
    private TextView llmOutput;
    private EditText llmInput;
    private Button generateLlmContentBtn;
    private OpenAIAPIService openAIAPIService;
    private ProgressBar progressBar;
    public WrappedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WrappedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WrappedFragment newInstance() {
        WrappedFragment fragment = new WrappedFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAccessToken = getArguments().getString("mAccessToken");
        }
        openAIAPIService = new OpenAIAPIService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wrapped, container, false);
        initViewComponents(view);
        return view;
    }


    private void initViewComponents(View view) {
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        SpotifyInfoAdapter adapter = new SpotifyInfoAdapter(getActivity());
        viewPager.setAdapter(adapter);

        llmInput = view.findViewById(R.id.llmInput);
        llmOutput = view.findViewById(R.id.llmOutput);
        generateLlmContentBtn = view.findViewById(R.id.generateLlmContentBtn);
        progressBar = view.findViewById(R.id.progressBar); // Initialize your ProgressBar

        generateLlmContentBtn.setOnClickListener(v -> {
            String musicTaste = llmInput.getText().toString();
            progressBar.setVisibility(View.VISIBLE); // Show the ProgressBar when the request starts

            openAIAPIService.generateText(musicTaste, new OpenAIAPIService.ApiServiceCallback() {
                @Override
                public void onSuccess(String result) {
                    llmOutput.setText(result);
                    progressBar.setVisibility(View.GONE); // Hide the ProgressBar on success
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Failed to generate description", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE); // Hide the ProgressBar on error
                }
            });
        });
    }

}













