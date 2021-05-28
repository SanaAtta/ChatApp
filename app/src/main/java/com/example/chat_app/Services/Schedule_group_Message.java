package com.example.chat_app.Services;

import androidx.annotation.NonNull;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Schedule_group_Message extends JobService {
    @Override
    public boolean onStartJob(@NonNull JobParameters job) {
        String sender = job.getExtras().getString("sender");
        String GroupId = job.getExtras().getString("GroupId");
        String message = job.getExtras().getString("message");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");

        String message_push_id = ref.getKey();
        String timeStamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", ""+sender);
        hashMap.put("message", message);
        hashMap.put("Time", timeStamp);
        hashMap.put("type", "text");

        ref.child(GroupId).child("Messages").child(timeStamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

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
