package com.dania.unite;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import ozaydin.serkan.com.image_zoom_view.ImageViewZoom;
import ozaydin.serkan.com.image_zoom_view.ImageViewZoomConfig;
import ozaydin.serkan.com.image_zoom_view.SaveFileListener;

public class image_viewer_act extends AppCompatActivity {


    private ImageViewZoom image_to_show;
    TextView text_to_show;

    FirebaseUser fuser;
    DatabaseReference reference;

    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer_act);
        image_to_show = findViewById(R.id.image_to_show);
        text_to_show = findViewById(R.id.text_to_show);


        intent = getIntent();

        final String userid = intent.getStringExtra("userid");
        final String imageurl = intent.getStringExtra("imageurl");
        final String username = intent.getStringExtra("username");


        ImageViewZoomConfig imageViewZoomConfig=new ImageViewZoomConfig();
        imageViewZoomConfig.saveProperty(true);
        imageViewZoomConfig.setGetImageFromGalery(true);

        ImageViewZoomConfig.ImageViewZoomConfigSaveMethod imageViewZoomConfigSaveMethod = ImageViewZoomConfig.ImageViewZoomConfigSaveMethod.onlyOnDialog; // You can use always
        imageViewZoomConfig.setImageViewZoomConfigSaveMethod(imageViewZoomConfigSaveMethod);

        image_to_show.setConfig(imageViewZoomConfig);

        image_to_show.saveImage(image_viewer_act.this, "Unite", userid, Bitmap.CompressFormat.JPEG, 1, imageViewZoomConfig,new SaveFileListener() {
            @Override
            public void onSuccess(File file) {
                Toast.makeText(image_viewer_act.this,"Success",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFail(Exception excepti) {
                Toast.makeText(image_viewer_act.this,"Error Save",Toast.LENGTH_SHORT).show();
            }
        });
        text_to_show.setText(username);
        if (imageurl.equals("default")){
            image_to_show.setImageResource(R.drawable.ic_account_circle_grey_24dp);
        }else{
            Picasso.get().load(imageurl).into(image_to_show);
        }



        fuser = FirebaseAuth.getInstance().getCurrentUser();


        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String image = dataSnapshot.child("imageurl").getValue().toString();
                String user_name = dataSnapshot.child("username").getValue().toString();
                text_to_show.setText(user_name);
                if (image.equals("default")){
                    image_to_show.setImageResource(R.drawable.ic_account_circle_grey_24dp);
                }else{
                    Picasso.get().load(image).placeholder(R.drawable.white).into(image_to_show);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
}
