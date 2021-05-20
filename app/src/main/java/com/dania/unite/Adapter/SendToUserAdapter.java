package com.dania.unite.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dania.unite.ChattingActivity;
import com.dania.unite.FriendsData;
import com.dania.unite.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class SendToUserAdapter extends RecyclerView.Adapter<SendToUserAdapter.ViewHolder> {

    private Context mContext;
    private List<FriendsData> mUsers;
    private DatabaseReference UsersRef, ChatMsg;
    private Uri resultUri;
    String my_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public SendToUserAdapter(Context mContext, List<FriendsData> mUsers, Uri resultUri){
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.resultUri = resultUri;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatMsg = FirebaseDatabase.getInstance().getReference().child("Users").child(my_id).child("Chats");
        return new SendToUserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final FriendsData user = mUsers.get(position);
        if (user.getImageurl().equals("default")){
        }else {
            //Picasso.get().load(user.getImageurl()).placeholder(R.drawable.ic_account_circle_grey_24dp).into(holder.profile_image);
            Glide.with(mContext).load(user.getImageurl()).thumbnail(0.1f).into(holder.profile_image);
        }
        holder.username.setText(user.getUsername());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChattingActivity.class);
                intent.setData(resultUri);
                intent.putExtra("userid", user.getId());
                intent.putExtra("username",user.getUsername());
                intent.putExtra("imageurl",user.getImageurl());
                mContext.startActivity(intent);
            }
        });
        readUserOnce(user,holder);


    }
    private void readUserOnce(FriendsData user, final SendToUserAdapter.ViewHolder holder) {

        UsersRef.child(user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String user_name = dataSnapshot.child("username").getValue().toString();
                String image = dataSnapshot.child("imageurl").getValue().toString();

                holder.username.setText(user_name);

                if (image.equals("default")){
                }else {
                    //Picasso.get().load(image).placeholder(R.drawable.ic_account_circle_grey_24dp).into(holder.profile_image);
                    Glide.with(mContext).load(image).thumbnail(0.1f).into(holder.profile_image);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username, last_msg, msg_count;
        public ImageView profile_image,green_status;
        public RelativeLayout user_item_frame;



        public ViewHolder(View itemView) {
            super(itemView);

            last_msg = itemView.findViewById(R.id.last_msg);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            msg_count = itemView.findViewById(R.id.msg_count);
            green_status = itemView.findViewById(R.id.green_status);
            user_item_frame = itemView.findViewById(R.id.user_item_frame);
        }
    }




}

