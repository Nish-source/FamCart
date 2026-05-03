package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;

/**
 * Admin Login screen — validates hardcoded admin credentials
 * and navigates to the Admin Orders panel on success.
 *
 * Hardcoded credentials (for development):
 *   Email:    admin@famcart.com
 *   Password: admin123
 */
public class AdminLoginActivity extends AppCompatActivity {

    // Hardcoded admin credentials — swap for Firebase-based auth if needed
    private static final String ADMIN_EMAIL = "admin@famcart.com";
    private static final String ADMIN_PASSWORD = "admin123";

    private EditText emailInput, passwordInput;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        emailInput = findViewById(R.id.adminEmailInput);
        passwordInput = findViewById(R.id.adminPasswordInput);
        loginButton = findViewById(R.id.adminLoginButton);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        // Basic validation
        if (email.isEmpty()) {
            emailInput.setError("Enter admin email");
            emailInput.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Enter password");
            passwordInput.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            emailInput.requestFocus();
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");

        // Simple hardcoded credential check
        if (email.equalsIgnoreCase(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD)) {
            Toast.makeText(this, "Admin login successful", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(AdminLoginActivity.this, AdminOrdersActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            loginButton.setEnabled(true);
            loginButton.setText("Sign In");
            Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
