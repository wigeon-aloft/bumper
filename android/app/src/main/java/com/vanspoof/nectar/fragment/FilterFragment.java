package com.vanspoof.nectar.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.vanspoof.nectar.R;
import com.vanspoof.nectar.view.GenreView;
import com.vanspoof.nectar.view.NumpickerView;
import com.vanspoof.nectar.view.RatingView;
import com.vanspoof.nectar.model.Filter;
import com.vanspoof.nectar.model.MainActivityViewModel;
import com.vanspoof.nectar.model.Rating;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FilterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilterFragment extends Fragment {

    public static final String TAG = "FilterFragment";

    private MainActivityViewModel mViewModel;
    private Filter mCurrentFilter;

    // UI Components
    private CheckBox mCheckboxEnable;
    private NumpickerView mNumpickerViewRuntime;
    private NumpickerView mNumpickerViewReleaseYearStart;
    private NumpickerView mNumpickerViewReleaseYearEnd;
    private ArrayList<RatingView> mArrayListRatingView;
    private ArrayList<GenreView> mArrayListGenreView;

    public FilterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FilmInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FilterFragment newInstance() {
        FilterFragment fragment = new FilterFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Add the RatingViews to a list for easy configuration later.
        mArrayListRatingView = new ArrayList<>();
        mArrayListGenreView = new ArrayList<>();
        mCheckboxEnable = getView().findViewById(R.id.fragment_filter_checkBox_enableFiltering);
        mNumpickerViewRuntime = getView().findViewById(R.id.fragment_filter_numpickerView_runtime);
        mNumpickerViewReleaseYearStart = getView().findViewById(R.id.fragment_filter_numpickerView_releaseYearStart);
        mNumpickerViewReleaseYearEnd = getView().findViewById(R.id.fragment_filter_numpickerView_releaseYearEnd);

        // Get ViewModel
        mViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
        mCurrentFilter = mViewModel.getCurrentFilter().getValue();

        // Create the observer which updates the UI when the new LimitFilter is populated.
        final Observer<Filter> filterObserver = new Observer<Filter>() {
            @Override
            public void onChanged(@Nullable final Filter newFilter) {
                // Call UI update method.
                updateUIWithCurrentLimitFilter(newFilter);
            }
        };
        mViewModel.updateLimitFilter();

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        mViewModel.getCurrentLimitFilter().observe(getViewLifecycleOwner(), filterObserver);

        mCheckboxEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCurrentFilter.setEnabled(isChecked);
                setUiEnabled(isChecked);
            }
        });

        setUiEnabled(mCurrentFilter.isEnabled());
        mCheckboxEnable.setChecked(mCurrentFilter.isEnabled());
    }

    @Override
    public void onStop() {
        super.onStop();
        // Update the Filter instance stored in the ViewModel
        mCurrentFilter.setRuntime(new int[]{0, mNumpickerViewRuntime.getValue()});
        mCurrentFilter.setReleaseYear(new int[]{mNumpickerViewReleaseYearStart.getValue(), mNumpickerViewReleaseYearEnd.getValue()});

        // Clear the existing rating list from the currentFilter and update with newly selected ratings.
        mCurrentFilter.clearRatingList();
        for(RatingView ratingView : mArrayListRatingView) {
            if (ratingView.isUiEnabled()) {
                mCurrentFilter.addRating(ratingView.getRating());
            }
        }
//        mArrayListRatingView.clear();

        // Clear the existing genre list from the currentFilter and update with new selections.
        mCurrentFilter.clearGenreList();
        for (GenreView genreView : mArrayListGenreView) {
            if (genreView.isSelected()) {
                mCurrentFilter.addGenre(genreView.getGenre());
            }
        }

        // todo add release year to viewmodel filter
        Log.v(TAG, mCurrentFilter.buildURLQueryString());
        mViewModel.setCurrentFilter(mCurrentFilter);
    }

    /**
     * Updates UI with values from the currentFilter.
     */
    public void updateUIWithCurrentFilter() {
        // Update UI with user-selected genres from the previous filter (if there are any)
        // Get list of currently displayed Genres
//        ArrayList<String> genres = new ArrayList<>();
//        for (GenreView genreView : mArrayListGenreView) {
//            genres.add(genreView.getGenre());
//        }
        if (mCurrentFilter.getGenreList().size() > 0) {
            for (GenreView genreView : mArrayListGenreView) {
                if (mCurrentFilter.getGenreList().contains(genreView.getGenre())) {
                    genreView.setSelected(true);
                } else {
                    genreView.setSelected(false);
                }
            }
        } else {
            for (GenreView genreView : mArrayListGenreView) {
                genreView.setSelected(false);
            }
        }

        // Update UI with rating information.
        boolean existingView = false;
        if (mCurrentFilter.getRatingList().size() > 0) {
            for (RatingView ratingView : mArrayListRatingView) {
                existingView = false;
                for (Rating rating : mCurrentFilter.getRatingList()) {
                    if (rating.getSource().equals(ratingView.getRating().getSource())) {
                        existingView = true;
                        ratingView.setScore(rating.getScore());
                        ratingView.setEqualityOperator(rating.getOperator());
                        ratingView.setRatingInputUIEnabled(rating.isEnabled());
                    }
                }
                if (!existingView) {
                    ratingView.setRatingInputUIEnabled(false);
                    ratingView.setEqualityOperator("=");
                }
            }
        } else {
            for (RatingView ratingView : mArrayListRatingView) {
                ratingView.setRatingInputUIEnabled(false);
            }
        }

        mNumpickerViewRuntime.setValue(mCurrentFilter.getRuntime()[1]);
        mNumpickerViewReleaseYearStart.setValue(mCurrentFilter.getReleaseYear()[0]);
        if (mCurrentFilter.getReleaseYear()[1] == 0) {
            mNumpickerViewReleaseYearEnd.setValue(mNumpickerViewReleaseYearEnd.getMaxValue());
        } else {
            mNumpickerViewReleaseYearEnd.setValue(mCurrentFilter.getReleaseYear()[1]);
        }
    }

    public void updateUIWithCurrentLimitFilter(Filter filter) {

        // Get the Rating LinearLayout from the layout file.
        LinearLayout ratingLayout = getView().findViewById(R.id.fragment_filter_linearLayout_rating);
        // Generate RatingViews from the Ratings in the filter and add them to the UI.
        // Create a list of the current Rating providers for checking.
        ArrayList<String> ratingSources = new ArrayList<>();
        for (RatingView ratingView : mArrayListRatingView) {
            ratingSources.add(ratingView.getRating().getSource());
        }
        for (Rating rating : filter.getRatingList()) {
            if (!ratingSources.contains(rating.getSource())) {
                RatingView ratingView = new RatingView(getContext(), null);
                ratingView.setRatingProviderName(rating.getSource());
                ratingLayout.addView(ratingView);
                mArrayListRatingView.add(ratingView);
            }
        }

        // Retrieve Genre LinearLayout from layout file.
        GridLayout genreLayout = getView().findViewById(R.id.fragment_filter_gridLayout_genre);
        // todo figure out how to space the cells evenly (might need to use a GridView with an array adapter).
        // Generate TextViews from the Genres listed in the filter and add them to the UI.
        ArrayList<String> genreList = new ArrayList<>();
        for (GenreView existingGenreView : mArrayListGenreView) {
            genreList.add(existingGenreView.getGenre());
        }
        for (String genre : filter.getGenreList()) {
            // todo implement GenreView
            // todo implement if to check if the genre is already listed on the UI
            if (!genreList.contains(genre)) {
                GenreView genreView = new GenreView(getContext());
                genreView.setGenre(genre);
//                genreView.setLayoutParams(params);
                genreLayout.addView(genreView);
                mArrayListGenreView.add(genreView);
            }
        }

        int[] runtime = filter.getRuntime();
        mNumpickerViewRuntime.setMinValue(runtime[0]);
        mNumpickerViewRuntime.setMaxValue(runtime[1]);

        int[] year = filter.getReleaseYear();
        mNumpickerViewReleaseYearStart.setMinValue(year[0]);
        mNumpickerViewReleaseYearStart.setMaxValue(year[1]);
        mNumpickerViewReleaseYearEnd.setMinValue(year[0]);
        mNumpickerViewReleaseYearEnd.setMaxValue(year[1]);

        updateUIWithCurrentFilter();
    }

    public void setUiEnabled(boolean enabled) {
        for (RatingView ratingView : mArrayListRatingView) {
            ratingView.setUiEnabled(enabled);
        }

        for (GenreView genreView : mArrayListGenreView) {
            genreView.setUiEnabled(enabled);
        }

        mNumpickerViewReleaseYearStart.setEnabled(enabled);
        mNumpickerViewReleaseYearEnd.setEnabled(enabled);
        mNumpickerViewRuntime.setEnabled(enabled);
    }
}