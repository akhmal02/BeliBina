package com.belobina.app.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.belobina.app.R;
import com.belobina.app.adapters.AdminOrderAdapter;
import com.belobina.app.models.Order;
import com.belobina.app.models.User;
import com.belobina.app.repositories.AssemblerRepository;
import com.belobina.app.repositories.AuthRepository;
import com.belobina.app.repositories.OrderRepository;
import com.belobina.app.utils.Callback;
import com.belobina.app.utils.Constants;
import com.belobina.app.utils.FirebaseHelper;

import java.util.List;

public class AdminDashboardFragment extends Fragment {

    private TextView tvTotalOrders, tvPendingOrders, tvActiveAssemblers, tvCompletedOrders;
    private TextView tvAdminName;
    private RecyclerView rvRecentOrders;
    private final OrderRepository orderRepo = new OrderRepository();
    private final AuthRepository authRepo = new AuthRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalOrders = view.findViewById(R.id.tv_total_orders);
        tvPendingOrders = view.findViewById(R.id.tv_pending_orders);
        tvActiveAssemblers = view.findViewById(R.id.tv_active_assemblers);
        tvCompletedOrders = view.findViewById(R.id.tv_completed_orders);
        tvAdminName = view.findViewById(R.id.tv_admin_name);
        rvRecentOrders = view.findViewById(R.id.rv_recent_orders);
        rvRecentOrders.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadAdminName();

        // Animate greeting header on enter
        View headerCard = view.findViewById(R.id.card_admin_greeting);
        Animation headerAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_slide_up);
        headerCard.startAnimation(headerAnim);

        loadDashboardData();
        loadAssemblerCount();
    }

    private void loadAdminName() {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) return;
        authRepo.getUserData(uid, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                if (!isAdded() || user == null) return;
                String name = user.getUserFullName();
                tvAdminName.setText((name != null && !name.isEmpty()) ? name + "!" : "Admin!");
            }
            @Override
            public void onError(String errorMessage) {}
        });
    }

    private void loadDashboardData() {
        orderRepo.getAllOrders(new Callback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                if (!isAdded()) return;

                int total = orders.size();
                int pending = 0, completed = 0;
                for (Order o : orders) {
                    if (Constants.STATUS_PENDING.equals(o.getStatus())) pending++;
                    if (Constants.STATUS_COMPLETED.equals(o.getStatus())) completed++;
                }

                tvTotalOrders.setText(String.valueOf(total));
                tvPendingOrders.setText(String.valueOf(pending));
                tvCompletedOrders.setText(String.valueOf(completed));

                // Show 5 most recent orders using AdminOrderAdapter for richer display
                List<Order> recent = orders.subList(0, Math.min(5, orders.size()));
                AdminOrderAdapter recentAdapter = new AdminOrderAdapter(
                        requireContext(), recent, order -> {});
                rvRecentOrders.setAdapter(recentAdapter);
            }

            @Override
            public void onError(String errorMessage) {}
        });
    }

    private void loadAssemblerCount() {
        new AssemblerRepository()
                .getAllAssemblers(new Callback<List<com.belobina.app.models.Assembler>>() {
                    @Override
                    public void onSuccess(List<com.belobina.app.models.Assembler> assemblers) {
                        if (!isAdded()) return;
                        tvActiveAssemblers.setText(String.valueOf(assemblers.size()));
                    }
                    @Override
                    public void onError(String errorMessage) {}
                });
    }
}
