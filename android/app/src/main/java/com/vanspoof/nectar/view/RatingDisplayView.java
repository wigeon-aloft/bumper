package com.vanspoof.nectar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vanspoof.nectar.R;

public class RatingDisplayView extends LinearLayout {

    protected TextView mSourceTextView;
    protected TextView mScoreTextView;
    protected int mScore;
    protected String mSource;

    public RatingDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_ratingdisplay, this);

        mSourceTextView = findViewById(R.id.view_ratingDisplay_textView_source);
        mScoreTextView = findViewById(R.id.view_ratingDisplay_textView_score);
    }

    public void setSource(String source) {
        mSource = source;
        mSourceTextView.setText(source);
    }

    public void setScore(int score) {
        mScore = score;
        mScoreTextView.setText(String.format("%s", score));
    }
}
