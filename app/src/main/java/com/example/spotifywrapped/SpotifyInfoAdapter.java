package com.example.spotifywrapped;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
// ...other necessary imports 

public class SpotifyInfoAdapter extends FragmentStateAdapter {

    public SpotifyInfoAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TopArtistsFragment(); // Replace with your fragment class
            case 1:
                return new TopSongsFragment(); // Replace with your fragment class
            case 2:
                return new TopSongsFragment(); // Replace with your fragment class
            case 3:
                return new TopSongsFragment(); // Replace with your fragment class
            case 4:
                return new TopSongsFragment(); // Replace with your fragment class
            default:
                return new TopArtistsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5; // Adjust depending on the number of slides
    }
}