package com.example.chat_app.Adopter;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app.Chat_Activity;
import com.example.chat_app.ImageViewerActivity;
import com.example.chat_app.MainActivity;
import com.example.chat_app.R;
import com.example.chat_app.classes.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class  MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
     private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;


    public MessageAdapter(List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;


        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
        }
    }

    @NonNull
    @NotNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MessageAdapter.MessageViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);


        if (fromMessageType.equals("text"))
        {
            if (fromUserID.equals(messageSenderId))
            {
                holder.senderMessageText.setVisibility(View.VISIBLE);

               // holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
            }
        }
        else if (fromMessageType.equals("image"))
        {
        if (fromUserID.equals(messageSenderId))
        {
        holder.messageSenderPicture.setVisibility(View.VISIBLE);
        Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);

        }
        else
        {
            holder.receiverProfileImage.setVisibility(View.VISIBLE);
            holder.messageReceiverPicture.setVisibility(View.VISIBLE);
            Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);
        }
        }
        else  if (fromMessageType.equals("pdf") ||  (fromMessageType.equals("docx")))
        {
            if (fromUserID.equals(messageSenderId))
            {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/comchatapp-bc852.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=032b9b9c-3248-42da-a407-4cf560665944").into(holder.messageSenderPicture);
//                holder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
//                        holder.itemView.getContext().startActivity(intent1);
//                    }
//                });
            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/comchatapp-bc852.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=032b9b9c-3248-42da-a407-4cf560665944").into(holder.messageReceiverPicture);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }
        if (fromUserID.equals(messageSenderId))
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and View this Document",
                                        "Cancel",
                                        "Delete from EveryOne"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which ==0)
                                {
                                    deleteSentMessages(position, holder);
                                }
                                if (which ==1)
                                {
                                    Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent1);
                                }

                                if (which ==3)
                                {
                                    deleteMessagesFromEveryOne(position, holder);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("text") )
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel",
                                        "Delete from EveryOne"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which ==0)
                                {
                                    deleteSentMessages(position, holder);
                                    Intent intent =new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                if (which ==2)
                                {
                                    deleteMessagesFromEveryOne(position, holder);
                                    Intent intent =new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("image") )
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        " View this Image",
                                        "Cancel",
                                        "Delete from EveryOne"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which ==0)
                                {
                                    deleteSentMessages(position, holder);
                                    Intent intent =new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if (which ==1)
                                {
                                    Intent intent =new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                if (which ==3)
                                {
                                    deleteMessagesFromEveryOne(position, holder);
                                    Intent intent =new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
        else
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and View this Document",
                                        "Cancel",

                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which ==0)
                                {
                                deleteReceivedMessages(position,holder);

                                }
                                if (which ==1)
                                {
                                    Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent1);
                                }
                                if (which ==2)
                                {

                                }

                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("text") )
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel",

                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which ==0)
                                {
                                    deleteReceivedMessages(position,holder);
                                    Intent intent =new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }
                   else if (userMessagesList.get(position).getType().equals("image") )
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        " View this Image",
                                        "Cancel",
                                        "Delete from EveryOne"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which ==0)
                                {
                                    deleteReceivedMessages(position,holder);
                                    Intent intent =new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if (which ==1)
                                {
                                    Intent intent =new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                if (which ==3)
                                {

                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }


    private  void  deleteSentMessages( final  int postion, final MessageViewHolder messageViewHolder)
    {
DatabaseReference rootRef =FirebaseDatabase.getInstance().getReference();
rootRef.child("Messages")
        .child(userMessagesList.get(postion).getFrom())
        .child(userMessagesList.get(postion).getTo())
        .child(userMessagesList.get(postion).getMessageID())
        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
    @Override
    public void onComplete(@NonNull @NotNull Task<Void> task)
    {
        if (task.isSuccessful()){
            Toast.makeText(messageViewHolder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(messageViewHolder.itemView.getContext(), "Error Ocurr", Toast.LENGTH_SHORT).show();

        }
    }
});

    }

    private  void  deleteReceivedMessages( final  int postion, final MessageViewHolder messageViewHolder)
    {
        DatabaseReference rootRef =FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(postion).getTo())
                .child(userMessagesList.get(postion).getFrom())
                .child(userMessagesList.get(postion).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task)
            {
                if (task.isSuccessful()){
                    Toast.makeText(messageViewHolder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(messageViewHolder.itemView.getContext(), "Error Ocurr", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }
    private  void  deleteMessagesFromEveryOne( final  int postion, final MessageViewHolder messageViewHolder)
    {
        DatabaseReference rootRef =FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(postion).getTo())
                .child(userMessagesList.get(postion).getFrom())
                .child(userMessagesList.get(postion).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task)
            {
                if (task.isSuccessful()){
                    rootRef.child("Messages")
                            .child(userMessagesList.get(postion).getFrom())
                            .child(userMessagesList.get(postion).getTo())
                            .child(userMessagesList.get(postion).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(messageViewHolder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(messageViewHolder.itemView.getContext(), "Error Ocurr", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

}
