package com.dania.unite;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditPost extends AppCompatActivity {


    private String Post_Key, current_user_id;
    private CircleImageView post_profile_image;
    private TextView post_username , post_date , post_time;
    private EditText description_text;
    private String description = " ";
    private Button delete , save;
    private ImageView post_image;
    private DatabaseReference UsersRef, PostsRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        Post_Key = getIntent().getExtras().get("PostKey").toString();
        current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        post_profile_image = findViewById(R.id.post_profile_image);
        post_username = findViewById(R.id.post_user_name);
        post_date = findViewById(R.id.post_date);
        post_time = findViewById(R.id.post_time);
        description_text = findViewById(R.id.description_text);
        delete = findViewById(R.id.delete);
        save = findViewById(R.id.save);
        post_image = findViewById(R.id.post_image);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key);

        PostsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                post_username.setText(dataSnapshot.child("fullname").getValue().toString());
                post_date.setText(dataSnapshot.child("date").getValue().toString());
                post_time.setText(dataSnapshot.child("time").getValue().toString());
                if (dataSnapshot.child("description").exists()) {
                    description = dataSnapshot.child("description").getValue().toString();
                    description_text.setText(description);

                }else {
                    description_text.setText(description);
                }
                String dp = dataSnapshot.child("profileimage").getValue().toString();
                String image = dataSnapshot.child("postimage").getValue().toString();
                if (dp.equals("default")){
                    post_profile_image.setImageResource(R.drawable.ic_account_circle_grey_24dp);
                }else{
                    //Picasso.get().load(dp).into(post_profile_image);
                    Glide.with(getApplicationContext()).load(dp).thumbnail(0.1f).into(post_profile_image);
                }

                //Picasso.get().load(image).into(post_image);
                Glide.with(getApplicationContext()).load(image).thumbnail(0.1f).into(post_image);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostsRef.child("description").setValue(description_text.getText().toString());
                Toast.makeText(EditPost.this, "Post updated successfully.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostsRef.removeValue();
                Toast.makeText(EditPost.this, "Post deleted successfully.", Toast.LENGTH_SHORT).show();
                finish();

            }
        });


    }
}
