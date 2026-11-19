package com.belobina.app.repositories;

import android.content.Context;

import com.belobina.app.models.Furniture;
import com.belobina.app.utils.Callback;
import com.belobina.app.utils.Constants;
import com.belobina.app.utils.FirebaseHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FurnitureRepository {
    private static final String SEED_FURNITURE_FILE = "furniture.json";

    private final Context appContext;

    public FurnitureRepository() {
        this(null);
    }

    public FurnitureRepository(Context context) {
        appContext = context != null ? context.getApplicationContext() : null;
    }

    public void getAllFurniture(Callback<List<Furniture>> callback) {
        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_FURNITURE)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        List<Furniture> localFurniture = loadSeedFurniture();
                        if (!localFurniture.isEmpty()) {
                            callback.onSuccess(localFurniture);
                        } else {
                            callback.onError(e.getMessage());
                        }
                        return;
                    }
                    List<Furniture> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            list.add(mapFurniture(doc));
                        }
                    }
                    callback.onSuccess(list.isEmpty() ? loadSeedFurniture() : list);
                });
    }

    private List<Furniture> loadSeedFurniture() {
        List<Furniture> list = new ArrayList<>();
        if (appContext == null) return list;

        try (InputStream inputStream = appContext.getAssets().open(SEED_FURNITURE_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            JSONArray items = new JSONArray(json.toString());
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                Furniture furniture = new Furniture();
                furniture.setId(item.optString("id", "seed-" + i));
                furniture.setProdName(item.optString("prodName", ""));
                furniture.setProdDesc(item.optString("prodDesc", ""));
                furniture.setProdPrice(item.optDouble("prodPrice", 0));
                furniture.setProdAssembleFee(item.optDouble("prodAssembleFee", 0));
                furniture.setProdPicture(item.optString("prodPicture", null));
                furniture.setProdCat(item.optString("prodCat", ""));
                furniture.setProdStock(item.optInt("prodStock", 0));
                list.add(furniture);
            }
        } catch (Exception ignored) {
            list.clear();
        }
        return list;
    }

    public void addFurniture(Furniture furniture, Callback<Void> callback) {
        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_FURNITURE)
                .add(furniture)
                .addOnSuccessListener(ref -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateFurniture(Furniture furniture, Callback<Void> callback) {
        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_FURNITURE)
                .document(furniture.getId())
                .set(furniture)
                .addOnSuccessListener(v -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteFurniture(String id, Callback<Void> callback) {
        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_FURNITURE)
                .document(id)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getFurnitureById(String id, Callback<Furniture> callback) {
        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_FURNITURE)
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onSuccess(mapFurniture(doc));
                    } else {
                        Furniture seedFurniture = findSeedFurnitureById(id);
                        if (seedFurniture != null) {
                            callback.onSuccess(seedFurniture);
                        } else {
                            callback.onError("Furniture not found");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Furniture seedFurniture = findSeedFurnitureById(id);
                    if (seedFurniture != null) {
                        callback.onSuccess(seedFurniture);
                    } else {
                        callback.onError(e.getMessage());
                    }
                });
    }

    private Furniture mapFurniture(com.google.firebase.firestore.DocumentSnapshot doc) {
        Furniture furniture = new Furniture();
        furniture.setId(doc.getId());
        furniture.setProdName(doc.getString("prodName"));
        furniture.setProdDesc(doc.getString("prodDesc"));
        furniture.setProdPrice(getDouble(doc, "prodPrice"));
        furniture.setProdAssembleFee(getDouble(doc, "prodAssembleFee"));
        furniture.setProdCat(doc.getString("prodCat"));
        furniture.setProdPicture(doc.getString("prodPicture"));
        furniture.setProdStock(getInt(doc, "prodStock"));
        return furniture;
    }

    private double getDouble(com.google.firebase.firestore.DocumentSnapshot doc, String field) {
        Number value = doc.getDouble(field);
        if (value == null) {
            value = doc.getLong(field);
        }
        return value != null ? value.doubleValue() : 0;
    }

    private int getInt(com.google.firebase.firestore.DocumentSnapshot doc, String field) {
        Number value = doc.getLong(field);
        if (value == null) {
            value = doc.getDouble(field);
        }
        return value != null ? value.intValue() : 0;
    }

    private Furniture findSeedFurnitureById(String id) {
        for (Furniture furniture : loadSeedFurniture()) {
            if (id != null && id.equals(furniture.getId())) {
                return furniture;
            }
        }
        return null;
    }
}
