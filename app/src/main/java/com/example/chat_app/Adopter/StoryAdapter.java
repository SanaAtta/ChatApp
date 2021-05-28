package com.example.chat_app.Adopter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app.Add_Story_Activity;
import com.example.chat_app.MainActivity;
import com.example.chat_app.R;
import com.example.chat_app.ShowStoryActivity;
import com.example.chat_app.classes.Story;
import com.example.chat_app.classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;


public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {
    private Context mcontext;
    private List<Story> mstory;

    public StoryAdapter(Context mcontext, List<Story> mstory) {
        this.mcontext = mcontext;
        this.mstory = mstory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==0) {
            View view = LayoutInflater.from(mcontext).inflate(R.layout.add_story_item, parent, false);
            return new ViewHolder(view);

        } else {
            View view = LayoutInflater.from(mcontext).inflate(R.layout.status_item, parent, false);
            return new ViewHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Story story = mstory.get(position);

        userInfo(holder,story.getUserid(),position);
        if(holder.getAdapterPosition() !=0){
            seenStory(holder,story.getUserid());
        }
        if(holder.getAdapterPosition() ==0){
            myStory(holder.addstory_txt,holder.story_plus,false);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getAdapterPosition() ==0){
                    myStory(holder.addstory_txt,holder.story_plus,true);
                }
                else {
                    Intent intent = new Intent(mcontext, MainActivity.class);
                    intent.putExtra("userid",story.getUserid());
                    mcontext.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mstory.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView story_photo,story_plus,story_photo_seen;
        public TextView story_username, addstory_txt;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            story_photo = itemView.findViewById(R.id.story_photo);
            story_photo_seen = itemView.findViewById(R.id.story_photo_seen);
            story_plus = itemView.findViewById(R.id.story_plus);

            story_username = itemView.findViewById(R.id.story_username);
            addstory_txt = itemView.findViewById(R.id.addstory_txt);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return 0;
        }
        return 1;
    }
    private void userInfo(ViewHolder viewHolder,String userId,int position){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Picasso.get().load(user.getImageURL()).into(viewHolder.story_photo);
                if (position != 0){
                    Picasso.get().load(user.getImageURL()).into(viewHolder.story_photo_seen);
                    viewHolder.story_username.setText(user.getUsername());

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void myStory(TextView textView, ImageView imageView, boolean click){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long timecurrent = System.currentTimeMillis();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Story story = snapshot.getValue(Story.class);
                    if(timecurrent>story.getTimestart() && timecurrent<story.getTimeend()){
                        count++;
                    }
                }
                if (click){
                    if(count>0){
                        AlertDialog alertDialog = new AlertDialog.Builder(mcontext).create();
                        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "View Story", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(mcontext, ShowStoryActivity.class);
                                intent.putExtra("userid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                                mcontext.startActivity(intent);
                                dialogInterface.dismiss();

                            }
                        });
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Add Story", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(mcontext, Add_Story_Activity.class);
                                mcontext.startActivity(intent);
                                dialogInterface.dismiss();
                            }
                        });
                    alertDialog.show();
                    }
                    else{
                        Intent intent = new Intent(mcontext, Add_Story_Activity.class);
                        mcontext.startActivity(intent);

                    }
                }
                else {
                    if (count>0){
                        textView.setText("My Story");
                        imageView.setVisibility(View.GONE);
                    }
                    else{
                        textView.setText("Add Story");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void seenStory(ViewHolder viewHolder, String userId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i=0;
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    if (!snapshot.child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .exists() && System.currentTimeMillis()<snapshot.getValue(Story.class).getTimeend()){
                        i++;
                    }
                }
                if (i>0){
                    viewHolder.story_photo_seen.setVisibility(View.GONE);
                    viewHolder.story_photo.setVisibility(View.VISIBLE);
                }
                else {
                    viewHolder.story_photo_seen.setVisibility(View.VISIBLE);
                    viewHolder.story_photo.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
