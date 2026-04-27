package com.example.famcart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

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

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = nameInput.getText().toString();
                String phone = phoneInput.getText().toString();
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

                    // Convert phone to fake email
                    String email = phone + "@famcart.com";

                    // Firebase signup
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {

                                if(task.isSuccessful()){

                                    Toast.makeText(SignupActivity.this,
                                            "Account Created Successfully",
                                            Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                    startActivity(intent);

                                    finish();

                                }

                                else {

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