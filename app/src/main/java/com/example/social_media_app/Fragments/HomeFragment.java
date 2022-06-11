package com.example.social_media_app.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.social_media_app.Adapters.AdapterPosts;
import com.example.social_media_app.Models.ModelPosts;
import com.example.social_media_app.ViewOtherProfilePageActivity;
import com.example.social_media_app.R;
import com.example.social_media_app.SearchUserActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<ModelPosts> modelPostsList;
    private AdapterPosts adapterPosts;
    private MaterialButton btnSearchUser;
    private String email;
    private DatabaseReference databaseReference;
    private CircleImageView profileImage;
    private String avatar, myUid;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnSearchUser = (MaterialButton) view.findViewById(R.id.searchUserBtn);
        profileImage = (CircleImageView) view.findViewById(R.id.profileImageInHome);

        recyclerView = (RecyclerView) view.findViewById(R.id.postRecyclerView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(layoutManager);

        modelPostsList = new ArrayList<>();

        adapterPosts = new AdapterPosts(getActivity(), modelPostsList);
        recyclerView.setAdapter(adapterPosts);

        loadMyProfileImage();

        loadPosts();

        btnSearchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchUserIntent = new Intent(getActivity(), SearchUserActivity.class);
                startActivity(searchUserIntent);
            }
        });

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myProfileIntent = new Intent(getContext(), ViewOtherProfilePageActivity.class);
                myProfileIntent.putExtra("uid", myUid);
                startActivity(myProfileIntent);
            }
        });

        return view;
    }

    private void loadMyProfileImage() {
        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        Query query = databaseReference.orderByChild("email").equalTo(email);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    avatar = "" + dataSnapshot1.child("avatar").getValue().toString();
                }

                try {
                    Glide.with(getContext()).load(avatar).into(profileImage);
                }
                catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void loadPosts() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelPostsList.clear();

                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()) {
                    ModelPosts modelPost = dataSnapshot1.getValue(ModelPosts.class);

                    modelPostsList.add(modelPost);
                }

                adapterPosts.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }
}
