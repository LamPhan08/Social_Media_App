package com.example.social_media_app.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {
    private Context context;
    private String myuid;
    private DatabaseReference likeDatabaseReference, postDatabaseReference;
    boolean mprocesslike = false;

    public AdapterPosts(Context context, List<ModelPosts> modelPosts) {
        this.context = context;
        this.modelPosts = modelPosts;
        myuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        postDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Posts");
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
        final String mPostTime = modelPosts.get(position).getPostTime();
        String mAvatar = modelPosts.get(position).getUserAvatar();
        String mPostLikes = modelPosts.get(position).getPostLikes();
        final String mPostImage = modelPosts.get(position).getPostImage();
        String mPostComments = modelPosts.get(position).getPostComments();
        final String postId = modelPosts.get(position).getPostTime();

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(mPostTime));

        String timedate = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.name.setText(mName);
        holder.description.setText(mDescription);
        holder.time.setText(timedate);
        holder.likes.setText(mPostLikes);
        holder.comments.setText(mPostComments + " comments");

        setLikes(holder, mPostTime);

        try {
            Glide.with(context).load(mAvatar).into(holder.avatar);
        }
        catch (Exception e) {

        }

        holder.image.setVisibility(View.VISIBLE);

        try {
            Glide.with(context).load(mPostImage).into(holder.image);
        }
        catch (Exception e) {

        }

        holder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), PeopleWhoLiked.class);
                intent.putExtra("pid", postId);
                holder.itemView.getContext().startActivity(intent);
            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int plike = Integer.parseInt(modelPosts.get(position).getPostLikes());

                mprocesslike = true;

                final String postid = modelPosts.get(position).getPostTime();

                likeDatabaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mprocesslike) {
                            if (dataSnapshot.child(postid).hasChild(myuid)) {
                                postDatabaseReference.child(postid).child("postLikes").setValue("" + (plike - 1));
                                likeDatabaseReference.child(postid).child(myuid).removeValue();

                                mprocesslike = false;
                            } else {
                                postDatabaseReference.child(postid).child("postLikes").setValue("" + (plike + 1));
                                likeDatabaseReference.child(postid).child(myuid).setValue("Liked");

                                mprocesslike = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, uid, myuid, mPostTime, mPostImage);
            }
        });

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
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
                Intent profileIntent = new Intent(context, Other_Profile_Page.class);
                profileIntent.putExtra("uid", uid);
                holder.itemView.getContext().startActivity(profileIntent);
            }
        });
    }

    private void showMoreOptions(ImageButton more, String uid, String myuid, final String pid, final String image) {
        PopupMenu popupMenu = new PopupMenu(context, more, Gravity.END);

        if (uid.equals(myuid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete this post");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    builder.setTitle("Delete Post");
                    builder.setMessage("Are you sure to delete this post?");

                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deletePost(pid, image);
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

    private void deletePost(final String pid, String image) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting this post...");
        progressDialog.show();

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
    }

    private void setLikes(final MyHolder holder, final String pid) {
        likeDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(pid).hasChild(myuid)) {
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    holder.likeBtn.setText("Liked");
                } else {
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0);
                    holder.likeBtn.setText("Like");
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
        TextView name, time, description, likes, comments;
        ImageButton moreBtn;
        MaterialButton likeBtn, commentBtn;
        LinearLayout profile;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatar = (CircleImageView) itemView.findViewById(R.id.picturetv);
            image = (ImageView) itemView.findViewById(R.id.pimagetv);
            name = (TextView) itemView.findViewById(R.id.unametv);
            time = (TextView) itemView.findViewById(R.id.utimetv);
            description = (TextView) itemView.findViewById(R.id.descript);
            likes = (TextView) itemView.findViewById(R.id.plikeb);
            comments = (TextView) itemView.findViewById(R.id.pcommentco);
            moreBtn = (ImageButton) itemView.findViewById(R.id.btnMore);
            likeBtn = (MaterialButton) itemView.findViewById(R.id.btnLike);
            commentBtn = (MaterialButton) itemView.findViewById(R.id.btnComment);
            profile = (LinearLayout) itemView.findViewById(R.id.profilelayout);
        }
    }
}
