package com.example.android.sunshine.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by debashispaul on 04/01/2016.
 * This class is for custom LocationEditTextPreference view
 */
public class LocationEditTextPreference extends EditTextPreference {
    private int mMinLength;
    private final static int DEFAULT_MIN_LOCATION_LENGTH = 2;
    private final String LOG_TAG = LocationEditTextPreference.class.getSimpleName();
    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,R.styleable.LocationEditTextPreference,0,0);
        try {
            mMinLength = typedArray.getInteger(R.styleable.LocationEditTextPreference_minLength,DEFAULT_MIN_LOCATION_LENGTH);
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        EditText editText = getEditText();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Log.v(LOG_TAG,"beforeTextChanged is called");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Log.v(LOG_TAG,"onTextChanged is called");
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Log.v(LOG_TAG,"afterTextChanged is called");
                Dialog dialog = getDialog();
                //To ensure code is safe, we first check if dialog is an instance of AlertDialog
                if(dialog instanceof AlertDialog) {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    //Check the length of the EditText
                    if(editable.length() < mMinLength) {
                        okButton.setEnabled(false);
                    } else {
                        okButton.setEnabled(true);
                    }
                }
            }
        });
    }
}
