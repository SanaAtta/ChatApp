package com.example.chat_app;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.chat_app.Adopter.UserStatusAdapter;
import com.example.chat_app.classes.Story;
import com.example.chat_app.classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class ShowStoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    int counter  = 0;
    long presstime = 0L;
    private List<User> mUsers;
    long limit = 2000L;
    long view_count = 0;

    StoriesProgressView storiesProgressView;
    ImageView image,story_photo,story_delete;
    TextView story_username,seen_number,start_time;

    LinearLayout r_seen;
    private RecyclerView recyclerView;

    List<String> Images;
    List<String> Storyids;
    private List<String> idList;
    private List<String> seen_count_list;
    private UserStatusAdapter userAdapter;

    String userid;
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    presstime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit<now - presstime;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_story);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        storiesProgressView = findViewById(R.id.stories);
        image = findViewById(R.id.image);
        story_photo = findViewById(R.id.story_photo);
        story_username = findViewById(R.id.story_username);
        r_seen =findViewById(R.id.r_seen);
        story_delete = findViewById(R.id.story_delete);
        seen_number = findViewById(R.id.seen_number);
        start_time =findViewById(R.id.story_posting_start);
        idList = new ArrayList<>();
        seen_count_list = new ArrayList<>();
        mUsers = new ArrayList<>();



        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        userAdapter = new UserStatusAdapter(this,mUsers,false, seen_count_list);
        recyclerView.setAdapter(userAdapter);


        userid = getIntent().getStringExtra("userid");
        getStories(userid);
        userInfo(userid);

        r_seen.setVisibility(View.GONE);
        story_delete.setVisibility(View.GONE);



        if (userid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            r_seen.setVisibility(View.VISIBLE);
            story_delete.setVisibility(View.VISIBLE);

        }
        // Next story if exist !
        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);
        // previous story if exist !
        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);
        // For view the Story
        r_seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    DatabaseReference reference =FirebaseDatabase.getInstance().getReference("Story").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Storyids.get(counter)).child("views");
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            idList.clear();
                            seen_count_list.clear();
                            for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                                idList.add(snapshot .getKey());
                                seen_count_list.add(String.valueOf(snapshot.getValue()));
                            }
                            userShow();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
            }
        });
        // For deleting the specific story
        story_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference =FirebaseDatabase.getInstance().getReference("Story").child(userid).child(Storyids.get(counter));
                reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(ShowStoryActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        });
    }
    private void userShow(){
        DatabaseReference reference =  FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    for (String id : idList) {
                        if (user.getId().equals(id)) {
                            mUsers.add(user);
                        }
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //Methods
    @Override
    public void onNext() {
        Picasso.get().load(Images.get(++counter)).into(image);
        addView(Storyids.get(counter));
        seenNumber(Storyids.get(counter));

    }
    @Override
    public void onPrev() {
        if ((counter-1)<0) return;
        Picasso.get().load(Images.get(--counter)).into(image);
        seenNumber(Storyids.get(counter));

    }
    @Override
    public void onComplete() {
        finish();
    }
    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }
    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }
    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }
    // Get story from firebase
    private void getStories(String Userid){

        Images = new ArrayList<>();
        Storyids = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(Userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Images.clear();
                Storyids.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);
                    long timecurrent = System.currentTimeMillis();
                    if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                        Images.add(story.getImageurl());
                        Storyids.add(story.getStoryid());
                        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                        calendar.setTimeInMillis(story.getTimestart());
                        String dateTime = DateFormat.format("hh:mm aa",calendar).toString();
                        start_time.setText(dateTime);
                    }
                }
                storiesProgressView.setStoriesCount(Images.size());
                storiesProgressView.setStoryDuration(6000L);
                storiesProgressView.setStoriesListener(ShowStoryActivity.this);
                storiesProgressView.startStories(counter);

                Picasso.get().load(Images.get(counter)).into(image);

                addView(Storyids.get(counter));
                seenNumber(Storyids.get(counter));

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    // Get user basic's
    private void userInfo(String Userid) {
        //For user name and image
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(Userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                Picasso.get().load(user.getImageURL()).into(story_photo);
                story_username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addView(String Storyids){
        FirebaseDatabase.getInstance().getReference("Story").child(userid)
                .child(Storyids).child("views")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                      if(dataSnapshot.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        view_count = (long) dataSnapshot.getValue();
                           FirebaseDatabase.getInstance().getReference("Story").child(userid)
                                   .child(Storyids).child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(++view_count);

                       }


                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        FirebaseDatabase.getInstance().getReference("Story").child(userid)
                .child(Storyids).child("views")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(view_count);


    }

    private void seenNumber(String Storyids){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Story").child(userid)
                .child(Storyids).child("views");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                seen_number.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
