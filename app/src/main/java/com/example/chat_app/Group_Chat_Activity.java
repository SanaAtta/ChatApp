package com.example.chat_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.chat_app.Adopter.GroupMessageAdaptor;
import com.example.chat_app.classes.Messages;
import com.example.chat_app.classes.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Group_Chat_Activity extends AppCompatActivity {


    private Toolbar         mToolbar;
    private ImageButton     SendMessageButton,btn_media,btn_audio,Camera_btn, audioCall_btn,videoCall_btn, addParticipant;
    private EditText        userMessageInput;
    private ScrollView mScrollView;
    private TextView        displayTextMessages;
    GroupMessageAdaptor groupMessageAdaptor;
    private final List<Messages> messagesList = new ArrayList<>();
    public String Checker ="text",myUrl =""  ;
    private MediaRecorder recorder;
    private String fileName = null;
     ProgressDialog LoadingBar;
    private static final String LOG_TAG = "Record_Log";
    static final int REQUEST_VIDEO_CAPTURE = 10;
    static final int RC_PIC_CODE = 101;
    String GroupId,myGroupRole="";
    Uri photoURI;
    String currentPhotoPath;
    String checker="";
    private Uri fileUri;
    private StorageTask uploadTask;
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;
    private String saveCurrentTime, saveCurrentDate;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;

    @Override
    protected void onStart() {
        super.onStart();
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

//                Messages messages = snapshot.getValue(Messages.class);
//
//                messagesList.add(messages);
//
//                groupMessageAdaptor.notifyDataSetChanged();
//

               // mScrollView.smoothScrollToPosition(mScrollView.getAdapter().getItemCount());
                if (snapshot.exists()){
                    DisplayMessage(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                if (snapshot.exists()){
                    DisplayMessage(snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void DisplayMessage(DataSnapshot snapshot) {
        Iterator iterator =snapshot.getChildren().iterator();
        while (iterator.hasNext()){
            String chatDate = (String)((DataSnapshot)iterator.next()).getValue();
            String chatMessages =(String)((DataSnapshot)iterator.next()).getValue();
            String chatName =(String)((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String)((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName + " :\n" + chatMessages + "\n" + chatTime + "     " + chatDate + "\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
       // messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
//        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
//        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(Group_Chat_Activity.this, currentGroupName, Toast.LENGTH_SHORT).show();


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        InitializeFields();
        GetUserInfo();
        btn_media.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        CharSequence options[] = new CharSequence[]
                {
                        "Images",
                        "PDF Files",
                        "MS Word Files"
                };
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Group_Chat_Activity.this);
        builder.setTitle("Select  the File");
        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int i) {

                if (i == 0){
                    checker ="image";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent,"Select Image"),24);
                }
                if (i == 1){
                    checker ="pdf";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(intent.createChooser(intent,"Select PDF"),24);
                }
                if (i == 2){
                    checker ="docx";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/msword");
                    startActivityForResult(intent.createChooser(intent,"Select Docs"),24);
                }


            }
        });
        builder.show();
    }
});
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDatabase();
                userMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }

            private void SaveMessageInfoToDatabase() {
                String message = userMessageInput.getText().toString();
                String messagekEY = GroupNameRef.push().getKey();

                if (TextUtils.isEmpty(message))
                {
                    Toast.makeText(Group_Chat_Activity.this, "Please write message first...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Calendar calForDate = Calendar.getInstance();
                    SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
                    currentDate = currentDateFormat.format(calForDate.getTime());

                    Calendar calForTime = Calendar.getInstance();
                    SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                    currentTime = currentTimeFormat.format(calForTime.getTime());


                    HashMap<String, Object> groupMessageKey = new HashMap<>();
                    GroupNameRef.updateChildren(groupMessageKey);

                    GroupMessageKeyRef = GroupNameRef.child(messagekEY);

                    HashMap<String, Object> messageInfoMap = new HashMap<>();
                    messageInfoMap.put("name", currentUserName);
                    messageInfoMap.put("message", message);
                    messageInfoMap.put("date", currentDate);
                    messageInfoMap.put("time", currentTime);
                    GroupMessageKeyRef.updateChildren(messageInfoMap);
                }
            }
        });

        btn_audio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (ContextCompat.checkSelfPermission(Group_Chat_Activity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(Group_Chat_Activity.this, Manifest.permission.RECORD_AUDIO)) {

                        LayoutInflater factory = LayoutInflater.from(Group_Chat_Activity.this);
                        final View cumDialogView = factory.inflate(R.layout.cum_alert_dialog, null);
                        final android.app.AlertDialog cumDialog = new android.app.AlertDialog.Builder(Group_Chat_Activity.this).create();
                        cumDialog.setView(cumDialogView);
                        cumDialogView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cumDialog.dismiss();
                                if (ActivityCompat.shouldShowRequestPermissionRationale(Group_Chat_Activity.this, Manifest.permission.RECORD_AUDIO)) {
                                    ActivityCompat.requestPermissions(Group_Chat_Activity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 100);
                                }
                            }
                        });
                        cumDialog.show();
                    } else {
                    }
                } else {
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        startRecording();
                    }
                    else if (event.getAction() == MotionEvent.ACTION_UP){
                        stopRecording();
                    }
                }

                return false;
            }
        });

        Camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CharSequence options[] = new CharSequence[]
                        {
                                "Click Image",
                                "Record Video",
                                "Location"
                        };
                AlertDialog.Builder builder =new AlertDialog.Builder(Group_Chat_Activity.this);
                builder.setTitle("Select an Option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            if (ContextCompat.checkSelfPermission(Group_Chat_Activity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(Group_Chat_Activity.this, Manifest.permission.CAMERA)) {

                                    LayoutInflater factory = LayoutInflater.from(Group_Chat_Activity.this);
                                    final View cumDialogView = factory.inflate(R.layout.cum_alert_dialog, null);
                                    final android.app.AlertDialog cumDialog = new android.app.AlertDialog.Builder(Group_Chat_Activity.this).create();
                                    cumDialog.setView(cumDialogView);
                                    cumDialogView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            cumDialog.dismiss();
                                            if (ActivityCompat.shouldShowRequestPermissionRationale(Group_Chat_Activity.this, Manifest.permission.CAMERA)) {
                                                ActivityCompat.requestPermissions(Group_Chat_Activity.this, new String[]{Manifest.permission.CAMERA}, 100);
                                            }
                                        }
                                    });
                                    cumDialog.show();
                                } else {
                                }
                            } else {

                                Checker = "clickImage";
                                ClickImage();
                            }
                        }
                        if (which == 1) {
                            if (ContextCompat.checkSelfPermission(Group_Chat_Activity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(Group_Chat_Activity.this, Manifest.permission.CAMERA)) {

                                    LayoutInflater factory = LayoutInflater.from(Group_Chat_Activity.this);
                                    final View cumDialogView = factory.inflate(R.layout.cum_alert_dialog, null);
                                    final android.app.AlertDialog cumDialog = new android.app.AlertDialog.Builder(Group_Chat_Activity.this).create();
                                    cumDialog.setView(cumDialogView);
                                    cumDialogView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            cumDialog.dismiss();
                                            if (ActivityCompat.shouldShowRequestPermissionRationale(Group_Chat_Activity.this, Manifest.permission.CAMERA)) {
                                                ActivityCompat.requestPermissions(Group_Chat_Activity.this, new String[]{Manifest.permission.CAMERA}, 100);
                                            }
                                        }
                                    });
                                    cumDialog.show();
                                } else {
                                }
                            } else {


                                Checker = "recordVideo";
                                ClickVideo();
                            }
                        }
