package com.belobina.app.fragments.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.belobina.app.R;
import com.belobina.app.adapters.ProductAdminAdapter;
import com.belobina.app.models.Furniture;
import com.belobina.app.repositories.FurnitureRepository;
import com.belobina.app.utils.Callback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class ManageProductFragment extends Fragment {

    private ProductAdminAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FurnitureRepository furnitureRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        furnitureRepo = new FurnitureRepository(requireContext());
        RecyclerView rv = view.findViewById(R.id.rv_products);
        MaterialButton fab = view.findViewById(R.id.fab_add_product);
        TextInputEditText etSearch = view.findViewById(R.id.et_search);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);

        adapter = new ProductAdminAdapter(requireContext(), new ProductAdminAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Furniture furniture) {
                AddEditProductBottomSheet.newInstance(furniture, ManageProductFragment.this::updateProduct)
                        .show(getChildFragmentManager(), "edit_product");
            }
            @Override
            public void onDelete(Furniture furniture) {
                confirmDelete(furniture);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        fab.setOnClickListener(v ->
                AddEditProductBottomSheet.newInstance(null, this::addProduct)
                        .show(getChildFragmentManager(), "add_product"));

        loadProducts();
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        furnitureRepo.getAllFurniture(new Callback<List<Furniture>>() {
            @Override
            public void onSuccess(List<Furniture> items) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                adapter.setData(items);
                tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addProduct(Furniture furniture) {
        furnitureRepo.addFurniture(furniture, new Callback<Void>() {
            @Override
            public void onSuccess(Void r) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Product added successfully.", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateProduct(Furniture furniture) {
        furnitureRepo.updateFurniture(furniture, new Callback<Void>() {
            @Override
            public void onSuccess(Void r) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Product updated.", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmDelete(Furniture furniture) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Product")
                .setMessage("Delete " + furniture.getProdName() + "? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) ->
                        furnitureRepo.deleteFurniture(furniture.getId(), new Callback<Void>() {
                            @Override
                            public void onSuccess(Void r) {
                                if (!isAdded()) return;
                                Toast.makeText(requireContext(), "Product deleted.", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onError(String errorMessage) {
                                if (!isAdded()) return;
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
