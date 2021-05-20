package com.dania.unite;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private ImageButton go_back, PostCommentButton;
    private RecyclerView CommentsList;
    private Button emoji_love, emoji_fire, emoji_face1, emoji_face2, emoji_face3, emoji_face4, emoji_clap, emoji_ok, cancel_btn, delete_btn;
    private EditText CommentInputText;
    private DatabaseReference UsersRef, PostsRef, LikesRef, CommentsRef;
    private FirebaseAuth mAuth;
    private String post_key_global;
    private TextView DisplayNoOfLikes;
    private RelativeLayout top_layout;
    private int countLikes, countComments;
    Dialog myDialog;

    private String Post_Key, current_user_id, user_profile_pic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        DisplayNoOfLikes = (TextView) findViewById(R.id.likes_count);
        emoji_love = (Button) findViewById(R.id.emoji_love);
        emoji_fire = (Button) findViewById(R.id.emoji_fire);
        emoji_face1 = (Button) findViewById(R.id.emoji_face1);
        emoji_face2 = (Button) findViewById(R.id.emoji_face2);
        myDialog = new Dialog(this);
        myDialog.setContentView(R.layout.delete_comment_dialog);
        cancel_btn = myDialog.findViewById(R.id.cancel_btn);
        delete_btn = myDialog.findViewById(R.id.delete_btn);
        emoji_face3 = (Button) findViewById(R.id.emoji_face3);
        emoji_face4 = (Button) findViewById(R.id.emoji_face4);
        emoji_clap = (Button) findViewById(R.id.emoji_clap);
        emoji_ok = (Button) findViewById(R.id.emoji_ok);

        top_layout = (RelativeLayout) findViewById(R.id.top_layout);
        Post_Key = getIntent().getExtras().get("PostKey").toString();
        post_key_global = Post_Key;
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Likes").child("Likes");
        CommentsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");


        mAuth = FirebaseAuth.getInstance();
        go_back = (ImageButton) findViewById(R.id.go_back);
        current_user_id = mAuth.getCurrentUser().getUid();
        CommentInputText = (EditText) findViewById(R.id.comment_input);
        PostCommentButton = (ImageButton) findViewById(R.id.comment_send_button);


        LikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    countLikes = (int) dataSnapshot.getChildrenCount();
                    DisplayNoOfLikes.setText(Integer.toString(countLikes)+(" Likes"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        CommentsList= (RecyclerView) findViewById(R.id.comments_recycler_view);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        CommentsList.setLayoutManager(linearLayoutManager);

        Query SortPostsInDescendingOrder = PostsRef.orderByChild("timestamp");


        FirebaseRecyclerAdapter<CommentsData, CommentsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<CommentsData, CommentsViewHolder>
                (
                        CommentsData.class,
                        R.layout.all_post_comments,
                        CommentsViewHolder.class,
                        SortPostsInDescendingOrder
                ) {
            @Override
            protected void populateViewHolder(final CommentsViewHolder viewHolder, CommentsData model, int position) {

                viewHolder.setUsername(model.getUsername());
                viewHolder.setComment(model.getComment());
                final String CommentKey = getRef(position).getKey();
                String timeAgo = TimeAgoComments.getTimeAgo(model.getTimestamp());
                viewHolder.setTime(timeAgo);
                UsersRef.child(model.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String user_profile_image = dataSnapshot.child("imageurl").getValue().toString();
                            viewHolder.setUserProfile(user_profile_image,getApplicationContext());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                viewHolder.LikeOnce(CommentKey,PostsRef);
                PostsRef.child(CommentKey).child("Likes").child("Likes").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        viewHolder.LikeOnce(CommentKey, PostsRef);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        viewHolder.LikeOnce(CommentKey, PostsRef);
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
                viewHolder.like_comment_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
                        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2,20);
                        animation.setInterpolator(interpolator);
                        viewHolder.like_comment_btn.startAnimation(animation);
                        PostsRef.child(CommentKey).child("Likes").child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(current_user_id)){
                                        PostsRef.child(CommentKey).child("Likes").child("Likes").child(current_user_id).removeValue();
                                    }else {
                                        Calendar calForDate = Calendar.getInstance();
                                        SimpleDateFormat currentDate = new SimpleDateFormat("MMM d, yyyy ");
                                        final String saveCurrentDate = currentDate.format(calForDate.getTime());


                                        Calendar calForTime = Calendar.getInstance();
                                        SimpleDateFormat currentTime = new SimpleDateFormat("h:mm a");
                                        final String saveCurrentTime = currentTime.format(calForTime.getTime());
                                        Long timestamp = System.currentTimeMillis();


                                        HashMap likesMap = new HashMap();
                                        likesMap.put("uid", current_user_id);
                                        likesMap.put("date", saveCurrentDate);
                                        likesMap.put("time", saveCurrentTime);
                                        likesMap.put("timestamp", timestamp);
                                        PostsRef.child(CommentKey).child("Likes").child("Likes").child(current_user_id).updateChildren(likesMap)
                                                .addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        if (task.isSuccessful()) {
                                                            viewHolder.like_comment_btn.setImageResource(R.drawable.ic_favorite_red_24dp);
                                                        } else {


                                                        }
                                                    }
                                                });

                                    }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });

                if (model.getUid().equals(current_user_id)) {
                    viewHolder.comment_holder_round.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            delete_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PostsRef.child(CommentKey).removeValue();
                                    myDialog.dismiss();
                                }
                            });

                            cancel_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    myDialog.dismiss();
                                }
                            });
                            myDialog.show();
                            return false;
                        }
                    });
                }
                else {

                }

            }
        };
        CommentsList.setAdapter(firebaseRecyclerAdapter);

        emoji_love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String userName = dataSnapshot.child("username").getValue().toString();
                            String emoji = "❤";
                            sendEmoji(userName, emoji);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

               }
        });

        emoji_fire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String userName = dataSnapshot.child("username").getValue().toString();
                            String emoji = "\uD83D\uDD25";
                            sendEmoji(userName, emoji);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        emoji_face1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String userName = dataSnapshot.child("username").getValue().toString();
                            String emoji = "\uD83D\uDE0D";
                            sendEmoji(userName, emoji);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        emoji_face2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String userName = dataSnapshot.child("username").getValue().toString();
                            String emoji = "\uD83D\uDE00";
                            sendEmoji(userName, emoji);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        emoji_face3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String userName = dataSnapshot.child("username").getValue().toString();
                            String emoji = "\uD83D\uDE02";
                            sendEmoji(userName, emoji);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        emoji_face4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String userName = dataSnapshot.child("username").getValue().toString();
                            String emoji = "\uD83D\uDE32";
                            sendEmoji(userName, emoji);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        emoji_clap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String userName = dataSnapshot.child("username").getValue().toString();
                            String emoji = "\uD83D\uDC4F";
                            sendEmoji(userName, emoji);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        emoji_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String userName = dataSnapshot.child("username").getValue().toString();
                            String emoji = "\uD83D\uDC4C";
                            sendEmoji(userName, emoji);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });



        DisplayNoOfLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LikesActivity.class);
                intent.putExtra("PostKey", Post_Key);
                startActivity(intent);

            }
        });

        top_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LikesActivity.class);
                intent.putExtra("PostKey", Post_Key);
                startActivity(intent);

            }
        });

        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       if (dataSnapshot.exists()){
                           String userName = dataSnapshot.child("username").getValue().toString();
                           ValidateComment(userName);
                           CommentInputText.setText(null);
                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {

                   }
               });
            }
        });

    }

    private void sendEmoji(String userName, String emoji) {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM d, yyyy ");
        final String saveCurrentDate = currentDate.format(calForDate.getTime());


        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("h:mm a");
        final String saveCurrentTime = currentTime.format(calForTime.getTime());
        final String RandomKey = current_user_id + saveCurrentDate + saveCurrentTime;
        Long timestamp = System.currentTimeMillis();


        HashMap commentsMap = new HashMap();
        commentsMap.put("uid",current_user_id);
        commentsMap.put("comment",emoji);
        commentsMap.put("date",saveCurrentDate);
        commentsMap.put("time",saveCurrentTime);
        commentsMap.put("username",userName);
        commentsMap.put("timestamp",timestamp);
        PostsRef.child(RandomKey).updateChildren(commentsMap)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){

                        }
                        else {

                            Toast.makeText(CommentsActivity.this,"Error Occurred, Try Again", Toast.LENGTH_SHORT).show();

                        }
                    }
                });



    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{

        ImageButton like_comment_btn;
        View mView;
        DatabaseReference PostsRef2;
        String currentUserId;
        TextView comment_like_no, comment;
        ImageView like_image;
        RelativeLayout comment_holder_round;
        int countLikes;

        public CommentsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            like_comment_btn = mView.findViewById(R.id.like_comment_btn);
            PostsRef2 = FirebaseDatabase.getInstance().getReference().child("Posts");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            comment_like_no = mView.findViewById(R.id.comment_like_no);
            like_image = mView.findViewById(R.id.like_image);
            comment_holder_round = mView.findViewById(R.id.comment_whole);
            comment = mView.findViewById(R.id.comment);

        }

        public void setUsername(String username){
            TextView UserName = (TextView) mView.findViewById(R.id.comment_user_name);
            UserName.setText(username);
        }

        public void setTime(String time){


            TextView time1 = (TextView) mView.findViewById(R.id.comment_time);
            time1.setText(time);

        }


        public void setComment(String comment){

            TextView comment_text = (TextView) mView.findViewById(R.id.comment);
            comment_text.setText(comment);

        }

        public void setUserProfile(String image, Context applicationContext){
            CircleImageView user_dp = (CircleImageView) mView.findViewById(R.id.user_profile_image);

            if (image.equals("default")){
                user_dp.setImageResource(R.drawable.ic_account_circle_grey_24dp);
            }else{
                //Picasso.get().load(image).into(user_dp);
                Glide.with(applicationContext).load(image).into(user_dp);
            }

        }


        public void LikeOnce(String commentKey, DatabaseReference postsRef) {
            postsRef.child(commentKey).child("Likes").child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild(currentUserId)){
                            countLikes = (int) dataSnapshot.getChildrenCount();
                            like_comment_btn.setImageResource(R.drawable.ic_favorite_red_24dp);
                            comment_like_no.setText(Integer.toString(countLikes));
                            comment_like_no.setVisibility(View.VISIBLE);
                            like_image.setVisibility(View.VISIBLE);
                        }
                        else {
                            countLikes = (int) dataSnapshot.getChildrenCount();
                            like_comment_btn.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            comment_like_no.setText(Integer.toString(countLikes));
                            comment_like_no.setVisibility(View.VISIBLE);
                            like_image.setVisibility(View.VISIBLE);

                        }
                         } else {
                        like_comment_btn.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                        comment_like_no.setVisibility(View.GONE);
                        like_image.setVisibility(View.GONE);

                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void ValidateComment(String userName) {
        String commentText = CommentInputText.getText().toString();

        if (TextUtils.isEmpty(commentText)){
        }
        else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("MMM d, yyyy ");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());


            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("h:mm a");
            final String saveCurrentTime = currentTime.format(calForTime.getTime());
            Long timestamp = System.currentTimeMillis();
            final String RandomKey = current_user_id + timestamp;


            HashMap commentsMap = new HashMap();
            commentsMap.put("uid",current_user_id);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("username",userName);
            commentsMap.put("timestamp",timestamp);
            PostsRef.child(RandomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
}
                            else {

                                Toast.makeText(CommentsActivity.this,"Error Occurred, Try Again", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });



        }
    }
}
