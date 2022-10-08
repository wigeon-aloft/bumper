package com.vanspoof.nectar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.vanspoof.nectar.R;
import com.vanspoof.nectar.model.MainActivityViewModel;
import com.vanspoof.nectar.model.Relationship;

/**
 * Displays another user's details along with any relationship that they have with the currently
 * logged in user.
 *
 * Extends the base UserView class to add buttons to request/confirm/reject/recant a relationship
 * request and a TextView for viewing the current status of a relationship request.
 */
public class UserRelationshipView extends UserView {
    private Relationship mRelationship;
    private TextView mTextViewStatus;
    private Button mButtonAction;
    private MainActivityViewModel mViewModel;

    public UserRelationshipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_userrelationship, this);

        mTextViewStatus = findViewById(R.id.view_userrelationship_textView_status);
        mButtonAction = findViewById(R.id.view_userrelationship_button_action);

//        mViewModel =  mViewModel = new ViewModelProvider().get(MainActivityViewModel.class);
    }

    public void setRelationship(Relationship relationship) {
        mRelationship = relationship;
        updateUi(mRelationship.getStatus());
    }

    public Relationship getRelationship() {
        return mRelationship;
    }

    public void updateUi(String status) {
        mTextViewStatus.setText(status);
        if (status.equals("requested")) {
            mButtonAction.setText("Accept");
        }
    }
}
