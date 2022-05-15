package com.example.social_media_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.social_media_app.Adapters.AdapterPosts;
import com.example.social_media_app.Models.ModelPosts;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Other_Profile_Page extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ImageView mCover;
    private CircleImageView mAvatar;
    private TextView name, email;
    private FloatingActionButton btnChat;
    private String uid;
    private ActionBar actionBar;
    private ArrayList<ModelPosts> modelPostsArrayList;
    private AdapterPosts adapterPosts;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile_page);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseUser = firebaseAuth.getCurrentUser();

        mAvatar = (CircleImageView) findViewById(R.id.otherAvatarIV);
        mCover = (ImageView) findViewById(R.id.otherCoverIV);
        name = (TextView) findViewById(R.id.otherNameTV);
        email = (TextView) findViewById(R.id.otherEmailTV);
        btnChat = (FloatingActionButton) findViewById(R.id.chatButton);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerPostsOtherProfile);

        uid = getIntent().getStringExtra("uid");

        modelPostsArrayList = new ArrayList<>();

        loadOthersPosts();

        if (firebaseUser.getUid().equals(uid)) {
            btnChat.setVisibility(View.GONE);
        }

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        Query query = databaseReference.orderByChild("uid").equalTo(uid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String mName = "" + dataSnapshot.child("name").getValue();
                    String mEmail = "" + dataSnapshot.child("email").getValue();
                    String avatar = "" + dataSnapshot.child("avatar").getValue();
                    String cover = "" + dataSnapshot.child("cover").getValue();

                    name.setText(mName);
                    email.setText(mEmail);

                    try {
                        Glide.with(Other_Profile_Page.this).load(avatar).into(mAvatar);
                        Glide.with(Other_Profile_Page.this).load(cover).into(mCover);
                    }
                    catch (Exception e) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatIntent = new Intent(Other_Profile_Page.this, ChatActivity.class);
                chatIntent.putExtra("UID", uid);
                startActivity(chatIntent);
            }
        });
    }

    private void loadOthersPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(layoutManager);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = databaseReference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelPostsArrayList.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelPosts modelPost = dataSnapshot1.getValue(ModelPosts.class);

                    modelPostsArrayList.add(modelPost);

                    adapterPosts = new AdapterPosts(Other_Profile_Page.this, modelPostsArrayList);

                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(Other_Profile_Page.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}