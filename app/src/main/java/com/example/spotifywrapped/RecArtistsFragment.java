package com.example.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WelcomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecArtistsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Bundle bundle;

    private ArrayList<String> recArtists;
    private static final int PROGRESS_COUNT = 6;

    public RecArtistsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WelcomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WelcomeFragment newInstance(String param1, String param2) {
        WelcomeFragment fragment = new WelcomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            bundle = getArguments();
            recArtists = (ArrayList<String>) ((HashMap<String, Object>) bundle.getSerializable("data")).get("recArtists");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        LayoutInflater lf = getActivity().getLayoutInflater();
        View inflateview =  lf.inflate(R.layout.fragment_rec_artists, container, false);

        ((TextView) inflateview.findViewById(R.id.recArtist1)).setText(recArtists.get(0));
        ((TextView) inflateview.findViewById(R.id.recArtist2)).setText(recArtists.get(1));
        ((TextView) inflateview.findViewById(R.id.recArtist3)).setText(recArtists.get(2));
        ((TextView) inflateview.findViewById(R.id.recArtist4)).setText(recArtists.get(3));
        ((TextView) inflateview.findViewById(R.id.recArtist5)).setText(recArtists.get(4));

        Button button = (Button) inflateview.findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LlmFragment fragment = new LlmFragment();
                fragment.setArguments(bundle);
                // Transaction
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment);
                fragmentTransaction.addToBackStack(null); // Optional for back button
                fragmentTransaction.commit();
            }
        });
        return inflateview;
    }
}