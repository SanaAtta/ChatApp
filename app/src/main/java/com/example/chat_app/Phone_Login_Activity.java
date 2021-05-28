package com.example.chat_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class Phone_Login_Activity extends AppCompatActivity {

    private Button  continueAndNextButton;
    private CountryCodePicker countryCodePicker;
    private EditText  phoneText, codeText;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private String mVerificationId, checker="", phoneNumber="";
    RelativeLayout relativeLayout;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone__login_);

            mAuth = FirebaseAuth.getInstance();

            phoneText=findViewById(R.id.phoneText);
            codeText=findViewById(R.id.codeText);
            continueAndNextButton=findViewById(R.id.continueNextButton);
            relativeLayout=findViewById(R.id.phoneAuth);
            countryCodePicker =findViewById(R.id.ccp);
            countryCodePicker.registerCarrierNumberEditText(phoneText);
            mAuth.setLanguageCode("en");


            loadingBar = new ProgressDialog(this);
            continueAndNextButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (continueAndNextButton.getText().equals("Submit") || checker.equals("Code Sent"))
            {
                String verificationCode =codeText.getText().toString();
                if (verificationCode.equals(""))
                {
                    Toast.makeText(Phone_Login_Activity.this, "please write verification code first", Toast.LENGTH_SHORT).show();
                }
                else
                    {
                        loadingBar.setTitle("Code Verification");
                        loadingBar.setMessage("please wait, while we are authenticating your code...");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                       signInWithPhoneAuthCredential(credential);
                }
            }
            else
            {
                phoneNumber=countryCodePicker.getFullNumberWithPlus();
                if (!phoneNumber.equals(""))
                {
                    loadingBar.setTitle("Phone Number Verification");
                    loadingBar.setMessage("please wait, while we are authenticating your phone number...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAuth)
                                    .setPhoneNumber(phoneNumber)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(Phone_Login_Activity.this)                 // Activity (for callback binding)
                                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);

                }
                else
                {
                    Toast.makeText(Phone_Login_Activity.this, "please write valid phone number", Toast.LENGTH_SHORT).show();
                }
            }
        }
    });



            callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                loadingBar.dismiss();
                Toast.makeText(Phone_Login_Activity.this, "Invalid Phone Number, Please enter correct phone number with your country code...", Toast.LENGTH_SHORT).show();

                relativeLayout.setVisibility(View.VISIBLE);
                continueAndNextButton.setText("Continue");
                codeText.setVisibility(View.GONE);

            }

            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token)
            {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                 PhoneAuthProvider.ForceResendingToken mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(Phone_Login_Activity.this, "Code has been sent, please check and verify...", Toast.LENGTH_SHORT).show();

                relativeLayout.setVisibility(View.GONE);
                checker="Code Sent";
                continueAndNextButton.setText("Submit");
                codeText.setVisibility(View.VISIBLE);
//                InputPhoneNumber.setVisibility(View.INVISIBLE);
//
                codeText.setVisibility(View.VISIBLE);
//                InputVerificationCode.setVisibility(View.VISIBLE);
            }
        };
    }

            private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful())
                                {
                                    loadingBar.dismiss();
                                    Toast.makeText(Phone_Login_Activity.this, "Congratulations, you're logged in successfully...", Toast.LENGTH_SHORT).show();
                                    SendUserToMainActivity();
                                }
                                else
                                {
                                    loadingBar.dismiss();
                                    String message = task.getException().toString();
                                    Toast.makeText(Phone_Login_Activity.this, "Error : "  +  message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(Phone_Login_Activity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}