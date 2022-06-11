package com.example.social_media_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ImageView back;
    private EditText email;
    private Button submit;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_forgot_password);

        back = (ImageView) findViewById(R.id.backForgotPassword);
        email = (EditText) findViewById(R.id.edtEmailToResetPassword);
        submit = (Button) findViewById(R.id.btnSubmit);

        progressDialog = new ProgressDialog(ForgotPasswordActivity.this);
        progressDialog.setMessage("Submitting...");

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginToResetPassword();
                progressDialog.show();
            }
        });
    }

    private void beginToResetPassword() {
        String mEmail = email.getText().toString().trim();

        if (TextUtils.isEmpty(mEmail)) {
            email.setError("Please Enter Email Address!");
        }
        else {
            FirebaseAuth.getInstance().sendPasswordResetEmail(mEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "Submitted Successfully!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "Invalid Email!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}