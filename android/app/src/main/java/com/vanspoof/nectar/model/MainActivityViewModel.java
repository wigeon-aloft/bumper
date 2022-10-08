package com.vanspoof.nectar.model;

import android.app.Application;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.RelativeLayout;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivityViewModel extends AndroidViewModel {
    private static final String TAG = "MainActivityViewModel";

    private MutableLiveData<Film> currentFilm;
    private MutableLiveData<User> currentUser;
    private MutableLiveData<Filter> currentFilter;
    private MutableLiveData<Filter> currentLimitFilter;
    private MutableLiveData<ArrayList<User>> userSearchResultsList;
    private MutableLiveData<ArrayList<Relationship>> relationshipList;

    private RequestQueue mRequestQueue;

    private final String FILM_URL = "http://10.0.2.2:5000/film";
    private final String VOTE_URL = "http://10.0.2.2:5000/vote";
    private final String CREATE_USER_URL = "http://10.0.2.2:5000/user";
    private final String LOGIN_USER_URL = "http://10.0.2.2:5000/login";
    private final String USERSEARCH_URL = "http://10.0.2.2:5000/usersearch";
    private final String RELATIONSHIP_URL = "http://10.0.2.2:5000/relationship";
    private final String FILTER_URL = "http://10.0.2.2:5000/filter";


    public MainActivityViewModel(Application application) {
        super(application);
        currentFilm = new MutableLiveData<>();
        currentUser = new MutableLiveData<>();
        currentFilter = new MutableLiveData<>();
        currentLimitFilter = new MutableLiveData<>();
        userSearchResultsList = new MutableLiveData<>();
        relationshipList = new MutableLiveData<>();

        // Initialise values
        currentFilm.setValue(new Film());
        userSearchResultsList.setValue(new ArrayList<User>());
        currentFilter.setValue(new Filter());

        // Set up Volley's RequestQueue with Application instance
        mRequestQueue = Volley.newRequestQueue(getApplication());
    }

    /**
     * Appends current user's login token to request URL.
     * @param url
     *  String representing URL that token will be appended to.
     * @return
     *  String formatted with user's token parameter.
     */
    private String appendToken(String url) {
        /* Check if there is already a query string in the URL.
        * If there is, append token using '&'. */
        if (url.contains("?")) {
            return url + "&token=" + currentUser.getValue().getToken();
        } else {
            return url + "?token=" + currentUser.getValue().getToken();
        }
    }

    public LiveData<Film> getCurrentFilm() {
        return currentFilm;
    }

    public LiveData<ArrayList<Relationship>> getRelationshipList() {
        return relationshipList;
    }

    /**
     * Takes JSONObject input and creates Film object to assign to currentFilm field.
     */
    public void updateCurrentFilm(JSONObject filmJSON) {
        currentFilm.setValue(new Film(filmJSON));
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser.setValue(user);
    }

    public void setCurrentFilter(Filter filter) {
        currentFilter.setValue(filter);
    }

    /**
     * Sets the current 'max' filter. This is used to set the
     * upper/lower bounds for values in FilterFragment UI elements.
     * 
     * @param filter
     * Filter object initialised with the max/min value for all filter properties.
     */
    public void setCurrentLimitFilter(Filter filter) {
        currentLimitFilter.setValue(filter);
    }

    public LiveData<Filter> getCurrentLimitFilter() {
        return currentLimitFilter;
    }

    public LiveData<Filter> getCurrentFilter() {
        return currentFilter;
    }

    public LiveData<ArrayList<User>> getUserSearchResultsList() {
        return userSearchResultsList;
    }

    public void setUserSearchResultsList(JSONArray userSearchResultJSON) {
        ArrayList<User> userList = new ArrayList<>();
        for (int i = 0; i < userSearchResultJSON.length(); i++) {
            try {
                JSONObject userJSON = userSearchResultJSON.getJSONObject(i);
                User user = new User(userJSON, "");
                userList.add(user);
            } catch (JSONException jse) {
                jse.printStackTrace();
            }
        }
        userSearchResultsList.setValue(userList);
    }

    /**
     * Allows clearing of user search result list when closing UserSearchFragment instance.
     */
    public void clearUserSearchResultsList() {
        userSearchResultsList.setValue(new ArrayList<User>());
    }

    public void getNewFilm() {
        String queryString = "";
        if (currentFilter.getValue() != null) {
            queryString = currentFilter.getValue().buildURLQueryString();
        }
        Log.v(TAG, FILM_URL + queryString);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, appendToken(FILM_URL + queryString),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                updateCurrentFilm((JSONObject) new JSONObject(response).get("film"));

                            } catch (JSONException je) {
                                je.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            // Add the request to the RequestQueue.
            mRequestQueue.add(stringRequest);
    }

    public void sendVote(String vote) {
        Log.v(TAG, "reached sendVote");
        Film film = currentFilm.getValue();
        String voteUrl = VOTE_URL + "?film=" + film.getId() + "&user=" + currentUser.getValue().getUserId() + "&vote=" + vote;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, appendToken(voteUrl),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            if (json.getBoolean("success")) {
                                getNewFilm();
                            }
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );

        mRequestQueue.add(stringRequest);
    }

    public void sendLoginRequest(String username, String password) {
        String loginUserUrl = LOGIN_USER_URL + "?username=" + username + "&password=" + password;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, loginUserUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            if (json.getBoolean("success")) {
//                                setCurrentUser(json.getString("email"), json.getInt("user_id"), json.getString("token"));
                                User user = new User(
                                        json.getJSONObject("user"),
                                        json.getString("token")
                                );
                                setCurrentUser(user);
                            }
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );

        mRequestQueue.add(stringRequest);
    }

    public void sendCreateUserRequest(String username, String password) {
        String createUserUrl = CREATE_USER_URL + "?username=" + username + "&password=" + password;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, appendToken(createUserUrl),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            if (json.getBoolean("success")) {
                                setCurrentUser(new User(json.getJSONObject("user"), json.getString("token")));
                            }
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );

        mRequestQueue.add(stringRequest);
    }

    public void sendUserSearchRequest(String search) {
        if (search.equals("")) {
            userSearchResultsList.setValue(new ArrayList<User>());
        } else {
            String userSearchUrl = appendToken(USERSEARCH_URL + "?search=" + search);
            Log.v(TAG, "Search URL: " + userSearchUrl);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, appendToken(userSearchUrl),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject json = new JSONObject(response);
                                if (json.getBoolean("success")) {
                                    setUserSearchResultsList(json.getJSONArray("users"));
                                }
                            } catch (JSONException je) {
                                je.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    }
            );
            mRequestQueue.add(stringRequest);
        }
    }

    /**
     * Sends HTTP POST request to API to add relationship to database.
     *
     * @param requester
     *  int representing the user ID of the person who sent the relationship request.
     * @param recipient
     *  int representing the user ID of the person who is to receive the relationship request.
     */
    public void sendRelationshipRequest(int requester, int recipient, String status) {
        String addRelationshipUrl = RELATIONSHIP_URL + "?requester=" + requester + "&recipient=" + recipient;
        if (!status.equals("")) {
            addRelationshipUrl += "?status=" + status;
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, appendToken(addRelationshipUrl),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, "POST relationship request sent successfully.");
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );
        mRequestQueue.add(stringRequest);
    }

    /**
     * Sends HTTP POST request to API to add relationship to database.
     *
     * @param requester
     *  int representing the user ID of the person who sent the relationship request.
     * @param recipient
     *  int representing the user ID of the person who is to receive the relationship request.
     * @param status
     *  String, only values of "recanted" and "rejected" will be accepted by the API.
     */
    public void sendDeleteRelationshipRequest(int requester, int recipient, String status) {
        String deleteRelationshipUrl = RELATIONSHIP_URL + "?requester=" + requester + "&recipient=" + recipient + "&status=" + status;

        // Check if value of passed "status" parameter is valid.
        if (status.equals("recanted") || status.equals("rejected")) {
            StringRequest stringRequest = new StringRequest(Request.Method.DELETE, appendToken(deleteRelationshipUrl),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.v(TAG, "DELETE relationship request sent successfully.");
                        }

                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.v(TAG, "There was an issue whilst sending DELETE relationship request.");
                            error.printStackTrace();
                        }
                    }
            );
            mRequestQueue.add(stringRequest);
        }
    }

    public void getRelationshipList(boolean verified_only) {
        String getRelationshipUrl = RELATIONSHIP_URL + "?user_id=" + currentUser.getValue().getUserId();
        if (verified_only) {
            getRelationshipUrl += "?verified_only=" + Boolean.toString(verified_only);
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, appendToken(getRelationshipUrl),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, "GET Relationship request sent successfully.");
                        try {
                            JSONObject json = new JSONObject(response);
                            if (json.getBoolean("success")) {
                                setRelationshipList(json.getJSONArray("relationship"));
                            }
                        } catch (JSONException je) {
                            Log.w(TAG, "There was an error whilst decoding the JSON response to a genreListUpdate request.");
                            je.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v(TAG, "There was an issue whilst retrieving the list of relationships for the current user.");
                        error.printStackTrace();
                    }
                }
        );
        mRequestQueue.add(stringRequest);
    }

    private void setRelationshipList(JSONArray relationshipJSONArray) {
        ArrayList<Relationship> newRelationshipList = new ArrayList<>();
        try {
            for (int i = 0; i < relationshipJSONArray.length(); i++) {
//                Log.v(TAG, );
                JSONObject relationshipJSON = relationshipJSONArray.getJSONObject(i);
                newRelationshipList.add(new Relationship(
                        new User(relationshipJSON.getJSONObject("requester"), ""),
                        new User(relationshipJSON.getJSONObject("recipient"), ""),
                        relationshipJSON.getString("status")
                ));
            }
            // Clear the contents of the old relationship list and set value as the new list.
            if (relationshipList.getValue() != null) {
                relationshipList.getValue().clear();
            }
            relationshipList.setValue(newRelationshipList);
        } catch (JSONException jse) {
            jse.printStackTrace();
        }
    }

    /**
     * Request a set of possible filter values from the API.
     */
    public void updateLimitFilter() {
        String genreListUrl = FILTER_URL;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, appendToken(FILTER_URL),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, "POST genre request sent successfully.");
                        try {
                            JSONObject json = new JSONObject(response);
                            if (json.getBoolean("success")) {
                                setCurrentLimitFilter(new Filter(json.getJSONObject("filter")));
                            }
                        } catch (JSONException je) {
                            Log.w(TAG, "There was an error whilst decoding the JSON response to a genreListUpdate request.");
                            je.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v(TAG, "There was an issue whilst retrieving the min/max filter parameters.");
                        error.printStackTrace();
                    }
                }
        );
        mRequestQueue.add(stringRequest);
    }
}
