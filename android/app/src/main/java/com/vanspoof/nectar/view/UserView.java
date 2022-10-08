package com.vanspoof.nectar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vanspoof.nectar.R;
import com.vanspoof.nectar.model.User;

import org.json.JSONObject;

/**
 * Display basic User information, such as: username, email, and profile avatar.
 */
public class UserView extends LinearLayout {
    // UI Components
    private TextView mTextViewUsername;
    private TextView mTextViewEmail;
    private ImageView mImageViewAvatar;

    private User mUser;

    public UserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_user, this);

        mTextViewUsername = findViewById(R.id.view_user_textView_username);
        mTextViewEmail = findViewById(R.id.view_user_textView_email);
        mImageViewAvatar = findViewById(R.id.view_user_imageView_avatar);
    }

    public void setUser(User user) {
        mUser = user;

        mTextViewUsername.setText(mUser.getUsername());
        mTextViewEmail.setText(mUser.getEmail());
        mImageViewAvatar.setImageResource(R.drawable.ic_default_avatar);
    }

    public User getUser() {
        return mUser;
    }
}
