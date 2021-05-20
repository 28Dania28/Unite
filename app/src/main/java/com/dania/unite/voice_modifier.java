package com.dania.unite;

import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class voice_modifier extends AppCompatActivity {

    private Button record,stop, play;
    private EditText set_frequency;
    File myFile = new File(Environment.getExternalStorageDirectory(),"test.pcm");
    private Boolean recording = false;
    private String currentUserId, image;
    private AudioTrack audioTrack;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_modifier);
        record = findViewById(R.id.record);
        stop = findViewById(R.id.stop);
        play = findViewById(R.id.play);
        set_frequency = findViewById(R.id.set_frequency);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        record.setEnabled(true);
        stop.setEnabled(false);
        play.setEnabled(true);


        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                image = dataSnapshot.child("imageurl").getValue().toString();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record.setEnabled(false);
                stop.setEnabled(true);
                play.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            recording= true;
                            startRecording();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recording = false;
                record.setEnabled(true);
                stop.setEnabled(false);
                play.setEnabled(true);
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(set_frequency.getText())) {
                    Toast.makeText(voice_modifier.this, "Enter Voice Frequency first.", Toast.LENGTH_SHORT).show();

                }else{

                    if (Integer.parseInt(set_frequency.getText().toString())>3999) {
                        if (myFile.exists()) {

                            record.setEnabled(true);
                            stop.setEnabled(false);
                            play.setEnabled(true);
                            try {
                                playRecording();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }else {
                        Toast.makeText(voice_modifier.this, "Frequency should be greater than 3999", Toast.LENGTH_SHORT).show();
                    }

                }
            }

        });



    }

    private void startRecording() throws IOException {
        File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"test.pcm");
        myFile.createNewFile();

        OutputStream outputStream = new FileOutputStream(myFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

        int minBufferSize = AudioRecord.getMinBufferSize(11025,2,2);
        short[] audioData = new short[minBufferSize];

        AudioRecord audioRecord = new AudioRecord(1,11025,2,2,minBufferSize);
        audioRecord.startRecording();

        while (recording){
            int numberOfShort = audioRecord.read(audioData,0,minBufferSize);
            for (int i=0;i<numberOfShort;i++){
                dataOutputStream.writeShort(audioData[i]);
            }
        }

        if (!recording.booleanValue()){
            audioRecord.stop();
            dataOutputStream.close();
        }


    }

    private void playRecording() throws IOException {

        int i = 0;

        int shortSizeInBytes = Short.SIZE / Byte.SIZE;
        int bufferSizeInBytes = (int) (myFile.length() / shortSizeInBytes);
        short[] audioData = new short[bufferSizeInBytes];

        InputStream inputStream = new FileInputStream(myFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

        int j=0;
        while (dataInputStream.available() > 0){
            audioData[j] = dataInputStream.readShort();
            j++;
        }
        dataInputStream.close();
        String frequency = set_frequency.getText().toString();
        i = Integer.parseInt(frequency);

        audioTrack = new AudioTrack(3,i,2,2,bufferSizeInBytes,1);
        audioTrack.play();
        audioTrack.write(audioData,0,bufferSizeInBytes);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recording = false;
        if (audioTrack!=null){
            audioTrack.release();
        }
    }
}
