package com.dania.unite;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
public class MyService extends Service {
    private DatabaseReference notificationRef, UsersRef;
    String my_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
    public static boolean isRunning;
    int notificationId = 1;
    String channelId = "channel-01";
    String channelName = "Unite";
    int importance = NotificationManager.IMPORTANCE_HIGH;
    String name;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
                    notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications").child(my_id);
                    final NotificationManager notificationManager = (NotificationManager) MyService.this.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
                        notificationManager.createNotificationChannel(mChannel);
                    }
                    Intent intent = new Intent(MyService.this, Main3Activity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    final NotificationCompat.Builder mBuiler = new NotificationCompat.Builder(MyService.this, channelId)
                            .setSmallIcon(R.drawable.app_icon)
                            .setContentTitle("Unite")
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent);


                    notificationRef.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            final NotificationData notiData = dataSnapshot.getValue(NotificationData.class);
                            UsersRef.child(notiData.getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    name = dataSnapshot.child("username").getValue().toString();
                                    mBuiler.setContentText(name + " : " + notiData.getMessage());
                                    notificationManager.notify(notificationId, mBuiler.build());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            notificationRef.removeValue();

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
                    });
                }
            }

        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        }
}
