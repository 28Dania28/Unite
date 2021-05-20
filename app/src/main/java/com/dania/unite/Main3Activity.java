package com.dania.unite;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.dania.unite.Fragments.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class Main3Activity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private BottomNavigationView navigation;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        wrap();
               }

    private void wrap() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser!=null){
            navigation = findViewById(R.id.navigation);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
            navigation.setSelectedItemId(R.id.navigation_home);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Home()).commit();
            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
            MyService.isRunning = true;
            startService(new Intent(this,MyService.class));
        }
        else {
            Intent intent = new Intent(Main3Activity.this, StartActivity.class);
            startActivity(intent);
            finish();
        }


    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            android.support.v4.app.Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_profile:
                    selectedFragment = new ProfileFragment();
                    break;



                case R.id.navigation_home:
                    setTheme(R.style.AppTheme2);
                    selectedFragment = new Home();
                    break;

                case R.id.navigation_posts:
                    selectedFragment = new posts();
                    break;

                case R.id.navigation_settings:
                    selectedFragment = new Settings();
                    break;

                case R.id.navigation_notification:
                    selectedFragment = new Notification();
                    break;

            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();

            return true;

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Status("online");
    }

    private void Status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);
    }
    @Override
    protected void onPause() {
        super.onPause();
        Status("offline");
    }







}
