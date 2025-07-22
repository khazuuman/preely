package com.example.preely.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.preely.R;
import com.example.preely.util.ViewUtil;
import com.example.preely.viewmodel.ForgotPasswordService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ForgotPassStFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ForgotPassStFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ForgotPassStFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ForgotPassStFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ForgotPassStFragment newInstance(String param1, String param2) {
        ForgotPassStFragment fragment = new ForgotPassStFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_pass_st, container, false);
    }

    private ForgotPasswordService forgotPasswordService;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        forgotPasswordService = new ViewModelProvider(requireActivity()).get(ForgotPasswordService.class);

        TextInputEditText usernameInput = view.findViewById(R.id.username_input);
        TextInputEditText phoneInput = view.findViewById(R.id.phone_input);
        Button continueBtn = view.findViewById(R.id.continue_btn);
        TextView usernameErrorTv = view.findViewById(R.id.username_error_tv);
        TextView phoneErrorTv = view.findViewById(R.id.phone_error_tv);

        forgotPasswordService.getUsernameError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                usernameErrorTv.setText(error);
                usernameErrorTv.setVisibility(View.VISIBLE);
            } else {
                usernameErrorTv.setVisibility(View.GONE);
            }
        });

        forgotPasswordService.getPhoneError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                phoneErrorTv.setText(error);
                phoneErrorTv.setVisibility(View.VISIBLE);
            } else {
                phoneErrorTv.setVisibility(View.GONE);
            }
        });

        ViewUtil.clearErrorOnTextChanged(usernameInput, usernameErrorTv);
        ViewUtil.clearErrorOnTextChanged(phoneInput, phoneErrorTv);

        forgotPasswordService.isUsernameValid().observe(getViewLifecycleOwner(), isValid -> {
            if (Boolean.TRUE.equals(isValid)) {
                forgotPasswordService.setUsername(usernameInput.getText().toString().trim());
                forgotPasswordService.setPhone(phoneInput.getText().toString().trim());

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_forgot_pass_st, new ForgotPassNdFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        continueBtn.setOnClickListener(v -> {

            String username = usernameInput.getText().toString();
            String phone = phoneInput.getText().toString();

            forgotPasswordService.checkUsername(username, phone);
        });
    }
}