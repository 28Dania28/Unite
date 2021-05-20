package com.dania.unite;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.dania.unite.Model.Chat;
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

public class ImageMessagesViewer extends AppCompatActivity {

    Intent intent;
    private ImageViewZoom image_to_show;
    private DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_messages_viewer);
        intent = getIntent();
        final String push_key = intent.getStringExtra("push_key");
        image_to_show = findViewById(R.id.image_to_show);
        ImageViewZoomConfig imageViewZoomConfig=new ImageViewZoomConfig();
        imageViewZoomConfig.saveProperty(true);

        imageViewZoomConfig.setGetImageFromGalery(true);


        ImageViewZoomConfig.ImageViewZoomConfigSaveMethod imageViewZoomConfigSaveMethod = ImageViewZoomConfig.ImageViewZoomConfigSaveMethod.onlyOnDialog; // You can use always
        imageViewZoomConfig.setImageViewZoomConfigSaveMethod(imageViewZoomConfigSaveMethod);

        image_to_show.setConfig(imageViewZoomConfig);

        image_to_show.saveImage(ImageMessagesViewer.this, "Unite", "ImageMsg", Bitmap.CompressFormat.JPEG, 1, imageViewZoomConfig,new SaveFileListener() {
            @Override
            public void onSuccess(File file) {
                Toast.makeText(ImageMessagesViewer.this,"Success",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFail(Exception excepti) {
                Toast.makeText(ImageMessagesViewer.this,"Error Save",Toast.LENGTH_SHORT).show();
            }
        });



        reference = FirebaseDatabase.getInstance().getReference().child("Chats").child(push_key);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    Picasso.get().load(chat.getMessage()).into(image_to_show);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });

    }
}