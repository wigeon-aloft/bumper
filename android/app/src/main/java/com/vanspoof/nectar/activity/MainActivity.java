package com.vanspoof.nectar.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.RequestQueue;
import com.vanspoof.nectar.R;
import com.vanspoof.nectar.fragment.FilmInfoFragment;
import com.vanspoof.nectar.fragment.FilterFragment;
import com.vanspoof.nectar.fragment.LoginFragment;
import com.vanspoof.nectar.fragment.SettingsFragment;
import com.vanspoof.nectar.fragment.user.UserFragment;
import com.vanspoof.nectar.fragment.UserSearchFragment;
import com.vanspoof.nectar.fragment.VoteFragment;
import com.vanspoof.nectar.fragment.user.UserFragmentInterface;
import com.vanspoof.nectar.model.MainActivityViewModel;
import com.vanspoof.nectar.model.User;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements UserFragmentInterface {
    private static final String TAG = "MainActivity";

    private MainActivityViewModel mViewModel;
    private LoginFragment mLoginFragment;
    private FilmInfoFragment mFilmInfoFragment;
    private VoteFragment mVoteFragment;
    private UserFragment mUserFragment;
    private UserSearchFragment mUserSearchFragment;
    private FilterFragment mFilterFragment;
    private SettingsFragment mSettingsFragment;

    private ArrayList<Boolean> mToobarVisibilityBackStack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mainToolbar = (Toolbar) findViewById(R.id.activity_main_toolbar_main);
        setSupportActionBar(mainToolbar);

        // Initialise fragments.
        mLoginFragment = new LoginFragment();
        mFilmInfoFragment = new FilmInfoFragment();
        mVoteFragment = new VoteFragment();
        mUserFragment = new UserFragment();
        mUserSearchFragment = new UserSearchFragment();
        mFilterFragment = new FilterFragment();
        mSettingsFragment = new SettingsFragment();

        // Initialise toolbar visibility back stack
        mToobarVisibilityBackStack = new ArrayList<Boolean>();

        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        // Create the observer which updates the UI.
        final Observer<User> userObserver = new Observer<User>() {
            @Override
            public void onChanged(@Nullable final User newUser) {
                // call UI update method

                ArrayList<Fragment> layoutFragments = new ArrayList<>();

                // if there is no user logged in, show loginfragment
                if (newUser == null) {
                    Log.v(TAG, "No user logged in. Displaying login fragment and hiding Toolbar.");
                    layoutFragments.add(mLoginFragment);
                    showNewFragments(layoutFragments, false, false);
                } else {
                    Log.v(TAG, "User logged in. Displaying main layout.");
                    layoutFragments.add(mFilmInfoFragment);
                    layoutFragments.add(mVoteFragment);
                    showNewFragments(layoutFragments, true, false);
                }

            }
        };

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        mViewModel.getCurrentUser().observe(this, userObserver);
        User currentUser = (User) mViewModel.getCurrentUser().getValue();

        ArrayList<Fragment> layoutFragments = new ArrayList<>();

        if (currentUser != null) {
            Log.v(TAG, "User logged in. Displaying main layout.");
            layoutFragments.add(mFilmInfoFragment);
            layoutFragments.add(mVoteFragment);
            showNewFragments(layoutFragments, true, false);
        } else {
            Log.v(TAG, "No user logged in. Displaying login fragment and hiding Toolbar.");
            layoutFragments.add(mLoginFragment);
            showNewFragments(layoutFragments, false, false);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.overflow_menu_toolbar_main, menu);
        return true;
    }

    /**
     * Takes list of Fragment objects and replaces the current layout with them.
     *
     * @param newFragments
     *  ArrayList containing all of the new fragments to add to the UI, in order.
     * @param toolbarVisible
     *  Boolean dictating whether the App's toolbar should be hidden when the UI is updated.
     *  A 'true' value will show the app's toolbar and 'false' will hide it.
     * @param addToBackStack
     *  Boolean indicating whether the current layout should be added to the FragmentManager's
     *  back stack once the UI is updated.
     */
    public void showNewFragments(ArrayList<Fragment> newFragments, boolean toolbarVisible, boolean addToBackStack) {
        boolean USER_FRAGMENT_IN_LIST = false;
        UserFragment userFragment;

        // Check that the newFragments ArrayList is not empty.
        if (newFragments.size() > 0) {
            // Start the FragmentManager transaction.
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // If the list contains just a single Fragment then update the UI using the replace() method
            if (newFragments.size() == 1) {
                    transaction.replace(R.id.fragmentContainer_frameLayout_mainActivity, newFragments.get(0));
                    if (newFragments.get(0) instanceof UserFragment) {
                        USER_FRAGMENT_IN_LIST = true;
                        userFragment = (UserFragment) newFragments.get(0);
                    }
            } else {
                // Remove the existing fragments from the layout
                for (Fragment existingFrag : getSupportFragmentManager().getFragments()) {
                    transaction.remove(existingFrag);
                }

                // Iterate over list of new Fragments, adding each to the UI.
                for (Fragment newFrag : newFragments) {
                    transaction.add(R.id.fragmentContainer_frameLayout_mainActivity, newFrag);
                    if (newFrag instanceof UserFragment) {
                        USER_FRAGMENT_IN_LIST = true;
                        userFragment = (UserFragment) newFrag;
                    }
                }
            }

            Log.v(TAG, "Checking if Toolbar should be visible.");
            // Show the toolbar if toolbarVisible is true and the toolbar is currently hidden.
            if (toolbarVisible && !getSupportActionBar().isShowing()) {
                Log.v(TAG, "Showing Toolbar.");
                getSupportActionBar().show();
                mToobarVisibilityBackStack.add(false);
            // Hide the toolbar if toolbarVisible is false and the toolbar is currently shown.
            } else if (!toolbarVisible && getSupportActionBar().isShowing()){
                Log.v(TAG, "Hiding Toolbar.");
                getSupportActionBar().hide();
                mToobarVisibilityBackStack.add(true);
            }

            // If addToBackStack is true, add the previous layout to back stack.
            if (addToBackStack) {
                transaction.addToBackStack(null);
            }

            /* Checks if list of new Fragments contains an instance of UserFragment.
             * If so, this code sets the interface of the UserFragment to be this activity.
             */
            if (USER_FRAGMENT_IN_LIST) {
                for (Fragment frag : newFragments) {
                    if (frag instanceof UserFragment) {
                        ((UserFragment) frag).setInterface(this);
                    }
                }
            }

            // Commit the transaction, updating the UI.
            transaction.commit();

        } else {
            throw new IllegalArgumentException("newFragments argument cannot be empty.");
        }
    }

    @Override
    public void addUserButtonClicked(View v) {
        ArrayList<Fragment> newFragments = new ArrayList<>();
        newFragments.add(mUserSearchFragment);
        showNewFragments(newFragments, false, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_user_profile:
//                showUserProfileFragment();
                ArrayList<Fragment> userProfileFragments = new ArrayList<Fragment>();
                userProfileFragments.add(mUserFragment);
                showNewFragments(userProfileFragments, false, true);
                return true;
            case R.id.action_filter:
                ArrayList<Fragment> filterFragments = new ArrayList<Fragment>();
                filterFragments.add(mFilterFragment);
                showNewFragments(filterFragments, false, true);
                return true;
            case R.id.action_settings:
                ArrayList<Fragment> settingsFragments = new ArrayList<Fragment>();
                settingsFragments.add(mSettingsFragment);
                showNewFragments(settingsFragments, false, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Override of built-in onBackPressed method.
     *
     * Method checks the toolbar visibility back stack and sets the toolbars visibility accordingly.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // Set the toolbar's visibility based on most recent value of mToolbarVisibilityBackStack
        Boolean toolbarVisibility = mToobarVisibilityBackStack.remove(mToobarVisibilityBackStack.size() - 1);
        if (toolbarVisibility) {
            getSupportActionBar().show();
        } else {
            getSupportActionBar().hide();
        }

    }
}