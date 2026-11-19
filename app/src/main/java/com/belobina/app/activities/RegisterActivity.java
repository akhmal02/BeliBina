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
import com.belobina.app.repositories.AuthRepository;
import com.belobina.app.utils.Callback;
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

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword,
            tilPhone, tilAddress;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword,
            etPhone, etAddress;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private final AuthRepository authRepo = new AuthRepository();
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    this::handleGoogleSignInResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tilName = findViewById(R.id.til_name);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        tilPhone = findViewById(R.id.til_phone);
        tilAddress = findViewById(R.id.til_address);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress_bar);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.btn_login).setOnClickListener(v -> finish());
        findViewById(R.id.btn_google_sign_in).setOnClickListener(v -> launchGoogleSignIn());
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void launchGoogleSignIn() {
        googleSignInClient.signOut().addOnCompleteListener(this, task ->
                googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));
    }

    private void handleGoogleSignInResult(ActivityResult result) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            String idToken = account.getIdToken();
            if (idToken == null) {
                Toast.makeText(this, "Google sign-up failed: missing token.", Toast.LENGTH_LONG).show();
                return;
            }
            setLoading(true);
            authRepo.signInWithGoogle(idToken, new Callback<String>() {
                @Override
                public void onSuccess(String role) {
                    setLoading(false);
                    Toast.makeText(RegisterActivity.this,
                            "Account created with Google! Welcome to BeliBina.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onError(String errorMessage) {
                    setLoading(false);
                    Toast.makeText(RegisterActivity.this, "Google sign-up failed: " + errorMessage,
                            Toast.LENGTH_LONG).show();
                }
            });
        } catch (ApiException e) {
            Toast.makeText(this, "Google sign-up cancelled or failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void attemptRegister() {
        String name = getText(etName);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);
        String phone = getText(etPhone);
        String address = getText(etAddress);

        clearErrors();

        if (TextUtils.isEmpty(name)) { tilName.setError(getString(R.string.error_required_field)); return; }
        if (TextUtils.isEmpty(email)) { tilEmail.setError(getString(R.string.error_required_field)); return; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email_invalid)); return;
        }
        if (TextUtils.isEmpty(password)) { tilPassword.setError(getString(R.string.error_required_field)); return; }
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_passwords_no_match)); return;
        }
        if (TextUtils.isEmpty(phone)) { tilPhone.setError(getString(R.string.error_required_field)); return; }
        if (TextUtils.isEmpty(address)) { tilAddress.setError(getString(R.string.error_required_field)); return; }

        setLoading(true);
        authRepo.register(email, password, name, phone, address, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Account created! Welcome to BeliBina.",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                String msg = errorMessage != null ? errorMessage : "Registration failed";
                Toast.makeText(RegisterActivity.this, "Registration failed: " + msg,
                        Toast.LENGTH_LONG).show();
                Snackbar.make(btnRegister, msg, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void clearErrors() {
        tilName.setError(null); tilEmail.setError(null); tilPassword.setError(null);
        tilConfirmPassword.setError(null); tilPhone.setError(null); tilAddress.setError(null);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
    }
}
