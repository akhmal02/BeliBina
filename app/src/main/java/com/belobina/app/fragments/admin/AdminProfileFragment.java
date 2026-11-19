package com.belobina.app.fragments.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.belobina.app.R;
import com.belobina.app.activities.OnboardingActivity;
import com.belobina.app.models.User;
import com.belobina.app.repositories.AuthRepository;
import com.belobina.app.utils.Callback;
import com.belobina.app.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AdminProfileFragment extends Fragment {

    private TextView tvAvatar, tvNameDisplay, tvNameValue, tvEmailValue, tvPhoneValue, tvAddressValue;
    private TextInputLayout tilName, tilPhone, tilAddress;
    private TextInputEditText etName, etPhone, etAddress;
    private MaterialButton btnEditSave, btnCancel, btnManageAdmins, btnLogout, btnUpdatePassword;
    private ProgressBar progressBar;

    private final AuthRepository authRepo = new AuthRepository();
    private User currentUser;
    private boolean editMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvAvatar = view.findViewById(R.id.tv_avatar);
        tvNameDisplay = view.findViewById(R.id.tv_admin_name_display);
        tvNameValue = view.findViewById(R.id.tv_name_value);
        tvEmailValue = view.findViewById(R.id.tv_email_value);
        tvPhoneValue = view.findViewById(R.id.tv_phone_value);
        tvAddressValue = view.findViewById(R.id.tv_address_value);
        tilName = view.findViewById(R.id.til_name);
        tilPhone = view.findViewById(R.id.til_phone);
        tilAddress = view.findViewById(R.id.til_address);
        etName = view.findViewById(R.id.et_name);
        etPhone = view.findViewById(R.id.et_phone);
        etAddress = view.findViewById(R.id.et_address);
        btnEditSave = view.findViewById(R.id.btn_edit_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnUpdatePassword = view.findViewById(R.id.btn_update_password);
        btnManageAdmins = view.findViewById(R.id.btn_manage_admins);
        btnLogout = view.findViewById(R.id.btn_logout);
        progressBar = view.findViewById(R.id.progress_bar);

        setEditMode(false);
        loadProfile();

        btnEditSave.setOnClickListener(v -> {
            if (!editMode) {
                setEditMode(true);
            } else {
                saveProfile();
            }
        });

        btnCancel.setOnClickListener(v -> {
            setEditMode(false);
            if (currentUser != null) populateFields(currentUser);
        });

        btnUpdatePassword.setOnClickListener(v -> showUpdatePasswordDialog());

        btnManageAdmins.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_adminProfileFragment_to_manageAdminFragment));

        btnLogout.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Log out")
                        .setMessage("Log out of BeliBina?")
                        .setPositiveButton("Log Out", (d, w) -> {
                            authRepo.signOut();
                            Toast.makeText(requireContext(), "You have been logged out.",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(requireContext(), OnboardingActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
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
                if (user != null) populateFields(user);
            }
            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populateFields(User user) {
        String name = user.getUserFullName() != null ? user.getUserFullName() : "";
        tvNameDisplay.setText(name);
        tvNameValue.setText(name);
        tvEmailValue.setText(user.getUserEmail() != null ? user.getUserEmail() : "");
        tvPhoneValue.setText(user.getUserPhone() != null ? user.getUserPhone() : "");
        tvAddressValue.setText(user.getUserAddress() != null && !user.getUserAddress().trim().isEmpty()
                ? user.getUserAddress()
                : "Not set");
        tvAvatar.setText(getInitials(name));
        etName.setText(name);
        etPhone.setText(user.getUserPhone() != null ? user.getUserPhone() : "");
        etAddress.setText(user.getUserAddress() != null ? user.getUserAddress() : "");
    }

    private void saveProfile() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Required");
            return;
        }
        tilName.setError(null);

        if (currentUser == null) return;
        currentUser.setUserFullName(name);
        currentUser.setUserPhone(phone);
        currentUser.setUserAddress(address);

        progressBar.setVisibility(View.VISIBLE);
        btnEditSave.setEnabled(false);
        authRepo.updateUserData(currentUser.getUid(), currentUser, new Callback<Void>() {
            @Override
            public void onSuccess(Void r) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                btnEditSave.setEnabled(true);
                setEditMode(false);
                populateFields(currentUser);
                Toast.makeText(requireContext(), "Profile updated successfully.", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                btnEditSave.setEnabled(true);
                Toast.makeText(requireContext(), "Failed to update profile. Try again.", Toast.LENGTH_LONG).show();
            }
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

    private void setEditMode(boolean enabled) {
        editMode = enabled;
        tilName.setVisibility(enabled ? View.VISIBLE : View.GONE);
        tilPhone.setVisibility(enabled ? View.VISIBLE : View.GONE);
        tilAddress.setVisibility(enabled ? View.VISIBLE : View.GONE);
        tvNameValue.setVisibility(enabled ? View.GONE : View.VISIBLE);
        tvPhoneValue.setVisibility(enabled ? View.GONE : View.VISIBLE);
        tvAddressValue.setVisibility(enabled ? View.GONE : View.VISIBLE);
        btnEditSave.setText(enabled ? "Save Changes" : "Edit Profile");
        btnCancel.setVisibility(enabled ? View.VISIBLE : View.GONE);
        btnUpdatePassword.setVisibility(enabled ? View.GONE : View.VISIBLE);
        btnManageAdmins.setVisibility(enabled ? View.GONE : View.VISIBLE);
        btnLogout.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return String.valueOf(parts[0].charAt(0)).toUpperCase()
                    + String.valueOf(parts[1].charAt(0)).toUpperCase();
        }
        return String.valueOf(parts[0].charAt(0)).toUpperCase();
    }
}
