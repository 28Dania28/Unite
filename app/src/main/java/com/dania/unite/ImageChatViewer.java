package com.dania.unite;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;

import ozaydin.serkan.com.image_zoom_view.ImageViewZoom;
import ozaydin.serkan.com.image_zoom_view.ImageViewZoomConfig;
import ozaydin.serkan.com.image_zoom_view.SaveFileListener;

public class ImageChatViewer extends AppCompatActivity {

    Intent intent;
    private ImageViewZoom image_to_show;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_chat_viewer);

        intent = getIntent();
        final String image_uri = intent.getStringExtra("image_uri");
        image_to_show = findViewById(R.id.image_to_show);
        ImageViewZoomConfig imageViewZoomConfig=new ImageViewZoomConfig();
        imageViewZoomConfig.saveProperty(true);

        imageViewZoomConfig.setGetImageFromGalery(true);


        ImageViewZoomConfig.ImageViewZoomConfigSaveMethod imageViewZoomConfigSaveMethod = ImageViewZoomConfig.ImageViewZoomConfigSaveMethod.onlyOnDialog; // You can use always
        imageViewZoomConfig.setImageViewZoomConfigSaveMethod(imageViewZoomConfigSaveMethod);

        image_to_show.setConfig(imageViewZoomConfig);

        image_to_show.saveImage(ImageChatViewer.this, "Unite", "ImageMsg", Bitmap.CompressFormat.JPEG, 1, imageViewZoomConfig,new SaveFileListener() {
            @Override
            public void onSuccess(File file) {
                Toast.makeText(ImageChatViewer.this,"Success",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFail(Exception excepti) {
                Toast.makeText(ImageChatViewer.this,"Error Save",Toast.LENGTH_SHORT).show();
            }
        });
        Picasso.get().load(image_uri).into(image_to_show);



    }
}
