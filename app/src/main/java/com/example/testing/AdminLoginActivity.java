package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Admin Login screen — authenticates via Firebase Auth then checks
 * if the user has admin role in the database.
 *
 * To create an admin user:
 * 1. Sign up normally with admin@famcart.com
 * 2. In Firebase Console → Realtime Database, add:
 *    admin/allowedEmails/admin@famcart.com = true
 */
public class AdminLoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        emailInput = findViewById(R.id.adminEmailInput);
        passwordInput = findViewById(R.id.adminPasswordInput);
        loginButton = findViewById(R.id.adminLoginButton);
        mAuth = FirebaseAuth.getInstance();

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

        // Authenticate via Firebase Auth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Verify admin role in database
                        verifyAdminRole(email);
                    } else {
                        loginButton.setEnabled(true);
                        loginButton.setText("Sign In");
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Check if the email is listed as an admin in Firebase Database.
     * Path: admin/allowedEmails/{sanitized_email} = true
     */
    private void verifyAdminRole(String email) {
        // Firebase keys can't contain dots, so sanitize email
        String sanitizedEmail = email.replace(".", ",");

        FirebaseDatabase.getInstance()
                .getReference("admin")
                .child("allowedEmails")
                .child(sanitizedEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        Boolean isAdmin = task.getResult().getValue(Boolean.class);
                        if (Boolean.TRUE.equals(isAdmin)) {
                            Toast.makeText(this, "Admin login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AdminLoginActivity.this, AdminOrdersActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            loginButton.setEnabled(true);
                            loginButton.setText("Sign In");
                            Toast.makeText(this, "Not authorized as admin", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                    } else {
                        loginButton.setEnabled(true);
                        loginButton.setText("Sign In");
                        Toast.makeText(this, "Admin access denied", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                });
    }
}
