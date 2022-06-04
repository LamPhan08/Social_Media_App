package com.example.social_media_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.social_media_app.Fragments.CreatePostFragment;
import com.example.social_media_app.Fragments.ChatListFragment;
import com.example.social_media_app.Fragments.HomeFragment;
import com.example.social_media_app.Fragments.ProfileFragment;
import com.example.social_media_app.Fragments.UsersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class DashboardActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String myUID;
    private DatabaseReference databaseReference;
    private BottomNavigationView bottomNavigationView;
    private ActionBar actionBar;

    private static int homeFragment = 0,
            usersFragment = 1,
            createPostFragment = 2,
            chatListFragment = 3,
            profileFragment = 4;

    private static int currentFragment = -1;

    private String myName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        currentFragment = homeFragment;

        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();

        myUID = user.getUid();

        actionBar = getSupportActionBar();
        actionBar.setTitle("Home");

        auth = FirebaseAuth.getInstance();

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, new HomeFragment());
        fragmentTransaction.commit();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        Query query = databaseReference.orderByChild("uid").equalTo(myUID);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menuHome: {
                        if (currentFragment != homeFragment) {
                            actionBar.setTitle("Home");
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.content, new HomeFragment());
                            currentFragment = homeFragment;
                            transaction.commit();
                        }
                        return true;
                    }

                    case R.id.menuProfile: {
                        if (currentFragment != profileFragment) {
                            actionBar.setTitle(myName);
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.content, new ProfileFragment());
                            currentFragment = profileFragment;
                            transaction.commit();
                        }
                        return true;
                    }

                    case R.id.menuUsers: {
                        if (currentFragment != usersFragment) {
                            actionBar.setTitle("Users");
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.content, new UsersFragment());
                            currentFragment = usersFragment;
                            transaction.commit();
                        }
                        return true;
                    }

                    case R.id.menuCreatePost: {
                        if (currentFragment != createPostFragment) {
                            actionBar.setTitle("Create Post");
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.content, new CreatePostFragment());
                            currentFragment = createPostFragment;
                            transaction.commit();
                        }
                        return true;
                    }

                    case R.id.menuChatList: {
                        if (currentFragment != chatListFragment) {
                            actionBar.setTitle("Chats");
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.content, new ChatListFragment());
                            currentFragment = chatListFragment;
                            transaction.commit();
                        }
                        return true;
                    }
                }

                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timeStamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timeStamp);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("Online");
        super.onResume();
    }

    @Override
    protected void onStart() {
        checkOnlineStatus("Online");
        super.onStart();
    }

    private void checkOnlineStatus(String status){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myUID);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        databaseReference.updateChildren(hashMap);
    }
}