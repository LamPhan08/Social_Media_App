package com.example.social_media_app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.social_media_app.ChatActivity;
import com.example.social_media_app.Models.ModelUsers;
import com.example.social_media_app.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.Myholder> {

    private Context context;
    private FirebaseAuth firebaseAuth;
    private String uid;

    public AdapterChatList(Context context, List<ModelUsers> users) {
        this.context = context;
        this.usersList = users;
        lastMessageMap = new HashMap<>();
        firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getUid();
    }

    private List<ModelUsers> usersList;
    private HashMap<String, String> lastMessageMap;

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new Myholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, final int position) {
        final String hisuid = usersList.get(position).getUid();
        String userAvatar = usersList.get(position).getAvatar();
        String username = usersList.get(position).getName();
        String lastmess = lastMessageMap.get(hisuid);
        String status = usersList.get(position).getStatus();

        holder.name.setText(username);

        if (lastmess == null || lastmess.equals("default")) {
            holder.lastmessage.setVisibility(View.GONE);
        } else {
            holder.lastmessage.setVisibility(View.VISIBLE);
            holder.lastmessage.setText(lastmess);
        }

        try {
            Glide.with(context).load(userAvatar).into(holder.profile);
        }
        catch (Exception e) {

        }

        if (status.equals("Online")) {
            holder.statusOn.setVisibility(View.VISIBLE);
            holder.statusOff.setVisibility(View.GONE);
        }
        else {
            holder.statusOn.setVisibility(View.GONE);
            holder.statusOff.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);

                intent.putExtra("UID", hisuid);
                context.startActivity(intent);
            }
        });

    }

    public void setlastMessageMap(String userId, String lastmessage) {
        lastMessageMap.put(userId, lastmessage);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class Myholder extends RecyclerView.ViewHolder {
        CircleImageView profile;
        TextView name, lastmessage;
        CircleImageView statusOn, statusOff;

        public Myholder(@NonNull View itemView) {
            super(itemView);

            profile = (CircleImageView) itemView.findViewById(R.id.profileimage);
            name = (TextView) itemView.findViewById(R.id.nameonline);
            lastmessage = (TextView) itemView.findViewById(R.id.lastmessge);
            statusOn = (CircleImageView) itemView.findViewById(R.id.img_on);
            statusOff = (CircleImageView) itemView.findViewById(R.id.img_off);
        }
    }
}
