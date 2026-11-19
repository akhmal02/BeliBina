package com.belobina.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.belobina.app.R;
import com.belobina.app.models.Assembler;

import java.util.ArrayList;
import java.util.List;

public class AssemblerAdapter extends RecyclerView.Adapter<AssemblerAdapter.ViewHolder> {

    public interface OnAssemblerActionListener {
        void onEdit(Assembler assembler);
        void onDelete(Assembler assembler);
    }

    private final Context context;
    private List<Assembler> allAssemblers = new ArrayList<>();
    private List<Assembler> filteredAssemblers = new ArrayList<>();
    private final OnAssemblerActionListener listener;

    public AssemblerAdapter(Context context, OnAssemblerActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setData(List<Assembler> assemblers) {
        this.allAssemblers = new ArrayList<>(assemblers);
        this.filteredAssemblers = new ArrayList<>(assemblers);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredAssemblers = new ArrayList<>();
        for (Assembler a : allAssemblers) {
            if (query == null || query.isEmpty()
                    || a.getAssFullName().toLowerCase().contains(query.toLowerCase())
                    || (a.getAssServiceArea() != null && a.getAssServiceArea().toLowerCase().contains(query.toLowerCase()))) {
                filteredAssemblers.add(a);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_assembler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Assembler a = filteredAssemblers.get(position);
        holder.tvName.setText(a.getAssFullName());
        holder.tvServiceArea.setText(a.getAssServiceArea());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(a));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(a));
        holder.itemView.setOnClickListener(v -> listener.onEdit(a));

        Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_slide_up);
        anim.setStartOffset(Math.min(position * 50L, 250L));
        holder.itemView.startAnimation(anim);
    }

    @Override
    public int getItemCount() {
        return filteredAssemblers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvServiceArea;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_assembler_name);
            tvServiceArea = itemView.findViewById(R.id.tv_service_area);
            btnEdit = itemView.findViewById(R.id.btn_edit_assembler);
            btnDelete = itemView.findViewById(R.id.btn_delete_assembler);
        }
    }
}
