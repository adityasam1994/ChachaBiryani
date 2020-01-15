package com.cyfoes.aditya.chachabiryani;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cyfoes.aditya.chachabiryani.admin.myaccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class login extends AppCompatActivity {

    TextView resendotp;
    EditText phone, otp;
    Button btnlogin, sendcode;
    FirebaseAuth fauth;
    ProgressDialog pd;
    private String phoneVarificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks varificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendingToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        pd = new ProgressDialog(this);

        resendotp = (TextView) findViewById(R.id.sendotp);
        otp = (EditText) findViewById(R.id.otp);
        phone = (EditText) findViewById(R.id.etphone);
        btnlogin = (Button) findViewById(R.id.btnlogin);
        sendcode = (Button) findViewById(R.id.sendcode);

        resendotp.setTextColor(Color.parseColor("#787878"));
        fauth = FirebaseAuth.getInstance();
        btnlogin.setEnabled(false);

        sendcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phone.getText().toString().trim().length() == 10) {
                    sendotpcode();
                } else {
                    Toast.makeText(login.this, "Invalid number!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        resendotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendToken();
            }
        });

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                varifyCode();
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.login);
        view.performClick();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.homeicon:
                        startActivity(new Intent(login.this, MainActivity.class));
                        break;

                    case R.id.orders:
                        startActivity(new Intent(login.this, myorder.class));
                        break;

                    case R.id.cart:
                        startActivity(new Intent(login.this, cart.class));
                        break;
                }
                return true;
            }
        });
    }

    public void varifyCode() {
        pd.setMessage("Logging you in...");
        pd.show();
        String code = otp.getText().toString();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneVarificationId, code);
        signInWithPhoneAuthCredentials(credential);
    }

    public void resendToken() {
        pd.setMessage("Resending Code...");
        pd.show();
        String phonenumber = "+91" + phone.getText().toString();
        setupVarificationCallacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phonenumber,
                60,
                TimeUnit.SECONDS,
                this,
                varificationCallbacks
        );
    }

    private void sendotpcode() {
        pd.setMessage("Sending Code...");
        pd.show();
        String phonenumber = "+91" + phone.getText().toString();
        setupVarificationCallacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phonenumber,
                60,
                TimeUnit.SECONDS,
                this,
                varificationCallbacks
        );
    }

    private void setupVarificationCallacks() {
        varificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                resendotp.setTextColor(Color.parseColor("#787878"));
                btnlogin.setEnabled(true);
                otp.setText("");
                signInWithPhoneAuthCredentials(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(login.this, "Invalid Credentials" + e, Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(login.this, "Message quota exausted", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                phoneVarificationId = s;
                resendingToken = forceResendingToken;

                btnlogin.setEnabled(true);
                sendcode.setEnabled(false);
                resendotp.setTextColor(Color.parseColor("#f6445c"));
                pd.dismiss();
            }
        };
    }

    private void signInWithPhoneAuthCredentials(PhoneAuthCredential phoneAuthCredential) {
        fauth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            otp.setText("");
                            resendotp.setTextColor(Color.parseColor("#787878"));
                            sendcode.setEnabled(false);
                            btnlogin.setEnabled(false);
                            FirebaseUser user = task.getResult().getUser();
                            startActivity(new Intent(login.this, myaccount.class));
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(login.this, "Invalid token", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.login);
        view.performClick();
        super.onResume();
    }
}
