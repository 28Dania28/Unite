package com.dania.unite;

import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dania.unite.Adapter.FriendUserAdapter;
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


public class Friends extends Fragment {

    private RecyclerView recyclerView;
    private List<FriendsData> mUsers;
    private SwipeRefreshLayout refresh;
    FirebaseUser fuser;
    private TextView blankText;
    String my_id, userid,username,dp,profile_pic;
    DatabaseHelper mdatabaseHelper;
    Cursor c;
    private GifImageView loading;
    private DatabaseReference reference;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friends, container,false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refresh = view.findViewById(R.id.refresh);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        loading = view.findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);
        blankText = view.findViewById(R.id.blankText);
        my_id = fuser.getUid();




        mdatabaseHelper = new DatabaseHelper(getContext());
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
            FriendUserAdapter userAdapter = new FriendUserAdapter(getContext(), mUsers);
            recyclerView.setAdapter(userAdapter);
            loading.setVisibility(View.GONE);
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

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int item_count = (int)dataSnapshot.getChildrenCount();
                        if (item_count==c.getCount()){

                        }else {
                            mdatabaseHelper.deleteAllFriends("FriendsList");
                            readOnce();
                        }
                        refresh.setRefreshing(false);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });


        return view;

    }

    private void readOnce() {

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    blankText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        final FriendsData user = snapshot.getValue(FriendsData.class);
                        DatabaseReference dp_ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getId());
                        dp_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                dp = dataSnapshot.child("imageurl").getValue().toString();
                                String name = dataSnapshot.child("username").getValue().toString();
                                boolean insertData = mdatabaseHelper.addFriendData(user.getId(),name,dp);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    addToArray();
                }else {
                    blankText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
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
            FriendUserAdapter userAdapter = new FriendUserAdapter(getContext(), mUsers);
            recyclerView.setAdapter(userAdapter);
            loading.setVisibility(View.GONE);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

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

}
