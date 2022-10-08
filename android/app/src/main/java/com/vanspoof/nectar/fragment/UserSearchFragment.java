package com.vanspoof.nectar.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.vanspoof.nectar.R;
import com.vanspoof.nectar.model.MainActivityViewModel;
import com.vanspoof.nectar.model.User;
import com.vanspoof.nectar.model.UserSearchArrayAdapter;

import java.util.ArrayList;

/**
 * Fragment for allowing App user to search for a another user, have a list of users matching their
 * search terms displayed, and functionality for "adding" another user.
 */
public class UserSearchFragment extends Fragment {
    private static final String TAG = "UserSearchFragment";

    private MainActivityViewModel mViewModel;
    private UserSearchArrayAdapter mUserSearchArrayAdapter;
    private ArrayList<User> mUserList;

    private EditText mEditTextUserSearch;
    private ListView mListViewUserList;
    private TextView mTextViewNoResults;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_usersearch, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the MainActivityViewModel instance from MainActivity
        mViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

        // Create an Observer for the UserSearchResultsList in the ViewModel that updates the list of search results.
        final Observer<ArrayList<User>> userSearchResultsObserver = new Observer<ArrayList<User>>() {
            @Override
            public void onChanged(ArrayList<User> users) {
                updateUserSearchResultsUI(users);
            }
        };

        // Observe the search results LiveData
        mViewModel.getUserSearchResultsList().observe(getViewLifecycleOwner(), userSearchResultsObserver);

        // Get UI components from layout
        mEditTextUserSearch = view.findViewById(R.id.fragment_usersearch_edittext_usersearch);
        mListViewUserList = view.findViewById(R.id.fragment_usersearch_listview_userlist);
        mTextViewNoResults = view.findViewById(R.id.fragment_usersearch_textview_noresults);

        // Set up the array adapter and associated listview
        mUserList = new ArrayList<>();
        mUserSearchArrayAdapter = new UserSearchArrayAdapter(getActivity(), mUserList);
        mListViewUserList.setAdapter(mUserSearchArrayAdapter);
        mUserSearchArrayAdapter.notifyDataSetChanged();

        // Create a listener to detect text being changed in EditText
        mEditTextUserSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    // When user changes text send a usersearch API request
                    mViewModel.sendUserSearchRequest(s.toString());
                } else {
                    // If search string is clear empty the userlist and update the arrayadapter
                    mUserList.clear();
                    mUserSearchArrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used.
            }
        });

        mListViewUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the requester and recipients user IDs.
                final int requester = mViewModel.getCurrentUser().getValue().getUserId();
                final int recipient = mUserList.get(position).getUserId();

                // Call the ViewModel's method for adding a relationship request.
                mViewModel.sendRelationshipRequest(requester, recipient, "");

                // todo this snackbar should only be displayed if the relationship request was successful.
                // Display a Snackbar that lets the user know that a request has been sent.
                Snackbar userClickedSnackbar = Snackbar.make(view, String.format("Sent relationship request to %s.", mUserList.get(position).getUsername()), Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Call ViewModel method that deletes an existing relationship request.
                                mViewModel.sendDeleteRelationshipRequest(requester, recipient, "recanted");

                                // Display a new Snackbar that tells user that the request has been cancelled.
                                // todo should only be displayed if rescinding the request was successful.
                                Snackbar undoSnackbar = Snackbar.make(view, "Relationship request retracted.", Snackbar.LENGTH_SHORT);
                                undoSnackbar.show();
                            }
                        });
                userClickedSnackbar.show();

            }
        });
    }

    private void updateUserSearchResultsUI(ArrayList<User> users) {
        Log.v(TAG,  "New search results received. Updating ListView.");
        // Clear the current user list
        mUserList.clear();
        if (users.size() > 0) {
            // Add new users to user list
            for (User user : users) {
                mUserList.add(user);
            }
            // Update ArrayAdapter
            mUserSearchArrayAdapter.notifyDataSetChanged();
            // Hide the "no results" textview
            mTextViewNoResults.setVisibility(View.INVISIBLE);
        } else {
            // Show the "no results" textview
            mTextViewNoResults.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewModel.clearUserSearchResultsList();
    }
}