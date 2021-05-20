package com.dania.unite;

import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class QuesRes extends AppCompatActivity {

    private TextView ques,info;
    private Button option1,option2,option3,option4;
    DatabaseReference UsersRef, quesRef, reference;
    private RelativeLayout lay2;
    private LinearLayout space;
    FirebaseUser fuser;
    String my_id, result = "", Question, Option1, Option2, Option3, Option4;
    int total = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ques_res);
        info = (TextView) findViewById(R.id.info);
        ques = (TextView) findViewById(R.id.ques);
        lay2 = (RelativeLayout) findViewById(R.id.lay2);
        space = (LinearLayout) findViewById(R.id.space);
        option1 = (Button) findViewById(R.id.option1);
        option2 = (Button) findViewById(R.id.option2);
        option3 = (Button) findViewById(R.id.option3);
        option4 = (Button) findViewById(R.id.option4);
        ques.setVisibility(View.VISIBLE);
        space.setVisibility(View.VISIBLE);
        lay2.setVisibility(View.VISIBLE);
        info.setVisibility(View.GONE);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        my_id = fuser.getUid();

        reference = FirebaseDatabase.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        quesRef = FirebaseDatabase.getInstance().getReference().child("Today's Questions").child("Questions");
        updateQuestion();
    }

    private void updateQuestion() {




        if (total < 11){

            quesRef.child(String.valueOf(total)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    QuesData quesSet = dataSnapshot.getValue(QuesData.class);
                    Question = quesSet.getQuestion();
                    Option1 = quesSet.getOption1();
                    Option2 = quesSet.getOption2();
                    Option3 = quesSet.getOption3();
                    Option4 = quesSet.getOption4();
                    ques.setText(quesSet.getQuestion());
                    option1.setText(quesSet.getOption1());
                    option2.setText(quesSet.getOption2());
                    option3.setText(quesSet.getOption3());
                    option4.setText(quesSet.getOption4());
                    option1.setBackgroundResource(R.drawable.options_lay);
                    option1.setTextColor(Color.parseColor("#000000"));
                    option2.setBackgroundResource(R.drawable.options_lay);
                    option2.setTextColor(Color.parseColor("#000000"));
                    option3.setBackgroundResource(R.drawable.options_lay);
                    option3.setTextColor(Color.parseColor("#000000"));
                    option4.setBackgroundResource(R.drawable.options_lay);
                    option4.setTextColor(Color.parseColor("#000000"));



                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            option1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    option1.setBackgroundResource(R.drawable.select_option_layout);
                    option1.setTextColor(Color.parseColor("#FFFFFF"));
                    result = result + "a";
                    UsersRef.child(my_id).child("Info").child(Question).setValue(Option1);
                    total++;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            updateQuestion();
                        }
                    },400);
                }
            });

            option2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    option2.setBackgroundResource(R.drawable.select_option_layout);
                    option2.setTextColor(Color.parseColor("#FFFFFF"));
                    result = result + "b";
                    UsersRef.child(my_id).child("Info").child(Question).setValue(Option2);
                    total++;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            updateQuestion();
                        }
                    },400);}
            });

            option3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    option3.setBackgroundResource(R.drawable.select_option_layout);
                    option3.setTextColor(Color.parseColor("#FFFFFF"));
                    result = result + "c";
                    UsersRef.child(my_id).child("Info").child(Question).setValue(Option3);
                    total++;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            updateQuestion();
                        }
                    },400);}
            });

            option4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    option4.setBackgroundResource(R.drawable.select_option_layout);
                    option4.setTextColor(Color.parseColor("#FFFFFF"));
                    result = result + "d";
                    UsersRef.child(my_id).child("Info").child(Question).setValue(Option4);
                    total++;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            updateQuestion();
                        }
                    },400);}
            });


        }else{

            doneForToday();

        }
    }

    private void doneForToday() {
        UsersRef.child(my_id).child("Today's Result").setValue(result);
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM d, yyyy ");
        final String saveCurrentDate = currentDate.format(calForDate.getTime());
        reference.child("Results").child(saveCurrentDate).child(result).child(my_id).child("id").setValue(my_id);
        ques.setVisibility(View.GONE);
        space.setVisibility(View.GONE);
        lay2.setVisibility(View.GONE);
        info.setVisibility(View.VISIBLE);

    }
}
