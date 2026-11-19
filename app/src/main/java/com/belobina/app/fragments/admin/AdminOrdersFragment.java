package com.belobina.app.fragments.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.belobina.app.R;
import com.belobina.app.models.Assembler;
import com.belobina.app.models.Order;
import com.belobina.app.repositories.AssemblerRepository;
import com.belobina.app.repositories.OrderRepository;
import com.belobina.app.utils.Callback;
import com.belobina.app.utils.Constants;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersFragment extends Fragment {

    private RecyclerView rvOrders;
    private ChipGroup chipGroup;
    private final OrderRepository orderRepo = new OrderRepository();
    private final AssemblerRepository assemblerRepo = new AssemblerRepository();
    private List<Order> allOrders = new ArrayList<>();
    private String currentFilter = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvOrders = view.findViewById(R.id.rv_admin_orders);
        chipGroup = view.findViewById(R.id.chip_group_filter);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));

        setupFilterChips();
        loadOrders();
    }

    private void setupFilterChips() {
        String[] filters = {"All", Constants.STATUS_PENDING, Constants.STATUS_ASSIGNED,
                Constants.STATUS_COMPLETED, Constants.STATUS_CANCELLED, Constants.STATUS_REJECTED};
        for (String f : filters) {
            Chip chip = new Chip(requireContext());
            chip.setText(f.substring(0, 1).toUpperCase() + f.substring(1));
            chip.setCheckable(true);
            chip.setChecked(f.equals(currentFilter));
            chip.setChipBackgroundColorResource(R.color.chip_selector);
            chip.setOnCheckedChangeListener((btn, checked) -> {
                if (checked) {
                    currentFilter = f;
                    applyFilter();
                }
            });
            chipGroup.addView(chip);
        }
    }

    private void loadOrders() {
        orderRepo.getAllOrders(new Callback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                if (!isAdded()) return;
                allOrders = orders;
                applyFilter();
            }
            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void applyFilter() {
        List<Order> filtered = new ArrayList<>();
        for (Order o : allOrders) {
            if ("All".equals(currentFilter) || currentFilter.equals(o.getStatus())) {
                filtered.add(o);
            }
        }
        setupOrdersRecycler(filtered);
    }

    private void setupOrdersRecycler(List<Order> orders) {
        com.belobina.app.adapters.AdminOrderAdapter adapter =
                new com.belobina.app.adapters.AdminOrderAdapter(requireContext(), orders, this::showOrderActionSheet);
        rvOrders.setAdapter(adapter);
    }

    private void showOrderActionSheet(Order order) {
        AdminOrderActionBottomSheet.newInstance(order, new AdminOrderActionBottomSheet.OnOrderActionListener() {
            @Override
            public void onStatusSelected(Order selectedOrder, String newStatus) {
                updateStatus(selectedOrder.getId(), newStatus);
            }

            @Override
            public void onAssignAssembler(Order selectedOrder) {
                showAssignDialog(selectedOrder);
            }
        }).show(getChildFragmentManager(), "order_action");
    }

    private void showAssignDialog(Order order) {
        assemblerRepo.getAllAssemblers(new Callback<List<Assembler>>() {
            @Override
            public void onSuccess(List<Assembler> assemblers) {
                if (!isAdded()) return;
                if (assemblers.isEmpty()) {
                    Snackbar.make(requireView(), "No assemblers found", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                List<String> names = new ArrayList<>();
                for (Assembler a : assemblers) names.add(a.getAssFullName());

                View dialogView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.dialog_assign_assembler, null, false);
                TextInputLayout tilAssembler = dialogView.findViewById(R.id.til_assign_assembler);
                AutoCompleteTextView actvAssembler = dialogView.findViewById(R.id.actv_assign_assembler);
                android.widget.TextView tvOrderName = dialogView.findViewById(R.id.tv_assign_order_name);
                com.google.android.material.button.MaterialButton btnCancel =
                        dialogView.findViewById(R.id.btn_assign_cancel);
                com.google.android.material.button.MaterialButton btnConfirm =
                        dialogView.findViewById(R.id.btn_assign_confirm);

                tvOrderName.setText(order.getFurnitureName() != null && !order.getFurnitureName().trim().isEmpty()
                        ? order.getFurnitureName()
                        : "Order #" + order.getId());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        names);
                actvAssembler.setAdapter(adapter);
                actvAssembler.setThreshold(1);
                actvAssembler.setText("", false);

                AlertDialog dialog = new AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .create();

                btnCancel.setOnClickListener(v -> dialog.dismiss());
                btnConfirm.setOnClickListener(v -> {
                    String selectedName = actvAssembler.getText() != null
                            ? actvAssembler.getText().toString().trim()
                            : "";
                    int pos = names.indexOf(selectedName);
                    if (pos < 0) {
                        tilAssembler.setError("Select an assembler from the list");
                        return;
                    }
                    tilAssembler.setError(null);
                    Assembler selected = assemblers.get(pos);
                    orderRepo.assignAssembler(order.getId(), selected.getId(), selected.getAssFullName(),
                            new Callback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    if (isAdded()) {
                                        Toast.makeText(requireContext(), "Assembler assigned successfully.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onError(String errorMessage) {
                                    if (isAdded()) {
                                        Snackbar.make(requireView(), errorMessage,
                                                Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
                    dialog.dismiss();
                });

                dialog.show();
            }
            @Override
            public void onError(String errorMessage) {
                if (isAdded())
                    Snackbar.make(requireView(), "No assemblers found", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatus(String orderId, String status) {
        orderRepo.updateOrderStatus(orderId, status, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (!isAdded()) return;
                String msg;
                if (Constants.STATUS_COMPLETED.equals(status)) {
                    msg = "Order marked as completed.";
                } else if (Constants.STATUS_CANCELLED.equals(status)) {
                    msg = "Order has been cancelled.";
                } else if (Constants.STATUS_REJECTED.equals(status)) {
                    msg = "Order has been rejected.";
                } else {
                    msg = "Order updated.";
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String errorMessage) {
                if (isAdded())
                    Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
