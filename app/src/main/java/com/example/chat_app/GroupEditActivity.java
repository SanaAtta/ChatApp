package com.example.chat_app;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class GroupEditActivity extends AppCompatActivity {

    String groupId ;
    private FirebaseUser firebaseAuth;
    private ImageView groupIconIv;
    private EditText groupTitleEt,groupDiscriptionEt;
    private FloatingActionButton createGroupBtn;

    private Uri mImageUri;
    String myUrl = "";
    private StorageTask storageTask;
    StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        groupId = getIntent().getStringExtra("groupid");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Group Info...");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        groupIconIv = findViewById(R.id.groupIconIv);
        groupDiscriptionEt = findViewById(R.id.groupDiscriptionEt);
        groupTitleEt = findViewById(R.id.groupTitleEt);
        createGroupBtn = findViewById(R.id.createGroupBtn);
        storageReference = FirebaseStorage.getInstance().getReference("Group").child(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        LoadGroupInfo();

        groupIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPickImageDialog();
            }
        });
        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishStory();

            }
        });

    }
    private void showPickImageDialog() {
        CropImage.activity()
                .setAspectRatio(9,16)
                .start(GroupEditActivity.this);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = GroupEditActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }
    private void publishStory(){
        ProgressDialog pd = new ProgressDialog(GroupEditActivity.this);
        pd.setMessage("Uploading");

        String groupTitle = groupTitleEt.getText().toString().trim();
        String groupDiscription = groupDiscriptionEt.getText().toString().trim();
        if (TextUtils.isEmpty(groupTitle)){
            Toast.makeText(this, "Please Enter Title", Toast.LENGTH_SHORT).show();
            return;
        }
        pd.show();

        if (mImageUri != null){
            //Create Goup With Image
            String g_timeStamp = ""+System.currentTimeMillis();

            StorageReference imageReference = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(mImageUri));
            storageTask = imageReference.putFile(mImageUri);
            storageTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw  task.getException();
                    }
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = (Uri) task.getResult();
                        myUrl = downloadUri.toString();
                        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
                        HashMap<String,Object> map = new HashMap<>();
                        map.put("groupTitle",groupTitle);
                        map.put("groupDiscription",groupDiscription);
                        map.put("groupIcon",myUrl);
                        reference.child(groupId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                pd.dismiss();
                                Toast.makeText(GroupEditActivity.this, "Group Info Updated", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(GroupEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();

                            }
                        });
                        finish();
                    }
                    else {
                        Toast.makeText(GroupEditActivity.this, "Failed !!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GroupEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }
        else {
            //Create Grroup Without Image
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
            String g_timeStamp = ""+System.currentTimeMillis();

            HashMap<String,Object> map = new HashMap<>();
            map.put("groupTitle",groupTitle);
            map.put("groupDiscription",groupDiscription);
            reference.child(groupId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    pd.dismiss();
                    Toast.makeText(GroupEditActivity.this, "Group Info Updated", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(GroupEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

        }
        else {
            Toast.makeText(this, "Something gona wrong", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(GroupEditActivity.this,MainActivity.class));
            finish();
        }
    }
    private void LoadGroupInfo() {
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    String groupId =""+snapshot.child("groupId").getValue();
                    String groupTitle =""+snapshot.child("groupTitle").getValue();
                    String groupIcon =""+snapshot.child("groupIcon").getValue();
                    String groupDiscription =""+snapshot.child("groupDiscription").getValue();
                    String timeStamp =""+snapshot.child("timeStamp").getValue();
                    String createdBy =""+snapshot.child("createdBy").getValue();


                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(timeStamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();


                    groupTitleEt.setText(groupTitle);
                    groupDiscriptionEt.setText(groupDiscription);

                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.user_img).into(groupIconIv);
                    }catch (Exception e){
                        groupIconIv.setImageResource(R.drawable.user_img);
                    }

                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


}
