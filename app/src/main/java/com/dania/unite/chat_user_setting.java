package com.dania.unite;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class chat_user_setting extends AppCompatActivity {



    CircleImageView profile_image;
    TextView username, status_ans,relation_status;
    String userid, my_id,user_name, imageurl;

    FirebaseUser fuser;
    DatabaseReference reference, FriendsRef, reference2, FriendsRef2, Friend, LoveRef, LoveRef2, Love;
    Button add_as_friend, add_as_love,remove_as_friend, remove_as_love;


    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_user_setting);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        status_ans = findViewById(R.id.status_ans);
        relation_status = findViewById(R.id.relation_status);
        add_as_friend = (Button) findViewById(R.id.add_as_friend);
        add_as_love = (Button) findViewById(R.id.add_as_love);
        remove_as_love = (Button) findViewById(R.id.remove_as_love);
        remove_as_friend = (Button) findViewById(R.id.remove_as_friend);

        intent = getIntent();

        userid = intent.getStringExtra("userid");
        user_name = intent.getStringExtra("username");
        imageurl = intent.getStringExtra("imageurl");
        username.setText(user_name);
        if (imageurl.equals("default")){
        }else{
            //Picasso.get().load(imageurl).placeholder(R.drawable.ic_account_circle_grey_24dp).into(profile_image);
            Glide.with(getApplicationContext()).load(imageurl).thumbnail(0.1f).into(profile_image);

        }
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        my_id = fuser.getUid();


        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference2 = FirebaseDatabase.getInstance().getReference("Users").child(my_id);
        Friend = FirebaseDatabase.getInstance().getReference("Users").child(my_id).child("Friends");
        Love = FirebaseDatabase.getInstance().getReference("Users").child(my_id).child("Love");
        FriendsRef = FirebaseDatabase.getInstance().getReference("Users").child(my_id).child("Friends").child(userid);
        FriendsRef2 = FirebaseDatabase.getInstance().getReference("Users").child(userid).child("Friends").child(my_id);

        LoveRef = FirebaseDatabase.getInstance().getReference("Users").child(my_id).child("Love").child(userid);
        LoveRef2 = FirebaseDatabase.getInstance().getReference("Users").child(userid).child("Love").child(my_id);



        Friend.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userid)){
                    add_as_friend.setVisibility(View.GONE);
                    remove_as_friend.setVisibility(View.VISIBLE);
                    remove_as_love.setVisibility(View.GONE);
                    add_as_love.setVisibility(View.VISIBLE);
                    relation_status.setText("Friend");
                }
                else {

                    checkRelation2();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String image = dataSnapshot.child("imageurl").getValue().toString();
                String user_name = dataSnapshot.child("username").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                username.setText(user_name);
                status_ans.setText(status);

                if (status.equals("online")){
                    status_ans.setTextColor(getResources().getColor(R.color.online_green));
                }
                else {
                    status_ans.setTextColor(getResources().getColor(R.color.Black5));

                }

                if (image.equals("default")){
                }else{
                    //Picasso.get().load(image).placeholder(R.drawable.ic_account_circle_grey_24dp).into(profile_image);
                    Glide.with(getApplicationContext()).load(image).thumbnail(0.1f).into(profile_image);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        remove_as_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FriendsRef.removeValue();
                FriendsRef2.removeValue();

            }
        });

        remove_as_love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoveRef.removeValue();
                LoveRef2.removeValue();
                addtofriend();
            }
        });

        add_as_love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FriendsRef.removeValue();
                FriendsRef2.removeValue();
                addtoLove();


            }
        });

        add_as_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addtofriend();

            }
        });



        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), image_viewer_act.class);
                intent.putExtra("userid", userid);
                intent.putExtra("username",user_name);
                intent.putExtra("imageurl",imageurl);
                Pair[] pairs = new Pair[2];
                pairs[0] = new Pair<View, String>(profile_image, "image_to_show_transition");
                pairs[1] = new Pair<View, String>(username, "text_to_show_transition");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(chat_user_setting.this, pairs);

                startActivity(intent, options.toBundle());

            }    });





    }

    private void addtoLove() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String imageL1 = dataSnapshot.child("imageurl").getValue().toString();
                String user_nameL1 = dataSnapshot.child("username").getValue().toString();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("id", userid);
                hashMap.put("username", user_nameL1);
                hashMap.put("imageurl", imageL1);

                LoveRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    String imageL2 = dataSnapshot.child("imageurl").getValue().toString();
                                    String user_nameL2 = dataSnapshot.child("username").getValue().toString();
                                    HashMap<String, Object> hashMap2 = new HashMap<>();
                                    hashMap2.put("id", my_id);
                                    hashMap2.put("username", user_nameL2);
                                    hashMap2.put("imageurl", imageL2);

                                    LoveRef2.setValue(hashMap2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                            }

                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void addtofriend() {

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String imageF1 = dataSnapshot.child("imageurl").getValue().toString();
                String user_nameF1 = dataSnapshot.child("username").getValue().toString();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("id", userid);
                hashMap.put("username", user_nameF1);
                hashMap.put("imageurl", imageF1);

                FriendsRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String imageF2 = dataSnapshot.child("imageurl").getValue().toString();
                                    String user_nameF2 = dataSnapshot.child("username").getValue().toString();
                                    HashMap<String, Object> hashMap2 = new HashMap<>();
                                    hashMap2.put("id", my_id);
                                    hashMap2.put("username", user_nameF2);
                                    hashMap2.put("imageurl", imageF2);

                                    FriendsRef2.setValue(hashMap2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                            }

                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkRelation2() {
        Love.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(userid)){
                    add_as_friend.setVisibility(View.GONE);
                    remove_as_friend.setVisibility(View.GONE);
                    remove_as_love.setVisibility(View.VISIBLE);
                    add_as_love.setVisibility(View.GONE);
                    relation_status.setText("Love");

                }else {
                    add_as_friend.setVisibility(View.VISIBLE);
                    remove_as_friend.setVisibility(View.GONE);
                    remove_as_love.setVisibility(View.GONE);
                    add_as_love.setVisibility(View.GONE);
                    relation_status.setText("Stranger");

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
