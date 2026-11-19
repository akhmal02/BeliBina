package com.belobina.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.belobina.app.R;
import com.belobina.app.models.Furniture;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class FurnitureAdapter extends RecyclerView.Adapter<FurnitureAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Furniture furniture);
    }

    public interface OnAddToCartClickListener {
        void onAddToCartClick(Furniture furniture);
    }

    public interface OnProductClickListener {
        void onProductClick(Furniture furniture);
    }

    private final Context context;
    private List<Furniture> allItems = new ArrayList<>();
    private List<Furniture> filteredItems = new ArrayList<>();
    private OnOrderClickListener listener;
    private OnAddToCartClickListener cartListener;
    private OnProductClickListener productClickListener;

    public FurnitureAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public FurnitureAdapter(Context context, OnOrderClickListener listener,
                            OnAddToCartClickListener cartListener) {
        this.context = context;
        this.listener = listener;
        this.cartListener = cartListener;
    }

    public void setCartListener(OnAddToCartClickListener cartListener) {
        this.cartListener = cartListener;
    }

    public void setProductClickListener(OnProductClickListener productClickListener) {
        this.productClickListener = productClickListener;
    }

    public void setData(List<Furniture> items) {
        this.allItems = new ArrayList<>(items);
        this.filteredItems = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    public void filter(String query, String category) {
        filteredItems = new ArrayList<>();
        for (Furniture f : allItems) {
            String productName = f.getProdName();
            boolean matchesQuery = query == null || query.isEmpty()
                    || (productName != null && productName.toLowerCase().contains(query.toLowerCase()));
            boolean matchesCategory = category == null || category.equals("All")
                    || category.equals(f.getProdCat());
            if (matchesQuery && matchesCategory) {
                filteredItems.add(f);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_furniture, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Furniture f = filteredItems.get(position);
        holder.tvName.setText(f.getProdName() != null ? f.getProdName() : "Untitled product");
        holder.tvPrice.setText(String.format("RM %.0f", f.getProdPrice()));
        holder.tvDesc.setText(f.getProdDesc() != null ? f.getProdDesc() : "");
        holder.tvMeta.setText(String.format("%s - %d in stock",
                f.getProdCat() != null ? f.getProdCat() : "Uncategorized",
                f.getProdStock()));
        holder.tvAssemblyFee.setText(String.format("Assembly RM %.0f", f.getProdAssembleFee()));

        Glide.with(context)
                .load(f.getProdPicture())
                .placeholder(R.drawable.placeholder_bg)
                .error(R.drawable.placeholder_bg)
                .centerCrop()
                .into(holder.ivImage);

        holder.itemView.setOnClickListener(v -> {
            if (productClickListener != null) {
                productClickListener.onProductClick(f);
            } else {
                listener.onOrderClick(f);
            }
        });
        holder.btnOrder.setOnClickListener(v -> listener.onOrderClick(f));

        if (cartListener != null) {
            holder.btnAddToCart.setOnClickListener(v -> cartListener.onAddToCartClick(f));
            holder.btnAddToCart.setVisibility(View.VISIBLE);
        } else {
            holder.btnAddToCart.setVisibility(View.GONE);
        }

        Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_slide_up);
        anim.setStartOffset(Math.min(position * 50L, 250L));
        holder.itemView.startAnimation(anim);
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvDesc, tvMeta, tvAssemblyFee;
        MaterialButton btnOrder, btnAddToCart;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_furniture_image);
            tvName = itemView.findViewById(R.id.tv_furniture_name);
            tvPrice = itemView.findViewById(R.id.tv_furniture_price);
            tvDesc = itemView.findViewById(R.id.tv_furniture_desc);
            tvMeta = itemView.findViewById(R.id.tv_furniture_meta);
            tvAssemblyFee = itemView.findViewById(R.id.tv_furniture_assembly_fee);
            btnOrder = itemView.findViewById(R.id.btn_order_now);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}
