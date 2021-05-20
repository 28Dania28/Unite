package com.dania.unite;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FirstPrefResult extends RecyclerView.Adapter<FirstPrefResult.ViewHolder> {

    private Context mContext;
    private List<ResultData> first_pref_list;
    private DatabaseReference UsersRef;


    public FirstPrefResult(Context mContext, List<ResultData> first_pref_list){
        this.mContext = mContext;
        this.first_pref_list = first_pref_list;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return new FirstPrefResult.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final ResultData user = first_pref_list.get(position);

        UsersRef.child(user.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String user_name = dataSnapshot.child("username").getValue().toString();
                String image = dataSnapshot.child("imageurl").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                holder.username.setText(user_name);

                if (image.equals("default")){
                    holder.profile_image.setImageResource(R.drawable.ic_account_circle_grey_24dp);
                }else {

                    Picasso.get().load(image).placeholder(R.drawable.ic_account_circle_grey_24dp).into(holder.profile_image);

                }

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




        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChattingActivity.class);
                intent.putExtra("userid", user.getId());
                intent.putExtra("username",user.getId());
                mContext.startActivity(intent);

            }
        });

        holder.profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, image_viewer_act.class);
                intent.putExtra("userid", user.getId());
                Pair[] pairs = new Pair[2];
                pairs[0] = new Pair<View, String>(holder.profile_image, "image_to_show_transition");
                pairs[1] = new Pair<View, String>(holder.username, "text_to_show_transition");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity)mContext , pairs);

                mContext.startActivity(intent, options.toBundle());

            }
        });




    }

    @Override
    public int getItemCount() {
        return first_pref_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username, last_msg;
        public ImageView profile_image,green_status;


        public ViewHolder(View itemView) {
            super(itemView);

            last_msg = itemView.findViewById(R.id.last_msg);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            green_status = itemView.findViewById(R.id.green_status);
        }
    }


}

