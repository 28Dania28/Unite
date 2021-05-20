package com.dania.unite.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dania.unite.ChattingActivity;
import com.dania.unite.Model.User;
import com.dania.unite.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Strangers extends Fragment {

    private RecyclerView all_users_list;
    private DatabaseReference UsersRef;
    String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_strangers, container, false);
        all_users_list = view.findViewById(R.id.all_users_list);
        all_users_list.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        all_users_list.setLayoutManager(linearLayoutManager);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DisplayAllUsers();


        return view;
    }

    private void DisplayAllUsers() {
        FirebaseRecyclerAdapter<User, UsersViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<User, UsersViewHolder>
                        (
                                User.class,
                                R.layout.user_item,
                                UsersViewHolder.class,
                                UsersRef
                        ) {
                    @Override
                    protected void populateViewHolder(UsersViewHolder viewHolder, final User model, int position) {
                        String user = model.getId();

                        if (user.equals(currentUserId)) {
                            viewHolder.mView.setVisibility(View.GONE);

                        }
                        else {
                            viewHolder.mView.setVisibility(View.VISIBLE);
                            viewHolder.setUsername(model.getUsername());
                            viewHolder.setImageurl(model.getImageurl(),getContext());

                        }
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(), ChattingActivity.class);
                                intent.putExtra("userid", model.getId());
                                intent.putExtra("username",model.getUsername());
                                startActivity(intent);
                            }
                        });


                    }
                };

        all_users_list.setAdapter(firebaseRecyclerAdapter);


    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView username, last_msg;
        CircleImageView profile_image,green_status;
        String currentUserId;
        DatabaseReference UsersRef;


        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            last_msg = mView.findViewById(R.id.last_msg);
            username = mView.findViewById(R.id.username);
            profile_image = mView.findViewById(R.id.profile_image);
            green_status = mView.findViewById(R.id.green_status);

            UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }



        public void setUsername(String username){
            TextView UserName = (TextView) mView.findViewById(R.id.username);
            UserName.setText(username);
        }

        public void setImageurl(String imageurl, Context context){
            CircleImageView DP = mView.findViewById(R.id.profile_image);
            if (imageurl.equals("default")){
                DP.setImageResource(R.drawable.ic_account_circle_grey_24dp);
            }else{
                //Picasso.get().load(imageurl).placeholder(R.drawable.ic_account_circle_grey_24dp).into(DP);
                Glide.with(context).load(imageurl).placeholder(R.drawable.ic_account_circle_grey_24dp).into(DP);

            }

        }

    }
    }



