package com.vanspoof.nectar.fragment.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.vanspoof.nectar.R;
import com.vanspoof.nectar.model.Film;
import com.vanspoof.nectar.model.MainActivityViewModel;
import com.vanspoof.nectar.model.Relationship;
import com.vanspoof.nectar.model.User;
import com.vanspoof.nectar.view.UserRelationshipView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Displays User profile and actions (such as button for searching other users).
 */
public class UserFragment extends Fragment {
    private static final String TAG = "UserFragment";
    private MainActivityViewModel mViewModel;
    private Button mButtonAddUser;
    private LinearLayout mRelationshipLinearLayout;
    private UserFragmentInterface mUserFragmentInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View userFragment = inflater.inflate(R.layout.fragment_user, container, false);

        // Initialise the mButtonAddUser field in this method to avoid NPE when attempting to set onClickListener.
        mButtonAddUser = userFragment.findViewById(R.id.fragment_user_button_addUser);
        mRelationshipLinearLayout = userFragment.findViewById(R.id.fragment_user_linearLayout_relationship);

        // Initialise the onClickListener for the button
        mButtonAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserFragmentInterface.addUserButtonClicked(v);
            }
        });

        // Inflate the layout for this fragment
        return userFragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Acquire current instance of MainActivityViewModel
        mViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

        mViewModel.getRelationshipList(false);
        // Create the observer which updates the UI.
        final Observer<ArrayList<Relationship>> relationshipListObserver = new Observer<ArrayList<Relationship>>() {
            @Override
            public void onChanged(@Nullable final ArrayList<Relationship> relationshipList) {
                // call UI update method
                if (relationshipList != null) {
                    updateRelationshipUi(relationshipList);
                }
            }
        };

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        mViewModel.getRelationshipList().observe(getViewLifecycleOwner(), relationshipListObserver);

        // Get the UI components from the layout
    }

    public void updateRelationshipUi(ArrayList<Relationship> relationshipList) {
        mRelationshipLinearLayout.removeAllViews();
        for (Relationship relationship : relationshipList) {
            UserRelationshipView relationshipView = new UserRelationshipView(getContext(), null);
            relationshipView.setUser(relationship.getNonCurrentUser(mViewModel.getCurrentUser().getValue().getUserId()));
            relationshipView.setRelationship(relationship);
            mRelationshipLinearLayout.addView(relationshipView);
        }
    }


    public void setInterface(UserFragmentInterface userFragmentInterface) {
        mUserFragmentInterface = userFragmentInterface;
    }

    /**
     * Called by MainActivity to set the "Add User" button's onClickListener.
     *
     * As the intended function of this button is to indicate that the "user search" fragment should
     * be added to the UI, it's best for this listener to be defined in the MainActivity, as it has
     * built-in methods for displaying new fragments.
     *
     * @param listener
     *  onClickListener defined in MainActivity to display a "user search" fragment.
     */
    public void setAddUserOnClickListener(View.OnClickListener listener){
        mButtonAddUser.setOnClickListener(listener);
    }
}