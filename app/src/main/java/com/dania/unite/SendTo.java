package com.dania.unite;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.dania.unite.Adapter.FriendUserAdapter;
import com.dania.unite.Adapter.SendToUserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class SendTo extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<FriendsData> mUsers;
    FirebaseUser fuser;
    String my_id, userid,username,dp,profile_pic;
    DatabaseHelper mdatabaseHelper;
    Uri resultUri;
    Cursor c;
    private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_to);
        try {
            Intent receivedIntent = getIntent();
            String receivedAction = receivedIntent.getAction();
            String receivedType = receivedIntent.getType();
            if (receivedAction.equals(Intent.ACTION_SEND)){
                if (receivedType.startsWith("image/")){
                    resultUri = (Uri)receivedIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                    }
            }else {

            }
        }
        catch (Exception e){
            finish();
        }


        recyclerView = findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));



        fuser = FirebaseAuth.getInstance().getCurrentUser();
        my_id = fuser.getUid();




        mdatabaseHelper = new DatabaseHelper(getApplicationContext());
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(my_id).child("Friends");

        c = mdatabaseHelper.getFriendData("FriendsList");
        if (c.getCount()>0){
            if (c.moveToFirst()) {
                do {
                    userid = c.getString(1);
                    username = c.getString(2);
                    profile_pic = c.getString(3);
                    FriendsData m = new FriendsData(userid,username,profile_pic);
                    mUsers.add(m);
                } while (c.moveToNext());
            }
            SendToUserAdapter userAdapter = new SendToUserAdapter(getApplicationContext(), mUsers,resultUri);
            recyclerView.setAdapter(userAdapter);
        }
        else {
            readOnce();
        }

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int item_count = (int)dataSnapshot.getChildrenCount();
                if (item_count==c.getCount()){

                }else {
                    mdatabaseHelper.deleteAllFriends("FriendsList");
                    readOnce();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void readOnce() {

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        final FriendsData user = snapshot.getValue(FriendsData.class);
                        DatabaseReference dp_ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getId());
                        dp_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                dp = dataSnapshot.child("imageurl").getValue().toString();
                                boolean insertData = mdatabaseHelper.addFriendData(user.getId(),user.getUsername(),dp);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    addToArray();
                }else {

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });


    }

    private void addToArray() {
        mUsers.clear();
        c = mdatabaseHelper.getFriendData("FriendsList");
        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    userid = c.getString(1);
                    username = c.getString(2);
                    profile_pic = c.getString(3);
                    FriendsData m = new FriendsData(userid,username,profile_pic);
                    mUsers.add(m);
                } while (c.moveToNext());
            }
            SendToUserAdapter userAdapter = new SendToUserAdapter(getApplicationContext(), mUsers, resultUri);
            recyclerView.setAdapter(userAdapter);

        }
    }

}

