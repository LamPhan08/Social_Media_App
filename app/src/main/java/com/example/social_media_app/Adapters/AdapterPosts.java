package com.example.social_media_app.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.social_media_app.CommentPost;
import com.example.social_media_app.Models.ModelPosts;
import com.example.social_media_app.Other_Profile_Page;
import com.example.social_media_app.PeopleWhoLiked;
import com.example.social_media_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {
    private Context context;
    private String myUid, email, myName, myAvatar;
    private DatabaseReference likeDatabaseReference, postDatabaseReference, notificationsDatabaseReference, myInformation;
    boolean processLike = false;


    public AdapterPosts(Context context, List<ModelPosts> modelPosts) {
        this.context = context;
        this.modelPosts = modelPosts;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        postDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        notificationsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
    }

    private List<ModelPosts> modelPosts;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, @SuppressLint("RecyclerView") final int position) {
        final String uid = modelPosts.get(position).getUid();
        String mName = modelPosts.get(position).getUserName();
        String mEmail = modelPosts.get(position).getUserEmail();
        final String mDescription = modelPosts.get(position).getDescription();
        String mTitle = modelPosts.get(position).getTitle();
        final String mPostTime = modelPosts.get(position).getPostTime();
        String mAvatar = modelPosts.get(position).getUserAvatar();
        String mPostLikes = modelPosts.get(position).getPostLikes();
        final String mPostImage = modelPosts.get(position).getPostImage();
        String mPostComments = modelPosts.get(position).getPostComments();
        final String postId = modelPosts.get(position).getPostTime();

        loadMyInformation();

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(mPostTime));

        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.name.setText(mName);
        holder.time.setText(dateTime);
        holder.title.setText(mTitle);

        holder.description.setVisibility(View.VISIBLE);

        if (TextUtils.isEmpty(mDescription)) {
            holder.description.setVisibility(View.GONE);
        }
        else {
            holder.description.setText(mDescription);
        }

        if (mPostLikes.equals("0")) {
            holder.likeCountLayout.setVisibility(View.GONE);
        }
        else {
            holder.likeCountLayout.setVisibility(View.VISIBLE);
            holder.likes.setText(" " + mPostLikes);
        }

        if (mPostComments.equals("0")) {
            holder.commentTV.setText("");
        }
        else if (mPostComments.equals("1")) {
            holder.commentTV.setText(mPostComments + " Comment");
        }
        else {
            holder.commentTV.setText(mPostComments + " Comments");
        }

        setLikes(holder, mPostTime, position);

        try {
            Glide.with(context).load(mAvatar).into(holder.avatar);
        }
        catch (Exception e) {

        }

        holder.image.setVisibility(View.VISIBLE);

        if (mPostImage.equals("")) {
            holder.image.setVisibility(View.GONE);
        }
        else {
            try {
                Glide.with(context).load(mPostImage).into(holder.image);
            }
            catch (Exception e) {

            }
        }

        holder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), PeopleWhoLiked.class);
                intent.putExtra("pid", postId);
                holder.itemView.getContext().startActivity(intent);
            }
        });

        holder.likeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int plike = Integer.parseInt(modelPosts.get(position).getPostLikes());

                String timeStamp = String.valueOf(System.currentTimeMillis());

                processLike = true;

                likeDatabaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (processLike) {
                            if (dataSnapshot.child(postId).hasChild(myUid)) {
                                postDatabaseReference.child(postId).child("postLikes").setValue("" + (plike - 1));
                                likeDatabaseReference.child(postId).child(myUid).removeValue();

                                if (!myUid.equals(uid)) {
                                    notificationsDatabaseReference.child(modelPosts.get(position).getLikeRemoveValue()).removeValue();
                                }

                                processLike = false;


                            } else {
                                postDatabaseReference.child(postId).child("postLikes").setValue("" + (plike + 1));
                                likeDatabaseReference.child(postId).child(myUid).setValue(timeStamp);

                                if (!myUid.equals(uid)) {
                                    addToHisNotification(postId, timeStamp, uid);
                                }

                                processLike = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        if (!myUid.equals(uid)) {
            holder.moreBtn.setVisibility(View.GONE);
        }
        else {
            holder.moreBtn.setVisibility(View.VISIBLE);
        }

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, mPostTime, mPostImage, holder.getAdapterPosition());
            }
        });

        holder.commentTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), CommentPost.class);
                intent.putExtra("pid", mPostTime);
                holder.itemView.getContext().startActivity(intent);
            }
        });

        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(holder.itemView.getContext(), Other_Profile_Page.class);
                profileIntent.putExtra("uid", uid);
                holder.itemView.getContext().startActivity(profileIntent);
            }
        });

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(holder.itemView.getContext(), Other_Profile_Page.class);
                profileIntent.putExtra("uid", uid);
                holder.itemView.getContext().startActivity(profileIntent);
            }
        });
    }

    private void loadMyInformation() {
        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        myInformation = FirebaseDatabase.getInstance().getReference().child("Users");

        Query query = myInformation.orderByChild("email").equalTo(email);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myName = dataSnapshot.child("name").getValue().toString();
                    myAvatar = dataSnapshot.child("avatar").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addToHisNotification(String postId, String timeStamp, String hisUid) {
        HashMap<Object, String> notifyHashmap = new HashMap<>();
        notifyHashmap.put("postId", postId);
        notifyHashmap.put("time", timeStamp);
        notifyHashmap.put("hisUid", myUid);
        notifyHashmap.put("myUid", hisUid);
        notifyHashmap.put("notification", " liked your post.");
        notifyHashmap.put("name", myName);
        notifyHashmap.put("type", "Like");
        notifyHashmap.put("avatar", myAvatar);

        notificationsDatabaseReference.child(timeStamp).setValue(notifyHashmap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void showMoreOptions(ImageButton more, final String pid, final String image, int position) {
        PopupMenu popupMenu = new PopupMenu(context, more, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete this post");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    builder.setTitle("Delete Post");
                    builder.setIcon(R.drawable.ic_delete);
                    builder.setMessage("Are you sure to delete this post?");

                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deletePost(pid, image, position);
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.create().show();
                }

                return false;
            }
        });

        popupMenu.show();
    }

    private void deletePost(final String pid, String image, int position) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting this post...");
        progressDialog.show();

        if (image.equals("")) {
            Query query1 = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("postTime").equalTo(pid);

            query1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        dataSnapshot1.getRef().removeValue();

                        modelPosts.remove(position);

                    }

                    progressDialog.dismiss();
                    Toast.makeText(context, "Delete successfully!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Query query2 = notificationsDatabaseReference.orderByChild("postId").equalTo(pid);

            query2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        dataSnapshot.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(image);

            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("postTime").equalTo(pid);

                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                dataSnapshot1.getRef().removeValue();
                                modelPosts.remove(position);
                            }

                            progressDialog.dismiss();
                            Toast.makeText(context, "Delete successfully!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

            Query query = notificationsDatabaseReference.orderByChild("postId").equalTo(pid);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        dataSnapshot.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void setLikes(final MyHolder holder, final String pid, int position) {
        likeDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(pid).hasChild(myUid)) {
                    holder.likeTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_red, 0, 0, 0);

                    modelPosts.get(position).setLikeRemoveValue(dataSnapshot.child(pid).child(myUid).getValue().toString());
                }
                else {
                    holder.likeTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart, 0, 0, 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return modelPosts.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        ImageView image;
        TextView name, time, description, title, likes;
        ImageButton moreBtn;
        TextView likeTV, commentTV;
        LinearLayout likeCountLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatar = (CircleImageView) itemView.findViewById(R.id.picturetv);
            image = (ImageView) itemView.findViewById(R.id.pimagetv);
            name = (TextView) itemView.findViewById(R.id.unametv);
            time = (TextView) itemView.findViewById(R.id.utimetv);
            description = (TextView) itemView.findViewById(R.id.descript);
            title = (TextView) itemView.findViewById(R.id.postTitle);
            likes = (TextView) itemView.findViewById(R.id.likeCount);
            moreBtn = (ImageButton) itemView.findViewById(R.id.btnMore);
            likeTV = (TextView) itemView.findViewById(R.id.tvLike);
            commentTV = (TextView) itemView.findViewById(R.id.tvComment);
            likeCountLayout = (LinearLayout) itemView.findViewById(R.id.linearLayoutLikeCount);
        }
    }   
}
