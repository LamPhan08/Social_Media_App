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

public class DashboardActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String myUID;
    private BottomNavigationView bottomNavigationView;
    private ActionBar actionBar;

    private static int homeFragment = 0,
            usersFragment = 1,
            createPostFragment = 2,
            chatListFragment = 3,
            profileFragment = 4;

    private static int currentFragment = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        currentFragment = homeFragment;

        actionBar = getSupportActionBar();
        actionBar.setTitle("Home");

        auth = FirebaseAuth.getInstance();

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, new HomeFragment());
        fragmentTransaction.commit();

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
                            actionBar.setTitle("Profile");
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
}