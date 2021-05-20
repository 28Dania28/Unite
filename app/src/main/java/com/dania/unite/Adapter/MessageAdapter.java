package com.dania.unite.Adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dania.unite.ImageMessagesViewer;
import com.dania.unite.Model.Chat;
import com.dania.unite.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {


    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private Context mContext;
    private List<Chat> mChat;
    private DatabaseReference reference, chatRef;
    FirebaseUser fuser;
    String sender_id;


    public MessageAdapter(Context mContext, List<Chat> mChat){
        this.mChat = mChat;
        this.mContext = mContext;

    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right_1, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left_1, parent, false);
            return new MessageAdapter.ViewHolder(view);


        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder holder, int position) {

        final Chat chat = mChat.get(position);
        if (chat.getType().equals("Image")){

            holder.show_message.setVisibility(View.GONE);
            holder.imageMessage.setVisibility(View.VISIBLE);
            //Picasso.get().load(chat.getMessage()).placeholder(R.drawable.post_image_placeholder).into(holder.imageMessage);
            Glide.with(mContext).load(chat.getMessage()).thumbnail(0.1f).placeholder(R.drawable.post_image_placeholder).into(holder.imageMessage);

            holder.time.setText(chat.getTime());
            holder.imageMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ImageMessagesViewer.class);
                    intent.putExtra("push_key",chat.getPush_key() );
                    Pair[] pairs = new Pair[1];
                    pairs[0] = new Pair<View, String>(holder.imageMessage, "image_to_show_transition");
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity)mContext , pairs);
                    mContext.startActivity(intent, options.toBundle());

                }
            });
            if (chat.isIsseen()) {
                holder.seen_img.setImageResource(R.drawable.ic_blur_on_black_24dp);
            } else {

                holder.seen_img.setImageResource(R.drawable.ic_blur_off_black_24dp);
            }


            holder.imageMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete this message"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {

                                chatRef = FirebaseDatabase.getInstance().getReference().child("Chats");
                                chatRef.child(chat.getPush_key()).removeValue();

                            }
                        }
                    });
                    builder.show();
                    return false;
                }
            });


            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete this message"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {

                                chatRef = FirebaseDatabase.getInstance().getReference().child("Chats");
                                chatRef.child(chat.getPush_key()).removeValue();

                            }
                        }
                    });
                    builder.show();
                    return false;
                }
            });


        }
        else {

            holder.imageMessage.setVisibility(View.GONE);
            holder.show_message.setVisibility(View.VISIBLE);
            holder.show_message.setText(chat.getMessage());
            holder.time.setText(chat.getTime());

            reference = FirebaseDatabase.getInstance().getReference().child("Users");
            reference.child(chat.getSender()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        sender_id = dataSnapshot.child("username").getValue().toString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            if (chat.isIsseen()) {
                holder.seen_img.setImageResource(R.drawable.ic_blur_on_black_24dp);
            } else {
                holder.seen_img.setImageResource(R.drawable.ic_blur_off_black_24dp);
            }

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete this message"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {

                                chatRef = FirebaseDatabase.getInstance().getReference().child("Chats");
                                chatRef.child(chat.getPush_key()).removeValue();

                            }
                        }
                    });
                    builder.show();
                    return false;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message, time;
        public ImageView seen_img, imageMessage;


        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            time = itemView.findViewById(R.id.time);
            seen_img = itemView.findViewById(R.id.seen_img);
            imageMessage = itemView.findViewById(R.id.imageMessage);

        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }
}
