package com.belobina.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.belobina.app.R;

public class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.ViewHolder> {

    private final Context context;

    private static final int[] ICONS = {
            R.drawable.onboarding_1,
            R.drawable.onboarding_2,
            R.drawable.onboarding_3
    };
    private static final int[] TITLES = {
            R.string.onboarding_title_1,
            R.string.onboarding_title_2,
            R.string.onboarding_title_3
    };
    private static final int[] SUBS = {
            R.string.onboarding_sub_1,
            R.string.onboarding_sub_2,
            R.string.onboarding_sub_3
    };

    public OnboardingPagerAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.layout_onboarding_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.icon.setImageResource(ICONS[position]);
        holder.tvTitle.setText(TITLES[position]);
        holder.tvSub.setText(SUBS[position]);
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView tvTitle, tvSub;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_onboarding_icon);
            tvTitle = itemView.findViewById(R.id.tv_onboarding_title);
            tvSub = itemView.findViewById(R.id.tv_onboarding_sub);
        }
    }
}
