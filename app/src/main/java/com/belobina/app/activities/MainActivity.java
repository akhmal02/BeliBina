package com.belobina.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.belobina.app.R;
import com.belobina.app.repositories.AuthRepository;
import com.belobina.app.repositories.CartRepository;
import com.belobina.app.utils.Callback;
import com.belobina.app.utils.FirebaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private final AuthRepository authRepo = new AuthRepository();
    private final CartRepository cartRepo = new CartRepository();
    private TextView tvCartBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNav, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem cartItem = menu.findItem(R.id.action_cart);
        if (cartItem != null) {
            View cartView = cartItem.getActionView();
            if (cartView != null) {
                ImageButton btnCart = cartView.findViewById(R.id.btn_cart);
                tvCartBadge = cartView.findViewById(R.id.tv_cart_badge);
                if (btnCart != null) {
                    btnCart.setOnClickListener(v ->
                            startActivity(new Intent(this, CartActivity.class)));
                }
            }
        }
        updateCartBadge();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateCartBadge() {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null || tvCartBadge == null) return;
        cartRepo.getCartCount(uid, new Callback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                runOnUiThread(() -> {
                    if (tvCartBadge == null) return;
                    if (count > 0) {
                        tvCartBadge.setVisibility(View.VISIBLE);
                        tvCartBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                    } else {
                        tvCartBadge.setVisibility(View.GONE);
                    }
                });
            }
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (tvCartBadge != null) tvCartBadge.setVisibility(View.GONE);
                });
            }
        });
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    authRepo.signOut();
                    Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, OnboardingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
