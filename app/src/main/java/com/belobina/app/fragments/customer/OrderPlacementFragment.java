package com.belobina.app.fragments.customer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.belobina.app.R;
import com.belobina.app.models.Furniture;
import com.belobina.app.models.Order;
import com.belobina.app.models.User;
import com.belobina.app.repositories.AuthRepository;
import com.belobina.app.repositories.FurnitureRepository;
import com.belobina.app.repositories.OrderRepository;
import com.belobina.app.utils.Callback;
import com.belobina.app.utils.Constants;
import com.belobina.app.utils.FirebaseHelper;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

import java.util.Collections;

public class OrderPlacementFragment extends Fragment {

    private TextView tvFurnitureName, tvFurniturePrice, tvTotalPrice, tvAssemblyFee;
    private ShapeableImageView ivFurniture;
    private TextInputEditText etQuantity, etAddress;
    private SwitchMaterial switchAssembly;
    private View assemblyFeeRow;
    private MaterialButton btnConfirm;
    private ProgressBar progressBar;

    private Furniture furniture;
    private FurnitureRepository furnitureRepo;
    private final OrderRepository orderRepo = new OrderRepository();
    private final AuthRepository authRepo = new AuthRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_placement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        furnitureRepo = new FurnitureRepository(requireContext());
        tvFurnitureName = view.findViewById(R.id.tv_furniture_name);
        tvFurniturePrice = view.findViewById(R.id.tv_furniture_price);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        tvAssemblyFee = view.findViewById(R.id.tv_assembly_fee);
        ivFurniture = view.findViewById(R.id.iv_furniture_image);
        etQuantity = view.findViewById(R.id.et_quantity);
        etAddress = view.findViewById(R.id.et_address);
        switchAssembly = view.findViewById(R.id.switch_assembly);
        assemblyFeeRow = view.findViewById(R.id.layout_assembly_fee_row);
        btnConfirm = view.findViewById(R.id.btn_confirm_order);
        progressBar = view.findViewById(R.id.progress_bar);

        String furnitureId = getArguments() != null ? getArguments().getString("furnitureId") : null;
        if (furnitureId == null) {
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }

        loadFurniture(furnitureId);
        loadUserAddress();

        switchAssembly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            assemblyFeeRow.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            updateTotal();
        });

        etQuantity.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateTotal(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnConfirm.setOnClickListener(v -> placeOrder());
    }

    private void loadFurniture(String id) {
        progressBar.setVisibility(View.VISIBLE);
        furnitureRepo.getFurnitureById(id, new Callback<Furniture>() {
            @Override
            public void onSuccess(Furniture result) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                furniture = result;
                tvFurnitureName.setText(result.getProdName());
                tvFurniturePrice.setText(String.format("RM %.0f / unit", result.getProdPrice()));
                if (result.getProdAssembleFee() > 0) {
                    tvAssemblyFee.setText(String.format("Assembly fee: RM %.0f per item", result.getProdAssembleFee()));
                } else {
                    tvAssemblyFee.setText("Assembly fee: to be confirmed");
                }
                Glide.with(requireContext())
                        .load(result.getProdPicture())
                        .placeholder(R.drawable.placeholder_bg)
                        .centerCrop()
                        .into(ivFurniture);
                updateTotal();
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void loadUserAddress() {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) return;
        authRepo.getUserData(uid, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                if (!isAdded()) return;
                if (user != null && user.getUserAddress() != null) {
                    etAddress.setText(user.getUserAddress());
                }
            }
            @Override public void onError(String errorMessage) {}
        });
    }

    private void updateTotal() {
        if (furniture == null) return;
        int qty = getQty();
        double total = furniture.getProdPrice() * qty;
        if (switchAssembly.isChecked()) total += furniture.getProdAssembleFee() * qty;
        tvTotalPrice.setText(String.format("Total: RM %.0f", total));
    }

    private int getQty() {
        try {
            String s = etQuantity.getText() != null ? etQuantity.getText().toString() : "1";
            int qty = Integer.parseInt(s);
            return qty > 0 ? qty : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void placeOrder() {
        if (furniture == null) return;
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) return;

        int qty = getQty();
        boolean assembly = switchAssembly.isChecked();
        String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";
        if (address.isEmpty()) {
            Snackbar.make(requireView(), "Please enter delivery address", Snackbar.LENGTH_SHORT).show();
            return;
        }

        double total = furniture.getProdPrice() * qty + (assembly ? furniture.getProdAssembleFee() * qty : 0);

        btnConfirm.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        authRepo.getUserData(uid, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                Order order = new Order();
                order.setCustomerId(uid);
                order.setCustomerName(user != null ? user.getUserFullName() : "");
                order.setFurnitureName(furniture.getProdName());
                order.setFurnitureImageUrl(furniture.getProdPicture());
                order.setQuantity(qty);
                order.setTotalPrice(total);
                order.setAssemblyRequested(assembly);
                order.setAssemblerName(null);
                order.setStatus(Constants.STATUS_PENDING);
                order.setOrderDate(Timestamp.now());
                order.setDeliveryAddress(address);

                orderRepo.placeOrder(order,
                        Collections.singletonList(furniture.getId()),
                        Collections.singletonList(qty),
                        Collections.singletonList(furniture.getProdPrice()),
                        new Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        btnConfirm.setEnabled(true);
                        Toast.makeText(requireContext(), "Your order has been placed!", Toast.LENGTH_SHORT).show();
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Order Placed!")
                                .setMessage(getString(R.string.order_placed))
                                .setPositiveButton("View Orders", (d, w) ->
                                        NavHostFragment.findNavController(OrderPlacementFragment.this)
                                                .navigate(R.id.action_orderPlacementFragment_to_myOrdersFragment))
                                .setCancelable(false)
                                .show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        btnConfirm.setEnabled(true);
                        Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                btnConfirm.setEnabled(true);
                Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
