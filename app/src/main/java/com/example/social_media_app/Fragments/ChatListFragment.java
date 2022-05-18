package com.example.social_media_app.Fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.social_media_app.Adapters.AdapterChatList;
import com.example.social_media_app.Adapters.AdapterUsers;
import com.example.social_media_app.Models.ModelChat;
import com.example.social_media_app.Models.ModelChatList;
import com.example.social_media_app.Models.ModelUsers;
import com.example.social_media_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private List<ModelChatList> chatListList;
    private List<ModelUsers> usersList;
    private DatabaseReference reference;
    private FirebaseUser firebaseUser;
    private AdapterChatList adapterChatList;
    private List<ModelChat> chatList;
    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View  view=inflater.inflate(R.layout.fragment_chat_list, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = (RecyclerView) view.findViewById(R.id.chatlistrecycle);
        chatListList = new ArrayList<>();
        chatList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatListList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    ModelChatList modelChatList = ds.getValue(ModelChatList.class);

                    if(!modelChatList.getId().equals(firebaseUser.getUid())) {
                        chatListList.add(modelChatList);
                    }
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }

    private void loadChats() {
        usersList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();

                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    ModelUsers user = dataSnapshot1.getValue(ModelUsers.class);

                    for (ModelChatList chatList:chatListList){
                        if (user.getUid() != null && user.getUid().equals(chatList.getId())){
                            usersList.add(user);

                            break;
                        }
                    }

                    adapterChatList = new AdapterChatList(getActivity(), usersList);
                    recyclerView.setAdapter(adapterChatList);

                    for (int i = 0; i < usersList.size(); i++){
                        lastMessage(usersList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lastMessage(final String uid) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String lastMessage = "default";

                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    ModelChat chat=dataSnapshot1.getValue(ModelChat.class);

                    if (chat == null) {
                        continue;
                    }

                    String sender=chat.getSender();
                    String receiver=chat.getReceiver();

                    if (sender == null || receiver == null) {
                        continue;
                    }

                    if (chat.getReceiver().equals(firebaseUser.getUid()) &&
                            chat.getSender().equals(uid) ||
                            chat.getReceiver().equals(uid) &&
                                    chat.getSender().equals(firebaseUser.getUid())) {
                        if (chat.getType().equals("images")) {
                            lastMessage="Sent a Photo";
                        }
                        else {
                            lastMessage = chat.getMessage();
                        }
                    }

                }

                adapterChatList.setlastMessageMap(uid,lastMessage);
                adapterChatList.notifyDataSetChanged();
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_users, menu);

        MenuItem menuItem = menu.findItem(R.id.search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())) {
                    searchChats(query);
                }
                else {
                    getAllChats();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    searchChats(newText);
                }
                else {
                    getAllChats();
                }

                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void searchChats(String query) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelUsers modelUsers = dataSnapshot1.getValue(ModelUsers.class);
                    if (modelUsers.getUid() != null && !modelUsers.getUid().equals(firebaseUser.getUid())) {
                        if (modelUsers.getName().toLowerCase().contains(query.toLowerCase())) {
                            usersList.add(modelUsers);
                        }
                    }

                    adapterChatList.notifyDataSetChanged();

                    recyclerView.setAdapter(adapterChatList);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllChats() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();

                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()) {
                    ModelUsers modelUsers = dataSnapshot1.getValue(ModelUsers.class);
                    //if uid is not equl is current user uid
                    //the add modelusers
                    if (dataSnapshot1.exists()) {
                        if (!modelUsers.getUid().equals(firebaseUser.getUid())) {
                            usersList.add(modelUsers);
                        }

                        recyclerView.setAdapter(adapterChatList);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
