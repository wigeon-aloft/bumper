package com.vanspoof.nectar.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vanspoof.nectar.R;
import com.vanspoof.nectar.view.RatingDisplayView;
import com.vanspoof.nectar.model.Film;
import com.vanspoof.nectar.model.MainActivityViewModel;
import com.vanspoof.nectar.model.Rating;

import java.util.ArrayList;

public class FilmInfoFragment extends Fragment {
    private static final String TAG = "FilmInfoFragment";

    private TextView mTextViewTitle;
    private TextView mTextViewRuntime;
    private TextView mTextViewGenre;
    private TextView mTextViewRating;
    private Button mButtonNewFilm;

    private ArrayList<RatingDisplayView> mRatingDisplayViewArrayList;

    private MainActivityViewModel mViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filminfo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

        // Create the observer which updates the UI.
        final Observer<Film> filmObserver = new Observer<Film>() {
            @Override
            public void onChanged(@Nullable final Film newFilm) {
                // call UI update method
                updateFilmInfoTextView(newFilm);
            }
        };

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        mViewModel.getCurrentFilm().observe(getViewLifecycleOwner(), filmObserver);

        mTextViewTitle = getView().findViewById(R.id.fragment_filminfo_textview_title);
        mTextViewRuntime = getView().findViewById(R.id.fragment_filminfo_textview_runtime);
        mButtonNewFilm = getView().findViewById(R.id.fragment_filminfo_button_newfilm);

        mButtonNewFilm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.getNewFilm();
            }
        });

        mRatingDisplayViewArrayList = new ArrayList<>();
        mViewModel.updateLimitFilter();
        // Get a new movie from API.
    }

    public void updateFilmInfoTextView(Film currentFilm) {
        mTextViewTitle.setText(currentFilm.getTitle());
        mTextViewRuntime.setText(getString(R.string.runtime_mins_format, currentFilm.getRuntime()));

        // Create RatingDisplayViews to be displayed in the rating LinearLayout
        LinearLayout ratingLinearLayout = getView().findViewById(R.id.fragment_filminfo_linearLayout_rating);
        ratingLinearLayout.removeAllViews();
        if (currentFilm.getRatingList() != null) {
            for (Rating rating : currentFilm.getRatingList()) {
                RatingDisplayView ratingDisplayView = new RatingDisplayView(getContext(), null);
                ratingDisplayView.setSource(rating.getSource());
                ratingDisplayView.setScore(rating.getScore());
                ratingLinearLayout.addView(ratingDisplayView);
            }
        }

        // Create RatingDisplayViews to be displayed in the rating LinearLayout
        LinearLayout genreLinearLayout = getView().findViewById(R.id.fragment_filminfo_linearLayout_genre);
        genreLinearLayout.removeAllViews();
        if (currentFilm.getGenreList() != null) {
            for (String genre : currentFilm.getGenreList()) {
                TextView genreTextView = new TextView(getContext());
                genreTextView.setText(genre);
                genreLinearLayout.addView(genreTextView);
            }
        }
    }
}