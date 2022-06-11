package com.example.social_media_app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.social_media_app.Models.ModelNotifications;
import com.example.social_media_app.PostDetailsActivity;
import com.example.social_media_app.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterNotifications extends RecyclerView.Adapter<AdapterNotifications.MyHolder> {
    private Context context;
    private List<ModelNotifications> modelNotificationsList;
    private String myUid;

    public AdapterNotifications(Context context, List<ModelNotifications> modelNotificationsList) {
        this.context = context;
        this.modelNotificationsList = modelNotificationsList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public AdapterNotifications.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_notifications, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterNotifications.MyHolder holder, int position) {
        String mAvatar = modelNotificationsList.get(position).getAvatar();
        String mName = modelNotificationsList.get(position).getName();
        String mNotification = modelNotificationsList.get(position).getNotification();
        String mTime = modelNotificationsList.get(position).getTime();
        String mType = modelNotificationsList.get(position).getType();
        String mPostId = modelNotificationsList.get(position).getPostId();

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(mTime));

        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.notification.setText(mName + mNotification);
        holder.notifyTime.setText(dateTime);

        try {
            Glide.with(context).load(mAvatar).into(holder.avatar);
        }
        catch (Exception e) {

        }

        if (mType.equals("Like")) {
            holder.img_LikePost.setVisibility(View.VISIBLE);
            holder.img_CommentPost.setVisibility(View.GONE);
        }
        else {
            holder.img_LikePost.setVisibility(View.GONE);
            holder.img_CommentPost.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostDetailsActivity.class);
                intent.putExtra("postId", mPostId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelNotificationsList.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        private CircleImageView avatar, img_LikePost, img_CommentPost;
        private TextView notification, notifyTime;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatar = (CircleImageView) itemView.findViewById(R.id.notifyProfileImage);
            img_LikePost = (CircleImageView) itemView.findViewById(R.id.img_likePost);
            img_CommentPost = (CircleImageView) itemView.findViewById(R.id.img_commentPost);
            notification = (TextView) itemView.findViewById(R.id.notification);
            notifyTime = (TextView) itemView.findViewById(R.id.notifyTime);
        }
    }
}
