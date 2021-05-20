package com.dania.unite;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChattingActivity extends AppCompatActivity {

    private CircleImageView profile_image;
    private TextView username, status_online;
    private RelativeLayout rel_layout;
    private String seen,msg,time,date,status,type,sor,pk;
    private String saveCurrentDate, saveCurrentTime, downloadUrl;
    private LinearLayout lnr_box;
    private FirebaseUser fuser;
    private DatabaseReference reference, chat_ref,chat_ref_my, NotificationsReference;
    private StorageReference MessageImageStorageRef;
    private String other_person_id, my_id;
    private String userid, user_name, imageurl;
    int lastTopScrollPosition;
    int lastBottomScrollPosition;
    private DatabaseHelper mdatabaseHelper;
    private Cursor c;
    private ChildEventListener childEventListener;

    private ImageButton btn_send, settings, add_btn;
    private ProgressDialog loadingBar;

    private EditText text_send;

    private Joystick joystick;
    private ArrayList<Msg> m1;
    private static final int GALLERY_PICK = 1;
    private RecyclerView recyclerView;
    private String chat_id;

    private StorageTask uploadTask;

    private Intent intent;
    private ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);
        getWindow().setBackgroundDrawableResource(R.drawable.wallpaper3);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        user_name = intent.getStringExtra("username");
        imageurl = intent.getStringExtra("imageurl");


        MyService.isRunning = false;

        other_person_id = userid;

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        my_id = fuser.getUid();
        m1 = new ArrayList<>();

        chat_id = userid + my_id;

        mdatabaseHelper = new DatabaseHelper(this);
        joystick = findViewById(R.id.joystick);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        loadingBar = new ProgressDialog(this);
        lastTopScrollPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
        lastBottomScrollPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
        loadingBar.setMessage("Sending...");
        loadingBar.setCanceledOnTouchOutside(true);

        add_btn = findViewById(R.id.add_btn);
        settings = findViewById(R.id.settings);
        MessageImageStorageRef = FirebaseStorage.getInstance().getReference().child("Image_Messages");
        NotificationsReference = FirebaseDatabase.getInstance().getReference().child("Notifications");

        lnr_box = findViewById(R.id.lnr_box);

        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        status_online = findViewById(R.id.status_online);
        rel_layout = findViewById(R.id.rel_layout);
        username.setText(user_name);
        chat_ref = FirebaseDatabase.getInstance().getReference("Users").child(userid).child("Chats").child(my_id);
        chat_ref_my = FirebaseDatabase.getInstance().getReference("Users").child(my_id).child("Chats").child(other_person_id);


        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        try {
            Uri resultUri = intent.getData();
            sendUriMessage(resultUri);
        }catch (Exception e){

        }

        addInfoOnce();
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                readMsg();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


        c = mdatabaseHelper.getData(chat_id);
        if (c.getCount() > 0) {
            while (c.moveToNext()){
                seen = c.getString(1);
                msg = c.getString(2);
                time = c.getString(3);
                date = c.getString(4);
                status = c.getString(5);
                type = c.getString(6);
                sor = c.getString(7);
                pk = c.getString(8);
                Msg m = new Msg(seen,msg,time,date,status,type,sor,pk);
                m1.add(m);
            }
        }

        MsgAdapter msgAdapter = new MsgAdapter(ChattingActivity.this, m1);
        recyclerView.setAdapter(msgAdapter);
        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());

        readMsg();

        chat_ref_my.addChildEventListener(childEventListener);

        joystick.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {

            }

            @Override
            public void onDrag(float degrees, float offset) {

                while (degrees == 0) {
                    if (lastTopScrollPosition - 4 >= 0) {
                        recyclerView.smoothScrollToPosition(lastTopScrollPosition - 4);
                    } else {
                        recyclerView.smoothScrollToPosition(0);
                    }

                }
            }

            @Override
            public void onUp() {

            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastTopScrollPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
            }
        });


        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,GALLERY_PICK);
            }
        });


        lnr_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserProfileView.class);
                intent.putExtra("userid", userid);
                intent.putExtra("username",user_name);
                intent.putExtra("imageurl",imageurl);
                Pair[] pairs = new Pair[1];
                pairs[0] = new Pair<View, String>(profile_image, "image_to_show_transition");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(ChattingActivity.this, pairs);
                startActivity(intent, options.toBundle());
            }
        });


        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), chat_user_setting.class);
                intent.putExtra("userid", userid);
                intent.putExtra("username",user_name);
                intent.putExtra("imageurl",imageurl);
                Pair[] pairs = new Pair[2];
                pairs[0] = new Pair<View, String>(profile_image, "profile_image_transition");
                pairs[1] = new Pair<View, String>(username, "username_transition");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(ChattingActivity.this, pairs);
                startActivity(intent, options.toBundle());
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = text_send.getText().toString().trim();
                if (!msg.equals("")) {
                    AddMsg(msg, "Text");
                    text_send.setText("");

                } else {
                    Toast.makeText(ChattingActivity.this, "You can't send empty message.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        reference.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String status = dataSnapshot.getValue().toString();

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


    }

    private void sendUriMessage(Uri resultUri) {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM d, yyyy ");
        final String saveCurrentDateImage = currentDate.format(calForDate.getTime());


        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("h:mm a");
        final String saveCurrentTimeImage = currentTime.format(calForTime.getTime());

        final Long timestampImage = System.currentTimeMillis();

        final StorageReference filepath = MessageImageStorageRef.child(timestampImage + my_id + other_person_id + ".jpg");
        uploadTask = filepath.putFile(resultUri);
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
                    String push_key = chat_ref.push().getKey();
                    boolean insertData = mdatabaseHelper.addData(chat_id, "Sent", downloadUrl, saveCurrentTimeImage, saveCurrentDateImage, "sent", "Image", "send", push_key);
                    if (insertData) {
                        addToArray();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("userid", my_id);
                        hashMap.put("msg", downloadUrl);
                        hashMap.put("type", "Image");
                        hashMap.put("time", saveCurrentTimeImage);
                        hashMap.put("date", saveCurrentDateImage);
                        hashMap.put("status", "not delivered");
                        hashMap.put("push_key", push_key);
                        chat_ref.child(push_key).setValue(hashMap);
                        HashMap<String, String> notificationsData = new HashMap<>();
                        notificationsData.put("from", my_id);
                        notificationsData.put("message", msg);
                        NotificationsReference.child(other_person_id).push().setValue(notificationsData);

                    } else {
                        Toast.makeText(ChattingActivity.this, "Failed", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(ChattingActivity.this, "Image not send successfully", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void addInfoOnce() {

        if (imageurl.equals("default")){

        }else{
            //Picasso.get().load(imageurl).placeholder(R.drawable.ic_account_circle_grey_24dp).into(profile_image);
            Glide.with(getApplicationContext()).load(imageurl).thumbnail(0.1f).into(profile_image);
        }

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
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


    }

    private void readMsg() {
        chat_ref_my.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        MsgServer msgServer = snapshot.getValue(MsgServer.class);
                        if (msgServer.getStatus().equals("not delivered")) {
                            boolean insertData = mdatabaseHelper.addData(chat_id, "seen", msgServer.getMsg(), msgServer.getTime(), msgServer.getDate(), "seen", msgServer.getType(), "received",msgServer.getPush_key());
                            if (insertData) {

                                addToArray();
                                deleteNode(msgServer.getPush_key());


                            } else {

                            }
                        }
                        else {

                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void deleteNode(String push_key) {
        DatabaseReference dNode = FirebaseDatabase.getInstance().getReference("Users").child(my_id).child("Chats").child(other_person_id).child(push_key);
        dNode.removeValue();
    }

    private void addToArray() {
        m1.clear();
        c = mdatabaseHelper.getData(chat_id);
        if (c.getCount() > 0) {

            if (c.moveToFirst()) {
                do {
                    seen = c.getString(1);
                    msg = c.getString(2);
                    time = c.getString(3);
                    date = c.getString(4);
                    status = c.getString(5);
                    type = c.getString(6);
                    sor = c.getString(7);
                    pk = c.getString(8);
                    Msg m = new Msg(seen,msg,time,date,status,type,sor,pk);
                    m1.add(m);
                } while (c.moveToNext());
            }
            MsgAdapter msgAdapter = new MsgAdapter(ChattingActivity.this, m1);
            recyclerView.setAdapter(msgAdapter);
            recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
        }

    }


    private void AddMsg(String msg, String type) {


        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM d, yyyy ");
        saveCurrentDate = currentDate.format(calForDate.getTime());


        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("h:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());
        String push_key = chat_ref.push().getKey();
        boolean insertData = mdatabaseHelper.addData(chat_id, "Sent", msg, saveCurrentTime, saveCurrentDate, "sent", type, "send",push_key);
        if (insertData) {
            addToArray();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userid",my_id);
            hashMap.put("msg",msg);
            hashMap.put("type","Text");
            hashMap.put("time",saveCurrentTime);
            hashMap.put("date",saveCurrentDate);
            hashMap.put("status","not delivered");
            hashMap.put("push_key",push_key);
            chat_ref.child(push_key).setValue(hashMap);
            HashMap<String, String> notificationsData = new HashMap<>();
            notificationsData.put("from", my_id);
            notificationsData.put("message", msg);
            NotificationsReference.child(other_person_id).push().setValue(notificationsData);



        } else {
            Toast.makeText(ChattingActivity.this, "Failed", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();
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
                        String push_key = chat_ref.push().getKey();
                        boolean insertData = mdatabaseHelper.addData(chat_id, "Sent", downloadUrl, saveCurrentTimeImage, saveCurrentDateImage, "sent","Image", "send",push_key);
                        if (insertData) {
                            addToArray();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("userid",my_id);
                            hashMap.put("msg",downloadUrl);
                            hashMap.put("type","Image");
                            hashMap.put("time",saveCurrentTimeImage);
                            hashMap.put("date",saveCurrentDateImage);
                            hashMap.put("status","not delivered");
                            hashMap.put("push_key",push_key);
                            chat_ref.child(push_key).setValue(hashMap);
                            HashMap<String, String> notificationsData = new HashMap<>();
                            notificationsData.put("from", my_id);
                            notificationsData.put("message", msg);
                            NotificationsReference.child(other_person_id).push().setValue(notificationsData);

                        } else {
                            Toast.makeText(ChattingActivity.this, "Failed", Toast.LENGTH_SHORT).show();

                        }
                    }else{
                        Toast.makeText(ChattingActivity.this, "Image not send successfully", Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ChattingActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

            case R.id.main2:
                startActivity(new Intent(ChattingActivity.this, Main3Activity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));


                return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Status("online");
        MyService.isRunning = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Status("offline");
        chat_ref_my.removeEventListener(childEventListener);
        MyService.isRunning = true;
    }



    private void Status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(my_id);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

}


