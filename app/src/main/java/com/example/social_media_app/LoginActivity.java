package com.example.social_media_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private Button logInBtn;
    private TextView forgetPassword, newAccount;
    FirebaseUser currentUser;
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
                showRecoverPasswordDialog();
            }
        });
    }

    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        LinearLayout layout = new LinearLayout(this);

        EditText edtEmail = new EditText(this); /** Nhập tài khoản đã đăng ký */
        edtEmail.setHint("Email");
        edtEmail.setMinEms(16);
        edtEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        layout.addView(edtEmail);
        layout.setPadding(10, 10, 10, 10);

        builder.setView(layout);
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String emaill = edtEmail.getText().toString().trim();
                beginRecoverPassWord(emaill); /** Gửi mail tới địa chỉ email để phục hồi mật khẩu */
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }

    private void beginRecoverPassWord(String emaill) {
        loadingBar.setMessage("Sending Email...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        /** Gửi mail đặt lại mật khẩu */
        auth.sendPasswordResetEmail(emaill).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingBar.dismiss();

                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Done sent!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Error Occured!", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String emaill, String pass) {
        loadingBar.setMessage("Logging In...");
        loadingBar.show();

        /** Đăng nhập bằng email và password sau khi xác thực */
        auth.signInWithEmailAndPassword(emaill, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    loadingBar.dismiss();

                    FirebaseUser user = auth.getCurrentUser();

                    if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                        String mEmail = user.getEmail();
                        String uid = user.getUid();

                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("email", mEmail);
                        hashMap.put("uid", uid);
                        hashMap.put("name", "");
                        hashMap.put("onlineStatus", "online");
                        hashMap.put("typingTo", "noOne");
                        hashMap.put("phone", "");
                        hashMap.put("image", "");
                        hashMap.put("cover", "");

                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

                        /** Lưu trữ giá trị trong database trong node "Users" */
                        DatabaseReference databaseReference = firebaseDatabase.getReference("Users");

                        /** Lưu trữ giá trị trong Firebase */
                        databaseReference.child(uid).setValue(hashMap);
                    }

                    Toast.makeText(LoginActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();

                    Intent dashboardIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                    dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(dashboardIntent);

                    finish();
                }
                else {
                    loadingBar.dismiss();
                    Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this, "Error Occured!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}