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
import com.belobina.app.adapters.AssemblerAdapter;
import com.belobina.app.models.Assembler;
import com.belobina.app.repositories.AssemblerRepository;
import com.belobina.app.utils.Callback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class AdminAssemblersFragment extends Fragment {

    private AssemblerAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private final AssemblerRepository assemblerRepo = new AssemblerRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_assemblers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_assemblers);
        MaterialButton fab = view.findViewById(R.id.fab_add_assembler);
        TextInputEditText etSearch = view.findViewById(R.id.et_search_assembler);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);

        adapter = new AssemblerAdapter(requireContext(), new AssemblerAdapter.OnAssemblerActionListener() {
            @Override
            public void onEdit(Assembler assembler) {
                AddAssemblerBottomSheet.newInstance(assembler, a -> updateAssembler(a))
                        .show(getChildFragmentManager(), "edit_assembler");
            }
            @Override
            public void onDelete(Assembler assembler) {
                confirmDelete(assembler);
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
                AddAssemblerBottomSheet.newInstance(null, a -> addAssembler(a))
                        .show(getChildFragmentManager(), "add_assembler"));

        loadAssemblers();
    }

    private void loadAssemblers() {
        progressBar.setVisibility(View.VISIBLE);
        assemblerRepo.getAllAssemblers(new Callback<List<Assembler>>() {
            @Override
            public void onSuccess(List<Assembler> assemblers) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                adapter.setData(assemblers);
                tvEmpty.setVisibility(assemblers.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void addAssembler(Assembler assembler) {
        assemblerRepo.addAssembler(assembler, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Assembler added successfully.", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateAssembler(Assembler assembler) {
        assemblerRepo.updateAssembler(assembler, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Assembler updated.", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmDelete(Assembler assembler) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_assembler))
                .setMessage(getString(R.string.confirm_delete))
                .setPositiveButton("Delete", (d, w) ->
                        assemblerRepo.deleteAssembler(assembler.getId(), new Callback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                if (!isAdded()) return;
                                Toast.makeText(requireContext(), "Assembler deleted.", Toast.LENGTH_SHORT).show();
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
