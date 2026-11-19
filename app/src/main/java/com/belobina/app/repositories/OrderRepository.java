package com.belobina.app.repositories;

import com.belobina.app.models.Order;
import com.belobina.app.utils.Callback;
import com.belobina.app.utils.Constants;
import com.belobina.app.utils.FirebaseHelper;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderRepository {

    public void placeOrder(Order order,
                           List<String> prodIDs,
                           List<Integer> itemQuantities,
                           List<Double> unitPrices,
                           Callback<Void> callback) {
        String currentUserId = FirebaseHelper.getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("No authenticated user.");
            return;
        }
        assignOrderToCurrentUser(order, currentUserId);

        CollectionReference ordersCollection = FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_ORDERS);
        CollectionReference orderItemsCollection = FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_ORDER_ITEM);
        DocumentReference orderRef = ordersCollection.document();
        DocumentReference orderItemRef = orderItemsCollection.document();
        String orderID = orderRef.getId();
        String orderItemID = orderItemRef.getId();

        WriteBatch batch = FirebaseHelper.getDb().batch();
        batch.set(orderRef, buildOrderData(order, orderID));
        batch.set(orderItemRef, buildOrderItemData(orderItemID, orderID, prodIDs, itemQuantities, unitPrices));
        batch.commit()
                .addOnSuccessListener(ref -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void placeOrdersWithReceipt(List<Order> orders,
                                       List<String> prodIDs,
                                       List<Integer> itemQuantities,
                                       List<Double> unitPrices,
                                       Callback<Void> callback) {
        String currentUserId = FirebaseHelper.getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("No authenticated user.");
            return;
        }
        if (orders == null || orders.isEmpty()) {
            callback.onError("No orders to place.");
            return;
        }

        CollectionReference ordersCollection = FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_ORDERS);
        DocumentReference orderItemRef = FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_ORDER_ITEM)
                .document();
        String orderID = ordersCollection.document().getId();
        String orderItemID = orderItemRef.getId();

        WriteBatch batch = FirebaseHelper.getDb().batch();
        for (Order order : orders) {
            assignOrderToCurrentUser(order, currentUserId);
            DocumentReference orderRef = ordersCollection.document();
            batch.set(orderRef, buildOrderData(order, orderID));
        }
        batch.set(orderItemRef, buildOrderItemData(orderItemID, orderID, prodIDs, itemQuantities, unitPrices));
        batch.commit()
                .addOnSuccessListener(ref -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getCurrentCustomerOrders(Callback<List<Order>> callback) {
        String currentUserId = FirebaseHelper.getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("No authenticated user.");
            return;
        }

        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_ORDERS)
                .whereEqualTo(Constants.FIELD_CUSTOMER_ID, currentUserId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        callback.onError(e.getMessage());
                        return;
                    }
                    List<Order> list = buildOrderList(snapshots);
                    sortOrdersByDateDescending(list);
                    callback.onSuccess(list);
                });
    }

    public void getAllOrders(Callback<List<Order>> callback) {
        runIfCurrentUserIsAdmin(callback, () ->
                FirebaseHelper.getDb()
                        .collection(Constants.COLLECTION_ORDERS)
                        .orderBy(Constants.FIELD_ORDER_DATE, Query.Direction.DESCENDING)
                        .addSnapshotListener((snapshots, e) -> {
                            if (e != null) {
                                callback.onError(e.getMessage());
                                return;
                            }
                            List<Order> list = buildOrderList(snapshots);
                            callback.onSuccess(list);
                        }));
    }

    private void sortOrdersByDateDescending(List<Order> orders) {
        Collections.sort(orders, new Comparator<Order>() {
            @Override
            public int compare(Order first, Order second) {
                Timestamp firstDate = first.getOrderDate();
                Timestamp secondDate = second.getOrderDate();
                if (firstDate == null && secondDate == null) return 0;
                if (firstDate == null) return 1;
                if (secondDate == null) return -1;
                return secondDate.compareTo(firstDate);
            }
        });
    }

    public void updateOrderStatus(String orderId, String status, Callback<Void> callback) {
        runIfCurrentUserIsAdmin(callback, () -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put(Constants.FIELD_STATUS, status);
            FirebaseHelper.getDb()
                    .collection(Constants.COLLECTION_ORDERS)
                    .document(orderId)
                    .update(updates)
                    .addOnSuccessListener(v -> callback.onSuccess(null))
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        });
    }

    public void assignAssembler(String orderId, String assemblerId, String assemblerName, Callback<Void> callback) {
        runIfCurrentUserIsAdmin(callback, () -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put(Constants.FIELD_STATUS, Constants.STATUS_ASSIGNED);
            updates.put(Constants.FIELD_ASS_ID, assemblerId);
            updates.put(Constants.FIELD_ASSEMBLER_NAME, assemblerName);
            FirebaseHelper.getDb()
                    .collection(Constants.COLLECTION_ORDERS)
                    .document(orderId)
                    .update(updates)
                    .addOnSuccessListener(v -> callback.onSuccess(null))
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        });
    }

    private void assignOrderToCurrentUser(Order order, String currentUserId) {
        order.setUserID(currentUserId);
        order.setCustomerId(currentUserId);
    }

    private void runIfCurrentUserIsAdmin(Callback<?> callback, Runnable action) {
        String currentUserId = FirebaseHelper.getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("No authenticated user.");
            return;
        }

        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_USERS)
                .document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    String role = doc.getString(Constants.FIELD_ROLE);
                    if (Constants.ROLE_ADMIN.equals(role)) {
                        action.run();
                    } else {
                        callback.onError("Admin access required.");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private List<Order> buildOrderList(com.google.firebase.firestore.QuerySnapshot snapshots) {
        List<Order> list = new ArrayList<>();
        if (snapshots != null) {
            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                Order o = doc.toObject(Order.class);
                if (o != null) {
                    o.setId(doc.getId());
                    applyLegacyOrderFields(doc, o);
                    list.add(o);
                }
            }
        }
        return list;
    }

    private Map<String, Object> buildOrderData(Order order, String orderID) {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put(Constants.FIELD_ORDER_ID, orderID);
        orderData.put(Constants.FIELD_ASS_ID, order.getAssID());
        orderData.put(Constants.FIELD_ORDER_DATE,
                order.getOrderDate() != null ? order.getOrderDate() : Timestamp.now());
        orderData.put(Constants.FIELD_ORDER_AMOUNT, order.getOrderAmount());
        orderData.put(Constants.FIELD_ORDER_ASSEMBLY_REQ, order.isOrderAssemblyReq());
        orderData.put(Constants.FIELD_STATUS,
                order.getOrderStatus() != null ? order.getOrderStatus() : Constants.STATUS_PENDING);
        orderData.put(Constants.FIELD_USER_ID, order.getUserID() != null
                ? order.getUserID()
                : order.getCustomerId());
        orderData.put("customerName", order.getCustomerName());
        orderData.put("furnitureName", order.getFurnitureName());
        orderData.put("furnitureImageUrl", order.getFurnitureImageUrl());
        orderData.put("quantity", order.getQuantity());
        orderData.put("deliveryAddress", order.getDeliveryAddress());
        return orderData;
    }

    private Map<String, Object> buildOrderItemData(String orderItemID,
                                                  String orderID,
                                                  List<String> prodIDs,
                                                  List<Integer> itemQuantities,
                                                  List<Double> unitPrices) {
        Map<String, Object> orderItemData = new HashMap<>();
        orderItemData.put(Constants.FIELD_ORDER_ITEM_ID, orderItemID);
        orderItemData.put(Constants.FIELD_ITEM_QUANTITY, itemQuantities);
        orderItemData.put(Constants.FIELD_UNIT_PRICE, unitPrices);
        orderItemData.put(Constants.FIELD_ORDER_ID, orderID);
        orderItemData.put(Constants.FIELD_PROD_ID, prodIDs);
        return orderItemData;
    }

    private void applyLegacyOrderFields(com.google.firebase.firestore.DocumentSnapshot doc, Order order) {
        if (order.getOrderAmount() == 0) {
            Number legacyTotal = getNumber(doc, "totalPrice");
            if (legacyTotal != null) {
                order.setOrderAmount(legacyTotal.doubleValue());
            }
        }
        if (!order.isOrderAssemblyReq() && doc.contains("assemblyRequested")) {
            Boolean legacyAssembly = doc.getBoolean("assemblyRequested");
            if (legacyAssembly != null) {
                order.setOrderAssemblyReq(legacyAssembly);
            }
        }
        if (order.getOrderStatus() == null && doc.contains("status")) {
            order.setOrderStatus(doc.getString("status"));
        }
    }

    private Number getNumber(com.google.firebase.firestore.DocumentSnapshot doc, String field) {
        Number value = doc.getDouble(field);
        if (value == null) {
            value = doc.getLong(field);
        }
        return value;
    }
}
