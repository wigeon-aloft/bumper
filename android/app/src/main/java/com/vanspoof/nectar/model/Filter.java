package com.vanspoof.nectar.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Stores all information pertaining to a particular filter configuration. This object is used in
 * MainActivityViewModel to store the previously entered filter settings.
 */
public class Filter {
    public static final String TAG = "Filter";

    private boolean isEnabled;
    private ArrayList<Rating> ratingArrayList;
    private ArrayList<String> genreArrayList;
    private int[] runtime;
    private int[] releaseYear;

    /**
     * Default constructor.
     */
    public Filter() {
        // Initialise default field values.
        ratingArrayList = new ArrayList<>();
        genreArrayList = new ArrayList<>();
        runtime = new int[]{0,90};
        releaseYear = new int[]{0, 0};
        isEnabled = false;
    }

    /**
     * Takes JSONObject representing a filter (returned from the API).
     *
     * Should only be used when initialising a filter from an API response.
     */
    public Filter(JSONObject filter) {
        try {
            JSONArray ratingJSONArray = filter.getJSONArray("rating");
            JSONArray genreJSONArray = filter.getJSONArray("genre");
            JSONObject runtimeJSONObject = filter.getJSONObject("runtime");
            JSONObject yearJSONObject = filter.getJSONObject("year");

            ArrayList<Rating> ratingArrayList = new ArrayList<>();
            ArrayList<String> genreArrayList = new ArrayList<>();

            // Iterate through JSON array containing rating sources.
            for(int i = 0; i < ratingJSONArray.length(); i++) {
                ratingArrayList.add(new Rating(ratingJSONArray.getString(i)));
            }
            this.ratingArrayList = ratingArrayList;

            for(int i = 0; i < genreJSONArray.length(); i++) {
                genreArrayList.add(genreJSONArray.getString(i));
            }
            this.genreArrayList = genreArrayList;

            runtime = new int[]{runtimeJSONObject.getInt("min"), runtimeJSONObject.getInt("max")};
            releaseYear = new int[]{yearJSONObject.getInt("min"), yearJSONObject.getInt("max")};

        } catch (JSONException jse) {
            jse.printStackTrace();
        }

    }

    public Filter(ArrayList<Rating> ratingArrayList, ArrayList<String> genreArrayList, int[] runtime, int[] releaseYear) {
        this.ratingArrayList = ratingArrayList;
        this.genreArrayList = genreArrayList;
        this.runtime = runtime;
        this.releaseYear = releaseYear;
    }

    public void addRating(Rating rating) {
        ratingArrayList.add(rating);
    }

    public void removeRating(Rating rating) {
        ratingArrayList.remove(rating);
    }

    public ArrayList<Rating> getRatingList() {
        return this.ratingArrayList;
    }

    public void clearRatingList() {
        this.ratingArrayList.clear();
    }

    public void addGenre(String genre) {
        genreArrayList.add(genre);
    }

    public void removeGenre(String genre) {
        genreArrayList.add(genre);
    }

    public ArrayList<String> getGenreList() {
        return this.genreArrayList;
    }

    public void clearGenreList() {
        this.genreArrayList.clear();
    }

    public int[] getRuntime() {
        return runtime;
    }

    public void setRuntime(int[] runtime) {
        this.runtime = runtime;
    }

    public int[] getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int[] releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setReleaseYearStart(int releaseYearStart) {
        this.releaseYear[0] = releaseYearStart;
    }

    public void setReleaseYearEnd(int releaseYearEnd) {
        this.releaseYear[1] = releaseYearEnd;
    }

    /**
     * Returns a string that can be appended to a 'film' API request to filter the results.
     */
    public String buildURLQueryString() {
        if (isEnabled) {
            StringBuilder stringBuilder = new StringBuilder("?");
            // Append runtime and release year.
            stringBuilder
                    .append("runtime=").append(runtime[1]);

            if (releaseYear[0] == 0 && releaseYear[1] == 0) {
                stringBuilder.append("&releaseYear=[").append(releaseYear[0]).append(",").append(releaseYear[1]).append("]");
            }

            // Append the selected ratings as an array
            if (ratingArrayList.size() > 0) {
                stringBuilder.append("&rating=[");
                for (Rating rating : ratingArrayList) {
                    stringBuilder
                            .append("[")
                            .append(rating.getSource()).append(",")
                            .append(rating.getScore()).append(",")
                            .append(rating.getOperator())
                            .append("]");
                    // Check if the item is the last in the list. If so, don't append a ','.
                    if (ratingArrayList.indexOf(rating) != ratingArrayList.size() - 1) {
                        stringBuilder.append(",");
                    }
                }
                stringBuilder.append("]");
            }

            // Append the currently selected genres.
            if (genreArrayList.size() > 0) {
                stringBuilder.append("&genre=[");
                for (String genre : genreArrayList) {
                    // Check if the item is the last in the list. If so, don't append a ','.
                    if (genreArrayList.indexOf(genre) != genreArrayList.size() - 1) {
                        stringBuilder.append(genre).append(",");
                    } else {
                        stringBuilder.append(genre);
                    }
                }
                stringBuilder.append("]");
            }

            return stringBuilder.toString();
        } else {
            return "";
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}