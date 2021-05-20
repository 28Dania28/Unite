package com.dania.unite;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
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

import java.util.ArrayList;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder>{
    Context context;
    ArrayList<Msg> m1;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;


    public MsgAdapter(Context context, ArrayList<Msg> m1) {
        this.context = context;
        this.m1 = m1;
    }

    @NonNull
    @Override
    public MsgAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right_1, parent, false);
            return new MsgAdapter.ViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left_1, parent, false);
            return new MsgAdapter.ViewHolder(view);


        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Msg msg = m1.get(position);
        if (msg.getType().equals("Text")) {
            holder.imageMessage.setVisibility(View.GONE);
            holder.seen_img.setVisibility(View.GONE);
            holder.show_message.setVisibility(View.VISIBLE);
            holder.show_message.setText(msg.getMsg());
        }else {
            holder.imageMessage.setVisibility(View.VISIBLE);
            holder.seen_img.setVisibility(View.GONE);
            holder.show_message.setVisibility(View.GONE);
            //Picasso.get().load(msg.getMsg()).placeholder(R.drawable.ic_account_circle_grey_24dp).into(holder.imageMessage);
            Glide.with(context).load(msg.getMsg()).thumbnail(0.1f).placeholder(R.drawable.white).into(holder.imageMessage);
            holder.imageMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ImageChatViewer.class);
                    intent.putExtra("image_uri",msg.getMsg() );
                    Pair[] pairs = new Pair[1];
                    pairs[0] = new Pair<View, String>(holder.imageMessage, "image_to_show_transition");
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity)context , pairs);
                    context.startActivity(intent, options.toBundle());

                }
            });

        }
            holder.time.setText(msg.getTime());



    }

    @Override
    public int getItemCount() {
        return m1.size();
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
        if (m1.get(position).getS_or_r().equals("send")){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }


}
