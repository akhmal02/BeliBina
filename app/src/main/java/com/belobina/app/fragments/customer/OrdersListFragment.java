package com.belobina.app.fragments.customer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.belobina.app.R;
import com.belobina.app.adapters.OrderAdapter;
import com.belobina.app.models.Order;
import com.belobina.app.repositories.OrderRepository;
import com.belobina.app.utils.Callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrdersListFragment extends Fragment {

    private static final String ARG_STATUSES = "statuses";

    private OrderAdapter adapter;
    private final OrderRepository orderRepo = new OrderRepository();
    private String[] statusFilter;

    public static OrdersListFragment newInstance(String[] statuses) {
        OrdersListFragment f = new OrdersListFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_STATUSES, statuses);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            statusFilter = getArguments().getStringArray(ARG_STATUSES);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rv_orders);
        View emptyState = view.findViewById(R.id.layout_empty_state);

        adapter = new OrderAdapter(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        orderRepo.getCurrentCustomerOrders(new Callback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> allOrders) {
                if (!isAdded()) return;
                List<Order> filtered = filterByStatus(allOrders);
                adapter.setData(filtered);
                emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                Log.e("OrdersListFragment", "Failed to load orders: " + errorMessage);
                emptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private List<Order> filterByStatus(List<Order> orders) {
        if (statusFilter == null || statusFilter.length == 0) return orders;
        List<String> filter = Arrays.asList(statusFilter);
        List<Order> result = new ArrayList<>();
        for (Order o : orders) {
            if (o.getStatus() != null && filter.contains(o.getStatus())) {
                result.add(o);
            }
        }
        return result;
    }
}
