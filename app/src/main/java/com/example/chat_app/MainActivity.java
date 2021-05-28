package com.example.chat_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.chat_app.auth.Login_Activity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

     private TabLayout myTabLayout;
     public ViewPager myViewPager;
     public TabAccessAdaptor myTabAccessAdaptor;
     private FirebaseUser currentUser;
     FirebaseAuth mAuth;
     private DatabaseReference RoofRef;
     private String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        RoofRef= FirebaseDatabase.getInstance().getReference();
//          currentUserID = mAuth.getCurrentUser().getUid();

//        Toolbar myToolbar = findViewById(R.id.main_app_bar);
//        setSupportActionBar(myToolbar);

       myViewPager=findViewById(R.id.main_viewPage);
       myTabAccessAdaptor=new TabAccessAdaptor(getSupportFragmentManager());
       myViewPager.setAdapter(myTabAccessAdaptor);

        myTabLayout= (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser  currentUser=mAuth.getCurrentUser();
        if (currentUser == null){
            SendUserToLoginActivity();
        }
        else
        {
           // updateUserStatus("online");
            VerifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser  currentUser=mAuth.getCurrentUser();
        if (currentUser != null)
        {
            //updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser  currentUser=mAuth.getCurrentUser();
        if (currentUser != null)
        {
            //updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance(){
        String cusrrentUserID=mAuth.getCurrentUser().getUid();

        RoofRef.child("Users").child(cusrrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.child("name").exists()) && (snapshot.child("status").exists())){
                    Toast.makeText(MainActivity.this, "WelCome", Toast.LENGTH_SHORT).show();
                }
                else
                {
                   SendUserToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent=new Intent(MainActivity.this, Login_Activity.class);
        startActivity(loginIntent);
    }
    private void SendUserToFindFriendActivity() {
        Intent FindFriendIntent=new Intent(MainActivity.this, Find_Friend_Activity.class);
        startActivity(FindFriendIntent);
    }
    private void SendUserToRequestActivity() {
        Intent request=new Intent(MainActivity.this, Request_Activity.class);
        startActivity(request);
    }
    private void SendUserToSettingActivity(){
        Intent settingIntent= new Intent(MainActivity.this,Setting_Activity.class);
        settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingIntent);
        finish();
    }
    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        RoofRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.main_find_friend_option:
                Toast.makeText(this, "Find Friend click", Toast.LENGTH_SHORT).show();
                SendUserToFindFriendActivity();
                return  true;
            case R.id.main_setting_option:
                startActivity(new Intent(this, Setting_Activity.class));
                return  true;
            case R.id.main_create_group:
               RequestNewGroup();
                return true;
//            case R.id.main_request_option:
//                Toast.makeText(this, "Request click", Toast.LENGTH_SHORT).show();
//                SendUserToRequestActivity();
            case R.id.main_logOut_option:
                //updateUserStatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
               // Toast.makeText(this, "logout clicked", Toast.LENGTH_SHORT).show();
                return true;
            default:
               return super.onOptionsItemSelected(item);
        }
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name");
        final EditText groupNameField=new EditText(MainActivity.this);
        groupNameField.setHint("");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this, "Please write Group Name...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    CreateNewGroup(groupName);
                }
            }


        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();



    }
    private void CreateNewGroup (final String groupName){
        RoofRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, groupName + " group is Created Successfully...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}