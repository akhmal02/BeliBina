package com.belobina.app.repositories;

import com.belobina.app.models.User;
import com.belobina.app.utils.Callback;
import com.belobina.app.utils.Constants;
import com.belobina.app.utils.FirebaseHelper;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthRepository {

    public void login(String email, String password, Callback<String> callback) {
        FirebaseHelper.getAuth().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    getUserRole(uid, callback);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void register(String email, String password, String name,
                         String phone, String address, Callback<Void> callback) {
        FirebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    User user = new User(uid, name, email, phone, address, Constants.ROLE_CUSTOMER);
                    FirebaseHelper.getDb()
                            .collection(Constants.COLLECTION_USERS)
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Sign in or register with a Google ID token.
     * If the user is new (no Firestore doc), creates a customer record with Google profile data.
     */
    public void signInWithGoogle(String idToken, Callback<String> callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseHelper.getAuth().signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        callback.onError("Google sign-in failed.");
                        return;
                    }
                    String uid = firebaseUser.getUid();
                    FirebaseHelper.getDb()
                            .collection(Constants.COLLECTION_USERS)
                            .document(uid)
                            .get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    String role = doc.getString(Constants.FIELD_ROLE);
                                    callback.onSuccess(role != null ? role : Constants.ROLE_CUSTOMER);
                                } else {
                                    // New Google user — create a Firestore record
                                    String name = firebaseUser.getDisplayName() != null
                                            ? firebaseUser.getDisplayName() : "";
                                    String email = firebaseUser.getEmail() != null
                                            ? firebaseUser.getEmail() : "";
                                    User user = new User(uid, name, email, "", "", Constants.ROLE_CUSTOMER);
                                    FirebaseHelper.getDb()
                                            .collection(Constants.COLLECTION_USERS)
                                            .document(uid)
                                            .set(user)
                                            .addOnSuccessListener(v -> callback.onSuccess(Constants.ROLE_CUSTOMER))
                                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                                }
                            })
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Re-authenticates an email/password user, then updates their password.
     */
    public void updatePassword(String currentPassword, String newPassword, Callback<Void> callback) {
        FirebaseUser user = FirebaseHelper.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            callback.onError("No authenticated user.");
            return;
        }
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential)
                .addOnSuccessListener(unused ->
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(v -> callback.onSuccess(null))
                                .addOnFailureListener(e -> callback.onError(e.getMessage())))
                .addOnFailureListener(e -> callback.onError("Current password is incorrect."));
    }

    public void getUserRole(String uid, Callback<String> callback) {
        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString(Constants.FIELD_ROLE);
                        callback.onSuccess(role != null ? role : Constants.ROLE_CUSTOMER);
                    } else {
                        callback.onSuccess(Constants.ROLE_CUSTOMER);
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getUserData(String uid, Callback<User> callback) {
        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) user.setUid(doc.getId());
                        callback.onSuccess(user);
                    } else {
                        callback.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateUserData(String uid, User user, Callback<Void> callback) {
        FirebaseHelper.getDb()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void signOut() {
        FirebaseHelper.getAuth().signOut();
    }

    public FirebaseUser getCurrentUser() {
        return FirebaseHelper.getCurrentUser();
    }
}
