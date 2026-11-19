package com.belobina.app.fragments.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.belobina.app.R;
import com.belobina.app.activities.OnboardingActivity;
import com.belobina.app.models.User;
import com.belobina.app.repositories.AuthRepository;
import com.belobina.app.utils.Callback;
import com.belobina.app.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

public class ProfileFragment extends Fragment {

    private TextInputEditText etName, etPhone, etAddress;
    private TextInputLayout tilName, tilPhone, tilAddress;
    private TextView tvEmail;
    private MaterialButton btnSave, btnLogout, btnUpdatePassword;
    private ProgressBar progressBar;
    private final AuthRepository authRepo = new AuthRepository();
    private User currentUser;
    private boolean editMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEmail = view.findViewById(R.id.tv_email);
        etName = view.findViewById(R.id.et_name);
        etPhone = view.findViewById(R.id.et_phone);
        etAddress = view.findViewById(R.id.et_address);
        tilName = view.findViewById(R.id.til_name);
        tilPhone = view.findViewById(R.id.til_phone);
        tilAddress = view.findViewById(R.id.til_address);
        btnSave = view.findViewById(R.id.btn_save);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnUpdatePassword = view.findViewById(R.id.btn_update_password);
        progressBar = view.findViewById(R.id.progress_bar);

        setEditMode(false);
        loadProfile();

        btnSave.setOnClickListener(v -> {
            if (!editMode) {
                setEditMode(true);
            } else {
                saveProfile();
            }
        });

        btnUpdatePassword.setOnClickListener(v -> showUpdatePasswordDialog());

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        authRepo.signOut();
                        Toast.makeText(requireContext(), "You have been logged out.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(requireContext(), OnboardingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void showUpdatePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_update_password, null);

        TextInputLayout tilCurrent = dialogView.findViewById(R.id.til_current_password);
        TextInputLayout tilNew = dialogView.findViewById(R.id.til_new_password);
        TextInputLayout tilConfirm = dialogView.findViewById(R.id.til_confirm_new_password);
        TextInputEditText etCurrent = dialogView.findViewById(R.id.et_current_password);
        TextInputEditText etNew = dialogView.findViewById(R.id.et_new_password);
        TextInputEditText etConfirm = dialogView.findViewById(R.id.et_confirm_new_password);

        new AlertDialog.Builder(requireContext())
                .setTitle("Update Password")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String current = etCurrent.getText() != null ? etCurrent.getText().toString() : "";
                    String newPass = etNew.getText() != null ? etNew.getText().toString() : "";
                    String confirm = etConfirm.getText() != null ? etConfirm.getText().toString() : "";

                    tilCurrent.setError(null);
                    tilNew.setError(null);
                    tilConfirm.setError(null);

                    if (TextUtils.isEmpty(current)) {
                        tilCurrent.setError("Required");
                        return;
                    }
                    if (newPass.length() < 6) {
                        tilNew.setError("Minimum 6 characters");
                        return;
                    }
                    if (!newPass.equals(confirm)) {
                        tilConfirm.setError("Passwords do not match");
                        return;
                    }
                    progressBar.setVisibility(View.VISIBLE);
                    authRepo.updatePassword(current, newPass, new Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            if (!isAdded()) return;
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), "Password updated successfully.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(String errorMessage) {
                            if (!isAdded()) return;
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadProfile() {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) return;
        progressBar.setVisibility(View.VISIBLE);
        authRepo.getUserData(uid, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                currentUser = user;
                if (user != null) {
                    tvEmail.setText(user.getUserEmail());
                    etName.setText(user.getUserFullName());
                    etPhone.setText(user.getUserPhone());
                    etAddress.setText(user.getUserAddress());
                }
            }
            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void saveProfile() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) { tilName.setError("Required"); return; }
        if (TextUtils.isEmpty(phone)) { tilPhone.setError("Required"); return; }
        if (TextUtils.isEmpty(address)) { tilAddress.setError("Required"); return; }

        if (currentUser == null) return;
        currentUser.setUserFullName(name);
        currentUser.setUserPhone(phone);
        currentUser.setUserAddress(address);

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        authRepo.updateUserData(currentUser.getUid(), currentUser, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                setEditMode(false);
                Toast.makeText(requireContext(), "Profile updated successfully.", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(requireContext(), "Failed to update profile. Try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setEditMode(boolean enabled) {
        editMode = enabled;
        etName.setEnabled(enabled);
        etPhone.setEnabled(enabled);
        etAddress.setEnabled(enabled);
        tilName.setError(null);
        tilPhone.setError(null);
        tilAddress.setError(null);
        btnSave.setText(enabled ? getString(R.string.btn_save) : getString(R.string.btn_edit));
    }
}
