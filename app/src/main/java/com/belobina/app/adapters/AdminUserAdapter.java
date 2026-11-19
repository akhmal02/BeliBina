package com.belobina.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.belobina.app.R;
import com.belobina.app.models.User;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    public interface OnAdminActionListener {
        void onView(User user);
        void onEdit(User user);
        void onRevoke(User user);
    }

    private final Context context;
    private List<User> items = new ArrayList<>();
    private final OnAdminActionListener listener;
    private String currentUid;

    public AdminUserAdapter(Context context, String currentUid, OnAdminActionListener listener) {
        this.context = context;
        this.currentUid = currentUid;
        this.listener = listener;
    }

    public void setData(List<User> data) {
        this.items = new ArrayList<>(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = items.get(position);

        String name = user.getUserFullName() != null ? user.getUserFullName() : "";
        holder.tvName.setText(name);
        holder.tvEmail.setText(user.getUserEmail() != null ? user.getUserEmail() : "");
        holder.tvPhone.setText(user.getUserPhone() != null && !user.getUserPhone().trim().isEmpty()
                ? user.getUserPhone()
                : "No phone");
        holder.tvAddress.setText(user.getUserAddress() != null && !user.getUserAddress().trim().isEmpty()
                ? user.getUserAddress()
                : "No address");

        String initials = getInitials(name);
        holder.tvInitials.setText(initials);

        boolean isCurrentUser = user.getUid() != null && user.getUid().equals(currentUid);
        holder.tvYou.setVisibility(isCurrentUser ? View.VISIBLE : View.GONE);
        holder.actionContainer.setVisibility(isCurrentUser ? View.GONE : View.VISIBLE);
        holder.btnRevoke.setVisibility(isCurrentUser ? View.GONE : View.VISIBLE);

        holder.btnRevoke.setOnClickListener(v -> listener.onRevoke(user));

        holder.itemView.setOnClickListener(v -> listener.onView(user));
        holder.itemView.setOnLongClickListener(isCurrentUser ? null : v -> {
            listener.onRevoke(user);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return String.valueOf(parts[0].charAt(0)).toUpperCase()
                    + String.valueOf(parts[1].charAt(0)).toUpperCase();
        }
        return String.valueOf(parts[0].charAt(0)).toUpperCase();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName, tvEmail, tvPhone, tvAddress, tvYou;
        View actionContainer;
        MaterialButton btnRevoke;

        ViewHolder(View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tv_avatar_initials);
            tvName = itemView.findViewById(R.id.tv_admin_name);
            tvEmail = itemView.findViewById(R.id.tv_admin_email);
            tvPhone = itemView.findViewById(R.id.tv_admin_phone);
            tvAddress = itemView.findViewById(R.id.tv_admin_address);
            tvYou = itemView.findViewById(R.id.tv_you_label);
            actionContainer = itemView.findViewById(R.id.layout_admin_actions);
            btnRevoke = itemView.findViewById(R.id.btn_revoke_admin);
        }
    }
}
