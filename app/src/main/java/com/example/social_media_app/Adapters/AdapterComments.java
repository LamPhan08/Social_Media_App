package com.example.social_media_app.Adapters;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.social_media_app.Models.ModelComments;
import com.example.social_media_app.ViewOtherProfilePageActivity;
import com.example.social_media_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder> {
    private Context context;
    private List<ModelComments> modelCommentsList;
    private DatabaseReference notificationsDatabaseReference;

    public AdapterComments(Context context, List<ModelComments> modelCommentsList, String myuid, String postid) {
        this.context = context;
        this.modelCommentsList = modelCommentsList;
        this.myuid = myuid;
        this.postid = postid;
        notificationsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
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
        String mPostId = modelCommentsList.get(position).getPostId();
        String hisUid = modelCommentsList.get(position).getUid();
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

        try {
            Glide.with(context).load(mAvatar).into(holder.avatar);
        }
        catch (Exception e) {

        }

        holder.comment.setVisibility(View.VISIBLE);
        holder.image.setVisibility(View.VISIBLE);

        if (mCommentImage.equals("")) {
            holder.image.setVisibility(View.GONE);
            holder.comment.setText(mComment);
        }
        else {
            try {
                holder.comment.setVisibility(View.GONE);
                Glide.with(context).load(mCommentImage).into(holder.image);
            }
            catch (Exception e) {

            }
        }

        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(holder.itemView.getContext(), ViewOtherProfilePageActivity.class);
                profileIntent.putExtra("uid", uid);
                holder.itemView.getContext().startActivity(profileIntent);
            }
        });

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(holder.itemView.getContext(), ViewOtherProfilePageActivity.class);
                profileIntent.putExtra("uid", uid);
                holder.itemView.getContext().startActivity(profileIntent);
            }
        });

        if (!myuid.equals(uid)) {
            holder.btnMore.setVisibility(View.GONE);
        }
        else {
            holder.btnMore.setVisibility(View.VISIBLE);
        }

        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOption(holder.btnMore, mCommentId, mCommentImage, holder.getAdapterPosition(), uid);
            }
        });
    }

    private void showOption(ImageButton btnMore, String mCommentId, String mCommentImage, int position, String uid) {
        PopupMenu popupMenu = new PopupMenu(context, btnMore, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete this comment");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    builder.setTitle("Delete Comment");
                    builder.setIcon(R.drawable.ic_delete);
                    builder.setMessage("Are you sure to delete this Comment?");

                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteComment(mCommentId, mCommentImage, position, uid);
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

    private void deleteComment(String mCommentId, String mCommentImage, int position, String uid) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting this comment...");
        progressDialog.show();

        if (mCommentImage.equals("")) {
            Query query = FirebaseDatabase.getInstance().getReference("Comments").orderByChild("commentId").equalTo(mCommentId);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        dataSnapshot1.getRef().removeValue();

                        modelCommentsList.remove(position);
                    }

                    if (myuid.equals(uid)) {
                        notificationsDatabaseReference.child(mCommentId).removeValue();
                    }

                    updateCommentsCount();

                    progressDialog.dismiss();

                    Toast.makeText(context, "Delete successfully!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        else {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(mCommentImage);

            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Query query = FirebaseDatabase.getInstance().getReference("Comments").orderByChild("commentId").equalTo(mCommentId);

                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                dataSnapshot1.getRef().removeValue();
                                modelCommentsList.remove(position);
                            }

                            if (myuid.equals(uid)) {
                                notificationsDatabaseReference.child(mCommentId).removeValue();
                            }

                            updateCommentsCount();

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
    }

    private boolean isDeleted = false;

    private void updateCommentsCount() {
        isDeleted = true;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isDeleted) {
                    String comments = "" + dataSnapshot.child("postComments").getValue();

                    int newComment = Integer.parseInt(comments) - 1;

                    databaseReference.child("postComments").setValue("" + newComment);

                    isDeleted = false;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return modelCommentsList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        ImageView image;
        TextView name, comment, time;
        ImageButton btnMore;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatar = (CircleImageView) itemView.findViewById(R.id.avatarComment);
            name = (TextView) itemView.findViewById(R.id.commentName);
            comment = (TextView) itemView.findViewById(R.id.commentText);
            time = (TextView) itemView.findViewById(R.id.commentTime);
            image = (ImageView) itemView.findViewById(R.id.imageComment);
            btnMore = (ImageButton) itemView.findViewById(R.id.btnCommentMore);
        }
    }
}
