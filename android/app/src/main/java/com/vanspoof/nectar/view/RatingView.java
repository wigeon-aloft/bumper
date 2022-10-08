package com.vanspoof.nectar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vanspoof.nectar.R;
import com.vanspoof.nectar.model.Rating;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Custom view for displaying rating information and controls for changing the rating value. It will
 * display the provider of the rating (e.g. IMDb, Rotten Tomatoes, etc.) and value to filter by. The
 * user can also change whether the filter is 'less than' or 'greater than'.
 *
 * This view will be displayed in the FilterFragment.
 */
public class RatingView extends LinearLayout {

    // todo move this OPERATORS list to the 'Rating' class
    private static final ArrayList<String> OPERATORS = new ArrayList<>(Arrays.asList(">", "<", "="));
    // todo why isn't the below info just stored in a 'Rating' object
    protected String mCurrentOperator;
    protected boolean mUiEnabled;
    protected String mRatingProviderName;

    // UI components
    protected CheckBox mEnabledCheckBox;
    protected TextView mRatingProviderNameTextView;
    protected Button mEqualityOperatorButton;
    protected NumpickerView mNumpickerViewValue;

    public RatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_rating, this);

        // Get XML attribute values.
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RatingView, 0, 0);

        String ratingProviderName = a.getString(R.styleable.RatingView_providerName);
        String equalityOperator = a.getString(R.styleable.RatingView_equalityOperator);
        a.recycle();

        // Get layout components
        mEnabledCheckBox = findViewById(R.id.view_rating_checkbox_enabled);
        mRatingProviderNameTextView = findViewById(R.id.view_rating_textview_ratingProviderName);
        mEqualityOperatorButton = findViewById(R.id.view_rating_button_equalityOperator);
        mNumpickerViewValue = findViewById(R.id.view_rating_numpickerview_value);

        mEqualityOperatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEqualityOperator();
            }
        });

        mEnabledCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckboxClicked(v);
            }
        });

        mRatingProviderName = ratingProviderName;

        // Initialise the UI components with their default values.
        mRatingProviderNameTextView.setText(ratingProviderName);
        if (equalityOperator != null) {
            setEqualityOperator(equalityOperator);
        } else {
            setEqualityOperator("=");
        }
//        setUiEnabledStatus(mUiEnabled);
//        mEnabledCheckBox.setChecked(mUiEnabled);
    }

    /**
     * Cycles through the available operators when the mEqualityOperatorButton is pressed.
     */
    public void toggleEqualityOperator() {
        // Find the position of the currentOperator in the OPERATORS list and get the next one.
        setEqualityOperator(OPERATORS.get((OPERATORS.indexOf(mCurrentOperator) + 1) % OPERATORS.size()));
    }

    public void setEqualityOperator(String operator) {
        if (OPERATORS.contains(operator)) {
            mCurrentOperator = operator;
            mEqualityOperatorButton.setText(mCurrentOperator);
        }
    }

    /**
     * Disables/enables UI components to reflect the passed 'enabled' value.
     *
     * @param enabled
     *  Boolean indicating the intended status of the UI.
     */
    public void setRatingInputUIEnabled(boolean enabled) {
        mUiEnabled = enabled;
//        mEnabledCheckBox.setChecked(mUiEnabled);
//        mEnabledCheckBox.setEnabled(enabled);
//        mRatingProviderNameTextView.setEnabled(mUiEnabled);
        mEqualityOperatorButton.setEnabled(mUiEnabled);
        mNumpickerViewValue.setEnabled(mUiEnabled);
    }

    public void setUiEnabled(boolean enabled) {
        mEnabledCheckBox.setEnabled(enabled);
        mRatingProviderNameTextView.setEnabled(enabled);
        if (mEnabledCheckBox.isChecked()) {
            setRatingInputUIEnabled(enabled);
        }
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        if (R.id.view_rating_checkbox_enabled == view.getId()) {
                setRatingInputUIEnabled(checked);
        }
    }

    public Rating getRating() {
        return new Rating(
                mRatingProviderName,
                mNumpickerViewValue.getValue(),
                mCurrentOperator,
                mEnabledCheckBox.isChecked()
        );
    }

    public void setScore(int score) {
        mNumpickerViewValue.setValue(score);
    }

    public void setRatingProviderName(String source) {
        mRatingProviderName = source;
        mRatingProviderNameTextView.setText(source);
    }

    public boolean isUiEnabled() {
        return mUiEnabled;
    }
}
