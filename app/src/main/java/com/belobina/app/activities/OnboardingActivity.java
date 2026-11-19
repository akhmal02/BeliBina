package com.belobina.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.belobina.app.R;
import com.belobina.app.adapters.OnboardingPagerAdapter;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    private static final int PAGE_COUNT = 3;
    private static final long AUTO_SCROLL_DELAY_MS = 3000;

    private ViewPager2 viewPager;
    private LinearLayout dotContainer;
    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            int next = (viewPager.getCurrentItem() + 1) % PAGE_COUNT;
            viewPager.setCurrentItem(next, true);
            autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.view_pager_onboarding);
        dotContainer = findViewById(R.id.dot_indicator);
        MaterialButton btnGetStarted = findViewById(R.id.btn_get_started);
        MaterialButton btnLogin = findViewById(R.id.btn_already_login);

        viewPager.setAdapter(new OnboardingPagerAdapter(this));
        setupDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
            }
        });

        // Pause auto-scroll while user is touching, resume after they lift finger
        viewPager.getChildAt(0).setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    stopAutoScroll();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    startAutoScroll();
                    break;
            }
            return false;
        });

        btnGetStarted.setOnClickListener(v -> goToLogin());
        btnLogin.setOnClickListener(v -> goToLogin());
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAutoScroll();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoScroll();
    }

    private void startAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY_MS);
    }

    private void stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    private void setupDots(int active) {
        dotContainer.removeAllViews();
        for (int i = 0; i < PAGE_COUNT; i++) {
            View dot = new View(this);
            int size = getResources().getDimensionPixelSize(
                    i == active ? R.dimen.dot_active_size : R.dimen.dot_inactive_size);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == active
                    ? R.drawable.dot_active
                    : R.drawable.dot_inactive);
            dotContainer.addView(dot);
        }
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
