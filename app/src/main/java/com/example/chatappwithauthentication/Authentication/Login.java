package com.example.chatappwithauthentication.Authentication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatappwithauthentication.MainActivity;
import com.example.chatappwithauthentication.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends AppCompatActivity {
    EditText email, password;
    Button loginBtn;
    TextView forgotPassword, registerText;
    FirebaseAuth auth;

    // google sign in
    private SignInButton googleSignIn;
    private GoogleSignInClient googleSignInClient;
    private String TAG ="Login";
    private int ResultCode_SignIn = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        registerText = findViewById(R.id.registerText);

        auth = FirebaseAuth.getInstance();
        googleSignIn = findViewById(R.id.googleSignIn);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
                startActivity(new Intent(Login.this,MainActivity.class));
            }
        });


        /** if user already login n baar baar show nhi hona chahiye the login page then **/
        if (auth.getCurrentUser() != null){
            startActivity(new Intent(Login.this, MainActivity.class));
            Log.i("Get User",""+auth.getCurrentUser());
        }


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(email.getText().toString().trim().isEmpty() ){
                    email.setError("Enter valid email");
                    return;
                }if(password.getText().toString().isEmpty()){
                    password.setError("Enter Correct Password");
                    return;
                }
                LoginUser();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Login.this, "forgot password", Toast.LENGTH_SHORT).show();
                EditText resetEmail = new EditText(v.getContext());
//                resetEmail.setPadding(20,0,20,4);
                resetEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                resetEmail.setHint("Email ID");
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext())
                        .setTitle("Reset Password")
                        .setIcon(R.drawable.ic_password)
                        .setMessage("Enter your email to set new Password:")
                        .setView(resetEmail)
                        .setPositiveButton("Yes, reset", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                auth.sendPasswordResetEmail(resetEmail.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(Login.this, " Reset link sent to your email", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull  Exception e) {
                                        Toast.makeText(Login.this, " Error! Reset link not sent"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                passwordResetDialog.
                            }
                        });
                        passwordResetDialog.create().show();

            }
        });
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

    }



    private void LoginUser() {
        auth.signInWithEmailAndPassword(email.getText().toString() , password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull  Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Log.i("Exception and Result",""+task.getException()+"\n");
//                            +task.getResult()    ------>>> error
                            Toast.makeText(Login.this, "Login Failed, try to login with appropriate ID and password", Toast.LENGTH_SHORT).show();
                        }else {
                            //matches with the database and than logged in
                            startActivity(new Intent(Login.this, MainActivity.class));
                        }
                    }
                });
    }
    private void signIn() {
        startActivityForResult(googleSignInClient.getSignInIntent(), ResultCode_SignIn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ResultCode_SignIn){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Toast.makeText(this, "successfully signed in", Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuthentication(account);
        }catch (ApiException e){
//            https://www.youtube.com/watch?v=E1eqRNTZqDM
            Toast.makeText(this, "signin failed!", Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuthentication(null);
        }
    }

    private void FirebaseGoogleAuthentication(GoogleSignInAccount account) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        auth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull  Task<AuthResult> task) {
                if(task.isSuccessful()){
//                    Toast.makeText(Login.this, "Successful", Toast.LENGTH_SHORT).show();
                    FirebaseUser user = auth.getCurrentUser();
                    updateUI(user);
                }
                else {
                    Toast.makeText(Login.this, "Failed", Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(account!=null){
            String userName = account.getDisplayName(),
                    userGivenName  = account.getGivenName(),
                    familyName = account.getFamilyName(),
                    userEmail = account.getEmail(),
                    userID = account.getId();
            Uri userPhoto = account.getPhotoUrl();


        }

    }
    /**
     * api call
     * json object
     * server -- post get
     * postman and volley
     * network calls

    **/
}