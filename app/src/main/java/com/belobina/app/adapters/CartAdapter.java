package com.belobina.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.belobina.app.R;
import com.belobina.app.models.CartItem;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import android.widget.ImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface CartItemListener {
        void onQuantityDecrement(CartItem item, int newQty);
        void onQuantityIncrement(CartItem item, int newQty);
        void onAssemblyToggled(CartItem item, boolean requested);
        void onDeleteItem(CartItem item);
    }

    private final Context context;
    private List<CartItem> items = new ArrayList<>();
    private final CartItemListener listener;

    public CartAdapter(Context context, CartItemListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setData(List<CartItem> data) {
        this.items = new ArrayList<>(data);
        notifyDataSetChanged();
    }

    public List<CartItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);

        holder.tvName.setText(item.getFurnitureName());
        holder.tvPrice.setText(String.format("RM %.0f / unit", item.getPricePerUnit()));
        holder.tvAssemblyFee.setText(String.format("Assembly (+RM %.0f)", item.getAssemblyFeePerUnit()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(context)
                .load(item.getFurnitureImageUrl())
                .placeholder(R.drawable.placeholder_bg)
                .error(R.drawable.placeholder_bg)
                .centerCrop()
                .into(holder.ivImage);

        holder.switchAssembly.setOnCheckedChangeListener(null);
        holder.switchAssembly.setChecked(item.isAssemblyRequested());
        holder.switchAssembly.setOnCheckedChangeListener((btn, checked) -> {
            item.setAssemblyRequested(checked);
            listener.onAssemblyToggled(item, checked);
        });

        holder.btnMinus.setOnClickListener(v -> {
            int newQty = item.getQuantity() - 1;
            listener.onQuantityDecrement(item, newQty);
        });

        holder.btnPlus.setOnClickListener(v -> {
            int newQty = Math.min(item.getQuantity() + 1, 99);
            listener.onQuantityIncrement(item, newQty);
        });

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteItem(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public CartItem getItemAt(int position) {
        return items.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvQuantity, tvAssemblyFee;
        MaterialButton btnMinus, btnPlus;
        SwitchMaterial switchAssembly;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_item_image);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvPrice = itemView.findViewById(R.id.tv_item_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvAssemblyFee = itemView.findViewById(R.id.tv_assembly_fee_label);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnPlus = itemView.findViewById(R.id.btn_plus);
            switchAssembly = itemView.findViewById(R.id.switch_assembly);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
