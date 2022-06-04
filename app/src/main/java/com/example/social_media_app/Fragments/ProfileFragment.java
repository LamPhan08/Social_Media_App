package com.example.social_media_app.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.social_media_app.Adapters.AdapterPosts;
import com.example.social_media_app.EditProfilePage;
import com.example.social_media_app.LoginActivity;
import com.example.social_media_app.Models.ModelPosts;
import com.example.social_media_app.R;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ImageView mCover;
    CircleImageView mAvatar;
    private TextView name, email, bio;
    private FloatingActionButton editProfile;
    private ProgressDialog progressDialog;
    private RecyclerView postRecyclerView;
    private ArrayList<ModelPosts> modelPostsArrayList;
    private AdapterPosts adapterPosts;

    private String uid;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // creating a view to inflate the layout
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseAuth = FirebaseAuth.getInstance();

        // getting current user data
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        // Initialising the text view and imageview
        mAvatar = (CircleImageView) view.findViewById(R.id.avatarIV);
        mCover = (ImageView) view.findViewById(R.id.coverIV);
        name = (TextView) view.findViewById(R.id.nametv);
        email = (TextView) view.findViewById(R.id.emailtv);
        bio = (TextView) view.findViewById(R.id.bioTV);
        editProfile = (FloatingActionButton) view.findViewById(R.id.fab);
        postRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerPosts);

        uid = FirebaseAuth.getInstance().getUid();
        modelPostsArrayList = new ArrayList<>();

        adapterPosts = new AdapterPosts(getActivity(), modelPostsArrayList);

        postRecyclerView.setAdapter(adapterPosts);

        loadMyPosts();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Logging out...");
        progressDialog.setCanceledOnTouchOutside(false);

        Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    // Retrieving Data from firebase
                    String mName = "" + dataSnapshot1.child("name").getValue();
                    String mEmail = "" + dataSnapshot1.child("email").getValue();
                    String mBio = "" + dataSnapshot1.child("bio").getValue();
                    String avatar = "" + dataSnapshot1.child("avatar").getValue();
                    String cover = "" + dataSnapshot1.child("cover").getValue();

                    // setting data to our text view
                    name.setText(mName);
                    email.setText(mEmail);
                    bio.setText(mBio);

                    try {
                        Glide.with(getActivity()).load(avatar).into(mAvatar);
                        Glide.with(getActivity()).load(cover).into(mCover);
                    }
                    catch (Exception e) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // On click we will open EditProfileActiity
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), EditProfilePage.class));
            }
        });
        return view;
    }

    private void loadMyPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        postRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = databaseReference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelPostsArrayList.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelPosts modelPost = dataSnapshot1.getValue(ModelPosts.class);

                    modelPostsArrayList.add(modelPost);
                }
                adapterPosts.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            progressDialog.show();

            firebaseAuth.signOut();

            Intent logInIntent = new Intent(getContext(), LoginActivity.class);
            startActivity(logInIntent);

            Toast.makeText(getActivity(), "Logout Successfully!", Toast.LENGTH_SHORT).show();

            getActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }
}