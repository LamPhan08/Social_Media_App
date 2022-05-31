package com.example.social_media_app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.social_media_app.Other_Profile_Page;
import com.example.social_media_app.Models.ModelUsers;
import com.example.social_media_app.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    private Context context;
    private FirebaseAuth firebaseAuth;
    private String uid;

    public AdapterUsers(Context context, List<ModelUsers> list) {
        this.context = context;
        this.list = list;
        firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getUid();
    }

    private List<ModelUsers> list;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        final String hisuid = list.get(position).getUid();
        String userAvatar = list.get(position).getAvatar();
        String username = list.get(position).getName();
        holder.name.setText(username);

        try {
            Glide.with(context).load(userAvatar).into(holder.profiletv);
        } catch (Exception e) {
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Other_Profile_Page.class);
                intent.putExtra("uid", hisuid);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView profiletv;
        TextView name;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profiletv = (CircleImageView) itemView.findViewById(R.id.imagep);
            name = (TextView) itemView.findViewById(R.id.namep);
        }
    }
}
