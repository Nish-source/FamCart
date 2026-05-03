package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText phoneInput, passwordInput;
    Button loginButton;
    TextView signupText, adminLoginText;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signupText = findViewById(R.id.signupText);
        adminLoginText = findViewById(R.id.adminLoginText);
        mAuth = FirebaseAuth.getInstance();

        // Auto-navigate if already logged in
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        signupText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Admin login entry point — opens separate admin flow
        adminLoginText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT).show();
            } else if (!phone.matches("[0-9]{10}")) {
                Toast.makeText(LoginActivity.this,
                        "Please enter valid phone number",
                        Toast.LENGTH_SHORT).show();
            } else {
                loginButton.setEnabled(false);
                loginButton.setText("Logging in...");

                String email = phone + "@famcart.com";

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this,
                                        "Login Successful",
                                        Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                loginButton.setEnabled(true);
                                loginButton.setText("Login");

                                Toast.makeText(LoginActivity.this,
                                        "First create your account",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
}
