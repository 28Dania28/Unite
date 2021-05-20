package com.dania.unite.Fragments;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dania.unite.CommentsActivity;
import com.dania.unite.EditPost;
import com.dania.unite.EditProfile;
import com.dania.unite.LikesActivity;
import com.dania.unite.Model.DoubleClickListener;
import com.dania.unite.MyBounceInterpolator;
import com.dania.unite.PostsData;
import com.dania.unite.R;
import com.dania.unite.TimeAgo;
import com.dania.unite.image_viewer_act;
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

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private CircleImageView profile_image,edit_dp;
    private TextView username, email_ans, friends_count, love_count, fame_no, edit_profile;

    private DatabaseReference reference;
    private FirebaseUser fuser;
    private String currentUserId, user_name, image;

    private StorageReference storageReference;
    private DatabaseReference PostsRef, LikesRef,UsersRef;
    private RecyclerView user_post_list;
    private static final int IMAGE_REQUEST = 1;
    private Uri resultUri;
    private StorageTask uploadTask;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profile_image = view.findViewById(R.id.profile_image);
        friends_count = view.findViewById(R.id.friends_count);
        love_count = view.findViewById(R.id.love_count);
        fame_no = view.findViewById(R.id.fame_no);
        edit_dp = view.findViewById(R.id.edit_dp);
        edit_profile = view.findViewById(R.id.edit_profile);
        username = view.findViewById(R.id.username);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        email_ans = view.findViewById(R.id.email_ans);
        user_post_list = view.findViewById(R.id.profile_posts_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        user_post_list.setLayoutManager(linearLayoutManager);
        user_post_list.setNestedScrollingEnabled(false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        storageReference = FirebaseStorage.getInstance().getReference("uploads");


        fuser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                email_ans.setText(fuser.getEmail());
                user_name = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                username.setText(user_name);

                if (dataSnapshot.child("Friends").exists()) {
                    int no_of_friends = (int) dataSnapshot.child("Friends").getChildrenCount();
                    friends_count.setText(Integer.toString(no_of_friends));
                }

                if (dataSnapshot.child("Love").exists()){
                    int no_of_love = (int) dataSnapshot.child("Love").getChildrenCount();
                    love_count.setText(Integer.toString(no_of_love));

                }
                if (dataSnapshot.child("Likes").exists()){
                    String likes_no = dataSnapshot.child("Likes").getValue().toString();
                    int fame = Integer.parseInt(likes_no)/10 ;
                    fame_no.setText(Integer.toString(fame));

                }
                image = dataSnapshot.child("imageurl").getValue().toString();
                if (image.equals("default")) {
                    profile_image.setImageResource(R.drawable.ic_nav_person);
                } else {
                    //Picasso.get().load(image).placeholder(R.drawable.ic_nav_person).into(profile_image);
                    Glide.with(getContext()).load(image).thumbnail(0.1f).placeholder(R.drawable.ic_nav_person).into(profile_image);
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
                Intent intent = new Intent(getContext(), image_viewer_act.class);
                intent.putExtra("userid", currentUserId);
                intent.putExtra("username",user_name);
                intent.putExtra("imageurl",image);
                Pair[] pairs = new Pair[2];
                pairs[0] = new Pair<View, String>(profile_image, "image_to_show_transition");
                pairs[1] = new Pair<View, String>(username, "text_to_show_transition");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), pairs);

                startActivity(intent, options.toBundle());

            }
        });

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EditProfile.class);
                startActivity(intent);

            }
        });

        edit_dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, IMAGE_REQUEST);


            }
        });


        return view;


    }


    private void DisplayUserPosts() {

        Query SortPostsInDescendingOrder = PostsRef.orderByChild("uid").equalTo(currentUserId);
        final FirebaseRecyclerAdapter<PostsData, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<PostsData, PostsViewHolder>(
                        PostsData.class,
                        R.layout.all_posts_layout,
                        PostsViewHolder.class,
                        SortPostsInDescendingOrder
                ) {
                    @Override
                    protected void populateViewHolder(final PostsViewHolder viewHolder, final PostsData model, final int position) {

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
                                final Animation animation = AnimationUtils.loadAnimation(getContext(),R.anim.bounce);
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
                                final Animation animation = AnimationUtils.loadAnimation(getContext(),R.anim.bounce);
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

        public void setProfileimage(String profileimage, Context context) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);

            if (profileimage.equals("default")){
                image.setImageResource(R.drawable.ic_account_circle_grey_24dp);
            }else{
                //Picasso.get().load(profileimage).into(image);
                Glide.with(context).load(profileimage).thumbnail(0.1f).placeholder(R.drawable.ic_account_circle_grey_24dp).into(image);
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


        public void setPostimage(String postimage, Context context) {
            ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
            //Picasso.get().load(postimage).placeholder(R.drawable.post_image_placeholder).into(post_image);
            Glide.with(context).load(postimage).thumbnail(0.1f).placeholder(R.drawable.post_image_placeholder).into(post_image);


        }


    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==IMAGE_REQUEST && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();
            CropImage.activity(ImageUri)
                    .setAspectRatio(1, 1)
                    .start(getContext(),this);
        }



        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
            }
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

    private String getFileExtention(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {

        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Uploading");
        pd.show();

        if (resultUri != null) {
            final StorageReference fileReference = storageReference.child(fuser.getUid()
                    + "." + getFileExtention(resultUri));

            uploadTask = fileReference.putFile(resultUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {

                        Uri downloadUri = (Uri) task.getResult();
                        assert downloadUri != null;
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageurl", mUri);
                        reference.updateChildren(map);

                        pd.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();

                }
            });
        } else {
            Toast.makeText(getContext(), "No image selected.", Toast.LENGTH_SHORT).show();

        }

    }




}