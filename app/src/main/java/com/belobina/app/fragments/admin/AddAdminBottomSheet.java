package com.belobina.app.fragments.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.belobina.app.R;
import com.belobina.app.models.User;
import com.belobina.app.utils.Constants;
import com.belobina.app.utils.FirebaseHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddAdminBottomSheet extends BottomSheetDialogFragment {

    public interface OnAdminAddedListener {
        void onAdminAdded(String name);
    }

    private OnAdminAddedListener listener;

    private TextInputLayout tilName, tilEmail, tilPassword, tilPhone, tilAddress;
    private TextInputEditText etName, etEmail, etPassword, etPhone, etAddress;
    private MaterialButton btnAddAdmin;

    public static AddAdminBottomSheet newInstance(OnAdminAddedListener listener) {
        AddAdminBottomSheet sheet = new AddAdminBottomSheet();
        sheet.listener = listener;
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilName = view.findViewById(R.id.til_name);
        tilEmail = view.findViewById(R.id.til_email);
        tilPassword = view.findViewById(R.id.til_password);
        tilPhone = view.findViewById(R.id.til_phone);
        tilAddress = view.findViewById(R.id.til_address);
        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etPhone = view.findViewById(R.id.et_phone);
        etAddress = view.findViewById(R.id.et_address);
        btnAddAdmin = view.findViewById(R.id.btn_add_admin);

        btnAddAdmin.setOnClickListener(v -> createAdmin());
    }

    private void createAdmin() {
        clearErrors();
        String name = getText(etName);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String phone = getText(etPhone);
        String address = getText(etAddress);

        if (TextUtils.isEmpty(name)) { tilName.setError("Required"); return; }
        if (TextUtils.isEmpty(email)) { tilEmail.setError("Required"); return; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email"); return;
        }
        if (TextUtils.isEmpty(password)) { tilPassword.setError("Required"); return; }
        if (password.length() < 8) { tilPassword.setError("Min 8 characters"); return; }

        btnAddAdmin.setEnabled(false);

        FirebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    User user = new User();
                    user.setUid(uid);
                    user.setUserFullName(name);
                    user.setUserEmail(email);
                    user.setUserPhone(phone);
                    user.setUserAddress(address);
                    user.setUserRole(Constants.ROLE_ADMIN);

                    FirebaseHelper.getDb()
                            .collection(Constants.COLLECTION_USERS)
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener(v -> {
                                if (!isAdded()) return;
                                if (listener != null) listener.onAdminAdded(name);
                                dismiss();
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                btnAddAdmin.setEnabled(true);
                                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    btnAddAdmin.setEnabled(true);
                    if (getView() != null) {
                        Snackbar.make(getView(), e.getMessage() != null ? e.getMessage() : "Error",
                                Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void clearErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilPhone.setError(null);
        tilAddress.setError(null);
    }
}
