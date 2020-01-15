package com.cyfoes.aditya.chachabiryani.user;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.cyfoes.aditya.chachabiryani.BuildConfig;
import com.cyfoes.aditya.chachabiryani.MainActivity;
import com.cyfoes.aditya.chachabiryani.R;
import com.cyfoes.aditya.chachabiryani.admin_home;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class splashscreen extends AppCompatActivity {

    DatabaseReference dbrapp = FirebaseDatabase.getInstance().getReference("app");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        getSupportActionBar().hide();

        if(BuildConfig.ADMIN_VERSION == false) {
            checkappversion();
        }
        else {
            starthome();
        }

        /*new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                *//* Create an Intent that will start the Menu-Activity. *//*
                if(BuildConfig.ADMIN_VERSION == true) {
                    startActivity(new Intent(splashscreen.this, admin_home.class));
                    finish();
                }
                else {
                    startActivity(new Intent(splashscreen.this, MainActivity.class));
                    finish();
                }
            }
        }, 2000);*/
    }

    private void checkappversion() {
        dbrapp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(BuildConfig.VERSION_CODE < Integer.parseInt(dataSnapshot.child("update").child("user_version").getValue().toString())){
                    if(dataSnapshot.child("update").child("type").getValue().toString().equals("critical")) {
                        showoldversiondialogcritical();
                    }
                    else if(dataSnapshot.child("update").child("type").getValue().toString().equals("normal")){
                        showoldversiondialognormal();
                    }
                    else {
                        starthome();
                    }
                }
                else {
                    starthome();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void starthome() {
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                if(BuildConfig.ADMIN_VERSION == true) {
                    startActivity(new Intent(splashscreen.this, admin_home.class));
                    finish();
                }
                else {
                    startActivity(new Intent(splashscreen.this, MainActivity.class));
                    finish();
                }
            }
        }, 2000);
    }

    private void showoldversiondialognormal() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Older version");
        builder.setCancelable(false);
        builder.setMessage("A newer version of the app is available, would you like to update?");
        builder.setPositiveButton("Update now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        })
                .setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        starthome();
                        Toast.makeText(splashscreen.this, "Okay", Toast.LENGTH_SHORT).show();
                    }
                }).create().show();
    }

    private void showoldversiondialogcritical() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Older version");
        builder.setCancelable(false);
        builder.setMessage("Kindly update the app to continue");
        builder.setPositiveButton("Update now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        })
                .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                        System.exit(0);
                    }
                }).create().show();


    }

    @Override
    protected void onResume() {
        if(BuildConfig.ADMIN_VERSION == false) {
            checkappversion();
        }
        super.onResume();
    }
}
