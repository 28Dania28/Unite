package com.dania.unite;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dania.unite.Fragments.ProfileFragment;
import com.dania.unite.Model.DoubleClickListener;
import com.dania.unite.Model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.UserWriteRecord;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileView extends AppCompatActivity {


    private CircleImageView profile_image , relation_icon;
    private TextView username, friends_count, love_count, fame_no, relation_text;

    private DatabaseReference reference;
    private FirebaseUser fuser;
    private String currentUserId, userid, user_name, imageurl;
    private LinearLayout message, relation_status_btn;

    private Button add_as_friend, add_as_love,remove_as_friend, remove_as_love;

    private DatabaseReference PostsRef, LikesRef,UsersRef, FriendsRef, reference2, FriendsRef2, Friend, LoveRef, LoveRef2, Love;
    private RecyclerView user_post_list;
    Intent intent;
    Dialog myDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_view);
        profile_image = findViewById(R.id.profile_image);
        friends_count = findViewById(R.id.friends_count);
        love_count = findViewById(R.id.love_count);
        relation_icon = findViewById(R.id.relation_icon);
        myDialog = new Dialog(this);
        myDialog.setContentView(R.layout.relation_status_dialog);
        relation_text = findViewById(R.id.relation_text);
        relation_status_btn = findViewById(R.id.relation_status_btn);
        fame_no = findViewById(R.id.fame_no);
        username = findViewById(R.id.username);
        message = findViewById(R.id.message);
        add_as_friend = myDialog.findViewById(R.id.add_as_friend);
        add_as_love = myDialog.findViewById(R.id.add_as_love);
        remove_as_love = myDialog.findViewById(R.id.remove_as_love);
        remove_as_friend = myDialog.findViewById(R.id.remove_as_friend);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        user_post_list = findViewById(R.id.profile_posts_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        user_post_list.setLayoutManager(linearLayoutManager);
        user_post_list.setNestedScrollingEnabled(false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        intent = getIntent();

        userid = intent.getStringExtra("userid");
        user_name = intent.getStringExtra("username");
        imageurl = intent.getStringExtra("imageurl");
        username.setText(user_name);
        if (imageurl.equals("default")) {
        } else {
            //Picasso.get().load(imageurl).placeholder(R.drawable.ic_nav_person).into(profile_image);
            Glide.with(getApplicationContext()).load(imageurl).thumbnail(0.1f).into(profile_image);
        }

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                intent.putExtra("userid", userid);
                intent.putExtra("username",user_name);
                intent.putExtra("imageurl",imageurl);
                startActivity(intent);
            }
        });
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        Friend = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("Friends");
        Love = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("Love");
        FriendsRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("Friends").child(userid);
        FriendsRef2 = FirebaseDatabase.getInstance().getReference("Users").child(userid).child("Friends").child(currentUserId);
        reference2 = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        LoveRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("Love").child(userid);
        LoveRef2 = FirebaseDatabase.getInstance().getReference("Users").child(userid).child("Love").child(currentUserId);



        Friend.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userid)){
                    relation_text.setText("Friends");
                    relation_icon.setImageResource(R.drawable.ic_person_color_24dp);
                    add_as_friend.setVisibility(View.GONE);
                    remove_as_friend.setVisibility(View.VISIBLE);
                    remove_as_love.setVisibility(View.GONE);
                    add_as_love.setVisibility(View.VISIBLE);

                }
                else {
                    checkRelation2();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        relation_status_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove_as_friend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        FriendsRef.removeValue();
                        FriendsRef2.removeValue();

                    }
                });

                remove_as_love.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LoveRef.removeValue();
                        LoveRef2.removeValue();
                        addtofriend();
                    }
                });

                add_as_love.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        FriendsRef.removeValue();
                        FriendsRef2.removeValue();
                        addtoLove();


                    }
                });

                add_as_friend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        addtofriend();

                    }
                });


                myDialog.show();
            }
        });


        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String user_name = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                username.setText(user_name);

                if (dataSnapshot.child("Friends").exists()) {
                    int no_of_friends = (int) dataSnapshot.child("Friends").getChildrenCount();
                    friends_count.setText(Integer.toString(no_of_friends));
                }else{
                    friends_count.setText(Integer.toString(0));

                }

                if (dataSnapshot.child("Love").exists()){
                    int no_of_love = (int) dataSnapshot.child("Love").getChildrenCount();
                    love_count.setText(Integer.toString(no_of_love));
                }else{
                    love_count.setText(Integer.toString(0));

                }

                if (dataSnapshot.child("Likes").exists()){
                    String likes_no = dataSnapshot.child("Likes").getValue().toString();
                    int fame = Integer.parseInt(likes_no)/10 ;
                    fame_no.setText(Integer.toString(fame));
                }else{
                    fame_no.setText(Integer.toString(0));

                }

                String image = dataSnapshot.child("imageurl").getValue().toString();
                if (image.equals("default")) {
                } else {
                    //Picasso.get().load(image).placeholder(R.drawable.ic_nav_person).into(profile_image);
                    Glide.with(getApplicationContext()).load(image).thumbnail(0.1f).into(profile_image);
                }
                DisplayUserPosts();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), image_viewer_act.class);
                intent.putExtra("userid", userid);
                intent.putExtra("username",user_name);
                intent.putExtra("imageurl",imageurl);
                Pair[] pairs = new Pair[2];
                pairs[0] = new Pair<View, String>(profile_image, "image_to_show_transition");
                pairs[1] = new Pair<View, String>(username, "text_to_show_transition");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(UserProfileView.this, pairs);

                startActivity(intent, options.toBundle());

            }
        });



    }

    private void addtoLove() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String imageL1 = dataSnapshot.child("imageurl").getValue().toString();
                String user_nameL1 = dataSnapshot.child("username").getValue().toString();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("id", userid);
                hashMap.put("username", user_nameL1);
                hashMap.put("imageurl", imageL1);

                LoveRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    String imageL2 = dataSnapshot.child("imageurl").getValue().toString();
                                    String user_nameL2 = dataSnapshot.child("username").getValue().toString();
                                    HashMap<String, Object> hashMap2 = new HashMap<>();
                                    hashMap2.put("id", currentUserId);
                                    hashMap2.put("username", user_nameL2);
                                    hashMap2.put("imageurl", imageL2);

                                    LoveRef2.setValue(hashMap2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                            }

                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void addtofriend() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String imageF1 = dataSnapshot.child("imageurl").getValue().toString();
                String user_nameF1 = dataSnapshot.child("username").getValue().toString();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("id", userid);
                hashMap.put("username", user_nameF1);
                hashMap.put("imageurl", imageF1);

                FriendsRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String imageF2 = dataSnapshot.child("imageurl").getValue().toString();
                                    String user_nameF2 = dataSnapshot.child("username").getValue().toString();
                                    HashMap<String, Object> hashMap2 = new HashMap<>();
                                    hashMap2.put("id", currentUserId);
                                    hashMap2.put("username", user_nameF2);
                                    hashMap2.put("imageurl", imageF2);

                                    FriendsRef2.setValue(hashMap2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                            }

                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkRelation2() {
        Love.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(userid)){
                    relation_text.setText("Love");
                    relation_icon.setImageResource(R.drawable.ic_favorite_red_24dp);
                    add_as_friend.setVisibility(View.GONE);
                    remove_as_friend.setVisibility(View.GONE);
                    remove_as_love.setVisibility(View.VISIBLE);
                    add_as_love.setVisibility(View.GONE);


                }else {
                    relation_text.setText("Add Friend");
                    relation_icon.setImageResource(R.drawable.ic_person_add_black_24dp);
                    add_as_friend.setVisibility(View.VISIBLE);
                    remove_as_friend.setVisibility(View.GONE);
                    remove_as_love.setVisibility(View.GONE);
                    add_as_love.setVisibility(View.GONE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void DisplayUserPosts() {

        Query SortPostsInDescendingOrder = PostsRef.orderByChild("uid").equalTo(userid);
        final FirebaseRecyclerAdapter<PostsData, UserProfileView.PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<PostsData, UserProfileView.PostsViewHolder>(
                        PostsData.class,
                        R.layout.all_posts_layout,
                        UserProfileView.PostsViewHolder.class,
                        SortPostsInDescendingOrder
                ) {
                    @Override
                    protected void populateViewHolder(final UserProfileView.PostsViewHolder viewHolder, PostsData model, final int position) {

                        final String PostKey = getRef(position).getKey();
                        viewHolder.setFullname(model.getFullname());
                        String timeAgo = TimeAgo.getTimeAgo(model.getTimestamp());
                        if (timeAgo=="Nope") {
                            viewHolder.setTime(model.getDate()+" at "+model.getTime());
                        }else if(timeAgo == "yesterday at "){

                            viewHolder.setTime(timeAgo+model.getTime());
                        }
                        else {
                            viewHolder.setTime(timeAgo);
                        }

                        if (model.getDescription() == null) {
                            viewHolder.setDescription2(model.getDescription());
                        }else {

                            viewHolder.setDescription(model.getDescription());
                        }
                        viewHolder.setProfileimage(model.getProfileimage(),getApplicationContext());
                        viewHolder.setPostimage(model.getPostimage(),getApplicationContext());
                        if (model.getUid().equals(currentUserId)){
                            viewHolder.edit.setVisibility(View.VISIBLE);
                            viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getApplicationContext(), EditPost.class);
                                    intent.putExtra("PostKey", PostKey);
                                    startActivity(intent);
                                }
                            });
                        }  else {
                            viewHolder.edit.setVisibility(View.GONE);
                        }


                        UsersRef.child(model.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String status = dataSnapshot.child("status").getValue().toString();
                                if (status.equals("online")) {
                                    viewHolder.green_status.setVisibility(View.VISIBLE);
                                } else {
                                    viewHolder.green_status.setVisibility(View.GONE);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        final String user_id = model.getUid();
                        final String image_profile = model.getProfileimage();
                        final String post_username = model.getFullname();

                        viewHolder.setLikeButtonStatus(PostKey);

                        viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), CommentsActivity.class);
                                intent.putExtra("PostKey", PostKey);
                                startActivity(intent);

                            }
                        });

                        viewHolder.post_user_name.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                                intent.putExtra("userid", user_id);
                                intent.putExtra("username",user_name);
                                intent.putExtra("imageurl",imageurl);
                                startActivity(intent);

                            }
                        });

                        viewHolder.DisplayNoOfComments.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), CommentsActivity.class);
                                intent.putExtra("PostKey", PostKey);
                                startActivity(intent);

                            }
                        });
                        viewHolder.DisplayNoOfLikes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), LikesActivity.class);
                                intent.putExtra("PostKey", PostKey);
                                startActivity(intent);

                            }
                        });


                        viewHolder.post_profile_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), image_viewer_act.class);
                                intent.putExtra("userid", user_id);
                                intent.putExtra("username",post_username);
                                intent.putExtra("imageurl",image_profile);
                                Pair[] pairs = new Pair[2];
                                pairs[0] = new Pair<View, String>(viewHolder.post_profile_image, "image_to_show_transition");
                                pairs[1] = new Pair<View, String>(viewHolder.post_user_name, "text_to_show_transition");
                                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(UserProfileView.this, pairs);

                                startActivity(intent, options.toBundle());

                            }
                        });

                        viewHolder.post_image.setOnClickListener(new DoubleClickListener() {
                            @Override
                            public void onDoubleClick() {
                                final Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.bounce);
                                MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2,20);
                                animation.setInterpolator(interpolator);
                                viewHolder.LikePostButton.startAnimation(animation);
                                LikesRef.child(PostKey).child("Likes").child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild(fuser.getUid())) {
                                            LikesRef.child(PostKey).child("Likes").child("Likes").child(fuser.getUid()).removeValue();
                                        } else {
                                            Calendar calForDate = Calendar.getInstance();
                                            SimpleDateFormat currentDate = new SimpleDateFormat("MMM d, yyyy ");
                                            final String saveCurrentDate = currentDate.format(calForDate.getTime());


                                            Calendar calForTime = Calendar.getInstance();
                                            SimpleDateFormat currentTime = new SimpleDateFormat("h:mm a");
                                            final String saveCurrentTime = currentTime.format(calForTime.getTime());
                                            Long timestamp = System.currentTimeMillis();


                                            HashMap likesMap = new HashMap();
                                            likesMap.put("uid", fuser.getUid());
                                            likesMap.put("date", saveCurrentDate);
                                            likesMap.put("time", saveCurrentTime);
                                            likesMap.put("timestamp", timestamp);
                                            LikesRef.child(PostKey).child("Likes").child("Likes").child(fuser.getUid()).updateChildren(likesMap)
                                                    .addOnCompleteListener(new OnCompleteListener() {
                                                        @Override
                                                        public void onComplete(@NonNull Task task) {
                                                            if (task.isSuccessful()) {
                                                                viewHolder.LikePostButton.setImageResource(R.drawable.ic_favorite_red_24dp);

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

                        viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.bounce);
                                MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2,20);
                                animation.setInterpolator(interpolator);
                                viewHolder.LikePostButton.startAnimation(animation);
                                LikesRef.child(PostKey).child("Likes").child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild(fuser.getUid())) {
                                            LikesRef.child(PostKey).child("Likes").child("Likes").child(currentUserId).removeValue();
                                        } else {
                                            Calendar calForDate = Calendar.getInstance();
                                            SimpleDateFormat currentDate = new SimpleDateFormat("MMM d, yyyy ");
                                            final String saveCurrentDate = currentDate.format(calForDate.getTime());


                                            Calendar calForTime = Calendar.getInstance();
                                            SimpleDateFormat currentTime = new SimpleDateFormat("h:mm a");
                                            final String saveCurrentTime = currentTime.format(calForTime.getTime());
                                            Long timestamp = System.currentTimeMillis();


                                            HashMap likesMap = new HashMap();
                                            likesMap.put("uid", currentUserId);
                                            likesMap.put("date", saveCurrentDate);
                                            likesMap.put("time", saveCurrentTime);
                                            likesMap.put("timestamp", timestamp);
                                            LikesRef.child(PostKey).child("Likes").child("Likes").child(currentUserId).updateChildren(likesMap)
                                                    .addOnCompleteListener(new OnCompleteListener() {
                                                        @Override
                                                        public void onComplete(@NonNull Task task) {
                                                            if (task.isSuccessful()) {
                                                                viewHolder.LikePostButton.setImageResource(R.drawable.ic_favorite_red_24dp);
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


                    }

                };

        user_post_list.setAdapter(firebaseRecyclerAdapter);

    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageButton LikePostButton, CommentPostButton,edit;
        TextView DisplayNoOfLikes, post_user_name, DisplayNoOfComments;
        CircleImageView post_profile_image, green_status;
        String currentUserId;
        ImageView post_image;
        DatabaseReference LikesRef, CommentsRef;
        int countLikes, countComments;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            LikePostButton = (ImageButton) mView.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton) mView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = (TextView) mView.findViewById(R.id.likes_count);
            DisplayNoOfComments = (TextView) mView.findViewById(R.id.comments_count);
            green_status = (CircleImageView) mView.findViewById(R.id.green_status);
            CommentsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
            LikesRef = FirebaseDatabase.getInstance().getReference().child("Posts");
            edit = mView.findViewById(R.id.edit);
            post_image = (ImageView) mView.findViewById(R.id.post_image);
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            post_profile_image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            post_user_name = (TextView) mView.findViewById(R.id.post_user_name);


        }

        public void setLikeButtonStatus(final String PostKey){
            LikesRef.child(PostKey).child("Likes").child("Likes").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    if (dataSnapshot.exists()) {

                        if (dataSnapshot.hasChild(currentUserId)) {
                            countLikes = (int) dataSnapshot.getChildrenCount();
                            LikePostButton.setImageResource(R.drawable.ic_favorite_red_24dp);
                            DisplayNoOfLikes.setText(Integer.toString(countLikes) + (" Likes"));
                        } else {
                            countLikes = (int) dataSnapshot.getChildrenCount();
                            LikePostButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            DisplayNoOfLikes.setText(Integer.toString(countLikes) + (" Likes"));
                        }
                    }else {

                        LikePostButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                        DisplayNoOfLikes.setText(Integer.toString(0) + (" Likes"));
                    }

                    CommentsRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            countComments = (int) dataSnapshot.child(PostKey).child("Comments").getChildrenCount();
                            DisplayNoOfComments.setText(Integer.toString(countComments)+(" Comments"));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



        }


        public void setFullname(String fullname) {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(String profileimage, Context applicationContext) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);

            if (profileimage.equals("default")){
                image.setImageResource(R.drawable.ic_account_circle_grey_24dp);
            }else{
                //Picasso.get().load(profileimage).into(image);
                Glide.with(applicationContext).load(profileimage).thumbnail(0.1f).into(image);
            }
        }



        public void setTime(String time) {
            TextView time1 = (TextView) mView.findViewById(R.id.post_time);
            time1.setText(time);

        }

        public void setDescription(String description) {
            TextView description_text = mView.findViewById(R.id.post_description);
            description_text.setVisibility(View.VISIBLE);
            description_text.setText(description);
        }

        public void setDescription2(String description) {
            TextView description_text = mView.findViewById(R.id.post_description);
            description_text.setVisibility(View.GONE);
        }


        public void setPostimage(String postimage, Context applicationContext) {
            ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
            //Picasso.get().load(postimage).placeholder(R.drawable.post_image_placeholder).into(post_image);
            Glide.with(applicationContext).load(postimage).thumbnail(0.1f).placeholder(R.drawable.post_image_placeholder).into(post_image);

        }


    }



   }
