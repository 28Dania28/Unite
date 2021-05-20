package com.dania.unite;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Result extends AppCompatActivity {

    DatabaseReference UsersRef, ResultRef;
    private String my_result, resultToSort, result_id;
    private TextView title, r2, r3;
    private List<ResultData> first_pref_list, second_pref_list, third_pref_list;
    private RecyclerView firstPrefRV,secondPrefRV,thirdPrefRV;
    FirebaseUser fuser;
    int Score = 0;
    String my_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM d, yyyy ");
        final String quesDate = currentDate.format(calForDate.getTime());
        ResultRef = FirebaseDatabase.getInstance().getReference().child("Results").child(quesDate);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        title = findViewById(R.id.destinyTitle);
        r2 = findViewById(R.id.r2);
        r3 = findViewById(R.id.r3);
        firstPrefRV = findViewById(R.id.first_pref);
        firstPrefRV.setHasFixedSize(true);
        firstPrefRV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        secondPrefRV = findViewById(R.id.second_pref);
        secondPrefRV.setHasFixedSize(true);
        secondPrefRV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        thirdPrefRV = findViewById(R.id.third_pref);
        thirdPrefRV.setHasFixedSize(true);
        thirdPrefRV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        my_id = fuser.getUid();
        first_pref_list = new ArrayList<>();
        second_pref_list = new ArrayList<>();
        third_pref_list = new ArrayList<>();
        UsersRef.child(my_id).child("Today's Result").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    my_result = dataSnapshot.getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ResultRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        resultToSort = snapshot.getKey();
                        if (my_result.equals(resultToSort)) {
                            for (DataSnapshot id_snapshot : dataSnapshot.child(resultToSort).getChildren()) {
                                ResultData user = id_snapshot.getValue(ResultData.class);
                                assert user != null;
                                if (!user.getId().equals(my_id)) {
                                    first_pref_list.add(user);
                                }
                            }

                        } else if (!my_result.equals(resultToSort)){
                            for (int i = 0; i < 10; i++) {
                                assert resultToSort != null;
                                if (resultToSort.charAt(i) == my_result.charAt(i)) {
                                    Score++;
                                }
                                }
                            checkPref(resultToSort);

                        }
                    }


                }
                FirstPrefResult firstPrefResult = new FirstPrefResult(getApplicationContext(), first_pref_list);
                firstPrefRV.setAdapter(firstPrefResult);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void checkPref(final String resultToSort) {

        if (Score == 9){
            ResultRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        for (DataSnapshot id9 : dataSnapshot.child(resultToSort).getChildren()){
                            ResultData user = id9.getValue(ResultData.class);
                            if (!user.getId().equals(my_id)){
                                second_pref_list.add(user);
                            }
                        }
                    }

                    SecondPrefResult secondPrefResult = new SecondPrefResult(getApplicationContext(), second_pref_list);
                    secondPrefRV.setAdapter(secondPrefResult);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            Score = 0;
        }else if (Score == 8){
            ResultRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        for (DataSnapshot id8 : dataSnapshot.child(resultToSort).getChildren()){
                            ResultData user = id8.getValue(ResultData.class);
                            if (!user.getId().equals(my_id)){
                                third_pref_list.add(user);
                            }
                        }
                    }

                    ThirdPrefResult thirdPrefResult = new ThirdPrefResult(getApplicationContext(), third_pref_list);
                    thirdPrefRV.setAdapter(thirdPrefResult);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Score = 0;
        }

    }
}