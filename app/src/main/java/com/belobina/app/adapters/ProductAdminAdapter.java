package com.belobina.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.belobina.app.R;
import com.belobina.app.models.Furniture;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class ProductAdminAdapter extends RecyclerView.Adapter<ProductAdminAdapter.ViewHolder> {

    public interface OnProductActionListener {
        void onEdit(Furniture furniture);
        void onDelete(Furniture furniture);
    }

    private final Context context;
    private List<Furniture> allItems = new ArrayList<>();
    private List<Furniture> filteredItems = new ArrayList<>();
    private final OnProductActionListener listener;

    public ProductAdminAdapter(Context context, OnProductActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setData(List<Furniture> items) {
        this.allItems = new ArrayList<>(items);
        this.filteredItems = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredItems = new ArrayList<>();
        for (Furniture f : allItems) {
            String productName = f.getProdName();
            if (query == null || query.isEmpty()
                    || (productName != null && productName.toLowerCase().contains(query.toLowerCase()))) {
                filteredItems.add(f);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Furniture f = filteredItems.get(position);
        holder.tvName.setText(f.getProdName() != null ? f.getProdName() : "Untitled product");
        holder.tvPrice.setText(String.format("RM %.0f", f.getProdPrice()));
        holder.chipCategory.setText(f.getProdCat() != null ? f.getProdCat() : "—");

        if (f.getProdAssembleFee() > 0) {
            holder.tvAssemblyFee.setVisibility(View.VISIBLE);
            holder.tvAssemblyFee.setText(String.format("Assemble: RM %.0f", f.getProdAssembleFee()));
        } else {
            holder.tvAssemblyFee.setVisibility(View.GONE);
        }

        int stock = f.getProdStock();
        holder.tvStock.setText(stock + " in stock");
        if (stock == 0) {
            holder.tvStock.setTextColor(0xFFF44336);
        } else if (stock < 5) {
            holder.tvStock.setTextColor(0xFFFFC107);
        } else {
            holder.tvStock.setTextColor(0xFF757575);
        }

        Glide.with(context)
                .load(f.getProdPicture())
                .placeholder(R.drawable.placeholder_bg)
                .error(R.drawable.placeholder_bg)
                .centerCrop()
                .into(holder.ivImage);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(f));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(f));
        holder.itemView.setOnClickListener(v -> listener.onEdit(f));
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvStock, tvAssemblyFee;
        Chip chipCategory;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            tvAssemblyFee = itemView.findViewById(R.id.tv_assembly_fee);
            tvStock = itemView.findViewById(R.id.tv_stock);
            chipCategory = itemView.findViewById(R.id.chip_category);
            btnEdit = itemView.findViewById(R.id.btn_edit_product);
            btnDelete = itemView.findViewById(R.id.btn_delete_product);
        }
    }
}
