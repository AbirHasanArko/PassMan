package com.passman.android.ui.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.passman.android.PassManApp;
import com.passman.android.R;
import com.passman.android.ui.auth.AuthActivity;
import com.passman.android.ui.main.MainActivity;

/**
 * Splash screen activity with animated logo
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1500L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle the splash screen transition
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        
        super.onCreate(savedInstanceState);

        // Keep the splash screen visible while we check auth state
        splashScreen.setKeepOnScreenCondition(() -> false);

        // Delay and navigate
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateToNextScreen();
        }, SPLASH_DELAY);
    }

    private void navigateToNextScreen() {
        PassManApp app = (PassManApp) getApplication();
        
        Intent intent;
        if (app.getSessionManager().isLoggedIn()) {
            // Already logged in, go to main
            intent = new Intent(this, MainActivity.class);
        } else {
            // Need to authenticate
            intent = new Intent(this, AuthActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        // Apply custom animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
