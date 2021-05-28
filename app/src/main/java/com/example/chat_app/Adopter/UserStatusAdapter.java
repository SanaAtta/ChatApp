package com.example.chat_app.Adopter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app.Chat_Activity;
import com.example.chat_app.R;
import com.example.chat_app.classes.Messages;
import com.example.chat_app.classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;


public class UserStatusAdapter extends RecyclerView.Adapter<UserStatusAdapter.ViewHolder>{

    private Context mContext;
    private List<User> mUsers;
    private List<String> seenCountList;
    private  boolean ischat;
    String theLastMessage;

    public UserStatusAdapter(Context mContext, List<User> mUsers, boolean ischat, List<String> seenCountList){
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.ischat = ischat;
        this.seenCountList = seenCountList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_status_items, parent,false);

        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.usermane.setText(user.getUsername());
        holder.seenTimes.setText(""+seenCountList.get(position));

        if(user.getImageURL().equals("default")){
            holder.profile_image.setImageResource(R.drawable.user_img);
        }
        else {
            Picasso.get().load(user.getImageURL()).into(holder.profile_image);
        }

        if (ischat){
            lastMessage(user.getId(),holder.last_msg);
        }
        else {
            holder.last_msg.setVisibility(View.GONE);
        }

        if(ischat){
            if (user.getStatus().equals("Online")){
                holder.usermane.setTextColor(Color.parseColor("#34eb71"));
            }
            else {
                holder.usermane.setTextColor(Color.parseColor("#7c827e"));

            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(mContext, Chat_Activity.class);
                intent.putExtra("userid",user.getId());
                //Toast.makeText(mContext, user.getId(), Toast.LENGTH_SHORT).show();
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView usermane;
        public ImageView profile_image;
        public TextView last_msg, seenTimes;
        public ImageButton call_btn;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usermane = itemView.findViewById(R.id.username);
            profile_image =itemView.findViewById(R.id.profile_image);
            last_msg =itemView.findViewById(R.id.last_msg);
          //  call_btn = itemView.findViewById(R.id.call_btn);
            seenTimes = itemView.findViewById(R.id.tv_seen_times);






        }
    }
    private void lastMessage(final String userid, final TextView last_msg){
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("chat");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    Messages chat = snapshot.getValue(Messages.class);
                    if(chat.getMessage().equals(firebaseUser.getUid()) && chat.getFrom().equals(userid)||
                            chat.getMessage().equals(userid) && chat.getFrom().equals(firebaseUser.getUid())){
                        if (!chat.getType().equals("map")) {
                            theLastMessage = chat.getMessage();
                        }
                        if (chat.getType().equals("map")) {
                            theLastMessage = "Location";
                        }

                    }
                }
                switch (theLastMessage){
                    case "default":
                        last_msg.setText("No Message");
                        break;
                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }

                theLastMessage="default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
