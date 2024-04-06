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
 * Use the {@link RecArtistsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecArtistsFragment extends Fragment {

    private Call mCall;
    public ArrayList<String> recArtists = new ArrayList<>();
    public RecArtistsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     @return A new instance of fragment RecArtistsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecArtistsFragment newInstance(String param1, String param2) {
        RecArtistsFragment fragment = new RecArtistsFragment();
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
        View view =   inflater.inflate(R.layout.fragment_rec_artists, container, false);
        this.recArtists = getArguments().getStringArrayList("rec_artists");
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout myLinearLayout = view.findViewById(R.id.recArtistsLayout);
        for (String g : recArtists) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            TextView recArtistsTextView = (TextView) inflater.inflate(R.layout.textview, myLinearLayout, false);
            recArtistsTextView.setText(g);
            myLinearLayout.addView(recArtistsTextView);
        }
    }
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
