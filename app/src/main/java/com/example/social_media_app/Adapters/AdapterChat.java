package com.example.social_media_app.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.social_media_app.Models.ModelChat;
import com.example.social_media_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.Myholder>{
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPR_RIGHT = 1;
    private Context context;
    private List<ModelChat> list;
    private String imageurl;
    private FirebaseUser firebaseUser;

    public AdapterChat(Context context, List<ModelChat> list, String imageurl) {
        this.context = context;
        this.list = list;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_LEFT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return new Myholder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
            return new Myholder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, final int position) {
        String message = list.get(position).getMessage();
        String timeStamp = list.get(position).getTimeStamp();
        String type = list.get(position).getType();
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);

        calendar.setTimeInMillis(Long.parseLong(timeStamp));

        String timedate = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.message.setText(message);
        holder.message.setVisibility(View.VISIBLE);
        holder.time.setText(timedate);

        try {
            Glide.with(context).load(imageurl).into(holder.image);
        }
        catch (Exception e){

        }

        if(type.equals("text")){
            holder.message.setVisibility(View.VISIBLE);
            holder.mimage.setVisibility(View.GONE);
            holder.message.setText(message);
        }
        else {
            holder.message.setVisibility(View.GONE);
            holder.mimage.setVisibility(View.VISIBLE);
            Glide.with(context).load(message).into(holder.mimage);
        }

        holder.msgLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle("Delete Message");
                builder.setIcon(R.drawable.ic_delete);
                builder.setMessage("Are you sure to delete this message?");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(holder.getAdapterPosition());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();

                return false;
            }
        });

    }

    private void deleteMessage(int position) {
        final String myuid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgtimestmp = list.get(position).getTimeStamp();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Chats");

        Query query = databaseReference.orderByChild("timeStamp").equalTo(msgtimestmp);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    if(dataSnapshot1.child("sender").getValue().equals(myuid)) {
                        dataSnapshot1.getRef().removeValue();

                        list.remove(position);

                        notifyItemRemoved(position);

                        Toast.makeText(context,"Message Deleted!",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context,"You can only delete your own message!",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (list.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPR_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    class Myholder extends RecyclerView.ViewHolder{

        private CircleImageView image;
        private ImageView mimage;
        private TextView message,time;
        private LinearLayout msgLayout;

        public Myholder(@NonNull View itemView) {
            super(itemView);

            image = (CircleImageView) itemView.findViewById(R.id.profilec);
            message = (TextView) itemView.findViewById(R.id.msgc);
            time = (TextView) itemView.findViewById(R.id.timetv);
            msgLayout = (LinearLayout) itemView.findViewById(R.id.msglayout);
            mimage = (ImageView) itemView.findViewById(R.id.images);
        }
    }
}

