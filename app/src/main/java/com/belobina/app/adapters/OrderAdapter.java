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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private final Context context;
    private List<Order> orders = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public OrderAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<Order> orders) {
        this.orders = new ArrayList<>(orders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order o = orders.get(position);
        holder.tvName.setText(o.getFurnitureName() != null && !o.getFurnitureName().trim().isEmpty()
                ? o.getFurnitureName()
                : "Order");
        holder.tvDate.setText(o.getOrderDate() != null
                ? sdf.format(o.getOrderDate().toDate()) : "");

        styleStatusChip(holder.chipStatus, o.getStatus());

        if (o.isAssemblyRequested() && o.getAssemblerName() != null && !o.getAssemblerName().isEmpty()) {
            holder.tvAssembler.setVisibility(View.VISIBLE);
            holder.tvAssembler.setText(o.getAssemblerName());
        } else {
            holder.tvAssembler.setVisibility(View.GONE);
        }

        // Expanded detail section
        holder.btnViewDetails.setOnClickListener(v -> {
            boolean expanded = holder.layoutDetails.getVisibility() == View.VISIBLE;
            holder.layoutDetails.setVisibility(expanded ? View.GONE : View.VISIBLE);
            holder.btnViewDetails.setText(expanded
                    ? context.getString(R.string.view_details)
                    : context.getString(R.string.hide_details));
        });

        holder.tvDetailQty.setText(o.getQuantity() > 0
                ? "Quantity: " + o.getQuantity()
                : "Quantity: Not stored");
        holder.tvDetailAddress.setText("Delivery address: "
                + (o.getDeliveryAddress() != null && !o.getDeliveryAddress().trim().isEmpty()
                ? o.getDeliveryAddress()
                : "Not stored"));
        holder.tvDetailTotal.setText(String.format("Total: RM %.0f", o.getTotalPrice()));
        if (o.getCompletionDate() != null) {
            holder.tvDetailCompletion.setVisibility(View.VISIBLE);
            holder.tvDetailCompletion.setText("Completed date: " + sdf.format(o.getCompletionDate().toDate()));
        } else {
            holder.tvDetailCompletion.setVisibility(View.GONE);
        }

        Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_slide_up);
        anim.setStartOffset(Math.min(position * 50L, 250L));
        holder.itemView.startAnimation(anim);
    }

    private void styleStatusChip(Chip chip, String status) {
        chip.setText(status != null ? status.substring(0, 1).toUpperCase() + status.substring(1) : "");
        int colorRes;
        switch (status != null ? status : "") {
            case Constants.STATUS_PENDING:    colorRes = R.color.status_pending;   break;
            case Constants.STATUS_ASSIGNED:   colorRes = R.color.status_assigned;  break;
            case Constants.STATUS_COMPLETED:  colorRes = R.color.status_completed; break;
            case Constants.STATUS_CANCELLED:  colorRes = R.color.status_cancelled; break;
            case Constants.STATUS_REJECTED:   colorRes = R.color.status_cancelled; break;
            default:                          colorRes = R.color.text_secondary;   break;
        }
        chip.setChipBackgroundColor(
                android.content.res.ColorStateList.valueOf(
                        ContextCompat.getColor(context, colorRes)));
        chip.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvAssembler, tvDetailAddress, tvDetailTotal,
                tvDetailQty, tvDetailCompletion;
        Chip chipStatus;
        MaterialButton btnViewDetails;
        View layoutDetails;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_order_name);
            tvDate = itemView.findViewById(R.id.tv_order_date);
            tvAssembler = itemView.findViewById(R.id.tv_assembler_name);
            chipStatus = itemView.findViewById(R.id.chip_status);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            layoutDetails = itemView.findViewById(R.id.layout_order_details);
            tvDetailAddress = itemView.findViewById(R.id.tv_detail_address);
            tvDetailTotal = itemView.findViewById(R.id.tv_detail_total);
            tvDetailQty = itemView.findViewById(R.id.tv_detail_qty);
            tvDetailCompletion = itemView.findViewById(R.id.tv_detail_completion);
        }
    }
}
