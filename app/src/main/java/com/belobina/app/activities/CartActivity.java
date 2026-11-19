package com.belobina.app.activities;

import androidx.annotation.NonNull;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.belobina.app.R;
import com.belobina.app.adapters.CartAdapter;
import com.belobina.app.models.CartItem;
import com.belobina.app.models.Order;
import com.belobina.app.models.User;
import com.belobina.app.repositories.AuthRepository;
import com.belobina.app.repositories.CartRepository;
import com.belobina.app.repositories.FurnitureRepository;
import com.belobina.app.repositories.OrderRepository;
import com.belobina.app.utils.Callback;
import com.belobina.app.utils.Constants;
import com.belobina.app.utils.FirebaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private static final long ORDER_SUCCESS_VISIBLE_MS = 2000L;

    private CartAdapter cartAdapter;
    private RecyclerView rvCart;
    private View layoutEmpty, layoutOrderSuccess;
    private ProgressBar progressBar;
    private MaterialButton btnPlaceOrder, btnBrowse;
    private TextView tvSubtotal, tvAssemblyTotal, tvTotal;

    private final CartRepository cartRepo = new CartRepository();
    private final OrderRepository orderRepo = new OrderRepository();
    private final AuthRepository authRepo = new AuthRepository();
    private FurnitureRepository furnitureRepo;
    private String uid;
    private List<CartItem> currentItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        uid = FirebaseHelper.getCurrentUserId();
        furnitureRepo = new FurnitureRepository(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvCart = findViewById(R.id.rv_cart);
        layoutEmpty = findViewById(R.id.layout_empty);
        layoutOrderSuccess = findViewById(R.id.layout_order_success);
        progressBar = findViewById(R.id.progress_bar);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        btnBrowse = findViewById(R.id.btn_browse);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvAssemblyTotal = findViewById(R.id.tv_assembly_total);
        tvTotal = findViewById(R.id.tv_total);

        cartAdapter = new CartAdapter(this, new CartAdapter.CartItemListener() {
            @Override
            public void onQuantityDecrement(CartItem item, int newQty) {
                if (newQty < 1) {
                    confirmDeleteItem(item);
                } else {
                    cartRepo.updateCartItemQuantity(uid, item.getCartItemId(), newQty, new Callback<Void>() {
                        @Override public void onSuccess(Void r) {
                            Toast.makeText(CartActivity.this, "Quantity updated.", Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onError(String e) {}
                    });
                }
            }

            @Override
            public void onQuantityIncrement(CartItem item, int newQty) {
                cartRepo.updateCartItemQuantity(uid, item.getCartItemId(), newQty, new Callback<Void>() {
                    @Override public void onSuccess(Void r) {
                        Toast.makeText(CartActivity.this, "Quantity updated.", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onError(String e) {}
                });
            }

            @Override
            public void onAssemblyToggled(CartItem item, boolean requested) {
                cartRepo.toggleAssembly(uid, item.getCartItemId(), requested, new Callback<Void>() {
                    @Override public void onSuccess(Void r) {}
                    @Override public void onError(String e) {}
                });
            }

            @Override
            public void onDeleteItem(CartItem item) {
                confirmDeleteItem(item);
            }
        });

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(cartAdapter);

        setupSwipeToDelete();

        btnBrowse.setOnClickListener(v -> finish());
        btnPlaceOrder.setOnClickListener(v -> placeOrder());

        loadCart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart) {
            confirmClearCart();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCart() {
        if (uid == null) return;
        progressBar.setVisibility(View.VISIBLE);
        cartRepo.getCartItems(uid, new Callback<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> items) {
                enrichCartFees(items);
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CartActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void enrichCartFees(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            currentItems = items;
            cartAdapter.setData(items);
            updateSummary(items);
            boolean empty = true;
            layoutEmpty.setVisibility(View.VISIBLE);
            rvCart.setVisibility(View.GONE);
            btnPlaceOrder.setVisibility(View.GONE);
            findViewById(R.id.card_order_summary).setVisibility(View.GONE);
            return;
        }

        final int[] remaining = {items.size()};
        for (CartItem item : items) {
            if (item.getAssemblyFeePerUnit() > 0 || item.getFurnitureId() == null) {
                if (--remaining[0] == 0) onCartReady(items);
                continue;
            }

            furnitureRepo.getFurnitureById(item.getFurnitureId(), new Callback<com.belobina.app.models.Furniture>() {
                @Override
                public void onSuccess(com.belobina.app.models.Furniture furniture) {
                    if (furniture != null) {
                        item.setAssemblyFeePerUnit(furniture.getProdAssembleFee());
                    }
                    if (--remaining[0] == 0) onCartReady(items);
                }

                @Override
                public void onError(String errorMessage) {
                    if (--remaining[0] == 0) onCartReady(items);
                }
            });
        }
    }

    private void onCartReady(List<CartItem> items) {
        progressBar.setVisibility(View.GONE);
        currentItems = items;
        cartAdapter.setData(items);
        updateSummary(items);
        boolean empty = items.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvCart.setVisibility(empty ? View.GONE : View.VISIBLE);
        btnPlaceOrder.setVisibility(empty ? View.GONE : View.VISIBLE);
        findViewById(R.id.card_order_summary).setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void updateSummary(List<CartItem> items) {
        double subtotal = 0;
        double assembly = 0;
        for (CartItem item : items) {
            subtotal += item.getPricePerUnit() * item.getQuantity();
            if (item.isAssemblyRequested()) {
                assembly += item.getAssemblyFeePerUnit() * item.getQuantity();
            }
        }
        tvSubtotal.setText(String.format("RM %.0f", subtotal));
        tvAssemblyTotal.setText(String.format("RM %.0f", assembly));
        tvTotal.setText(String.format("RM %.0f", subtotal + assembly));
    }

    private void confirmDeleteItem(CartItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Item")
                .setMessage("Remove " + item.getFurnitureName() + " from cart?")
                .setPositiveButton("Remove", (d, w) -> {
                    cartRepo.removeFromCart(uid, item.getCartItemId(), new Callback<Void>() {
                        @Override public void onSuccess(Void r) {
                            Toast.makeText(CartActivity.this,
                                    item.getFurnitureName() + " removed from cart.", Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onError(String e) {
                            Toast.makeText(CartActivity.this, e, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmClearCart() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Cart")
                .setMessage("Clear all items?")
                .setPositiveButton("Clear", (d, w) -> {
                    cartRepo.clearCart(uid, new Callback<Void>() {
                        @Override public void onSuccess(Void r) {
                            Toast.makeText(CartActivity.this, "Cart cleared.", Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onError(String e) {
                            Toast.makeText(CartActivity.this, e, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void placeOrder() {
        if (currentItems == null || currentItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPlaceOrder.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        authRepo.getUserData(uid, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                String customerName = user != null ? user.getUserFullName() : "";
                String deliveryAddress = user != null ? user.getUserAddress() : "";
                placeOrdersForItems(customerName, deliveryAddress);
            }

            @Override
            public void onError(String errorMessage) {
                placeOrdersForItems("", "");
            }
        });
    }

    private void placeOrdersForItems(String customerName, String deliveryAddress) {
        List<CartItem> items = currentItems;
        List<String> prodIDs = new ArrayList<>();
        List<Integer> itemQuantities = new ArrayList<>();
        List<Double> unitPrices = new ArrayList<>();
        List<Order> orders = new ArrayList<>();

        for (CartItem item : items) {
            prodIDs.add(item.getFurnitureId());
            itemQuantities.add(item.getQuantity());
            unitPrices.add(item.getPricePerUnit());

            double itemAssembly = item.isAssemblyRequested()
                    ? item.getAssemblyFeePerUnit() * item.getQuantity() : 0;
            double total = item.getPricePerUnit() * item.getQuantity() + itemAssembly;

            Order order = new Order();
            order.setCustomerId(uid);
            order.setCustomerName(customerName);
            order.setFurnitureName(item.getFurnitureName());
            order.setFurnitureImageUrl(item.getFurnitureImageUrl());
            order.setQuantity(item.getQuantity());
            order.setTotalPrice(total);
            order.setAssemblyRequested(item.isAssemblyRequested());
            order.setAssemblerName(null);
            order.setStatus(Constants.STATUS_PENDING);
            order.setOrderDate(Timestamp.now());
            order.setDeliveryAddress(deliveryAddress);
            orders.add(order);
        }

        orderRepo.placeOrdersWithReceipt(orders, prodIDs, itemQuantities, unitPrices, new Callback<Void>() {
            @Override
            public void onSuccess(Void r) {
                onAllOrdersPlaced();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(CartActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onAllOrdersPlaced() {
        cartRepo.clearCart(uid, new Callback<Void>() {
            @Override public void onSuccess(Void r) {}
            @Override public void onError(String e) {}
        });
        progressBar.setVisibility(View.GONE);
        btnPlaceOrder.setEnabled(false);
        showOrderSuccessThenExit();
    }

    private void showOrderSuccessThenExit() {
        layoutOrderSuccess.setVisibility(View.VISIBLE);
        Animation successAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up);
        layoutOrderSuccess.startAnimation(successAnimation);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }, ORDER_SUCCESS_VISIBLE_MS);
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                int pos = vh.getAdapterPosition();
                CartItem item = cartAdapter.getItemAt(pos);
                cartAdapter.notifyItemChanged(pos);
                confirmDeleteItem(item);
            }
        }).attachToRecyclerView(rvCart);
    }
}
