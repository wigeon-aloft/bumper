package com.vanspoof.nectar.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vanspoof.nectar.R;

public class GenreView extends LinearLayout {
    private LinearLayout mLinearLayoutMain;
    private TextView mTextViewGenre;
    private String mGenre;
    private boolean mSelected;
    private Drawable mDefaultBackground;

    public GenreView(Context context) {
        super(context);
        View.inflate(context, R.layout.view_genre, this);

        mLinearLayoutMain = findViewById(R.id.view_genre_linearLayout_main);
        mTextViewGenre = findViewById(R.id.view_genre_textView_genre);
        mDefaultBackground = mLinearLayoutMain.getBackground();

        mTextViewGenre.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the view's mSelected field and update the background accordingly.
                setSelected(!mSelected);
            }
        });
    }

    public GenreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_genre, this);

        mLinearLayoutMain = findViewById(R.id.view_genre_linearLayout_main);
        mTextViewGenre = findViewById(R.id.view_genre_textView_genre);
        mDefaultBackground = mLinearLayoutMain.getBackground();

        mTextViewGenre.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the view's mSelected field and update the background accordingly.
                setSelected(!mSelected);
            }
        });
    }

    public String getGenre() {
        return mGenre;
    }

    public void setGenre(String genre) {
        this.mGenre = genre;
        this.mTextViewGenre.setText(genre);
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean mSelected) {
        this.mSelected = mSelected;
        updateBackground();
    }

    private void updateBackground() {
        if (mSelected) {
            mLinearLayoutMain.setBackground(new ColorDrawable(Color.GREEN));
        } else {
            mLinearLayoutMain.setBackground(mDefaultBackground);
        }
    }

    public void setUiEnabled(boolean enabled) {
        setEnabled(enabled);
        mTextViewGenre.setEnabled(enabled);
    }
}
