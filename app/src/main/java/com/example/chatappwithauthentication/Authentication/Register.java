package com.example.chatappwithauthentication.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatappwithauthentication.MainActivity;
import com.example.chatappwithauthentication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {
    EditText name,email,contact, password, confirmPassword;
    Button registerBtn;
    TextView  loginText;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = findViewById(R.id.name);
        contact = findViewById(R.id.contact);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        registerBtn = findViewById(R.id.registerButton);
        loginText = findViewById(R.id.loginText);

        auth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password.getText().toString().isEmpty()){
                    Toast.makeText(Register.this, "Enter password to Register", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(! password.getText().toString().equals(confirmPassword.getText().toString())){
                    Toast.makeText(Register.this, "Password not matched", Toast.LENGTH_SHORT).show();
                    return;
                }
                RegisterUser();

            }
        });
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });
    }

    private void RegisterUser() {
        auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(Register.this, "Failed to Register! ", Toast.LENGTH_SHORT).show();
                            Log.i("Exception and Result",""+task.getException()+"\n"+task.getResult());
                        }
                        else {
                            //Save name and phone number with email in the DB if registered (task.isSuccessful())
                            startActivity(new Intent(Register.this, MainActivity.class));

                        }
                    }
                });
    }
}