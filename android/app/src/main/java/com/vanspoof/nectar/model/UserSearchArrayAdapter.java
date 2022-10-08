package com.vanspoof.nectar.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vanspoof.nectar.R;

import java.util.ArrayList;

public class UserSearchArrayAdapter extends ArrayAdapter<User> {
    private ArrayList<User> values;

    public UserSearchArrayAdapter(Context context, ArrayList<User> values) {
        super(context, R.layout.arrayadapter_user, values);
        this.values = values;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.arrayadapter_user, parent, false);

        // Get UI components from layout
        TextView textViewUsername = rowView.findViewById(R.id.arrayadapter_user_textview_username);
        TextView textViewEmail = rowView.findViewById(R.id.arrayadapter_user_textview_email);

        textViewUsername.setText(values.get(position).getUsername());
        textViewEmail.setText(values.get(position).getEmail());

        return rowView;
    }
}
