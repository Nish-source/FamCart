package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    EditText nameInput, phoneInput, passwordInput;
    Button signupButton;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        signupButton = findViewById(R.id.signupButton);

        mAuth = FirebaseAuth.getInstance();

        // Login link
        TextView loginText = findViewById(R.id.loginText);
        if (loginText != null) {
            loginText.setOnClickListener(v -> {
                finish(); // Go back to LoginActivity
            });
        }

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = nameInput.getText().toString().trim();
                String phone = phoneInput.getText().toString().trim();
                String password = passwordInput.getText().toString();

                if(name.isEmpty() || phone.isEmpty() || password.isEmpty()){

                    Toast.makeText(SignupActivity.this,
                            "Please fill all fields",
                            Toast.LENGTH_SHORT).show();

                }
                else if(!phone.matches("[0-9]{10}")){

                    Toast.makeText(SignupActivity.this,
                            "Please enter valid phone number",
                            Toast.LENGTH_SHORT).show();

                }
                else if(password.length() < 6){

                    Toast.makeText(SignupActivity.this,
                            "Password must be at least 6 characters",
                            Toast.LENGTH_SHORT).show();

                }
                else{

                    signupButton.setEnabled(false);
                    signupButton.setText("Creating Account...");

                    String email = phone + "@famcart.com";

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {

                                if(task.isSuccessful()){

                                    // Save user name and phone to Firebase Database
                                    String userId = mAuth.getCurrentUser().getUid();
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("name", name);
                                    userData.put("phone", phone);
                                    userData.put("email", email);

                                    FirebaseDatabase.getInstance()
                                            .getReference("users")
                                            .child(userId)
                                            .updateChildren(userData)
                                            .addOnCompleteListener(dbTask -> {
                                                Toast.makeText(SignupActivity.this,
                                                        "Account Created Successfully",
                                                        Toast.LENGTH_SHORT).show();

                                                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            });

                                }
                                else {
                                    signupButton.setEnabled(true);
                                    signupButton.setText("Create Account");

                                    Toast.makeText(SignupActivity.this,
                                            "Account already exists",
                                            Toast.LENGTH_SHORT).show();

                                }

                            });

                }

            }
        });

    }
}