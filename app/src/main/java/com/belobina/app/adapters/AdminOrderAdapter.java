package com.belobina.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.belobina.app.R;
import com.belobina.app.models.Order;
import com.belobina.app.utils.Constants;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onClick(Order order);
    }

    private final Context context;
    private final List<Order> orders;
    private final OnOrderClickListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public AdminOrderAdapter(Context context, List<Order> orders, OnOrderClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order o = orders.get(position);
        holder.tvCustomer.setText(o.getCustomerName() != null && !o.getCustomerName().trim().isEmpty()
                ? o.getCustomerName()
                : "User: " + safe(o.getCustomerId(), "Unknown"));
        holder.tvFurniture.setText(o.getFurnitureName() != null && !o.getFurnitureName().trim().isEmpty()
                ? o.getFurnitureName()
                : "Order");
        holder.tvDate.setText(o.getOrderDate() != null ? sdf.format(o.getOrderDate().toDate()) : "");
        holder.tvPrice.setText(String.format("RM %.0f", o.getTotalPrice()));
        holder.tvAssembly.setText(o.isAssemblyRequested() ? "Assembly: Yes" : "Assembly: No");

        styleStatusChip(holder.chipStatus, o.getStatus());
        holder.itemView.setOnClickListener(v -> listener.onClick(o));

        Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_slide_up);
        anim.setStartOffset(Math.min(position * 50L, 250L));
        holder.itemView.startAnimation(anim);
    }

    private void styleStatusChip(Chip chip, String status) {
        chip.setText(status != null ? status.substring(0, 1).toUpperCase() + status.substring(1) : "");
        int colorRes;
        switch (status != null ? status : "") {
            case Constants.STATUS_PENDING:   colorRes = R.color.status_pending;   break;
            case Constants.STATUS_ASSIGNED:  colorRes = R.color.status_assigned;  break;
            case Constants.STATUS_COMPLETED: colorRes = R.color.status_completed; break;
            case Constants.STATUS_CANCELLED: colorRes = R.color.status_cancelled; break;
            case Constants.STATUS_REJECTED:  colorRes = R.color.status_cancelled; break;
            default:                         colorRes = R.color.text_secondary;   break;
        }
        chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(context, colorRes)));
        chip.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    private String safe(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value : fallback;
    }

    @Override
    public int getItemCount() { return orders.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomer, tvFurniture, tvDate, tvPrice, tvAssembly;
        Chip chipStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvCustomer = itemView.findViewById(R.id.tv_customer_name);
            tvFurniture = itemView.findViewById(R.id.tv_furniture_name);
            tvDate = itemView.findViewById(R.id.tv_order_date);
            tvPrice = itemView.findViewById(R.id.tv_order_price);
            tvAssembly = itemView.findViewById(R.id.tv_assembly_indicator);
            chipStatus = itemView.findViewById(R.id.chip_status);
        }
    }
}
