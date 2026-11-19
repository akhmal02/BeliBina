package com.belobina.app.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.belobina.app.R;
import com.belobina.app.models.Order;
import com.belobina.app.utils.Constants;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

public class AdminOrderActionBottomSheet extends BottomSheetDialogFragment {

    public interface OnOrderActionListener {
        void onStatusSelected(Order order, String newStatus);
        void onAssignAssembler(Order order);
    }

    private Order order;
    private OnOrderActionListener listener;

    public static AdminOrderActionBottomSheet newInstance(Order order,
                                                          OnOrderActionListener listener) {
        AdminOrderActionBottomSheet sheet = new AdminOrderActionBottomSheet();
        sheet.order = order;
        sheet.listener = listener;
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_order_action, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvCustomer = view.findViewById(R.id.tv_order_customer);
        TextView tvFurniture = view.findViewById(R.id.tv_order_furniture);
        TextView tvPrice = view.findViewById(R.id.tv_order_price);
        TextView tvNoActions = view.findViewById(R.id.tv_no_actions);
        Chip chipStatus = view.findViewById(R.id.chip_order_status);
        MaterialCardView cardAssign = view.findViewById(R.id.card_assign);
        MaterialCardView cardComplete = view.findViewById(R.id.card_complete);
        MaterialCardView cardCancel = view.findViewById(R.id.card_cancel);
        MaterialCardView cardReject = view.findViewById(R.id.card_reject);
        MaterialButton btnClose = view.findViewById(R.id.btn_sheet_close);

        tvCustomer.setText(order.getCustomerName() != null && !order.getCustomerName().trim().isEmpty()
                ? order.getCustomerName()
                : "User: " + safe(order.getCustomerId(), "Unknown"));
        tvFurniture.setText(order.getFurnitureName() != null && !order.getFurnitureName().trim().isEmpty()
                ? order.getFurnitureName() + (order.getQuantity() > 0 ? " x " + order.getQuantity() : "")
                : "Order details not stored");
        tvPrice.setText(String.format("RM %.0f", order.getTotalPrice()));
        styleStatusChip(chipStatus, order.getStatus());

        configureActions(cardAssign, cardComplete, cardCancel, cardReject, tvNoActions);
        btnClose.setOnClickListener(v -> dismiss());
    }

    private void configureActions(MaterialCardView cardAssign,
                                  MaterialCardView cardComplete,
                                  MaterialCardView cardCancel,
                                  MaterialCardView cardReject,
                                  TextView tvNoActions) {
        String status = order.getStatus();
        boolean pending = Constants.STATUS_PENDING.equals(status);
        boolean assigned = Constants.STATUS_ASSIGNED.equals(status);
        boolean active = pending || assigned;
        boolean requiresAssembly = order.isAssemblyRequested();
        boolean hasAssembler = order.getAssemblerName() != null
                && !order.getAssemblerName().trim().isEmpty();

        boolean showAssign = pending && requiresAssembly && !hasAssembler;
        boolean showComplete = assigned || (pending && (!requiresAssembly || hasAssembler));
        boolean showCancelReject = active;

        cardAssign.setVisibility(showAssign ? View.VISIBLE : View.GONE);
        cardComplete.setVisibility(showComplete ? View.VISIBLE : View.GONE);
        cardCancel.setVisibility(showCancelReject ? View.VISIBLE : View.GONE);
        cardReject.setVisibility(showCancelReject ? View.VISIBLE : View.GONE);
        tvNoActions.setVisibility(!showAssign && !showComplete && !showCancelReject
                ? View.VISIBLE : View.GONE);

        cardAssign.setOnClickListener(v -> {
            listener.onAssignAssembler(order);
            dismiss();
        });
        cardComplete.setOnClickListener(v -> {
            listener.onStatusSelected(order, Constants.STATUS_COMPLETED);
            dismiss();
        });
        cardCancel.setOnClickListener(v -> {
            listener.onStatusSelected(order, Constants.STATUS_CANCELLED);
            dismiss();
        });
        cardReject.setOnClickListener(v -> {
            listener.onStatusSelected(order, Constants.STATUS_REJECTED);
            dismiss();
        });
    }

    private String safe(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value : fallback;
    }

    private void styleStatusChip(Chip chip, String status) {
        if (status == null) return;
        chip.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
        int colorRes;
        switch (status) {
            case Constants.STATUS_PENDING:   colorRes = R.color.status_pending;   break;
            case Constants.STATUS_ASSIGNED:  colorRes = R.color.status_assigned;  break;
            case Constants.STATUS_COMPLETED: colorRes = R.color.status_completed; break;
            case Constants.STATUS_CANCELLED: colorRes = R.color.status_cancelled; break;
            case Constants.STATUS_REJECTED:  colorRes = R.color.status_cancelled; break;
            default:                         colorRes = R.color.text_secondary;   break;
        }
        chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), colorRes)));
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
    }
}
