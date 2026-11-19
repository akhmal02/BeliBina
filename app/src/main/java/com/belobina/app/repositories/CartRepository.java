package com.belobina.app.repositories;

import com.belobina.app.models.CartItem;
import com.belobina.app.utils.Callback;
import com.belobina.app.utils.Constants;
import com.belobina.app.utils.FirebaseHelper;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartRepository {

    private com.google.firebase.firestore.CollectionReference cartCollection(String uid) {
        return FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .collection(Constants.COLLECTION_CART);
    }

    public void addToCart(String uid, CartItem item, Callback<Void> callback) {
        item.setAddedAt(Timestamp.now());
        cartCollection(uid).add(item)
                .addOnSuccessListener(ref -> {
                    item.setCartItemId(ref.getId());
                    ref.update("cartItemId", ref.getId())
                            .addOnSuccessListener(v -> callback.onSuccess(null))
                            .addOnFailureListener(e -> callback.onSuccess(null));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getCartItems(String uid, Callback<List<CartItem>> callback) {
        cartCollection(uid)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        callback.onError(e.getMessage());
                        return;
                    }
                    List<CartItem> list = buildList(snapshots);
                    callback.onSuccess(list);
                });
    }

    public void getCartCount(String uid, Callback<Integer> callback) {
        cartCollection(uid).get()
                .addOnSuccessListener(snapshots -> callback.onSuccess(snapshots.size()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getCartItemByFurnitureId(String uid, String furnitureId, Callback<CartItem> callback) {
        cartCollection(uid)
                .whereEqualTo(Constants.FIELD_FURNITURE_ID, furnitureId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots != null && !snapshots.isEmpty()) {
                        CartItem item = snapshots.getDocuments().get(0).toObject(CartItem.class);
                        if (item != null) item.setCartItemId(snapshots.getDocuments().get(0).getId());
                        callback.onSuccess(item);
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateCartItemQuantity(String uid, String cartItemId, int qty, Callback<Void> callback) {
        cartCollection(uid).document(cartItemId)
                .update(Constants.FIELD_QUANTITY, qty)
                .addOnSuccessListener(v -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void toggleAssembly(String uid, String cartItemId, boolean assemblyRequested, Callback<Void> callback) {
        cartCollection(uid).document(cartItemId)
                .update(Constants.FIELD_ASSEMBLY_REQUESTED, assemblyRequested)
                .addOnSuccessListener(v -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void removeFromCart(String uid, String cartItemId, Callback<Void> callback) {
        cartCollection(uid).document(cartItemId)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void clearCart(String uid, Callback<Void> callback) {
        cartCollection(uid).get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots == null || snapshots.isEmpty()) {
                        callback.onSuccess(null);
                        return;
                    }
                    final int[] remaining = {snapshots.size()};
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                        doc.getReference().delete()
                                .addOnSuccessListener(v -> {
                                    remaining[0]--;
                                    if (remaining[0] == 0) callback.onSuccess(null);
                                })
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private List<CartItem> buildList(QuerySnapshot snapshots) {
        List<CartItem> list = new ArrayList<>();
        if (snapshots != null) {
            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                CartItem item = doc.toObject(CartItem.class);
                if (item != null) {
                    item.setCartItemId(doc.getId());
                    list.add(item);
                }
            }
        }
        return list;
    }
}
