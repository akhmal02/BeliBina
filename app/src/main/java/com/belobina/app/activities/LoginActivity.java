package com.belobina.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.belobina.app.R;
import com.belobina.app.models.User;
import com.belobina.app.repositories.AuthRepository;
import com.belobina.app.utils.Callback;
import com.belobina.app.utils.Constants;
import com.belobina.app.utils.FirebaseHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private final AuthRepository authRepo = new AuthRepository();
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    this::handleGoogleSignInResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.tv_forgot_password).setOnClickListener(v ->
                Snackbar.make(v, "Password reset not yet implemented", Snackbar.LENGTH_SHORT).show());

        findViewById(R.id.btn_register).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        findViewById(R.id.btn_google_sign_in).setOnClickListener(v -> launchGoogleSignIn());

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void launchGoogleSignIn() {
        // Sign out silently first so the account picker always appears
        googleSignInClient.signOut().addOnCompleteListener(this, task ->
                googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));
    }

    private void handleGoogleSignInResult(ActivityResult result) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            String idToken = account.getIdToken();
            if (idToken == null) {
                Toast.makeText(this, "Google sign-in failed: missing token.", Toast.LENGTH_LONG).show();
                return;
            }
            setLoading(true);
            authRepo.signInWithGoogle(idToken, new Callback<String>() {
                @Override
                public void onSuccess(String role) {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, "Signed in with Google!", Toast.LENGTH_SHORT).show();
                    navigateByRole(role);
                }
                @Override
                public void onError(String errorMessage) {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, "Google sign-in failed: " + errorMessage,
                            Toast.LENGTH_LONG).show();
                }
            });
        } catch (ApiException e) {
            Toast.makeText(this, "Google sign-in cancelled or failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        tilEmail.setError(null);
        tilPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_required_field));
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email_invalid));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_required_field));
            return;
        }

        setLoading(true);
        authRepo.login(email, password, new Callback<String>() {
            @Override
            public void onSuccess(String role) {
                setLoading(false);
                String uid = FirebaseHelper.getCurrentUserId();
                authRepo.getUserData(uid, new Callback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        String name = user != null && user.getUserFullName() != null ? user.getUserFullName() : "User";
                        Toast.makeText(LoginActivity.this, "Welcome back, " + name + "!",
                                Toast.LENGTH_SHORT).show();
                        navigateByRole(role);
                    }
                    @Override
                    public void onError(String e) {
                        Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                        navigateByRole(role);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                String msg = errorMessage != null ? errorMessage : "Login failed";
                Toast.makeText(LoginActivity.this, "Login failed: " + msg, Toast.LENGTH_LONG).show();
                Snackbar.make(btnLogin, msg, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void navigateByRole(String role) {
        Intent intent;
        if (Constants.ROLE_ADMIN.equals(role)) {
            intent = new Intent(LoginActivity.this, AdminActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }
}
