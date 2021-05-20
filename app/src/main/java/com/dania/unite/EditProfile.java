package com.dania.unite;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

public class EditProfile extends AppCompatActivity {

    private TextView save;
    private MaterialEditText full_name, email_id;
    private String my_id;
    private DatabaseReference UsersRef;
    private FirebaseUser fUser;
    private ImageButton back_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        save = findViewById(R.id.save_info);
        full_name = findViewById(R.id.full_name);
        email_id = findViewById(R.id.email_id);
        back_button  = findViewById(R.id.back_button);
        my_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        email_id.setText(fUser.getEmail());
        UsersRef = FirebaseDatabase.getInstance().getReference("Users").child(my_id);
        UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                full_name.setText(dataSnapshot.child("username").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_name = full_name.getText().toString();
                if (TextUtils.isEmpty(txt_name)){
                    Toast.makeText(EditProfile.this, "Fill the Full Name", Toast.LENGTH_SHORT).show();
                }
                else {
                    UsersRef.child("username").setValue(txt_name).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EditProfile.this, "Profile info successfully updated.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
