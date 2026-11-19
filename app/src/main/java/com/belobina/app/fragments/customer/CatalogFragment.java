package com.belobina.app.fragments.customer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.belobina.app.R;
import com.belobina.app.activities.MainActivity;
import com.belobina.app.adapters.CarouselAdapter;
import com.belobina.app.adapters.FurnitureAdapter;
import com.belobina.app.models.CartItem;
import com.belobina.app.models.Furniture;
import com.belobina.app.models.User;
import com.belobina.app.repositories.AuthRepository;
import com.belobina.app.repositories.CartRepository;
import com.belobina.app.repositories.FurnitureRepository;
import com.belobina.app.utils.Callback;
import com.belobina.app.utils.FirebaseHelper;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CatalogFragment extends Fragment {

    private static final List<String> DEFAULT_CAROUSEL_IMAGES = Arrays.asList(
            "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1517705008128-361805f42e86?auto=format&fit=crop&w=1200&q=80"
    );
    private static final List<String> DEFAULT_CAROUSEL_NAMES = Arrays.asList(
            "Living Room Furniture",
            "Bedroom Essentials",
            "Storage and Tables"
    );
    private static final int CAROUSEL_INTERVAL_MS = 3000;

    private ViewPager2 vpCarousel;
    private final Handler carouselHandler = new Handler(Looper.getMainLooper());
    private Runnable carouselRunnable;

    private FurnitureAdapter adapter;
    private ChipGroup chipGroup;
    private TextInputEditText etSearch;
    private ProgressBar progressBar;
    private TextView tvGreetingLabel, tvUserName;
    private FurnitureRepository furnitureRepo;
    private final AuthRepository authRepo = new AuthRepository();
    private final CartRepository cartRepo = new CartRepository();
    private String currentCategory = "All";
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catalog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        furnitureRepo = new FurnitureRepository(requireContext());
        tvGreetingLabel = view.findViewById(R.id.tv_greeting_label);
        tvUserName = view.findViewById(R.id.tv_user_name);
        vpCarousel = view.findViewById(R.id.vp_carousel);
        chipGroup = view.findViewById(R.id.chip_group_categories);
        etSearch = view.findViewById(R.id.et_search);
        progressBar = view.findViewById(R.id.progress_bar);
        RecyclerView recyclerView = view.findViewById(R.id.rv_furniture);

        setTimeGreeting();
        loadUserName();
        setupCarousel(DEFAULT_CAROUSEL_IMAGES, DEFAULT_CAROUSEL_NAMES);

        adapter = new FurnitureAdapter(requireContext(),
                this::openOrderPlacement,
                furniture -> addToCart(furniture));
        adapter.setProductClickListener(this::showProductDetailDialog);

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString();
                adapter.filter(currentQuery, currentCategory);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        View headerCard = view.findViewById(R.id.card_greeting);
        Animation headerAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_slide_up);
        headerCard.startAnimation(headerAnim);

        loadFurniture();
    }

    private void openOrderPlacement(Furniture furniture) {
        Bundle args = new Bundle();
        args.putString("furnitureId", furniture.getId());
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_catalogFragment_to_orderPlacementFragment, args);
    }

    private void showProductDetailDialog(Furniture furniture) {
        if (!isAdded()) return;

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_product_detail, null, false);

        ImageView productImage = dialogView.findViewById(R.id.iv_product_detail_image);
        TextView tvName = dialogView.findViewById(R.id.tv_product_detail_name);
        TextView tvPrice = dialogView.findViewById(R.id.tv_product_detail_price);
        TextView tvDesc = dialogView.findViewById(R.id.tv_product_detail_desc);
        TextView tvCategory = dialogView.findViewById(R.id.tv_product_detail_category);
        TextView tvStock = dialogView.findViewById(R.id.tv_product_detail_stock);
        TextView tvAssemblyFee = dialogView.findViewById(R.id.tv_product_detail_assembly_fee);
        TextView tvQuantity = dialogView.findViewById(R.id.tv_product_detail_quantity);
        MaterialButton btnQtyMinus = dialogView.findViewById(R.id.btn_product_detail_qty_minus);
        MaterialButton btnQtyPlus = dialogView.findViewById(R.id.btn_product_detail_qty_plus);
        MaterialButton btnAddToCart = dialogView.findViewById(R.id.btn_product_detail_add_to_cart);
        MaterialButton btnOrder = dialogView.findViewById(R.id.btn_product_detail_order);

        tvName.setText(nonEmpty(furniture.getProdName(), "Untitled product"));
        tvPrice.setText(String.format("RM %.0f", furniture.getProdPrice()));
        tvDesc.setText(nonEmpty(furniture.getProdDesc(), "No description available."));
        tvCategory.setText("Category: " + nonEmpty(furniture.getProdCat(), "Uncategorized"));
        tvStock.setText("Stock: " + furniture.getProdStock() + " available");
        tvAssemblyFee.setText(String.format("Assembly fee: RM %.0f", furniture.getProdAssembleFee()));

        Glide.with(this)
                .load(furniture.getProdPicture())
                .placeholder(R.drawable.placeholder_bg)
                .error(R.drawable.placeholder_bg)
                .centerCrop()
                .into(productImage);

        int maxQuantity = furniture.getProdStock() > 0 ? furniture.getProdStock() : 1;
        final int[] selectedQuantity = {1};
        btnQtyMinus.setEnabled(false);
        btnQtyMinus.setOnClickListener(v -> {
            if (selectedQuantity[0] > 1) {
                selectedQuantity[0]--;
                tvQuantity.setText(String.valueOf(selectedQuantity[0]));
                btnQtyMinus.setEnabled(selectedQuantity[0] > 1);
                btnQtyPlus.setEnabled(selectedQuantity[0] < maxQuantity);
            }
        });
        btnQtyPlus.setOnClickListener(v -> {
            if (selectedQuantity[0] < maxQuantity) {
                selectedQuantity[0]++;
                tvQuantity.setText(String.valueOf(selectedQuantity[0]));
                btnQtyMinus.setEnabled(true);
                btnQtyPlus.setEnabled(selectedQuantity[0] < maxQuantity);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnAddToCart.setOnClickListener(v -> {
            addToCart(furniture, selectedQuantity[0]);
            dialog.dismiss();
        });
        btnOrder.setOnClickListener(v -> {
            dialog.dismiss();
            openOrderPlacement(furniture);
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private String nonEmpty(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value : fallback;
    }

    private void setupCarousel(List<String> imageUrls, List<String> names) {
        if (imageUrls == null || imageUrls.isEmpty() || names == null || names.isEmpty()) {
            imageUrls = DEFAULT_CAROUSEL_IMAGES;
            names = DEFAULT_CAROUSEL_NAMES;
        }
        carouselHandler.removeCallbacks(carouselRunnable);
        int startPosition = Integer.MAX_VALUE / 2;
        startPosition = startPosition - (startPosition % imageUrls.size());
        vpCarousel.setAdapter(new CarouselAdapter(imageUrls, names));
        vpCarousel.setCurrentItem(startPosition, false);

        final int[] currentPage = {startPosition};
        carouselRunnable = () -> {
            currentPage[0]++;
            vpCarousel.setCurrentItem(currentPage[0], true);
            carouselHandler.postDelayed(carouselRunnable, CAROUSEL_INTERVAL_MS);
        };
        carouselHandler.postDelayed(carouselRunnable, CAROUSEL_INTERVAL_MS);
    }

    @Override
    public void onPause() {
        super.onPause();
        carouselHandler.removeCallbacks(carouselRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (carouselRunnable != null) {
            carouselHandler.postDelayed(carouselRunnable, CAROUSEL_INTERVAL_MS);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        carouselHandler.removeCallbacks(carouselRunnable);
    }

    private void addToCart(Furniture furniture) {
        addToCart(furniture, 1);
    }

    private void addToCart(Furniture furniture, int quantityToAdd) {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) return;
        int safeQuantityToAdd = Math.max(1, quantityToAdd);

        cartRepo.getCartItemByFurnitureId(uid, furniture.getId(), new Callback<CartItem>() {
            @Override
            public void onSuccess(CartItem existingItem) {
                if (!isAdded()) return;
                if (existingItem != null) {
                    int newQty = existingItem.getQuantity() + safeQuantityToAdd;
                    cartRepo.updateCartItemQuantity(uid, existingItem.getCartItemId(), newQty,
                            new Callback<Void>() {
                                @Override public void onSuccess(Void r) {
                                    if (!isAdded()) return;
                                    Toast.makeText(requireContext(),
                                            furniture.getProdName() + " added to cart.", Toast.LENGTH_SHORT).show();
                                    refreshBadge();
                                }
                                @Override public void onError(String e) {}
                            });
                } else {
                    CartItem item = new CartItem();
                    item.setFurnitureId(furniture.getId());
                    item.setFurnitureName(furniture.getProdName());
                    item.setFurnitureImageUrl(furniture.getProdPicture());
                    item.setPricePerUnit(furniture.getProdPrice());
                    item.setAssemblyFeePerUnit(furniture.getProdAssembleFee());
                    item.setQuantity(safeQuantityToAdd);
                    item.setAssemblyRequested(false);

                    cartRepo.addToCart(uid, item, new Callback<Void>() {
                        @Override public void onSuccess(Void r) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(),
                                    furniture.getProdName() + " added to cart.", Toast.LENGTH_SHORT).show();
                            refreshBadge();
                        }
                        @Override public void onError(String e) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), e, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void refreshBadge() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateCartBadge();
        }
    }

    private void setTimeGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Good morning," : hour < 17 ? "Good afternoon," : "Good evening,";
        tvGreetingLabel.setText(greeting);
    }

    private void loadUserName() {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) return;
        authRepo.getUserData(uid, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                if (!isAdded() || user == null) return;
                String name = user.getUserFullName();
                tvUserName.setText((name != null && !name.isEmpty()) ? name + "!" : "User!");
            }
            @Override
            public void onError(String errorMessage) {}
        });
    }

    private void loadFurniture() {
        progressBar.setVisibility(View.VISIBLE);
        furnitureRepo.getAllFurniture(new Callback<List<Furniture>>() {
            @Override
            public void onSuccess(List<Furniture> items) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                adapter.setData(items);
                setupCarousel(buildCarouselImages(items), buildCarouselNames(items));
                populateCategories(items);
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private List<String> buildCarouselImages(List<Furniture> items) {
        List<String> images = new ArrayList<>();
        for (Furniture item : items) {
            String imageUrl = item.getProdPicture();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                images.add(imageUrl);
            }
            if (images.size() == 3) break;
        }
        return images.isEmpty() ? DEFAULT_CAROUSEL_IMAGES : images;
    }

    private List<String> buildCarouselNames(List<Furniture> items) {
        List<String> names = new ArrayList<>();
        for (Furniture item : items) {
            String productName = item.getProdName();
            String imageUrl = item.getProdPicture();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                names.add(productName != null && !productName.trim().isEmpty()
                        ? productName
                        : "Featured Furniture");
            }
            if (names.size() == 3) break;
        }
        return names.isEmpty() ? DEFAULT_CAROUSEL_NAMES : names;
    }

    private void populateCategories(List<Furniture> items) {
        Set<String> categories = new LinkedHashSet<>();
        categories.add("All");
        for (Furniture f : items) {
            if (f.getProdCat() != null) categories.add(f.getProdCat());
        }

        chipGroup.removeAllViews();
        for (String cat : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setChecked(cat.equals(currentCategory));
            chip.setChipBackgroundColorResource(R.color.chip_selector);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    currentCategory = cat;
                    adapter.filter(currentQuery, currentCategory);
                }
            });
            chipGroup.addView(chip);
        }
    }
}
