package com.vanspoof.nectar.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.vanspoof.nectar.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FilmSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilmSearchFragment extends Fragment {
    private static final String TAG = "FilmSearchFragment";

    public FilmSearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FilmInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FilmSearchFragment newInstance() {
        FilmSearchFragment fragment = new FilmSearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }
}