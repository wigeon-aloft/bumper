package com.vanspoof.nectar.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.vanspoof.nectar.R;
import com.vanspoof.nectar.model.MainActivityViewModel;

/**
     * A simple {@link Fragment} subclass.
     * Use the {@link LoginFragment#newInstance} factory method to
     * create an instance of this fragment.
     */
    public class LoginFragment extends Fragment {
        private MainActivityViewModel mViewModel;
        private Button mCreateUserButton;
        private Button mLoginUserButton;
        private EditText mUsernameEditText;
        private EditText mPasswordEditText;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_login, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            mViewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

            // Get UI components.
            mCreateUserButton = view.findViewById(R.id.fragment_login_button_createuser);
            mLoginUserButton = view.findViewById(R.id.fragment_login_button_login);
            mUsernameEditText = view.findViewById(R.id.fragment_login_edittext_username);
            mPasswordEditText = view.findViewById(R.id.fragment_login_edittext_password);

            mCreateUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get username and password that user entered.
                    String username = mUsernameEditText.getText().toString();
                    String password = mPasswordEditText.getText().toString();

                    // Check if user has entered username and password.
                    if (!username.isEmpty() | !password.isEmpty()) {
                        // Send create user request using view model
                        mViewModel.sendCreateUserRequest(username, password);
                    } else {
                        // Display toast indicating that user should enter a username and password
                        Toast.makeText(getContext(), R.string.username_password_warning, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            mLoginUserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String username = mUsernameEditText.getText().toString();
                    String password = mPasswordEditText.getText().toString();

                    // Check if user has entered username and password.
                    if (!username.isEmpty() | !password.isEmpty()) {
                        // Send create user request using view model
                        mViewModel.sendLoginRequest(username, password);
                    } else {
                        // Display toast indicating that user should enter a username and password
                        Toast.makeText(getContext(), R.string.username_password_warning, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
}
