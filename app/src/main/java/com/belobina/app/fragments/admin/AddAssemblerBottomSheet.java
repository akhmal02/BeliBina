package com.belobina.app.fragments.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.belobina.app.R;
import com.belobina.app.models.Assembler;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddAssemblerBottomSheet extends BottomSheetDialogFragment {

    public interface OnAssemblerSavedListener {
        void onSaved(Assembler assembler);
    }

    private static final String ARG_ASSEMBLER_ID = "assembler_id";
    private static final String ARG_NAME = "name";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_SERVICE_AREA = "service_area";

    private OnAssemblerSavedListener listener;
    private String assemblerIdToEdit;

    public static AddAssemblerBottomSheet newInstance(@Nullable Assembler existing,
                                                       OnAssemblerSavedListener listener) {
        AddAssemblerBottomSheet sheet = new AddAssemblerBottomSheet();
        sheet.listener = listener;
        if (existing != null) {
            Bundle args = new Bundle();
            args.putString(ARG_ASSEMBLER_ID, existing.getId());
            args.putString(ARG_NAME, existing.getAssFullName());
            args.putString(ARG_EMAIL, existing.getAssEmail());
            args.putString(ARG_PHONE, existing.getAssPhone());
            args.putString(ARG_SERVICE_AREA, existing.getAssServiceArea());
            sheet.setArguments(args);
        }
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_assembler, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputLayout tilName = view.findViewById(R.id.til_assembler_name);
        TextInputLayout tilEmail = view.findViewById(R.id.til_assembler_email);
        TextInputLayout tilPhone = view.findViewById(R.id.til_assembler_phone);
        TextInputLayout tilArea = view.findViewById(R.id.til_assembler_area);
        TextInputEditText etName = view.findViewById(R.id.et_assembler_name);
        TextInputEditText etEmail = view.findViewById(R.id.et_assembler_email);
        TextInputEditText etPhone = view.findViewById(R.id.et_assembler_phone);
        TextInputEditText etArea = view.findViewById(R.id.et_assembler_area);
        Button btnSave = view.findViewById(R.id.btn_save_assembler);

        Bundle args = getArguments();
        if (args != null) {
            assemblerIdToEdit = args.getString(ARG_ASSEMBLER_ID);
            etName.setText(args.getString(ARG_NAME));
            etEmail.setText(args.getString(ARG_EMAIL));
            etPhone.setText(args.getString(ARG_PHONE));
            etArea.setText(args.getString(ARG_SERVICE_AREA));
            btnSave.setText(getString(R.string.edit_assembler));
        }

        btnSave.setOnClickListener(v -> {
            String name = getText(etName);
            String email = getText(etEmail);
            String phone = getText(etPhone);
            String area = getText(etArea);

            tilName.setError(null);
            tilEmail.setError(null);
            tilPhone.setError(null);
            tilArea.setError(null);

            if (TextUtils.isEmpty(name)) { tilName.setError("Required"); return; }
            if (TextUtils.isEmpty(email)) { tilEmail.setError("Required"); return; }
            if (TextUtils.isEmpty(phone)) { tilPhone.setError("Required"); return; }
            if (TextUtils.isEmpty(area)) { tilArea.setError("Required"); return; }

            Assembler assembler = new Assembler(name, email, phone, area);
            if (assemblerIdToEdit != null) {
                assembler.setId(assemblerIdToEdit);
            }

            if (listener != null) listener.onSaved(assembler);
            dismiss();
        });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
