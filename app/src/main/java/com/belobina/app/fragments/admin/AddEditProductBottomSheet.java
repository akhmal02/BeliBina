package com.belobina.app.fragments.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.belobina.app.R;
import com.belobina.app.models.Furniture;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddEditProductBottomSheet extends BottomSheetDialogFragment {

    public interface OnProductSavedListener {
        void onProductSaved(Furniture furniture);
    }

    private static final String[] CATEGORIES = {"Storage", "Dining", "Living", "Bedroom", "Office", "Other"};

    private Furniture existingProduct;
    private OnProductSavedListener listener;

    private TextInputLayout tilName, tilDescription, tilPrice, tilCategory, tilStock, tilAssemblyFee, tilImageUrl;
    private TextInputEditText etName, etDescription, etPrice, etStock, etAssemblyFee, etImageUrl;
    private AutoCompleteTextView etCategory;

    public static AddEditProductBottomSheet newInstance(@Nullable Furniture furniture,
                                                        OnProductSavedListener listener) {
        AddEditProductBottomSheet sheet = new AddEditProductBottomSheet();
        sheet.existingProduct = furniture;
        sheet.listener = listener;
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        android.widget.TextView tvTitle = view.findViewById(R.id.tv_sheet_title);
        tilName = view.findViewById(R.id.til_name);
        tilDescription = view.findViewById(R.id.til_description);
        tilPrice = view.findViewById(R.id.til_price);
        tilCategory = view.findViewById(R.id.til_category);
        tilStock = view.findViewById(R.id.til_stock);
        tilAssemblyFee = view.findViewById(R.id.til_assembly_fee);
        tilImageUrl = view.findViewById(R.id.til_image_url);
        etName = view.findViewById(R.id.et_name);
        etDescription = view.findViewById(R.id.et_description);
        etPrice = view.findViewById(R.id.et_price);
        etCategory = view.findViewById(R.id.et_category);
        etStock = view.findViewById(R.id.et_stock);
        etAssemblyFee = view.findViewById(R.id.et_assembly_fee);
        etImageUrl = view.findViewById(R.id.et_image_url);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        etCategory.setAdapter(catAdapter);

        if (existingProduct != null) {
            tvTitle.setText("Edit Product");
            etName.setText(existingProduct.getProdName());
            etDescription.setText(existingProduct.getProdDesc());
            etPrice.setText(String.valueOf(existingProduct.getProdPrice()));
            etCategory.setText(existingProduct.getProdCat(), false);
            etStock.setText(String.valueOf(existingProduct.getProdStock()));
            if (existingProduct.getProdAssembleFee() > 0) {
                etAssemblyFee.setText(String.valueOf(existingProduct.getProdAssembleFee()));
            }
            etImageUrl.setText(existingProduct.getProdPicture());
        }

        view.findViewById(R.id.btn_save_product).setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        clearErrors();
        String name = getText(etName);
        String description = getText(etDescription);
        String priceStr = getText(etPrice);
        String category = etCategory.getText() != null ? etCategory.getText().toString().trim() : "";
        String stockStr = getText(etStock);
        String assemblyFeeStr = getText(etAssemblyFee);
        String imageUrl = getText(etImageUrl);

        if (TextUtils.isEmpty(name)) { tilName.setError("Required"); return; }
        if (TextUtils.isEmpty(description)) { tilDescription.setError("Required"); return; }
        if (TextUtils.isEmpty(priceStr)) { tilPrice.setError("Required"); return; }
        if (TextUtils.isEmpty(category)) { tilCategory.setError("Required"); return; }
        if (TextUtils.isEmpty(stockStr)) { tilStock.setError("Required"); return; }

        double price;
        int stock;
        double assemblyFee = 0;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) { tilPrice.setError("Price must be > 0"); return; }
        } catch (NumberFormatException e) {
            tilPrice.setError("Invalid price");
            return;
        }
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) { tilStock.setError("Stock must be >= 0"); return; }
        } catch (NumberFormatException e) {
            tilStock.setError("Invalid stock");
            return;
        }
        if (!TextUtils.isEmpty(assemblyFeeStr)) {
            try {
                assemblyFee = Double.parseDouble(assemblyFeeStr);
                if (assemblyFee < 0) { tilAssemblyFee.setError("Must be >= 0"); return; }
            } catch (NumberFormatException e) {
                tilAssemblyFee.setError("Invalid amount");
                return;
            }
        }

        Furniture furniture = existingProduct != null ? existingProduct : new Furniture();
        furniture.setProdName(name);
        furniture.setProdDesc(description);
        furniture.setProdPrice(price);
        furniture.setProdCat(category);
        furniture.setProdStock(stock);
        furniture.setProdAssembleFee(assemblyFee);
        furniture.setProdPicture(imageUrl.isEmpty() ? null : imageUrl);

        listener.onProductSaved(furniture);
        dismiss();
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void clearErrors() {
        tilName.setError(null);
        tilDescription.setError(null);
        tilPrice.setError(null);
        tilCategory.setError(null);
        tilStock.setError(null);
        tilAssemblyFee.setError(null);
    }
}
