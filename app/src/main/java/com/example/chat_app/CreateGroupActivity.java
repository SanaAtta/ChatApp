package com.example.chat_app;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class CreateGroupActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_create_group);

       firebaseAuth = FirebaseAuth.getInstance().getCurrentUser();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create new group...");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

       groupIconIv = findViewById(R.id.groupIconIv);
       groupDiscriptionEt = findViewById(R.id.groupDiscriptionEt);
       groupTitleEt = findViewById(R.id.groupTitleEt);
       createGroupBtn = findViewById(R.id.createGroupBtn);
        storageReference = FirebaseStorage.getInstance().getReference("Group").child(FirebaseAuth.getInstance().getCurrentUser().getEmail());


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
                .start(CreateGroupActivity.this);

    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = CreateGroupActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }
    private void publishStory(){
        ProgressDialog pd = new ProgressDialog(CreateGroupActivity.this);
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
                        String groupId = reference.push().getKey();
                        String g_timeStamp = ""+System.currentTimeMillis();
                        HashMap<String,Object> map = new HashMap<>();
                        map.put("groupId",""+g_timeStamp);
                        map.put("groupTitle",groupTitle);
                        map.put("groupDiscription",groupDiscription);
                        map.put("groupIcon",myUrl);
                        map.put("timeStamp", ServerValue.TIMESTAMP);
                        map.put("createdBy",firebaseAuth.getUid());
                        reference.child(g_timeStamp).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //Add Participants
                                HashMap<String,String> hashMap = new HashMap<>();
                                hashMap.put("id",firebaseAuth.getUid());
                                hashMap.put("role","creator");
                                hashMap.put("timeStamp",g_timeStamp);
                                reference.child(g_timeStamp).child("Participant").child(firebaseAuth.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(CreateGroupActivity.this, "Grroup Created", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                    }
                                });

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
                        Toast.makeText(CreateGroupActivity.this, "Failed !!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreateGroupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }else {
            //Create Grroup Without Image
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
            String groupId = reference.push().getKey();
            String g_timeStamp = ""+System.currentTimeMillis();

            HashMap<String,Object> map = new HashMap<>();
            map.put("groupId",""+g_timeStamp);
            map.put("groupTitle",groupTitle);
            map.put("groupDiscription",groupDiscription);
            map.put("groupIcon",myUrl);
            map.put("timeStamp", ServerValue.TIMESTAMP);
            map.put("createdBy",firebaseAuth.getUid());
            reference.child(g_timeStamp).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //Add Participants
                    HashMap<String,String> hashMap = new HashMap<>();
                    hashMap.put("id",firebaseAuth.getUid());
                    hashMap.put("role","creator");
                    hashMap.put("timeStamp",g_timeStamp);
                    reference.child(g_timeStamp).child("Participant").child(firebaseAuth.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            Toast.makeText(CreateGroupActivity.this, "Grroup Created", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();

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
            startActivity(new Intent(CreateGroupActivity.this,MainActivity.class));
            finish();
        }
    }

}
