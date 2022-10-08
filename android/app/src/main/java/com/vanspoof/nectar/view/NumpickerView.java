package com.vanspoof.nectar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vanspoof.nectar.R;

/**
 * Custom view for displaying an inline numberpicker.
 */
public class NumpickerView extends LinearLayout {

    public static final String TAG = "NumpickerView";

    private int mValue;
    private int mDefaultValue = 0;
    private int mMaxValue = 0;
    private int mMinValue = 0;
    private String mLabel = null;

    private TextView mTextviewValue;
    private Button mButtonMinus;
    private Button mButtonPlus;
    private TextView mTextviewLabel;

    public NumpickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_numpicker, this);

        // Get the parameters passed from the XML layout file.
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.NumpickerView, 0, 0);
        mDefaultValue = a.getInt(R.styleable.NumpickerView_defaultValue, mDefaultValue);
        mLabel = a.getString(R.styleable.NumpickerView_label);
        mMaxValue = a.getInt(R.styleable.NumpickerView_maxValue, 100);
        mMinValue = a.getInt(R.styleable.NumpickerView_minValue, 0);
        a.recycle();

        Log.v(TAG, String.format("The max and min values have been set to %d and %d", mMaxValue, mMinValue));

        // Get view components from file.
        mTextviewValue = findViewById(R.id.view_numpicker_textview_value);
        mButtonMinus = findViewById(R.id.view_numpicker_button_minus);
        mButtonPlus = findViewById(R.id.view_numpicker_button_plus);
        mTextviewLabel = findViewById(R.id.view_numpicker_textview_label);

        // Set up the button onClickListeners
        mButtonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setValue(mValue - 1);
            }
        });

        mButtonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setValue(mValue + 1);
            }
        });

        LinearLayout mainLayout = findViewById(R.id.view_numpicker_linearLayout_main);
        if (mLabel == null || mLabel.equals("")) {
            mainLayout.removeView(mTextviewLabel);
        } else {
            mTextviewLabel.setText(mLabel);
        }

        // todo when button is held it should accelerate the value's rate of increase/decrease

        setValue(mDefaultValue);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        // Enable/disable the UI components.
        mButtonMinus.setEnabled(enabled);
        mButtonPlus.setEnabled(enabled);
        mTextviewValue.setEnabled(enabled);
    }

    /**
     * Updates the currently stored mValue and updates the TextView with the new value.
     *
     * @param value
     *  int for the new value.
     */
    public void setValue(int value) {

        int newValue = 0;

        // Limit the value based on the max and min defaults.
        if (value < mMinValue) {
            newValue = mMinValue;
        } else if (value > mMaxValue) {
            newValue = mMaxValue;
        } else {
            newValue = value;
        }

        // Store the new value and update the TextView
        mValue = newValue;
        mTextviewValue.setText(Integer.toString(newValue));
    }

    public int getValue() {
        return mValue;
    }

    public void setMaxValue(int maxValue) {
        mMaxValue = maxValue;
    }

    public void setMinValue(int minValue) {
        mMinValue = minValue;
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public int getMinValue() {
        return mMinValue;
    }
}
