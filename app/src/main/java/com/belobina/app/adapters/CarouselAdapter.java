package com.belobina.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.belobina.app.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {

    private final List<String> imageUrls;
    private final List<String> names;

    public CarouselAdapter(List<String> imageUrls, List<String> names) {
        this.imageUrls = imageUrls;
        this.names = names;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_carousel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int index = position % imageUrls.size();
        Glide.with(holder.ivCarousel.getContext())
                .load(imageUrls.get(index))
                .placeholder(R.drawable.placeholder_bg)
                .error(R.drawable.placeholder_bg)
                .centerCrop()
                .into(holder.ivCarousel);
        holder.tvName.setText(names.get(index));
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCarousel;
        TextView tvName;

        ViewHolder(View itemView) {
            super(itemView);
            ivCarousel = itemView.findViewById(R.id.iv_carousel);
            tvName = itemView.findViewById(R.id.tv_carousel_name);
        }
    }
}
