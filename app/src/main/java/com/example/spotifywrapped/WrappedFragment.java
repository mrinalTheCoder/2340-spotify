package com.example.spotifywrapped;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;


import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jp.shts.android.storiesprogressview.StoriesProgressView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WrappedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WrappedFragment extends Fragment {

    private String mAccessToken, mAccessCode;
    private Call mCall;
    private boolean querySpotify;
    private Map<String, Object> pastWrappedData;

    public static final String CLIENT_ID = "17c3cc6f018c42ce8e1f55bc13f61d99";
    public static final String REDIRECT_URI = "spotifywrapped://auth";

    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser;
    private final String geminiApi = "AIzaSyDqL7aF7lzGTpNX_VLcNH7RMQGBoJNJwBM";
    GenerativeModel gm = new GenerativeModel(/* modelName */ "gemini-pro",
// Access your API key as a Build Configuration variable (see "Set up your API key" above)
            /* apiKey */ geminiApi);
    GenerativeModelFutures model = GenerativeModelFutures.from(gm);

    public WrappedFragment() {
        // Required empty public constructor
    }

    public static WrappedFragment newInstance() {
        WrappedFragment fragment = new WrappedFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (getArguments() != null) {
            mAccessToken = getArguments().getString("mAccessToken");
            querySpotify = getArguments().getBoolean("querySpotify", true);
            pastWrappedData = (Map<String, Object>) getArguments().getSerializable("pastWrappedData");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_wrapped, container, false);
//        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
//        SpotifyInfoAdapter adapter = new SpotifyInfoAdapter(getActivity());
//        viewPager.setAdapter(adapter);
        Log.d("WrappedFragment", querySpotify ? "Querying Spotify" : "Not querying Spotify");
        if (querySpotify) {
            getWrappedData(view, savedInstanceState);
        } else {
            showViewPager(view, pastWrappedData);
        }

        return view;
    }

    private void saveToFirestore(Map<String, Object> data) {
        db.collection(currentUser.getUid())
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("firestore", "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("firestore", "Error adding document", e);
                    }
                });
    }

        public void getWrappedData(View view, Bundle savedInstanceState) {
        SharedApiTokenViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedApiTokenViewModel.class);
        viewModel.getApiToken().observe(getViewLifecycleOwner(), apiToken -> {
            getUserTopArtists(apiToken, new TopArtistsCallback() {
                @Override
                public void onTopArtistsReceived(ArrayList<String> topArtists, ArrayList<String> artistIdsList) {
                    String artistIds = artistIdsList.get(0);
                    for (int i = 1; i < artistIdsList.size(); i++) {
                        artistIds += "," + artistIdsList.get(i);
                    }
                    getUserGenre(apiToken, artistIds, new GenreCallback() {
                        @Override
                        public void onGenreReceived(ArrayList<String> genre) {

                            getUserTopSongs(apiToken, new TopSongsCallback() {
                                public void onTopSongsReceived(ArrayList<String> topSongs, ArrayList<String> topId) {

                                    String songIds = topId.get(0);
                                    for (int i = 1; i < topId.size(); i++) {
                                        songIds += "," + topId.get(i);
                                    }
                                    final String songsIds = songIds;
                                    getUserAudioFeatures(apiToken, songsIds, new AudioFeaturesCallback() {
                                        public void onAudioFeaturesReceived(ArrayList<Double> audioFeatures) {
                                            getRec(apiToken, songsIds, new RecCallback() {
                                                @Override
                                                public void onRecReceived(ArrayList<String> recArtists) {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            // showViewPager(view, topArtists, topSongs, genre, audioFeatures, recArtists);
                                                            Content content = new Content.Builder()
                                                                    .addText(String.format("Describe how I act, think, and dress based on my top spotify artists. Here are my top artists: %s", topArtists.toString()))
                                                                    .build();
                                                            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
                                                            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                                                                @Override
                                                                public void onSuccess(GenerateContentResponse result) {
                                                                    Log.w("Top Artists", topArtists.toString());
                                                                    Log.w("Top Songs", topSongs.toString());
                                                                    Log.w("Top Genres", genre.toString());
                                                                    Log.w("Audio Features", audioFeatures.toString());
                                                                    Log.w("Recommended Artists", recArtists.toString());
                                                                    String LLMOutput = result.getText();
                                                                    Log.w("How I act, think, dress", LLMOutput);

                                                                    Map<String, Object> data = new HashMap<>();
                                                                    data.put("time", new Date());
                                                                    data.put("topArtists", topArtists);
                                                                    data.put("topSongs", topSongs);
                                                                    data.put("genre", genre);
                                                                    data.put("audioFeatures", audioFeatures);
                                                                    data.put("recArtists", recArtists);
                                                                    data.put("LLMOutput", LLMOutput);

                                                                    saveToFirestore(data);
                                                                     openWrapped(data);
                                                                }

                                                                @Override
                                                                public void onFailure(Throwable t) {
                                                                    t.printStackTrace();
                                                                }
                                                            }, getContext().getMainExecutor());
                                                        }
                                                    });
                                                }
                                            });
                                        }

                                    });
                                }
                            });
                        }
                    });
                }
            });
        });
    }

    private void openWrapped(Map<String, Object> data) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("data", (HashMap<String, Object>) data);

        WelcomeFragment welcomeFragment = new WelcomeFragment();
        welcomeFragment.setArguments(bundle);

        // Transaction
        FragmentManager welcomefragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction welcomefragmentTransaction = welcomefragmentManager.beginTransaction();
        welcomefragmentTransaction.replace(R.id.fragment_container, welcomeFragment);
        welcomefragmentTransaction.addToBackStack(null); // Optional for back button
        welcomefragmentTransaction.commit();
    }

    public void getRec(String mAccessToken, String ids, RecCallback callback) {
        ArrayList<String> recArtists = new ArrayList<>(5);
        if (mAccessToken == null) {
            Toast.makeText(getActivity(), "get access token", Toast.LENGTH_SHORT).show();
        }

//        Log.w("THIS IS THE IDS", ids);
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/recommendations?seed_tracks=" + ids)
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
                    JSONArray itemsArray = jsonObject.getJSONArray("tracks");

                    for (int i = 0; i < 8; i++) {
                        JSONObject track = itemsArray.getJSONObject(i);
                        JSONObject artist = track.getJSONArray("artists").getJSONObject(0);
                        String artistName = artist.getString("name");
                        recArtists.add(artistName);
                    }

                    callback.onRecReceived(recArtists);


                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                }
            }
        });
    }

    public void getUserGenre(String mAccessToken, String ids, GenreCallback callback) {
        ArrayList<String> genre = new ArrayList<>(5);
        if (mAccessToken == null) {
            Toast.makeText(getActivity(), "get access token", Toast.LENGTH_SHORT).show();
        }

//        Log.w("THIS IS THE IDS", ids);
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/artists?ids=" + ids)
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
                    JSONArray itemsArray = jsonObject.getJSONArray("artists");
                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject artist = itemsArray.getJSONObject(i);
                        JSONArray artistGenre = artist.getJSONArray("genres");
                        for (int j = 0; j < artistGenre.length(); j++) {
                            genre.add(artistGenre.getString(j));
                        }
                    }

                    callback.onGenreReceived(genre);


                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                }
            }
        });
    }

    public void showViewPager(View view, ArrayList<String> topArtists, ArrayList<String> topSongs, ArrayList<String> genre,
                              ArrayList<Double> audioFeatures, ArrayList<String> recArtists) {
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        SpotifyInfoAdapter adapter = new SpotifyInfoAdapter(getActivity(), topArtists, topSongs, genre, audioFeatures, recArtists);
        viewPager.setAdapter(adapter);
    }

    public void showViewPager(View view, Map<String, Object> pastWrappedData) {
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        SpotifyInfoAdapter adapter = new SpotifyInfoAdapter(getActivity(), pastWrappedData);
        viewPager.setAdapter(adapter);
    }

    public void getUserTopArtists(String mAccessToken, TopArtistsCallback callback) {
        ArrayList<String> topArtists = new ArrayList<>(5);
        ArrayList<String> artistIds = new ArrayList<>(5);
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
                        String ids = itemObject.getString("id");
                        topArtists.add(name);
                        artistIds.add(ids);
                    }
                    callback.onTopArtistsReceived(topArtists, artistIds); // Pass data to the callback


                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                }
            }
        });
    }

    public void getUserAudioFeatures(String mAccessToken, String ids, AudioFeaturesCallback callback) {
        ArrayList<Double> audioFeatures = new ArrayList<>(5);
        if (mAccessToken == null) {
            Toast.makeText(getActivity(), "get access token", Toast.LENGTH_SHORT).show();
        }

//        Log.w("THIS IS THE IDS", ids);
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/audio-features?ids=" + ids)
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
                    JSONArray itemsArray = jsonObject.getJSONArray("audio_features");

                    double avgDanceability = calculateAverage("danceability", itemsArray);
                    double avgAcousticness = calculateAverage("acousticness", itemsArray);
                    double avgEnergy = calculateAverage("energy", itemsArray);
                    double avgLiveness = calculateAverage("liveness", itemsArray);
                    double avgValence = calculateAverage("valence", itemsArray);

                    audioFeatures.add(avgDanceability);
                    audioFeatures.add(avgAcousticness);
                    audioFeatures.add(avgEnergy);
                    audioFeatures.add(avgLiveness);
                    audioFeatures.add(avgValence);

                    callback.onAudioFeaturesReceived(audioFeatures); // Pass data to the callback


                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                }
            }
        });
    }
    private static double calculateAverage(String featureName, JSONArray audioFeatures) throws JSONException {
        double sum = 0;
        for (int i = 0; i < audioFeatures.length(); i++) {
            JSONObject featureObject = audioFeatures.getJSONObject(i);
            sum += featureObject.getDouble(featureName);
        }
        return sum / audioFeatures.length();
    }
    public void getUserTopSongs(String mAccessToken, TopSongsCallback callback) {
        ArrayList<String> topSongs = new ArrayList<>(5);
        ArrayList<String> topId = new ArrayList<>(5);
        if (mAccessToken == null) {
            Toast.makeText(getActivity(), "get access token", Toast.LENGTH_SHORT).show();
        }

        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/tracks")
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
                        String id = itemObject.getString("uri");
                        topSongs.add(name);
                        topId.add(id.split(":")[2]);
                    }
                    callback.onTopSongsReceived(topSongs, topId); // Pass data to the callback


                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                }
            }
        });
    }

    private Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }
}

interface TopArtistsCallback {
    void onTopArtistsReceived(ArrayList<String> topArtists, ArrayList<String> artistIds);
}

interface TopSongsCallback {
    void onTopSongsReceived(ArrayList<String> topSongs, ArrayList<String> topId);
}


interface AudioFeaturesCallback{
    void onAudioFeaturesReceived(ArrayList<Double> audioFeatures);
}

interface GenreCallback{
    void onGenreReceived(ArrayList<String> genre);
}

interface RecCallback{
    void onRecReceived(ArrayList<String> recArtists);
}