//                        if(which ==2){
//                            Intent intent2= new Intent(Group_Chat_Activity.this, MainActivity.class);
//                            intent2.putExtra("userid",UserId);
//                            //Toast.makeText(mContext, user.getId(), Toast.LENGTH_SHORT).show();
//                            startActivity(intent2);
//
//                        }
                    }
                });
                builder.show();

                //     Intent intent = new Intent(MessageActivity.this,VideoActivity.class);
                //   startActivity(intent);

            }

        });



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==24 &&  resultCode ==RESULT_OK && data !=null && data.getData()!=null)
        {

            LoadingBar.setTitle("Sending File");
            LoadingBar.setMessage("Please wait, we are sending file...");
            LoadingBar.setCanceledOnTouchOutside(false);
            LoadingBar.show();

            fileUri = data.getData();
            if (!checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Docs Files");
                String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = UsersRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "."+checker);
                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful()){
                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", task.getResult().getStorage().getDownloadUrl().toString());
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                            UsersRef.updateChildren(messageBodyDetails);
                            LoadingBar.dismiss();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        LoadingBar.dismiss();
                        Toast.makeText(Group_Chat_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull @NotNull UploadTask.TaskSnapshot snapshot) {
                        double p=(100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        LoadingBar.setMessage((int) p + " % Uploading");
                    }
                });

            }
            else  if(checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = UsersRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "."+"jpg");
                uploadTask=filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull @NotNull Task task) throws Exception {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Uri> task) {
                        if (task.isSuccessful())
                        {
                            Uri downloadUri =task.getResult();
                            myUrl = downloadUri.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                            UsersRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        LoadingBar.dismiss();
                                        Toast.makeText(Group_Chat_Activity.this, "Image Sent Successfully...", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        LoadingBar.dismiss();
                                        Toast.makeText(Group_Chat_Activity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    //MessageInputText.setText("");
                                }
                            });

                        }
                    }
                });
            }
            else
            {
                LoadingBar.dismiss();
                Toast.makeText(this, "Nothing selected ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void ClickVideo() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createVideoFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                        BuildConfig.APPLICATION_ID + ".provider", photoFile);
               // VideoRecord = photoURI;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_VIDEO_CAPTURE);

            }
        }
    }
    private File createVideoFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "MP4_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;

    }


    private void ClickImage() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                        BuildConfig.APPLICATION_ID + ".provider", photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, RC_PIC_CODE);
            }
        }

    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
           // Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }
    //Stop and upload to firebase
    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
        uploadAudio();
    }
    private void InitializeFields()
    {
//        mToolbar =  findViewById(R.id.group_chat_bar_layout);
//        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);


        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        displayTextMessages = (TextView) findViewById(R.id.group_chat_text_display);
        mScrollView =  findViewById(R.id.my_scroll_view);
        groupMessageAdaptor=new GroupMessageAdaptor(messagesList);

        btn_audio = findViewById(R.id.btn_audio);
        Camera_btn = findViewById(R.id.Camera_btn);
        btn_media = findViewById(R.id.addMedia_btn);



        LoadingBar =new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());


    }
    private void uploadAudio() {
        LoadingBar.setMessage("Uploading Audio");
        LoadingBar.show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups").child(GroupId);
        String message_push_id = databaseReference.getKey();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Audio Files").child(mAuth.getUid()).child(message_push_id+".3gp");
        Uri uri = Uri.fromFile(new File(fileName));
        Checker = "Audio";String AudioUrl;
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        String timeStamp = ""+System.currentTimeMillis();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", ""+mAuth.getUid());
                        hashMap.put("message", String.valueOf(task.getResult()));
                        hashMap.put("Time", timeStamp);
                        hashMap.put("type", "Audio");

                        databaseReference.child("Messages").child(timeStamp)
                                .setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        LoadingBar.dismiss();
                                        LoadingBar.setMessage("Uploading Finished");
                                    }
                                });



                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("On Failure", e.toString());
            }
        });
    }
    private void GetUserInfo(){
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                currentUserName = snapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}