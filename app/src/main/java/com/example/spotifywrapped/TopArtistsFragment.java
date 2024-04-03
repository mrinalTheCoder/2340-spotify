package com.example.spotifywrapped;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TopArtistsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopArtistsFragment extends Fragment {

    private Call mCall;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    public static final String REDIRECT_URI = "spotifywrapped://auth";
    public TopArtistsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     @return A new instance of fragment TopArtistsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TopArtistsFragment newInstance(String param1, String param2) {
        TopArtistsFragment fragment = new TopArtistsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =   inflater.inflate(R.layout.fragment_top_artists, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // API Token Logic
        SharedApiTokenViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedApiTokenViewModel.class);
        viewModel.getApiToken().observe(getViewLifecycleOwner(), apiToken -> {
            getUserTopArtists(apiToken, new TopArtistsCallback() {
                @Override
                public void onTopArtistsReceived(ArrayList<String> topArtists) {
                    requireActivity().runOnUiThread(() -> {
                        LinearLayout myLinearLayout = view.findViewById(R.id.topArtistsLayout);
                        for (String artistName : topArtists) {
                            LayoutInflater inflater = LayoutInflater.from(getActivity());
                            TextView artistTextView = (TextView) inflater.inflate(R.layout.textview, myLinearLayout, false);
                            artistTextView.setText(artistName);
                            myLinearLayout.addView(artistTextView);
                        }
                    });
                }
            });
        });
    }
    public ArrayList<String> getUserTopArtists(String mAccessToken, TopArtistsCallback callback) {
        ArrayList<String> topArtists = new ArrayList<>(5);
        if (mAccessToken == null) {
            Toast.makeText(getActivity(), "get access token", Toast.LENGTH_SHORT).show();
        }

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray itemsArray = jsonObject.getJSONArray("items");

                    for (int i = 0; i < 5; i++) {
                        JSONObject itemObject = itemsArray.getJSONObject(i);
                        String name = itemObject.getString("name");
                        topArtists.add(name);
                    }
                    callback.onTopArtistsReceived(topArtists); // Pass data to the callback


                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                }
            }
        });
        return topArtists;
    }



    private Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
interface TopArtistsCallback {
    void onTopArtistsReceived(ArrayList<String> topArtists);
}