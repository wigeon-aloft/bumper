package com.vanspoof.nectar.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Film {
    private String TAG = "Film";

    private int id;
    private String title;
    private int runtime;
    private String imdbId;
    private ArrayList<String> genreList;
    private ArrayList<Rating> ratingList;

    public Film() {
        id = 0;
        title = "";
        runtime = 0;
        imdbId = "";
        genreList = new ArrayList<>();
        ratingList = new ArrayList<>();
    }

    /**
     *
     * @param filmInfo
     * @throws JSONException
     */
    public Film(JSONObject filmInfo) {
        try {
            this.id = filmInfo.getInt("id");
            this.title = filmInfo.getString("title");
            this.runtime = filmInfo.getInt("runtime");
            this.imdbId = filmInfo.getString("imdb_id");

            this.genreList = new ArrayList<>();
            this.ratingList = new ArrayList<>();

            JSONArray genreJSONArray = filmInfo.getJSONArray("genre");
            JSONArray ratingJSONArray = filmInfo.getJSONArray("rating");
//            Log.v(TAG, genreJSONArray.toString());
//            Log.v(TAG, ratingJSONArray.toString());

            // Extract the genres from the array
            for (int n = 0; n < genreJSONArray.length(); n++) {
//                Log.v(TAG, (String) genreJSONArray.getString(n));
                genreList.add((String) genreJSONArray.getString(n));
            }

            // use conversion method from Rating object to convert JSONArray to Rating array
            ratingList = Rating.convertJSONArrayToRatingArray(ratingJSONArray);
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getRuntime() {
        return runtime;
    }

    public String getImdbId() {
        return imdbId;
    }

    public ArrayList<String> getGenreList() {
        return genreList;
    }

    public ArrayList<Rating> getRatingList() {
        return ratingList;
    }
}
