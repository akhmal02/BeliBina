package com.belobina.app.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.belobina.app.fragments.customer.OrdersListFragment;
import com.belobina.app.utils.Constants;

public class OrdersPagerAdapter extends FragmentStateAdapter {

    public OrdersPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return OrdersListFragment.newInstance(
                        new String[]{Constants.STATUS_PENDING, Constants.STATUS_ASSIGNED});
            case 1:
                return OrdersListFragment.newInstance(
                        new String[]{Constants.STATUS_COMPLETED});
            case 2:
            default:
                return OrdersListFragment.newInstance(
                        new String[]{Constants.STATUS_CANCELLED, Constants.STATUS_REJECTED});
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
