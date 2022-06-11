package com.example.social_media_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {
    private EditText name, email, password, re_enterPassword;
    private Button registerBtn;
    private TextView signIn;
    private ProgressDialog loadingBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_registration);

        name = (EditText) findViewById(R.id.edtRegisterName);
        email = (EditText) findViewById(R.id.edtRegisterEmail);
        re_enterPassword = (EditText) findViewById(R.id.edtReenterPassword);
        password = (EditText) findViewById(R.id.edtRegisterPassword);
        registerBtn = (Button) findViewById(R.id.btnCreateAccount);
        signIn = (TextView) findViewById(R.id.txvHaveAnAccount);

        auth = FirebaseAuth.getInstance();

        loadingBar = new ProgressDialog(this);
        loadingBar.setMessage("Registering...");

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mName = name.getText().toString().trim();
                String mEmail = email.getText().toString().trim();
                String mPassword = password.getText().toString().trim();
                String mReEnterPassword = re_enterPassword.getText().toString().trim();

                /** Kiểm tra email có hợp lệ không */
                if (!Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {
                    email.setError("Invalid Email!");
                    email.setFocusable(true);
                }
                else if (mPassword.length() < 6) { /** Kiểm tra mật khẩu phải từ 6 ký tự trở lên */
                    password.setError("Password length must be greater than 6 character!");
                    password.setFocusable(true);
                }
                else if (!mReEnterPassword.equals(mPassword)) {
                    re_enterPassword.setError("Password mismatch!");
                    re_enterPassword.setFocusable(true);
                }
                else {
                    registerUser(mName, mEmail, mPassword);
                }
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(signInIntent);
            }
        });
    }

    /** Hàm tạo USER với email và password. Nếu không thành công thì ta sẽ hiển thị lỗi*/
    private void registerUser(String mName, String mEmail, String mPassword) {
        loadingBar.show();
        auth.createUserWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    loadingBar.dismiss();

                    FirebaseUser user = auth.getCurrentUser();
                    String emaill = user.getEmail();
                    String uid = user.getUid();

                    HashMap<Object, String> hashMap = new HashMap<>();
                    hashMap.put("email", emaill);
                    hashMap.put("uid", uid);
                    hashMap.put("name", mName);
                    hashMap.put("bio", "");
                    hashMap.put("status", "Online");
                    hashMap.put("avatar", "https://firebasestorage.googleapis.com/v0/b/social-media-app-e7c34.appspot.com/o/profile_image.png?alt=media&token=81c84b7a-4829-4d6d-9491-0dfea9c5d73e");
                    hashMap.put("cover", "");

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference reference = database.getReference("Users");
                    reference.child(uid).setValue(hashMap);

                    Toast.makeText(RegistrationActivity.this, "Register Successfully!", Toast.LENGTH_SHORT).show();

                    Intent dashboardIntent = new Intent(RegistrationActivity.this, DashboardActivity.class);
                    dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(dashboardIntent);

                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(RegistrationActivity.this, "Email already exists!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}