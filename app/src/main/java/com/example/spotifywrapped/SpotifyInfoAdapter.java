package com.example.spotifywrapped;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.Map;

import android.os.Bundle;
// ...other necessary imports

public class SpotifyInfoAdapter extends FragmentStateAdapter {

    public ArrayList<String> topArtists = new ArrayList<>();
    public ArrayList<String> topSongs = new ArrayList<>();

    public ArrayList<String> genre = new ArrayList<>();
    public ArrayList<Double> audioFeatures = new ArrayList<>();
    public ArrayList<String> recArtists = new ArrayList<>();
    public SpotifyInfoAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    public SpotifyInfoAdapter(FragmentActivity activity, ArrayList<String> topArtists, ArrayList<String> topSongs, ArrayList<String> genre, ArrayList<Double> audioFeatures, ArrayList<String> recArtists) {
        super(activity);
        this.topArtists = topArtists;
        this.topSongs = topSongs;
        this.genre = genre;
        this.audioFeatures = audioFeatures;
        this.recArtists = recArtists;
    }

    public SpotifyInfoAdapter(FragmentActivity activity, Map<String, Object> spotifyData) {
        super(activity);
        this.topArtists = (ArrayList<String>) spotifyData.get("topArtists");
        this.topSongs = (ArrayList<String>) spotifyData.get("topSongs");
        this.genre = (ArrayList<String>) spotifyData.get("genre");
        this.audioFeatures = (ArrayList<Double>) spotifyData.get("audioFeatures");
        this.recArtists = (ArrayList<String>) spotifyData.get("recArtists");
    }

    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0:
                Bundle topArtistArgs = new Bundle();
                topArtistArgs.putStringArrayList("top_artists", topArtists);
                Fragment topArtistsFragment = new TopArtistsFragment();
                topArtistsFragment.setArguments(topArtistArgs);
                return topArtistsFragment;
            case 1:
                Bundle topSongArgs = new Bundle();
                topSongArgs.putStringArrayList("top_songs", topSongs);
                Fragment topSongsFragment = new TopSongsFragment();
                topSongsFragment.setArguments(topSongArgs);
                return topSongsFragment;
            case 2:
                Bundle genreArgs = new Bundle();
                genreArgs.putStringArrayList("genre", genre);
                Fragment genreFragment = new GenreFragment();
                genreFragment.setArguments(genreArgs);
                return genreFragment;
            case 3:
                Bundle afArgs = new Bundle();
                double[] temp = new double[audioFeatures.size()];
                for (int i = 0; i < temp.length; i++) {
                    temp[i] = audioFeatures.get(i);
                }
                afArgs.putDoubleArray("audio_features", temp);
                Fragment audioFeaturesFragment = new AudioFeaturesFragment();
                audioFeaturesFragment.setArguments(afArgs);
                return audioFeaturesFragment;
            case 4:
                Bundle recArgs = new Bundle();
                recArgs.putStringArrayList("rec_artists", recArtists);
                Fragment recArtistsFragment = new RecArtistsFragment();
                recArtistsFragment.setArguments(recArgs);
                return recArtistsFragment;
            default:
                return new TopArtistsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5; // Adjust depending on the number of slides
    }
}