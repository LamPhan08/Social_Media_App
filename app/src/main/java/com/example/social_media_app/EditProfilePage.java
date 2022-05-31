package com.example.social_media_app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.social_media_app.Models.ModelPosts;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfilePage extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase usersDatabase;
    private FirebaseDatabase postDatabase;
    private FirebaseDatabase commentsDatabase;
    private DatabaseReference commentsReference;
    private DatabaseReference usersReference;
    private DatabaseReference postReference;
    private StorageReference storageReference;
    private String avatarStorage = "Users_Profile_Image/";
    private String coverStorage = "Users_Cover_Image/";
    private String uid, mName, mAvatar, mBio;
    private CircleImageView avatar;
    private ImageView cover;
    private TextView editAvatar, editCover, editname, editpassword, editBio, name, bio;
    private ProgressDialog progressDialog;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;
    private String cameraPermission[];
    private String storagePermission[];
    private Uri imageuri;
    private String profileOrCoverPhoto;
    private static int update = -1;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_page);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Edit Profile");
        actionBar.setDisplayHomeAsUpEnabled(true);

        editAvatar = (TextView) findViewById(R.id.avatarEdit);
        editCover = (TextView) findViewById(R.id.coverEdit);
        editname = (TextView) findViewById(R.id.editname);
        editBio = (TextView) findViewById(R.id.updateBio);
        editpassword = findViewById(R.id.changepassword);
        name = (TextView) findViewById(R.id.tvName);
        bio = (TextView) findViewById(R.id.tvBio);
        avatar = (CircleImageView) findViewById(R.id.avatarPic);
        cover = (ImageView) findViewById(R.id.coverPic);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();
        usersDatabase = FirebaseDatabase.getInstance();
        commentsDatabase = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        postDatabase = FirebaseDatabase.getInstance();
        usersReference = usersDatabase.getReference("Users");
        postReference = postDatabase.getReference("Posts");
        commentsReference = commentsDatabase.getReference("Comments");
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        Query query = usersReference.orderByChild("email").equalTo(firebaseUser.getEmail());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    mAvatar = "" + dataSnapshot1.child("avatar").getValue();
                    String mCover = "" + dataSnapshot1.child("cover").getValue();
                    mName = "" + dataSnapshot1.child("name").getValue();
                    mBio = "" + dataSnapshot1.child("bio").getValue();

                    name.setText(mName);
                    bio.setText(mBio);

                    try {
                        Glide.with(EditProfilePage.this).load(mAvatar).into(avatar);
                        Glide.with(EditProfilePage.this).load(mCover).into(cover);
                    }
                    catch (Exception e) {
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        editpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Changing Password...");
                showPasswordChangeDailog();
            }
        });

        editAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Updating Profile Picture...");
                profileOrCoverPhoto = "avatar";
                update = 0;
                showImagePicDialog();
            }
        });

        editCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Updating Cover Photo...");
                profileOrCoverPhoto = "cover";
                update = 1;
                showImagePicDialog();
            }
        });

        editname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Updating Name...");
                showNameUpdate("name");
            }
        });

        editBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Updating Biography...");
                showBioUpdate();
            }
        });
    }

    private void showBioUpdate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Describe yourself...");

        // creating a layout to update your biography
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);
        final EditText editText = new EditText(this);
        editText.setHint("What's new?");
        layout.addView(editText);
        builder.setView(layout);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String value = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)) {
                    progressDialog.show();

                    // Here we are updating the new name
                    HashMap<String, Object> result = new HashMap<>();
                    result.put("bio", value);

                    usersReference.child(firebaseUser.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();

                            // after updated we will show updated
                            Toast.makeText(EditProfilePage.this, " Saved! ", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProfilePage.this, "Unable to update!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Toast.makeText(EditProfilePage.this, "Unable to update!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.dismiss();
            }
        });

        builder.create().show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Query query = usersReference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    String mAvatar = "" + dataSnapshot1.child("avatar").getValue();
                    String mCover = "" + dataSnapshot1.child("cover").getValue();
                    String mName = "" + dataSnapshot1.child("name").getValue();

                    name.setText(mName);
                    try {
                        Glide.with(EditProfilePage.this).load(mAvatar).into(avatar);
                        Glide.with(EditProfilePage.this).load(mCover).into(cover);
                    } catch (Exception e) {
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        editpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Changing Password");
                showPasswordChangeDailog();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Query query = usersReference.orderByChild("email").equalTo(firebaseUser.getEmail());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    String mAvatar = "" + dataSnapshot1.child("avatar").getValue();
                    String mCover = "" + dataSnapshot1.child("cover").getValue();
                    String mName = "" + dataSnapshot1.child("name").getValue();

                    name.setText(mName);
                    try {
                        Glide.with(EditProfilePage.this).load(mAvatar).into(avatar);
                        Glide.with(EditProfilePage.this).load(mCover).into(cover);
                    } catch (Exception e) {
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        editpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Changing Password");
                showPasswordChangeDailog();
            }
        });
    }

    // checking storage permission ,if given then we can add something in our storage
    private Boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    // requesting for storage permission
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(storagePermission, STORAGE_REQUEST);
        }
    }

    // checking camera permission ,if given then we can click image using our camera
    private Boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    // requesting for camera permission if not given
    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(cameraPermission, CAMERA_REQUEST);
        }
    }

    // We will show an alert box where we will write our old and new password
    private void showPasswordChangeDailog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_password, null);
        final EditText oldpass = view.findViewById(R.id.oldpasslog);
        final EditText newpass = view.findViewById(R.id.newpasslog);
        Button editpass = view.findViewById(R.id.updatepass);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();
        editpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldp = oldpass.getText().toString().trim();
                String newp = newpass.getText().toString().trim();
                if (TextUtils.isEmpty(oldp)) {
                    Toast.makeText(EditProfilePage.this, "Current Password can't be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(newp)) {
                    Toast.makeText(EditProfilePage.this, "New Password can't be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updatePassword(oldp, newp);
            }
        });
    }

    // Now we will check that if old password was authenticated
    // correctly then we will update the new password
    private void updatePassword(String oldpass, final String newpass) {
        progressDialog.show();

        final FirebaseUser user = firebaseAuth.getCurrentUser();

        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldpass);

        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        user.updatePassword(newpass)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(EditProfilePage.this, "Password changed!", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(EditProfilePage.this, "Failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(EditProfilePage.this, "Failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Updating name
    private void showNameUpdate(final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update new " + key);

        // creating a layout to write the new name
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);
        final EditText editText = new EditText(this);
        editText.setHint("Enter new " + key);
        layout.addView(editText);
        builder.setView(layout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String value = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)) {
                    progressDialog.show();

                    // Here we are updating the new name
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    usersReference.child(firebaseUser.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();

                            // after updated we will show updated
                            Toast.makeText(EditProfilePage.this, " Updated! ", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProfilePage.this, "Unable to update!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    HashMap<String, Object> postHashMap = new HashMap<>();
                    postHashMap.put("userName", value);

                    Query postQuery = postReference.orderByChild("uid").equalTo(uid);

                    postQuery.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                dataSnapshot1.getRef().updateChildren(postHashMap);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    HashMap<String, Object> commentHashMap = new HashMap<>();
                    commentHashMap.put("commentUserName", value);

                    Query commentQuery = commentsReference.orderByChild("uid").equalTo(uid);

                    commentQuery.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                dataSnapshot.getRef().updateChildren(commentHashMap);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    Toast.makeText(EditProfilePage.this, "Unable to update!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.dismiss();
            }
        });
        builder.create().show();
    }

    // Here we are showing image pic dialog where we will select
    // and image either from camera or gallery
    private void showImagePicDialog() {
        String options[] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if access is not given then we will request for permission
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                } else if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGEPICK_GALLERY_REQUEST) {
                imageuri = data.getData();
                uploadProfileCoverPhoto(imageuri);
            }
            if (requestCode == IMAGE_PICKCAMERA_REQUEST) {
                uploadProfileCoverPhoto(imageuri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
                        Toast.makeText(this, "Please Enable Camera Permission!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST: {
                if (grantResults.length > 0) {
                    boolean writeStorageaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageaccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please Enable Storage Permission!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    // Here we will click a photo and then go to startactivityforresult for updating data
    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        imageuri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent camerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camerIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        startActivityForResult(camerIntent, IMAGE_PICKCAMERA_REQUEST);
    }

    // We will select an image from gallery
    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGEPICK_GALLERY_REQUEST);
    }

    private List<ModelPosts> modelPostsList;

    // We will upload the image from here.
    private void uploadProfileCoverPhoto(final Uri uri) {
        if (update == 0) {
            progressDialog.show();

            String filepathname = avatarStorage + "" + profileOrCoverPhoto + "_" + firebaseUser.getUid();

            StorageReference storageReference1 = storageReference.child(filepathname);

            storageReference1.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());

                    final Uri downloadUri = uriTask.getResult();
                    if (uriTask.isSuccessful()) {
                        HashMap<String, Object> userNameHashMap = new HashMap<>();
                        userNameHashMap.put(profileOrCoverPhoto, downloadUri.toString());

                        usersReference.child(firebaseUser.getUid()).updateChildren(userNameHashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(EditProfilePage.this, "Updated!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(EditProfilePage.this, "Error Updating! ", Toast.LENGTH_SHORT).show();
                            }
                        });

                        CreatePost(downloadUri.toString(), "updated profile picture.");

                        HashMap<String, Object> avatarHashMap = new HashMap<>();
                        avatarHashMap.put("userAvatar", downloadUri.toString());

                        Query query = postReference.orderByChild("uid").equalTo(uid);

                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                    dataSnapshot1.getRef().updateChildren(avatarHashMap);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        HashMap<String, Object> commentHashMap = new HashMap<>();
                        commentHashMap.put("commentUserAvatar", downloadUri.toString());

                        Query commentQuery = commentsReference.orderByChild("uid").equalTo(uid);

                        commentQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    dataSnapshot.getRef().updateChildren(commentHashMap);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfilePage.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfilePage.this, "Error!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            progressDialog.show();

            String filepathname = coverStorage + "" + profileOrCoverPhoto + "_" + firebaseUser.getUid();

            StorageReference storageReference1 = storageReference.child(filepathname);

            storageReference1.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                    while (!uriTask.isSuccessful()) ;
                    final Uri downloadUri = uriTask.getResult();
                    if (uriTask.isSuccessful()) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put(profileOrCoverPhoto, downloadUri.toString());
                        usersReference.child(firebaseUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(EditProfilePage.this, "Updated!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(EditProfilePage.this, "Error Updating! ", Toast.LENGTH_SHORT).show();
                            }
                        });

                        CreatePost(downloadUri.toString(), "updated cover photo.");
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfilePage.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfilePage.this, "Error!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void CreatePost(String image, String title) {
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        String filepathname = "Posts/" + "post" + timeStamp;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] data = byteArrayOutputStream.toByteArray();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filepathname);

        storageReference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                HashMap<Object, String> hashMap = new HashMap<>();
                hashMap.put("uid", uid);
                hashMap.put("userName", mName);
                hashMap.put("userEmail", email);
                hashMap.put("userAvatar", mAvatar);
                hashMap.put("description", "");
                hashMap.put("title", title);
                hashMap.put("postImage", image);
                hashMap.put("postTime", timeStamp);
                hashMap.put("postLikes", "0");
                hashMap.put("postComments", "0");

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

                databaseReference.child(timeStamp).setValue(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}
