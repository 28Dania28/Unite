package com.dania.unite.Adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dania.unite.ChattingActivity;
import com.dania.unite.FriendsData;
import com.dania.unite.MyBounceInterpolator;
import com.dania.unite.R;
import com.dania.unite.UserProfileView;
import com.dania.unite.chat_user_setting;
import com.dania.unite.image_viewer_act;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendUserAdapter extends RecyclerView.Adapter<FriendUserAdapter.ViewHolder> {

    private Context mContext;
    private List<FriendsData> mUsers;
    private DatabaseReference UsersRef, ChatMsg;
    CircleImageView profile_image;
    ImageButton setting;
    TextView username, friends_count , love_count, fame_no;
    Button view_profile, message;


    Dialog myDialog;

    String my_id = FirebaseAuth.getInstance().getCurrentUser().getUid();


    public FriendUserAdapter(Context mContext, List<FriendsData> mUsers){
        this.mUsers = mUsers;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public FriendUserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatMsg = FirebaseDatabase.getInstance().getReference().child("Users").child(my_id).child("Chats");
        myDialog = new Dialog(mContext);
        myDialog.setContentView(R.layout.profile_dialog);
        profile_image = myDialog.findViewById(R.id.profile_image);
        username = myDialog.findViewById(R.id.username);
        friends_count = myDialog.findViewById(R.id.friends_count);
        love_count = myDialog.findViewById(R.id.love_count);
        fame_no = myDialog.findViewById(R.id.fame_no);
        view_profile = myDialog.findViewById(R.id.view_profile);
        message = myDialog.findViewById(R.id.message);
        setting = myDialog.findViewById(R.id.setting);
        return new FriendUserAdapter.ViewHolder(view);
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
                intent.putExtra("userid", user.getId());
                intent.putExtra("username",user.getUsername());
                intent.putExtra("imageurl",user.getImageurl());
                mContext.startActivity(intent);

            }
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                myDialog.show();
                username.setText(user.getUsername());
                if (user.getImageurl().equals("default")){
                }else {
                    //Picasso.get().load(user.getImageurl()).placeholder(R.drawable.ic_account_circle_grey_24dp).into(profile_image);
                    Glide.with(myDialog.getContext()).load(user.getImageurl()).thumbnail(0.1f).into(profile_image);
                   }
                message.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, ChattingActivity.class);
                        intent.putExtra("userid", user.getId());
                        intent.putExtra("username",user.getUsername());
                        intent.putExtra("imageurl",user.getImageurl());
                        mContext.startActivity(intent);


                    }
                });

                view_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, UserProfileView.class);
                        intent.putExtra("userid", user.getId());
                        intent.putExtra("username",user.getUsername());
                        intent.putExtra("imageurl",user.getImageurl());
                        mContext.startActivity(intent);


                    }
                });

                setting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, chat_user_setting.class);
                        intent.putExtra("userid", user.getId());
                        intent.putExtra("username",user.getUsername());
                        intent.putExtra("imageurl",user.getImageurl());
                        mContext.startActivity(intent);

                    }
                });

                UsersRef.child(user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String user_name = dataSnapshot.child("username").getValue().toString();
                        String image = dataSnapshot.child("imageurl").getValue().toString();

                        username.setText(user_name);

                        if (image.equals("default")){
                            profile_image.setImageResource(R.drawable.ic_account_circle_grey_24dp);
                        }else {
                            //Picasso.get().load(image).placeholder(R.drawable.ic_account_circle_grey_24dp).into(profile_image);
                            Glide.with(mContext).load(image).thumbnail(0.1f).into(profile_image);

                        }
                        if (dataSnapshot.child("Friends").exists()) {
                            int no_of_friends = (int) dataSnapshot.child("Friends").getChildrenCount();
                            friends_count.setText(Integer.toString(no_of_friends));
                        }else{
                            friends_count.setText(Integer.toString(0));

                        }

                        if (dataSnapshot.child("Love").exists()){
                            int no_of_love = (int) dataSnapshot.child("Love").getChildrenCount();
                            love_count.setText(Integer.toString(no_of_love));
                        }else{
                            love_count.setText(Integer.toString(0));

                        }

                        if (dataSnapshot.child("Likes").exists()){
                            String likes_no = dataSnapshot.child("Likes").getValue().toString();
                            int fame = Integer.parseInt(likes_no)/10 ;
                            fame_no.setText(Integer.toString(fame));
                        }else{
                            fame_no.setText(Integer.toString(0));

                        }


                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                return false;
            }
        });

        holder.profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, image_viewer_act.class);
                intent.putExtra("userid", user.getId());
                intent.putExtra("username",user.getUsername());
                intent.putExtra("imageurl",user.getImageurl());
                Pair[] pairs = new Pair[2];
                pairs[0] = new Pair<View, String>(holder.profile_image, "image_to_show_transition");
                pairs[1] = new Pair<View, String>(holder.username, "text_to_show_transition");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity)mContext , pairs);
                mContext.startActivity(intent, options.toBundle());

            }
        });
        readUserOnce(user,holder);
        UsersRef.child(my_id).child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user.getId())){
                    long n = dataSnapshot.child(user.getId()).getChildrenCount();
                    holder.msg_count.setText(""+n+"");
                    holder.msg_count.setVisibility(View.VISIBLE);
                }else {
                    holder.msg_count.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UsersRef.child(user.getId()).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String status = dataSnapshot.getValue().toString();

                if (status.equals("online")){
                    holder.green_status.setVisibility(View.VISIBLE);
                }else {
                    holder.green_status.setVisibility(View.GONE);
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });










    }

    private void readUserOnce(FriendsData user, final ViewHolder holder) {

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
