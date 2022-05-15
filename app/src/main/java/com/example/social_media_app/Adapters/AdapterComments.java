package com.example.social_media_app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.social_media_app.Models.ModelComments;
import com.example.social_media_app.Other_Profile_Page;
import com.example.social_media_app.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder> {
    private Context context;
    private List<ModelComments> modelCommentsList;

    public AdapterComments(Context context, List<ModelComments> modelCommentsList, String myuid, String postid) {
        this.context = context;
        this.modelCommentsList = modelCommentsList;
        this.myuid = myuid;
        this.postid = postid;
    }

    private String myuid;
    private String postid;


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final String uid = modelCommentsList.get(position).getUid();
        String mName = modelCommentsList.get(position).getCommentUserName();
        String mEmail = modelCommentsList.get(position).getCommentUserEmail();
        String mAvatar = modelCommentsList.get(position).getCommentUserAvatar();
        final String mCommentId = modelCommentsList.get(position).getCommentId();
        String mComment = modelCommentsList.get(position).getComment();
        String timeStamp = modelCommentsList.get(position).getCommentTime();
        String mCommentImage = modelCommentsList.get(position).getImageComment();

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));

        String timeDate = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.name.setText(mName);
        holder.time.setText(timeDate);
        holder.comment.setText(mComment);

        try {
            Glide.with(context).load(mAvatar).into(holder.avatar);
        }
        catch (Exception e) {

        }

        holder.image.setVisibility(View.VISIBLE);

        try {
            Glide.with(context).load(mCommentImage).into(holder.image);
        }
        catch (Exception e) {

        }

//        holder.avatar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent profileIntent = new Intent(holder.itemView.getContext(), Other_Profile_Page.class);
//                profileIntent.putExtra("uid", uid);
//                holder.itemView.getContext().startActivity(profileIntent);
//            }
//        });
//
//        holder.name.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent profileIntent = new Intent(holder.itemView.getContext(), Other_Profile_Page.class);
//                profileIntent.putExtra("uid", uid);
//                holder.itemView.getContext().startActivity(profileIntent);
//            }
//        });

//        holder.
    }

    @Override
    public int getItemCount() {
        return modelCommentsList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        ImageView image;
        TextView name, comment, time;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatar = (CircleImageView) itemView.findViewById(R.id.avatarComment);
            name = (TextView) itemView.findViewById(R.id.commentName);
            comment = (TextView) itemView.findViewById(R.id.commentText);
            time = (TextView) itemView.findViewById(R.id.commentTime);
            image = (ImageView) itemView.findViewById(R.id.imageComment);
        }
    }
}
