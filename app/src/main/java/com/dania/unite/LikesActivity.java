package com.dania.unite;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class LikesActivity extends AppCompatActivity {

    private ImageButton go_back;
    private TextView DisplayNoOfLikes;
    private int countLikes;
    private String Post_Key, current_user_id;
    private FirebaseAuth mAuth;
    private DatabaseReference LikesRef, UsersRef;
    private RecyclerView likesList;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);

        DisplayNoOfLikes = (TextView) findViewById(R.id.likes_count);

        Post_Key = getIntent().getExtras().get("PostKey").toString();
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Likes").child("Likes");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        go_back = (ImageButton) findViewById(R.id.go_back);

        LikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    countLikes = (int) dataSnapshot.getChildrenCount();
                    DisplayNoOfLikes.setText(Integer.toString(countLikes)+(" Likes"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        likesList= (RecyclerView) findViewById(R.id.likes_recycler_view);
        likesList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        likesList.setLayoutManager(linearLayoutManager);
        FirebaseRecyclerAdapter<LikesData, LikesViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<LikesData, LikesViewHolder>
                        (
                                LikesData.class,
                                R.layout.like_user_item,
                                LikesViewHolder.class,
                                LikesRef
                        ) {
                    @Override
                    protected void populateViewHolder(final LikesViewHolder viewHolder, LikesData model, int position) {

                        UsersRef.child(model.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    String user_profile_image = dataSnapshot.child("imageurl").getValue().toString();
                                    String username = dataSnapshot.child("username").getValue().toString();
                                    viewHolder.setUsername(username);
                                    viewHolder.setUserProfile(user_profile_image,getApplicationContext());


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });



                    }
                };

        likesList.setAdapter(firebaseRecyclerAdapter);




        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public static class LikesViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public LikesViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username){
            TextView UserName = (TextView) mView.findViewById(R.id.username);
            UserName.setText(username);
        }

        public void setUserProfile(String image, Context applicationContext){
            CircleImageView user_dp = (CircleImageView) mView.findViewById(R.id.profile_image);

            if (image.equals("default")){
                user_dp.setImageResource(R.drawable.ic_account_circle_grey_24dp);
            }else{
                //Picasso.get().load(image).into(user_dp);
                Glide.with(applicationContext).load(image).thumbnail(0.1f).into(user_dp);
            }

        }


    }
    }
