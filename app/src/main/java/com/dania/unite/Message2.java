package com.dania.unite;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dania.unite.Adapter.MessageAdapter;
import com.dania.unite.Model.Chat;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Message2 extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username, status_online;
    RelativeLayout rel_layout;

    String saveCurrentDate, saveCurrentTime, downloadUrl;
    LinearLayout lnr_box;
    FirebaseUser fuser;
    DatabaseReference reference;
    private StorageReference MessageImageStorageRef;
    String other_person_id, my_id, push_key;

    ImageButton btn_send, settings, add_btn;
    private ProgressDialog loadingBar;

    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mChat;
    private static int GALLERY_PICK = 1;
    RecyclerView recyclerView;

    private StorageTask uploadTask;

    Intent intent;
    ValueEventListener seenListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message2);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        loadingBar = new ProgressDialog(this);


        loadingBar.setMessage("Sending...");
        loadingBar.setCanceledOnTouchOutside(true);

        add_btn = findViewById(R.id.add_btn);
        settings = findViewById(R.id.settings);
        MessageImageStorageRef = FirebaseStorage.getInstance().getReference().child("Image_Messages");
        lnr_box = findViewById(R.id.lnr_box);

        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        status_online = findViewById(R.id.status_online);
        rel_layout = findViewById(R.id.rel_layout);

        intent = getIntent();
        final String userid = intent.getStringExtra("userid");
        other_person_id = userid;

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        my_id = fuser.getUid();

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallaryIntent = new Intent();
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                gallaryIntent.setType("image/*");
                startActivityForResult(gallaryIntent, GALLERY_PICK);
            }
        });

        lnr_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), chat_user.class);
                intent.putExtra("userid", userid);

                Pair[] pairs = new Pair[1];
                pairs[0] = new Pair<View, String>(profile_image, "image_to_show_transition");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Message2.this, pairs);

                startActivity(intent, options.toBundle());
            }
        });



        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), chat_user_setting.class);
                intent.putExtra("userid", userid);
                Pair[] pairs = new Pair[2];
                pairs[0] = new Pair<View, String>(profile_image, "profile_image_transition");
                pairs[1] = new Pair<View, String>(username, "username_transition");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Message2.this, pairs);

                startActivity(intent, options.toBundle());
            }    });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = text_send.getText().toString();
                if (!msg.equals("")){
                    sendMessage(fuser.getUid(), userid, msg);
                    text_send.setText("");

                }
                else {
                    Toast.makeText(Message2.this, "You can't send empty message.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String image = dataSnapshot.child("imageurl").getValue().toString();
                String user_name = dataSnapshot.child("username").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                username.setText(user_name);
                if (image.equals("default")){
                    profile_image.setImageResource(R.drawable.ic_account_circle_grey_24dp);
                }else{
                    Picasso.get().load(image).placeholder(R.drawable.ic_account_circle_grey_24dp).into(profile_image);
                }

                if (status.equals("online")){
                    status_online.setVisibility(View.VISIBLE);
                }else {
                    status_online.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        seenMessage(userid);


    }

    private void seenMessage(final String userid){

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReciever().equals(fuser.getUid()) && chat.getSender().equals(userid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void sendMessage(String sender, final String reciever, String message){

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM d, yyyy ");
        saveCurrentDate = currentDate.format(calForDate.getTime());


        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("h:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Long timestamp = System.currentTimeMillis();

        String messageType = "Text";


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("time",saveCurrentTime);
        hashMap.put("date",saveCurrentDate);
        hashMap.put("reciever",reciever);
        hashMap.put("Type", messageType);
        hashMap.put("timestamp",timestamp);
        hashMap.put("message", message);
        hashMap.put("isseen", false);

        reference.child("Chats").push().setValue(hashMap);


    }



    private void readMessage(final String myid, final String userid, final String imageurl){
        mChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat =  snapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReciever().equals(myid) && chat.getSender().equals(userid) || chat.getReciever().equals(userid) &&chat.getSender().equals(myid)){
                        mChat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(Message2.this, mChat);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }





    @Override
    protected void onResume() {
        super.onResume();
        Status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        Status("offline");
    }


    private void Status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Message2.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

            case R.id.main2:
                startActivity(new Intent(Message2.this, Main3Activity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));


                return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();

            String message_receiver_ref = "Messages/" + other_person_id + "/" + my_id;
            String message_sender_ref = "Messages/" + my_id + "/" + other_person_id;

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("MMM d, yyyy ");
            final String saveCurrentDateImage = currentDate.format(calForDate.getTime());


            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("h:mm a");
            final String saveCurrentTimeImage = currentTime.format(calForTime.getTime());

            final Long timestampImage = System.currentTimeMillis();

            final StorageReference filepath = MessageImageStorageRef.child(timestampImage + my_id + other_person_id+ ".jpg");
            uploadTask = filepath.putFile(ImageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {

                        Uri uri = (Uri) task.getResult();
                        assert uri != null;
                        downloadUrl = uri.toString();
                        String messageType = "Image";
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", my_id);
                        hashMap.put("time",saveCurrentTimeImage);
                        hashMap.put("date",saveCurrentDateImage);
                        hashMap.put("reciever",other_person_id);
                        hashMap.put("Type", messageType);
                        hashMap.put("timestamp",timestampImage);
                        hashMap.put("message", downloadUrl);
                        hashMap.put("isseen", false);

                        push_key = reference.child("Chats").push().getKey();
                        reference.child("Chats").child(push_key).setValue(hashMap);


                        Toast.makeText(Message2.this, "Image send successfully", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(Message2.this, "Image not send successfully", Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }
    }
}

