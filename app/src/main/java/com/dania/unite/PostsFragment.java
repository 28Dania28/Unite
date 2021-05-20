package com.dania.unite;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dania.unite.Model.Chat;
import com.dania.unite.Model.DoubleClickListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
public class PostsFragment extends Fragment {


    private DatabaseReference reference;
    private FirebaseUser fuser;
    private String currentUserId;

    private DatabaseReference PostsRef, LikesRef,UsersRef;
    private RecyclerView user_post_list;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_posts2, container, false);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        user_post_list = view.findViewById(R.id.profile_posts_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        user_post_list.setLayoutManager(linearLayoutManager);
        user_post_list.setNestedScrollingEnabled(false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        fuser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        DisplayUserPosts();

        return view;


    }


    private void DisplayUserPosts() {

        Query SortPostsInDescendingOrder = PostsRef.orderByChild("timestamp");
        final FirebaseRecyclerAdapter<PostsData, PostsFragment.PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<PostsData, PostsFragment.PostsViewHolder>(
                        PostsData.class,
                        R.layout.all_posts_layout,
                        PostsFragment.PostsViewHolder.class,
                        SortPostsInDescendingOrder
                ) {
                    @Override
                    protected void populateViewHolder(final PostsFragment.PostsViewHolder viewHolder, PostsData model, final int position) {

                        final String PostKey = getRef(position).getKey();
                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setTime(model.getTime());
                        if (model.getDescription() == null) {
                            viewHolder.setDescription2(model.getDescription());
                        }else {

                            viewHolder.setDescription(model.getDescription());
                        }
                        viewHolder.setProfileimage(model.getProfileimage(),getContext());
                        viewHolder.setPostimage(model.getPostimage(),getContext());
                        if (model.getUid().equals(currentUserId)){
                            viewHolder.edit.setVisibility(View.VISIBLE);
                            viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getContext(), EditPost.class);
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

                        viewHolder.setLikeButtonStatus(PostKey);

                        viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(), CommentsActivity.class);
                                intent.putExtra("PostKey", PostKey);
                                startActivity(intent);

                            }
                        });

                        viewHolder.post_user_name.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(), UserProfileView.class);
                                intent.putExtra("userid", user_id);
                                startActivity(intent);

                            }
                        });

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

        public void setProfileimage(String profileimage, Context context) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);

            if (profileimage.equals("default")){
                image.setImageResource(R.drawable.ic_account_circle_grey_24dp);
            }else{
                //Picasso.get().load(profileimage).into(image);
                Glide.with(context).load(profileimage).thumbnail(0.1f).into(image);
            }
        }

        public void setDate(String date){
            TextView date1 = (TextView) mView.findViewById(R.id.post_date);
            date1.setText(date);

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


        public void setPostimage(String postimage, Context context) {
            ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
            //Picasso.get().load(postimage).placeholder(R.drawable.post_image_placeholder).into(post_image);
            Glide.with(context).load(postimage).thumbnail(0.1f).placeholder(R.drawable.post_image_placeholder).into(post_image);

        }


    }



}