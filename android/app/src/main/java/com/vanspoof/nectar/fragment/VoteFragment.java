package com.vanspoof.nectar.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.vanspoof.nectar.R;
import com.vanspoof.nectar.model.MainActivityViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VoteFragment extends Fragment {
    private static final String TAG = "VoteFragment";
    private MainActivityViewModel mViewModel;
    private Button mPositiveVoteButton;
    private Button mNegativeVoteButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vote, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

        mPositiveVoteButton = view.findViewById(R.id.fragment_vote_button_positive);
        mNegativeVoteButton = view.findViewById(R.id.fragment_vote_button_negative);

        mPositiveVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.sendVote("1");
            }
        });

        mNegativeVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.sendVote("0");
            }
        });
    }
}