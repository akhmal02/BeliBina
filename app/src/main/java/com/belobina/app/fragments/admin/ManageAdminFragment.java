package com.belobina.app.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.belobina.app.R;
import com.belobina.app.adapters.AdminUserAdapter;
import com.belobina.app.models.User;
import com.belobina.app.utils.Constants;
import com.belobina.app.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageAdminFragment extends Fragment {

    private AdminUserAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private String currentUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_admins, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUid = FirebaseHelper.getCurrentUserId();

        RecyclerView rv = view.findViewById(R.id.rv_admins);
        MaterialButton fab = view.findViewById(R.id.fab_add_admin);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);

        adapter = new AdminUserAdapter(requireContext(), currentUid, new AdminUserAdapter.OnAdminActionListener() {
            @Override
            public void onView(User user) {
                showAdminDetailsDialog(user);
            }

            @Override
            public void onEdit(User user) {
                showEditAdminDialog(user);
            }

            @Override
            public void onRevoke(User user) {
                showRevokeDialog(user);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        fab.setOnClickListener(v ->
                AddAdminBottomSheet.newInstance(name -> {
                    Toast.makeText(requireContext(), name + " has been added as an admin.",
                            Toast.LENGTH_SHORT).show();
                    loadAdmins();
                }).show(getChildFragmentManager(), "add_admin"));

        loadAdmins();
    }

    private void showAdminDetailsDialog(User user) {
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_admin_details, null, false);

        TextView tvInitials = content.findViewById(R.id.tv_detail_initials);
        TextView tvHeaderName = content.findViewById(R.id.tv_detail_name_header);
        TextView tvHeaderEmail = content.findViewById(R.id.tv_detail_email_header);
        Chip chipRole = content.findViewById(R.id.chip_detail_role);
        TextView tvUid = content.findViewById(R.id.tv_detail_uid);
        TextView tvName = content.findViewById(R.id.tv_detail_name);
        TextView tvEmail = content.findViewById(R.id.tv_detail_email);
        TextView tvPhone = content.findViewById(R.id.tv_detail_phone);
        TextView tvAddress = content.findViewById(R.id.tv_detail_address);
        TextView tvRole = content.findViewById(R.id.tv_detail_role);
        MaterialButton btnClose = content.findViewById(R.id.btn_detail_close);
        MaterialButton btnEdit = content.findViewById(R.id.btn_detail_edit);

        String name = nonEmpty(user.getUserFullName(), "Unnamed Admin");
        String email = nonEmpty(user.getUserEmail(), "No email");
        String role = nonEmpty(user.getUserRole(), Constants.ROLE_ADMIN);

        tvInitials.setText(getInitials(name));
        tvHeaderName.setText(name);
        tvHeaderEmail.setText(email);
        chipRole.setText(role);
        tvUid.setText(nonEmpty(user.getUid(), "Not available"));
        tvName.setText(name);
        tvEmail.setText(email);
        tvPhone.setText(nonEmpty(user.getUserPhone(), "Not set"));
        tvAddress.setText(nonEmpty(user.getUserAddress(), "Not set"));
        tvRole.setText(role);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(content)
                .create();
        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            showEditAdminDialog(user);
        });
        dialog.setOnShowListener(d -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
        });
        dialog.show();
    }

    private void loadAdmins() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_USERS)
                .whereEqualTo(Constants.FIELD_ROLE, Constants.ROLE_ADMIN)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    List<User> admins = new ArrayList<>();
                    if (snapshots != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            User u = doc.toObject(User.class);
                            if (u != null) {
                                u.setUid(doc.getId());
                                admins.add(u);
                            }
                        }
                    }
                    adapter.setData(admins);
                    tvEmpty.setVisibility(admins.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showEditAdminDialog(User user) {
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_admin, null, false);
        TextInputLayout tilName = content.findViewById(R.id.til_admin_name);
        TextInputLayout tilPhone = content.findViewById(R.id.til_admin_phone);
        TextInputLayout tilAddress = content.findViewById(R.id.til_admin_address);
        TextInputEditText etName = content.findViewById(R.id.et_admin_name);
        TextInputEditText etPhone = content.findViewById(R.id.et_admin_phone);
        TextInputEditText etAddress = content.findViewById(R.id.et_admin_address);
        TextView tvUid = content.findViewById(R.id.tv_admin_uid_readonly);
        TextView tvEmail = content.findViewById(R.id.tv_admin_email_readonly);

        etName.setText(user.getUserFullName() != null ? user.getUserFullName() : "");
        etPhone.setText(user.getUserPhone() != null ? user.getUserPhone() : "");
        etAddress.setText(user.getUserAddress() != null ? user.getUserAddress() : "");
        tvUid.setText(user.getUid() != null ? user.getUid() : "No ID");
        tvEmail.setText(user.getUserEmail() != null ? user.getUserEmail() : "No email");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit Admin")
                .setView(content)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    tilName.setError(null);
                    tilPhone.setError(null);
                    tilAddress.setError(null);

                    String name = getInput(etName);
                    String phone = getInput(etPhone);
                    String address = getInput(etAddress);
                    if (name.isEmpty()) {
                        tilName.setError("Required");
                        return;
                    }

                    saveAdminDetails(user, name, phone, address, dialog);
                }));

        dialog.show();
    }

    private void saveAdminDetails(User user, String name, String phone, String address,
                                  AlertDialog dialog) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("userFullName", name);
        updates.put("userPhone", phone);
        updates.put("userAddress", address);
        updates.put(Constants.FIELD_ROLE, Constants.ROLE_ADMIN);

        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_USERS)
                .document(user.getUid())
                .update(updates)
                .addOnSuccessListener(v -> {
                    if (!isAdded()) return;
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Admin details updated.",
                            Toast.LENGTH_SHORT).show();
                    loadAdmins();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String getInput(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private String nonEmpty(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value : fallback;
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

    private void showRevokeDialog(User user) {
        String name = user.getUserFullName() != null ? user.getUserFullName() : "this admin";
        new AlertDialog.Builder(requireContext())
                .setTitle("Revoke Admin Access")
                .setMessage("Revoke admin access for " + name + "?")
                .setPositiveButton("Revoke", (d, w) -> revokeAdmin(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void revokeAdmin(User user) {
        String name = user.getUserFullName() != null ? user.getUserFullName() : "Admin";
        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_USERS)
                .document(user.getUid())
                .update(Constants.FIELD_ROLE, Constants.ROLE_CUSTOMER)
                .addOnSuccessListener(v -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Admin access revoked for " + name + ".",
                            Toast.LENGTH_SHORT).show();
                    loadAdmins();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
