package com.dania.unite;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class chat_user extends AppCompatActivity {


    CircleImageView profile_image;
    TextView username;

    FirebaseUser fuser;
    DatabaseReference reference;


    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_user);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        intent = getIntent();

        final String userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();


        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String image = dataSnapshot.child("imageurl").getValue().toString();
                String user_name = dataSnapshot.child("username").getValue().toString();
                username.setText(user_name);
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

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), image_viewer_act.class);
                intent.putExtra("userid", userid); Pair[] pairs = new Pair[2];
                pairs[0] = new Pair<View, String>(profile_image, "image_to_show_transition");
                pairs[1] = new Pair<View, String>(username, "text_to_show_transition");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(chat_user.this, pairs);

                startActivity(intent, options.toBundle());
            }
        });





    }
}
