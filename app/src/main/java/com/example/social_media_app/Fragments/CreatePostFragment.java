package com.example.social_media_app.Fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.social_media_app.DashboardActivity;
import com.example.social_media_app.R;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class CreatePostFragment extends Fragment {


    public CreatePostFragment() {
        // Required empty public constructor
    }
    private FirebaseAuth firebaseAuth;
    private EditText description;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    private String cameraPermission[];
    private String storagePermission[];
    private ProgressDialog progressDialog;
    private ImageView image;
    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;

    private Uri imageuri = null;
    private String name, email, uid, avatar;
    private DatabaseReference databaseReference;
    private Button upload;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();

        View view= inflater.inflate(R.layout.fragment_create_post, container, false);

        description = (EditText) view.findViewById(R.id.descriptionEDT);
        image = (ImageView) view.findViewById(R.id.uploadImage);
        upload = (Button) view.findViewById(R.id.uploadBtn);

        uid = FirebaseAuth.getInstance().getUid();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCanceledOnTouchOutside(false);

        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        Query query = databaseReference.orderByChild("email").equalTo(email);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    name = dataSnapshot1.child("name").getValue().toString();
                    email = "" + dataSnapshot1.child("email").getValue();
                    avatar = "" + dataSnapshot1.child("avatar").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mDescription = "" + description.getText().toString().trim();

                if (TextUtils.isEmpty(mDescription)) {
                    if (imageuri == null) {
                        return;
                    }
                    else {
                        uploadData(mDescription);
                    }
                }
                else {
                    uploadData(mDescription);
                }

            }
        });

        return view;
    }

    private void showImagePicDialog() {
        String options[] = { "Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Pick Image From");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0) {
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

    private Boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST: {
                if (grantResults.length > 0) {
                    boolean camera_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageaccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (camera_accepted && writeStorageaccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(getContext(), "Please enable camera permission!", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            }

            case STORAGE_REQUEST: {
                if (grantResults.length > 0) {
                    boolean writeStorageaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (writeStorageaccepted) {
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(getContext(),"Please enable storage permission!",Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            }
        }

    }

    private void requestStoragePermission() {
        requestPermissions(storagePermission,STORAGE_REQUEST);
    }

    private Boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(getContext(),Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {
        requestPermissions(cameraPermission,CAMERA_REQUEST);
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");

        imageuri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri);

        startActivityForResult(cameraIntent,IMAGE_PICKCAMERA_REQUEST);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);

        galleryIntent.setType("image/*");

        startActivityForResult(galleryIntent, IMAGEPICK_GALLERY_REQUEST);
    }

    private void uploadData(final String mDescription) {
        if (imageuri == null && !TextUtils.isEmpty(mDescription)) {
            progressDialog.setMessage("Posting...");
            progressDialog.show();

            final String timeStamp = String.valueOf(System.currentTimeMillis());

            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("uid", uid);
            hashMap.put("userName", name);
            hashMap.put("userEmail", email);
            hashMap.put("userAvatar", avatar);
            hashMap.put("description", mDescription);
            hashMap.put("postImage", "");
            hashMap.put("postTime", timeStamp);
            hashMap.put("postLikes", "0");
            hashMap.put("postComments", "0");

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

            databaseReference.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Posted!", Toast.LENGTH_SHORT).show();

                            description.setText("");

                            startActivity(new Intent(getContext(), DashboardActivity.class));

                            getActivity().finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if ((imageuri != null && !TextUtils.isEmpty(mDescription)) || (imageuri != null && TextUtils.isEmpty(mDescription))) {
            progressDialog.setMessage("Posting...");
            progressDialog.show();

            final String timeStamp = String.valueOf(System.currentTimeMillis());
            String filepathname = "Posts/" + "post" + timeStamp;

            Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filepathname);

            storageReference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                    while (!uriTask.isSuccessful()) ;

                    String downloadUri = uriTask.getResult().toString();

                    if (uriTask.isSuccessful()) {
                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("uid", uid);
                        hashMap.put("userName", name);
                        hashMap.put("userEmail", email);
                        hashMap.put("userAvatar", avatar);
                        hashMap.put("description", mDescription);
                        hashMap.put("postImage", downloadUri);
                        hashMap.put("postTime", timeStamp);
                        hashMap.put("postLikes", "0");
                        hashMap.put("postComments", "0");

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

                        databaseReference.child(timeStamp).setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(), "Posted!", Toast.LENGTH_SHORT).show();

                                        description.setText("");

                                        image.setImageURI(null);
                                        imageuri = null;

                                        startActivity(new Intent(getContext(), DashboardActivity.class));

                                        getActivity().finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == getActivity().RESULT_OK){
            if(requestCode == IMAGEPICK_GALLERY_REQUEST){
                imageuri = data.getData();
                image.setImageURI(imageuri);
            }
            if(requestCode == IMAGE_PICKCAMERA_REQUEST){
                image.setImageURI(imageuri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}
