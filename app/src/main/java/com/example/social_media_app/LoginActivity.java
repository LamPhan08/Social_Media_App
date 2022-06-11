package com.example.social_media_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private Button logInBtn;
    private TextView forgetPassword, newAccount;
    private FirebaseUser currentUser;
    private ProgressDialog loadingBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.edtLoginEmail);
        password = (EditText) findViewById(R.id.edtLoginPassword);
        logInBtn = (Button) findViewById(R.id.btnLogIn);
        forgetPassword = (TextView) findViewById(R.id.txvForgetPassword);
        newAccount = (TextView) findViewById(R.id.txvNeedNewAccount);

        auth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        /** Kiểm tra USER null hay không */
        if (auth != null) {
            currentUser = auth.getCurrentUser();
        }

        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emaill = email.getText().toString().trim();
                String pass = password.getText().toString().trim();

                /** Kiểm tra định dạng email có hợp lệ không */
                if (!Patterns.EMAIL_ADDRESS.matcher(emaill).matches()) {
                    email.setError("Invalid Email!");
                    email.setFocusable(true);
                }
                else {
                    loginUser(emaill, pass);
                }
            }
        });

        /** Nếu cần tài khoản mới thì di chuyển tới màn hình RegistrationActivity */
        newAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registrationIntent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(registrationIntent);
            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser(String emaill, String pass) {
        loadingBar.setMessage("Logging in...");
        loadingBar.show();

        /** Đăng nhập bằng email và password sau khi xác thực */
        auth.signInWithEmailAndPassword(emaill, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    loadingBar.dismiss();

                    Toast.makeText(LoginActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();

                    Intent dashboardIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                    dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(dashboardIntent);

                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this, "Invalid Email or Password!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}