package com.example.social_media_app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_media_app.Adapters.AdapterComments;
import com.example.social_media_app.Models.ModelComments;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentPost extends AppCompatActivity {
    private String myUid, myName, myEmail, myAvatar, postId, postLikes;
    private TextView like;
    private MaterialButton likeBtn;
    private EditText comment;
    private ImageButton sendCommentBtn, commentImageBtn;
    private RecyclerView recyclerView;
    private List<ModelComments> modelCommentsList;
    private AdapterComments adapterComment;


    private String cameraPermission[];
    private String storagePermission[];

    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;

    private Uri imageuri = null;

    private boolean mlike = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_post);
        getSupportActionBar().hide();

        postId = getIntent().getStringExtra("pid");

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        myEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView = (RecyclerView) findViewById(R.id.recycleComment);
        like = (TextView) findViewById(R.id.likecountTV);
        likeBtn = (MaterialButton) findViewById(R.id.commentLikeBtn);
        comment = (EditText) findViewById(R.id.commentType);
        sendCommentBtn = (ImageButton) findViewById(R.id.sendComment);
        commentImageBtn = (ImageButton) findViewById(R.id.commentImage);

        progressDialog = new ProgressDialog(this);

        loadPostInfo();
        loadUserInfo();
        loadComments();

        sendCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CommentPost.this, PeopleWhoLiked.class);
                intent.putExtra("pid", postId);
                startActivity(intent);
            }
        });
        
        commentImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPickImageDialog();
            }
        });
    }

    private void showPickImageDialog() {
        String options[] = { "Camera","Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CommentPost.this);

        builder.setTitle("Pick Image From");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                else if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }

                }
            }
        });

        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_REQUEST: {
                if (grantResults.length > 0) {
                    boolean camera_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageaccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (camera_accepted && writeStorageaccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Please enable Camera permission!", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            }
            case STORAGE_REQUEST: {
                if (grantResults.length > 0) {
                    boolean writeStorageaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageaccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please enable Storage permission!", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGEPICK_GALLERY_REQUEST) {
                imageuri = data.getData();

                try {
                    sendImageComment(imageuri);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (requestCode == IMAGE_PICKCAMERA_REQUEST) {
                try {
                    sendImageComment(imageuri);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageComment(Uri imageuri) throws IOException {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Commenting Image...");
        dialog.show();

        final String timeStamp = "" + System.currentTimeMillis();
        String filepathandname = "CommentImages/" + "post" + timeStamp;

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageuri);
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,arrayOutputStream);

        final byte[] data = arrayOutputStream.toByteArray();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filepathandname);

        storageReference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String downloadUri = uriTask.getResult().toString();

                if (uriTask.isSuccessful()) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Comments");

                    HashMap<String,Object> hashMap = new HashMap<>();

                    hashMap.put("commentId", timeStamp);
                    hashMap.put("comment", "");
                    hashMap.put("commentTime", timeStamp);
                    hashMap.put("uid", myUid);
                    hashMap.put("postId", postId);
                    hashMap.put("imageComment", downloadUri);
                    hashMap.put("commentUserEmail", myEmail);
                    hashMap.put("commentUserAvatar", myAvatar);
                    hashMap.put("commentUserName", myName);

                    databaseReference.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            dialog.dismiss();

                            comment.setText("");

                            updateCommentCount();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(CommentPost.this,"Failed!",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);

        galleryIntent.setType("image/*");

        startActivityForResult(galleryIntent, IMAGEPICK_GALLERY_REQUEST);
    }

    private void pickFromCamera() {
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");

        imageuri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent camerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camerIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri);

        startActivityForResult(camerIntent,IMAGE_PICKCAMERA_REQUEST);
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(storagePermission,STORAGE_REQUEST);
        }
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(CommentPost.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(cameraPermission, CAMERA_REQUEST);
        }
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(layoutManager);

        modelCommentsList = new ArrayList<>();

        DatabaseReference commentDatabase = FirebaseDatabase.getInstance().getReference("Comments");

        commentDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelCommentsList.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelComments modelComments = dataSnapshot1.getValue(ModelComments.class);

                    modelCommentsList.add(modelComments);

                    adapterComment = new AdapterComments(CommentPost.this, modelCommentsList, myUid, postId);

                    recyclerView.setAdapter(adapterComment);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLikes() {
        final DatabaseReference likeDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Likes");

        likeDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postId).hasChild(myUid)) {
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                } else {
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost() {
        mlike = true;

        final DatabaseReference likeDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Posts");

        likeDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mlike) {
                    if (dataSnapshot.child(postId).hasChild(myUid)) {
                        postDatabaseReference.child(postId).child("postLikes").setValue("" + (Integer.parseInt(postLikes) - 1));
                        likeDatabaseReference.child(postId).child(myUid).removeValue();

                        mlike = false;

                    } else {
                        postDatabaseReference.child(postId).child("postLikes").setValue("" + (Integer.parseInt(postLikes) + 1));
                        likeDatabaseReference.child(postId).child(myUid).setValue("Liked");

                        mlike = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void postComment() {
        progressDialog.setMessage("Adding Comment...");

        final String mComments = comment.getText().toString().trim();

        if (TextUtils.isEmpty(mComments)) {
            return;
        }

        progressDialog.show();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Comments");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("commentId", timeStamp);
        hashMap.put("comment", mComments);
        hashMap.put("commentTime", timeStamp);
        hashMap.put("postId", postId);
        hashMap.put("uid", myUid);
        hashMap.put("commentUserEmail", myEmail);
        hashMap.put("commentUserAvatar", myAvatar);
        hashMap.put("commentUserName", myName);
        hashMap.put("imageComment", "");

        databaseReference.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();

                comment.setText("");

                updateCommentCount();

                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CommentPost.this, "Failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean count = false;

    private void updateCommentCount() {
        count = true;

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (count) {
                    String comments = "" + dataSnapshot.child("postComments").getValue();

                    int newComment = Integer.parseInt(comments) + 1;

                    reference.child("postComments").setValue("" + newComment);

                    count = false;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUserInfo() {
        Query query = FirebaseDatabase.getInstance().getReference("Users");

        query.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    myName = dataSnapshot1.child("name").getValue().toString();
                    myAvatar = dataSnapshot1.child("avatar").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadPostInfo() {
        setLikes();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

        Query query = databaseReference.orderByChild("postTime").equalTo(postId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    postLikes = dataSnapshot1.child("postLikes").getValue().toString();

                    if (postLikes.equals("0")) {
                        like.setVisibility(View.INVISIBLE);
                    }
                    else {
                        like.setVisibility(View.VISIBLE);
                        like.setText(postLikes);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}
