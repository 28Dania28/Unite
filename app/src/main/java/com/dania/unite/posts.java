package com.dania.unite;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import com.bumptech.glide.Glide;
import com.dania.unite.Model.DoubleClickListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.droidsonroids.gif.GifImageView;

public class posts extends Fragment {


    private ImageButton add_new_post;
    private RecyclerView postList;
    private DatabaseReference PostsRef, LikesRef, UsersRef;
    private GifImageView loading;
    private BottomNavigationView navigationView;
    private CircleImageView logo;
    private RelativeLayout post_top_layer;

    String currentUserId;


    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_posts, container, false);
        add_new_post = view.findViewById(R.id.add_new_post);
        postList = view.findViewById(R.id.all_users_post_list);
        logo = view.findViewById(R.id.logo);
        final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce2);
        add_new_post.startAnimation(animation);
        logo.startAnimation(animation);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        loading = view.findViewById(R.id.loading);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        loading.setVisibility(View.VISIBLE);
        navigationView = getActivity().findViewById(R.id.navigation);
        post_top_layer = view.findViewById(R.id.post_top_layer);
        postList.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef.keepSynced(true);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DisplayAllUsersPost();

        postList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy > 0 && navigationView.isShown()) {
                    navigationView.setVisibility(View.GONE);
                } else if (dy < 0) {
                    navigationView.setVisibility(View.VISIBLE);
                }


            }
        });


        add_new_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity().getApplication(), Add_new_post_activity.class);
                startActivity(intent);


            }
        });

        return view;
    }

    private void DisplayAllUsersPost() {

        Query SortPostsInDescendingOrder = PostsRef.orderByChild("timestamp");
        FirebaseRecyclerAdapter<PostsData, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<PostsData, PostsViewHolder>(
                        PostsData.class,
                        R.layout.all_posts_layout,
                        PostsViewHolder.class,
                        SortPostsInDescendingOrder
                ) {
                    @Override
                    protected void populateViewHolder(final PostsViewHolder viewHolder, final PostsData model, final int position) {

                        loading.setVisibility(View.GONE);
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
                        } else {

                            viewHolder.setDescription(model.getDescription());
                        }

                        viewHolder.setProfileimage(model.getProfileimage(),getContext());

                        if (model.getType().equals("video")){
                            viewHolder.post_video.setVisibility(View.VISIBLE);
                            viewHolder.post_image.setVisibility(View.GONE);
                            viewHolder.setPostvideo(model.getPostimage(), getContext());
                        }else {
                            viewHolder.post_image.setVisibility(View.VISIBLE);
                            viewHolder.post_video.setVisibility(View.GONE);
                            viewHolder.setPostimage(model.getPostimage(), getContext());
                        }

                        if (model.getUid().equals(currentUserId)) {
                            viewHolder.edit.setVisibility(View.VISIBLE);
                            viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getContext(), EditPost.class);
                                    intent.putExtra("PostKey", PostKey);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            viewHolder.edit.setVisibility(View.GONE);
                        }

                        UsersRef.child(model.getUid()).child("status").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String status = dataSnapshot.getValue().toString();


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

                        viewHolder.LikeOnce(PostKey);

                        LikesRef.child(PostKey).child("Likes").child("Likes").addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                viewHolder.LikeOnce(PostKey);
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                                viewHolder.LikeOnce(PostKey);
                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(), CommentsActivity.class);
                                intent.putExtra("PostKey", PostKey);
                                startActivity(intent);

                            }
                        });

                        if (user_id.equals(currentUserId)) {
                        } else {
                            viewHolder.post_user_name.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getContext(), UserProfileView.class);
                                    intent.putExtra("userid", user_id);
                                    intent.putExtra("username",model.getFullname());
                                    intent.putExtra("imageurl",model.getProfileimage());
                                    startActivity(intent);
                                }

                            });
                        }


                        viewHolder.DisplayNoOfComments.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(), CommentsActivity.class);
                                intent.putExtra("PostKey", PostKey);
                                startActivity(intent);

                            }
                        });

                        viewHolder.DisplayNoOfLikes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(), LikesActivity.class);
                                intent.putExtra("PostKey", PostKey);
                                startActivity(intent);

                            }
                        });


                        viewHolder.post_profile_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(), image_viewer_act.class);
                                intent.putExtra("userid", user_id);
                                intent.putExtra("username",model.getFullname());
                                intent.putExtra("imageurl",model.getProfileimage());
                                Pair[] pairs = new Pair[2];
                                pairs[0] = new Pair<View, String>(viewHolder.post_profile_image, "image_to_show_transition");
                                pairs[1] = new Pair<View, String>(viewHolder.post_user_name, "text_to_show_transition");
                                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), pairs);
                                startActivity(intent, options.toBundle());

                            }
                        });

                        viewHolder.post_image.setOnClickListener(new DoubleClickListener() {
                            @Override

                            public void onDoubleClick() {
                                final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);
                                MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
                                animation.setInterpolator(interpolator);
                                viewHolder.LikePostButton.startAnimation(animation);
                                LikesRef.child(PostKey).child("Likes").child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild(currentUserId)) {
                                            LikesRef.child(PostKey).child("Likes").child("Likes").child(currentUserId).removeValue();
                                            UsersRef.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.child("Likes").exists()) {
                                                        String number = dataSnapshot.child("Likes").getValue().toString();
                                                        int updated = Integer.parseInt(number) - 1;
                                                        UsersRef.child(user_id).child("Likes").setValue(updated);
                                                    } else {

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });


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
                                            UsersRef.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.child("Likes").exists()) {
                                                        String number = dataSnapshot.child("Likes").getValue().toString();
                                                        int updated = Integer.parseInt(number) + 1;
                                                        UsersRef.child(user_id).child("Likes").setValue(updated);
                                                    } else {

                                                        UsersRef.child(user_id).child("Likes").setValue("1");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

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
                                final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);
                                MyBounceInterpolator interpolator = new MyBounceInterpolator(0.5, 20);
                                animation.setInterpolator(interpolator);
                                viewHolder.LikePostButton.startAnimation(animation);
                                LikesRef.child(PostKey).child("Likes").child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild(currentUserId)) {
                                            LikesRef.child(PostKey).child("Likes").child("Likes").child(currentUserId).removeValue();
                                            UsersRef.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.child("Likes").exists()) {
                                                        String number = dataSnapshot.child("Likes").getValue().toString();
                                                        int updated = Integer.parseInt(number) - 1;
                                                        UsersRef.child(user_id).child("Likes").setValue(updated);
                                                    } else {

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });


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
                                            UsersRef.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.child("Likes").exists()) {
                                                        String number = dataSnapshot.child("Likes").getValue().toString();
                                                        int updated = Integer.parseInt(number) + 1;
                                                        UsersRef.child(user_id).child("Likes").setValue(updated);
                                                    } else {

                                                        UsersRef.child(user_id).child("Likes").setValue("1");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

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

        postList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageButton LikePostButton, CommentPostButton, edit;
        TextView DisplayNoOfLikes, post_user_name, DisplayNoOfComments;
        CircleImageView post_profile_image, green_status;
        String currentUserId;
        ImageView post_image;
        VideoView post_video;
        DatabaseReference LikesRef, CommentsRef;
        int countLikes, countComments;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            LikePostButton = mView.findViewById(R.id.like_button);
            CommentPostButton = mView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = mView.findViewById(R.id.likes_count);
            DisplayNoOfComments = mView.findViewById(R.id.comments_count);
            green_status = mView.findViewById(R.id.green_status);
            CommentsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
            LikesRef = FirebaseDatabase.getInstance().getReference().child("Posts");
            post_image = mView.findViewById(R.id.post_image);
            post_video = mView.findViewById(R.id.post_video);
            edit = mView.findViewById(R.id.edit);
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            post_profile_image = mView.findViewById(R.id.post_profile_image);
            post_user_name = mView.findViewById(R.id.post_user_name);


        }


        public void LikeOnce(final String postKey) {
            LikesRef.child(postKey).child("Likes").child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    if (dataSnapshot.exists()) {

                        if (dataSnapshot.hasChild(currentUserId)) {
                            countLikes = (int) dataSnapshot.getChildrenCount();
                            LikePostButton.setImageResource(R.drawable.ic_favorite_red_24dp);
                            DisplayNoOfLikes.setText(Integer.toString(countLikes));
                        } else {
                            countLikes = (int) dataSnapshot.getChildrenCount();
                            LikePostButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            DisplayNoOfLikes.setText(Integer.toString(countLikes));
                        }
                    } else {

                        LikePostButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                        DisplayNoOfLikes.setText(Integer.toString(0));
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            CommentsRef.child(postKey).child("Comments").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    countComments = (int) dataSnapshot.getChildrenCount();
                    DisplayNoOfComments.setText(Integer.toString(countComments));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }


        public void setFullname(String fullname) {
            TextView username = mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(String profileimage, Context context) {
            CircleImageView image = mView.findViewById(R.id.post_profile_image);

            if (profileimage.equals("default")) {
                image.setImageResource(R.drawable.ic_account_circle_grey_24dp);
            } else {
                Glide.with(context).load(profileimage).thumbnail(0.1f).into(image);
            }
        }



        public void setTime(String time) {
            TextView time1 = mView.findViewById(R.id.post_time);
            time1.setText(time);

        }

        public void setDescription(String description) {
            TextView description_text = mView.findViewById(R.id.post_description);
            description_text.setVisibility(View.VISIBLE);
            description_text.setText(description);
        }

        public void setPostimage(final String postimage, Context context) {
            final ImageView post_image = mView.findViewById(R.id.post_image);
            //Picasso.get().load(postimage).placeholder(R.drawable.post_image_placeholder).into(post_image);
            //Glide.with(context).load(postimage).placeholder(R.drawable.post_image_placeholder).into(post_image);
            Glide.with(context).load(postimage).thumbnail(0.1f).placeholder(R.drawable.white).into(post_image);

        }


        public void setDescription2(String description) {
            TextView description_text = mView.findViewById(R.id.post_description);
            description_text.setVisibility(View.GONE);
        }

        public void setPostvideo(String postimage, Context context) {
            VideoView post_video = mView.findViewById(R.id.post_video);
            Uri uri = Uri.parse(postimage);
            MediaController mc = new MediaController(context);
            mc.setAnchorView(post_video);
            post_video.setVideoURI(uri);
            post_video.start();
        }
    }

}
