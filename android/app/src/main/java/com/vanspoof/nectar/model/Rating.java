package com.vanspoof.nectar.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Rating {
    private String source;
    private int score;
    private String operator;
    private boolean enabled;

    public Rating(String source) {
        this.source = source;
        this.score = 0;
    }

    public Rating(String source, int score) {
        this.source = source;
        this.score = score;
    }

    public Rating(String source, int score, String operator) {
        this.source = source;
        this.score = score;
        this.operator = operator;
    }

    public Rating(String source, int score, String operator, boolean enabled) {
        this.source = source;
        this.score = score;
        this.operator = operator;
        this.enabled = enabled;
    }

    public Rating(JSONObject ratingJSON) {
        try {
            this.source = ratingJSON.getString("source");
            this.score = ratingJSON.getInt("score");
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    public String getSource() {
        return source;
    }

    public int getScore() {
        return score;
    }

    public String getScoreString() {
        return Integer.toString(score);
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Converts JSONArray of rating information (from API response) into ArrayList of Rating objects.
     * @param ratingJSONArray
     * @return
     */
    public static ArrayList<Rating> convertJSONArrayToRatingArray(JSONArray ratingJSONArray) {
        ArrayList<Rating> ratingArrayList = new ArrayList<>();
        for (int i=0; i < ratingJSONArray.length(); i++) {
            try {
                JSONObject ratingJSONObject = ratingJSONArray.getJSONObject(i);
                ratingArrayList.add(new Rating(ratingJSONObject));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return ratingArrayList;
    }
}
