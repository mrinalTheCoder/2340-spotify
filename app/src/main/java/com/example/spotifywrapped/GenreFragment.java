package com.example.spotifywrapped;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

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
 * Use the {@link GenreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenreFragment extends Fragment {

    private Call mCall;
    public ArrayList<String> genre = new ArrayList<>();
    public GenreFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     @return A new instance of fragment GenreFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GenreFragment newInstance(String param1, String param2) {
        GenreFragment fragment = new GenreFragment();
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
        View view =   inflater.inflate(R.layout.fragment_genre, container, false);
        ArrayList<String> genre = getArguments().getStringArrayList("genre");
        this.genre = genre;

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout myLinearLayout = view.findViewById(R.id.genreLayout);



        HashMap<String, Integer> ans = new HashMap<>();
        for (String g : genre) {
            if (ans.get(g) == null) {
                ans.put(g, 1);
            } else {
                ans.put(g, ans.get(g) + 1);
            }
        }
        ArrayList<HashMap.Entry<String, Integer>> entryList = new ArrayList<>(ans.entrySet());

        Collections.sort(entryList, (entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        ArrayList<String> sortedStrings = new ArrayList<>();
        for (HashMap.Entry<String, Integer> entry : entryList) {
            sortedStrings.add(entry.getKey());
        }


        for (String g : sortedStrings) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            TextView genreTextView = (TextView) inflater.inflate(R.layout.textview, myLinearLayout, false);
            genreTextView.setText(g);
            myLinearLayout.addView(genreTextView);
        }
    }
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
