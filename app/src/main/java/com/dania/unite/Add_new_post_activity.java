package com.dania.unite;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class Add_new_post_activity extends AppCompatActivity {

    private EditText post_description;
    private ImageButton add_image;
    private Button publish_post;
    private static final int GALLARY_PICK = 1;
    private Uri ImageUri;
    private Uri resultUri;
    private String Description, saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;
    private ProgressDialog loadingBar;

    private StorageReference PostsImageReference;
    private DatabaseReference UsersRef, PostsRef;
    private FirebaseAuth mAuth;

    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_post_activity);

        post_description = findViewById(R.id.post_description);
        add_image = findViewById(R.id.add_image);
        publish_post = findViewById(R.id.publish_post);
        PostsImageReference = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        loadingBar = new ProgressDialog(this);


        loadingBar.setMessage("Adding New Post");
        loadingBar.setCanceledOnTouchOutside(true);

        add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        try {
            Intent receivedIntent = getIntent();
            String receivedAction = receivedIntent.getAction();
            String receivedType = receivedIntent.getType();
            if (receivedAction.equals(Intent.ACTION_SEND)){
                if (receivedType.startsWith("image/")){
                    resultUri = (Uri)receivedIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                    add_image.setImageURI(resultUri);
                }
            }else {

            }
        }catch (Exception e){

        }



        publish_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });

    }

    private void ValidatePostInfo() {

        if (resultUri == null){
            Toast.makeText(this, "Please select an image to post", Toast.LENGTH_SHORT).show();
        }


        else if (TextUtils.isEmpty(post_description.getText())){
            Description = null;

        }

        else {

            Description = post_description.getText().toString();
         }
        loadingBar.show();
        StoringImageToFirebaseStorage();

    }

    private void StoringImageToFirebaseStorage() {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM d, yyyy ");
        saveCurrentDate = currentDate.format(calForDate.getTime());


        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("h:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());
        postRandomName = saveCurrentDate + saveCurrentTime;


        final StorageReference filePath = PostsImageReference.child("Post Images").child(resultUri.getLastPathSegment() + postRandomName + ".jpg");

        uploadTask = filePath.putFile(resultUri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {

                    Uri uri = (Uri) task.getResult();
                    assert uri != null;
                    downloadUrl = uri.toString();
                    SavingPostInformationToDatabase();



                }else {
                    String message = task.getException().getMessage();
                    Toast.makeText(Add_new_post_activity.this, "Error Occured: "+message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SavingPostInformationToDatabase() {

        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    String userFullName = dataSnapshot.child("username").getValue().toString();
                    String userProfileImage = dataSnapshot.child("imageurl").getValue().toString();
                    Long timestamp = System.currentTimeMillis();

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid",current_user_id);
                    postsMap.put("date",saveCurrentDate);
                    postsMap.put("time",saveCurrentTime);
                    postsMap.put("description",Description);
                    postsMap.put("postimage",downloadUrl);
                    postsMap.put("profileimage",userProfileImage);
                    postsMap.put("fullname",userFullName);
                    postsMap.put("timestamp",timestamp);
                    PostsRef.child( current_user_id + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful()){
                                        finish();
                                        loadingBar.dismiss();
                                    }else {
                                        Toast.makeText(Add_new_post_activity.this, "Error Occured while updating post", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }

                                }
                            });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void openGallery() {
        Intent gallaryIntent = new Intent();
        gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
        gallaryIntent.setType("image/*");
        startActivityForResult(gallaryIntent, GALLARY_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLARY_PICK && resultCode == RESULT_OK && data != null) {

            ImageUri = data.getData();
            CropImage.activity(ImageUri)
                    .start(this);
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                add_image.setImageURI(resultUri);
            }

        }



    }
}
