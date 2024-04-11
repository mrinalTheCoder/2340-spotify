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
public class WrappedFragment extends Fragment implements StoriesProgressView.StoriesListener {

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

    private static final int PROGRESS_COUNT = 6;

    private StoriesProgressView storiesProgressView;
    private ImageView image;

    private int counter = 0;

    private ArrayList<Bitmap> bitmapresources = new ArrayList<>();

    private final long[] durations = new long[]{
            500L, 1000L, 1500L, 4000L, 5000L, 1000,
    };

    long pressTime = 0L;
    long limit = 500L;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    private static Bitmap getBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);
        return bitmap;
    }

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

    private void saveToFirestore(
            ArrayList<String> topArtists,
            ArrayList<String> topSongs,
            ArrayList<String> genre,
            ArrayList<Double> audioFeatures,
            ArrayList<String> recArtists,
            String LLMOutput) {
        Map<String, Object> data = new HashMap<>();
        data.put("time", new Date());
        data.put("topArtists", topArtists);
        data.put("topSongs", topSongs);
        data.put("genre", genre);
        data.put("audioFeatures", audioFeatures);
        data.put("recArtists", recArtists);
        data.put("LLMOutput", LLMOutput);
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

    public void displayWrapped(View view, Bundle savedInstanceState, ArrayList<String> topArtists, ArrayList<String> topSongs, ArrayList<String> genre, ArrayList<Double> audioFeatures, ArrayList<String> recArtists) {
        View view1 = getLayoutInflater().inflate(R.layout.welcome_wrapped, null);
        Bitmap bitmap1 = getBitmapFromView(view1);
        bitmapresources.add(bitmap1);

        View view2 = getLayoutInflater().inflate(R.layout.topartists_wrapped, null);
        ((TextView) view2.findViewById(R.id.artist1)).setText(topArtists.get(0));
        ((TextView) view2.findViewById(R.id.artist2)).setText(topArtists.get(1));
        ((TextView) view2.findViewById(R.id.artist3)).setText(topArtists.get(2));
        ((TextView) view2.findViewById(R.id.artist4)).setText(topArtists.get(3));
        ((TextView) view2.findViewById(R.id.artist5)).setText(topArtists.get(4));
        Bitmap bitmap2 = getBitmapFromView(view2);
        bitmapresources.add(bitmap2);

        View view3 = getLayoutInflater().inflate(R.layout.topsongs_wrapped, null);
        ((TextView) view3.findViewById(R.id.song1)).setText(topSongs.get(0));
        ((TextView) view3.findViewById(R.id.song2)).setText(topSongs.get(1));
        ((TextView) view3.findViewById(R.id.song3)).setText(topSongs.get(2));
        ((TextView) view3.findViewById(R.id.song4)).setText(topSongs.get(3));
        ((TextView) view3.findViewById(R.id.song5)).setText(topSongs.get(4));
        Bitmap bitmap3 = getBitmapFromView(view3);
        bitmapresources.add(bitmap3);

        View view4 = getLayoutInflater().inflate(R.layout.topgenres_wrapped, null);
        ((TextView) view4.findViewById(R.id.genre1)).setText(genre.get(0));
        ((TextView) view4.findViewById(R.id.genre2)).setText(genre.get(1));
        ((TextView) view4.findViewById(R.id.genre3)).setText(genre.get(2));
        ((TextView) view4.findViewById(R.id.genre4)).setText(genre.get(3));
        ((TextView) view4.findViewById(R.id.genre5)).setText(genre.get(4));
        Bitmap bitmap4 = getBitmapFromView(view4);
        bitmapresources.add(bitmap4);

        View view5 = getLayoutInflater().inflate(R.layout.audiofeatures_wrapped, null);
        ProgressBar progressBar1 = (ProgressBar) view5.findViewById(R.id.progressBar18);
        ProgressBar progressBar2 = (ProgressBar) view5.findViewById(R.id.progressBar12);
        ProgressBar progressBar3 = (ProgressBar) view5.findViewById(R.id.progressBar13);
        ProgressBar progressBar4 = (ProgressBar) view5.findViewById(R.id.progressBar14);
        ProgressBar progressBar5 = (ProgressBar) view5.findViewById(R.id.progressBar16);
        progressBar1.setProgress((int) (100 * audioFeatures.get(0)));
        progressBar2.setProgress((int) (100 * audioFeatures.get(1)));
        progressBar3.setProgress((int) (100 * audioFeatures.get(2)));
        progressBar4.setProgress((int) (100 * audioFeatures.get(3)));
        progressBar5.setProgress((int) (100 * audioFeatures.get(4)));
        progressBar1.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar2.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar3.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar4.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar5.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        Bitmap bitmap5 = getBitmapFromView(view5);
        bitmapresources.add(bitmap5);

        View view6 = getLayoutInflater().inflate(R.layout.recartists_wrapped, null);
        ((TextView) view6.findViewById(R.id.recArtist1)).setText(recArtists.get(0));
        ((TextView) view6.findViewById(R.id.recArtist2)).setText(recArtists.get(1));
        ((TextView) view6.findViewById(R.id.recArtist3)).setText(recArtists.get(2));
        ((TextView) view6.findViewById(R.id.recArtist4)).setText(recArtists.get(3));
        ((TextView) view6.findViewById(R.id.recArtist5)).setText(recArtists.get(4));
        Bitmap bitmap6 = getBitmapFromView(view6);
        bitmapresources.add(bitmap6);

        super.onCreate(savedInstanceState);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().setContentView(R.layout.activity_main);

        storiesProgressView = (StoriesProgressView) getActivity().findViewById(R.id.stories);
        storiesProgressView.setStoriesCount(PROGRESS_COUNT);
        storiesProgressView.setStoryDuration(3000L);
        // or
        // storiesProgressView.setStoriesCountWithDurations(durations);
        storiesProgressView.setStoriesListener((StoriesProgressView.StoriesListener) this);
//        storiesProgressView.startStories();
        counter = 0;
        storiesProgressView.startStories(counter);

        image = (ImageView) getActivity().findViewById(R.id.image);
        image.setImageBitmap(bitmapresources.get(counter));

        // bind reverse view
        View reverse = getActivity().findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        // bind skip view
        View skip = getActivity().findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);

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
                                                            showViewPager(view, topArtists, topSongs, genre, audioFeatures, recArtists);
                                                        }
                                                    });
                                                    Content content = new Content.Builder()
                                                            .addText(String.format("Describe how I act, think, and dress based on my top spotify artists. Here are my top artists: %s", topArtists.toString()))
                                                            .build();
                                                    ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
                                                    Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                                                        @Override
                                                        public void onSuccess(GenerateContentResponse result) {
                                                            displayWrapped(view, savedInstanceState, topArtists, topSongs, genre, audioFeatures, recArtists);

                                                            Log.w("Top Artists", topArtists.toString());
                                                            Log.w("Top Songs", topSongs.toString());
                                                            Log.w("Top Genres", genre.toString());
                                                            Log.w("Audio Features", audioFeatures.toString());
                                                            Log.w("Recommended Artists", recArtists.toString());
                                                            String LLMOutput = result.getText();
                                                            Log.w("How I act, think, dress", LLMOutput);
                                                            saveToFirestore(topArtists, topSongs, genre, audioFeatures, recArtists, LLMOutput);
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
        });
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

    @Override
    public void onNext() {
        image.setImageBitmap(bitmapresources.get(++counter));;
    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        image.setImageBitmap(bitmapresources.get(--counter));
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void onDestroy() {
        // Very important !
        storiesProgressView.destroy();
        super.onDestroy();
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








