package com.example.famcart;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;




public class LoginActivity extends AppCompatActivity {

    EditText phoneInput, passwordInput;
    Button loginButton;
    TextView signupText;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signupText = findViewById(R.id.signupText);
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        }

        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phone = phoneInput.getText().toString();
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

                    String email = phone + "@famcart.com";

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {

                                if (task.isSuccessful()) {

                                    Toast.makeText(LoginActivity.this,
                                            "Login Successful",
                                            Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);

                                    finish();

                                }


                                else {

                                    Toast.makeText(LoginActivity.this,
                                            "First create your account",
                                            Toast.LENGTH_LONG).show();

                                }

                            });

                    signupText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                            startActivity(intent);

                        }

                            });

                }

            }
        });

    }
}








