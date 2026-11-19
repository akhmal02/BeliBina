package com.belobina.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.belobina.app.R;
import com.belobina.app.repositories.AuthRepository;
import com.belobina.app.utils.Constants;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private final AuthRepository authRepo = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthState, 2000);
    }

    private void checkAuthState() {
        FirebaseUser user = authRepo.getCurrentUser();
        if (user == null) {
            navigate(OnboardingActivity.class);
        } else {
            authRepo.getUserRole(user.getUid(), new com.belobina.app.utils.Callback<String>() {
                @Override
                public void onSuccess(String role) {
                    if (Constants.ROLE_ADMIN.equals(role)) {
                        navigate(AdminActivity.class);
                    } else {
                        navigate(MainActivity.class);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    navigate(OnboardingActivity.class);
                }
            });
        }
    }

    private void navigate(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
