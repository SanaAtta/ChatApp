package com.example.chat_app.Services;

import androidx.annotation.NonNull;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Schedule_Message extends JobService {
    @Override
    public boolean onStartJob(@NonNull JobParameters job) {
        String sender = job.getExtras().getString("sender");
        String reciever = job.getExtras().getString("reciever");
        String message = job.getExtras().getString("message");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Groups");

        String message_push_id = reference.getKey();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("reciever",reciever);
        hashMap.put("message",message);
        hashMap.put("isSeen",false);
        hashMap.put("Time",String.valueOf(System.currentTimeMillis()));
        hashMap.put("messageId",message_push_id);

        hashMap.put("type", "text");

        reference.child("chat").push().setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(sender)
                .child(reciever);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(reciever);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.setValue(hashMap);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

        return true;
    }

    @Override
    public boolean onStopJob(@NonNull JobParameters job) {
        jobFinished(job,false);
        return true;
    }
}














