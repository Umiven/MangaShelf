package com.pgv.david.mangalist;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{

    private TextInputLayout tilUser;
    private EditText etUser;
    private TextInputLayout tilPassword;
    private EditText etPassword;
    private CheckBox chkShowPass;
    private FirebaseAuth firebaseAuth;
    private String user;
    private String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.tilUser = findViewById(R.id.tilLoginUser);
        this.etUser = findViewById(R.id.etLoginUser);
        this.tilPassword = findViewById(R.id.tilLoginPassword);
        this.etPassword = findViewById(R.id.etLoginPassword);
        this.chkShowPass = findViewById(R.id.chkLoginShowPassword);
        chkShowPass.setOnCheckedChangeListener(this);
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            overridePendingTransition(R.transition.transition_fade_in,R.transition.transition_fade_out);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            etPassword.setTransformationMethod(null);
        } else {
            etPassword.setTransformationMethod(new PasswordTransformationMethod());
        }
    }

    private boolean isValidUser() {
        user = etUser.getText().toString();
        if (user.equals("")) {
            tilUser.setErrorEnabled(true);
            tilUser.setError("Please input an email address");
            return false;
        } else {
            tilUser.setErrorEnabled(false);
            return true;
        }
    }

    private boolean isValidPassword() {
        pass = etPassword.getText().toString();
        if (pass.equals("")) {
            tilPassword.setErrorEnabled(true);
            tilPassword.setError("Please input a password");
            return false;
        } else {
            tilPassword.setErrorEnabled(false);
            return true;
        }
    }

    public void signIn(View view)
    {
        boolean isValidUser = isValidUser();
        boolean isValidPassword = isValidPassword();

        if (isValidUser && isValidPassword) {
            firebaseAuth.signInWithEmailAndPassword(user,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        finish();
                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                        overridePendingTransition(R.transition.transition_fade_in,R.transition.transition_fade_out);
                    } else {
                        Toast.makeText(LoginActivity.this,"Login failed",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void register(View view)
    {
        startActivity(new Intent(this,RegisterActivity.class));
    }
}
