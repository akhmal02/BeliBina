package com.belobina.app.repositories;

import com.belobina.app.models.Assembler;
import com.belobina.app.utils.Callback;
import com.belobina.app.utils.Constants;
import com.belobina.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class AssemblerRepository {

    public void getAllAssemblers(Callback<List<Assembler>> callback) {
        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_ASSEMBLERS)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        callback.onError(e.getMessage());
                        return;
                    }
                    List<Assembler> list = buildList(snapshots);
                    callback.onSuccess(list);
                });
    }

    public void addAssembler(Assembler assembler, Callback<Void> callback) {
        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_ASSEMBLERS)
                .add(assembler)
                .addOnSuccessListener(ref -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateAssembler(Assembler assembler, Callback<Void> callback) {
        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_ASSEMBLERS)
                .document(assembler.getId())
                .set(assembler)
                .addOnSuccessListener(v -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteAssembler(String id, Callback<Void> callback) {
        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_ASSEMBLERS)
                .document(id)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private List<Assembler> buildList(com.google.firebase.firestore.QuerySnapshot snapshots) {
        List<Assembler> list = new ArrayList<>();
        if (snapshots != null) {
            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                Assembler a = doc.toObject(Assembler.class);
                if (a != null) {
                    a.setId(doc.getId());
                    list.add(a);
                }
            }
        }
        return list;
    }
}
