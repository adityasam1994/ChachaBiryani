package com.cyfoes.aditya.chachabiryani;

import android.app.TimePickerDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.cyfoes.aditya.chachabiryani.admin.admin_completed;
import com.cyfoes.aditya.chachabiryani.admin.menu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class shop extends AppCompatActivity {

    Switch shopswitch;
    EditText charge, radius, etphone;
    Button btnsave;
    Boolean shopopen;
    TextView opentime, closetime;
    DatabaseReference dbrshop = FirebaseDatabase.getInstance().getReference("shop");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        getSupportActionBar().hide();

        opentime = (TextView)findViewById(R.id.opentime);
        closetime = (TextView) findViewById(R.id.closetime);
        radius = (EditText) findViewById(R.id.radius);
        charge = (EditText) findViewById(R.id.charge);
        btnsave = (Button)findViewById(R.id.btnsave);
        shopswitch = (Switch)findViewById(R.id.shopswitch);
        etphone = (EditText)findViewById(R.id.etphone);

        //opentime.setText("");
        //closetime.setText("");

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.shop);
        view.performClick();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.accepted:
                        startActivity(new Intent(shop.this, admin_home.class));
                        break;

                    case R.id.completed:
                        startActivity(new Intent(shop.this, admin_completed.class));
                        break;

                    case R.id.pending:
                        startActivity(new Intent(shop.this, admin_completed.class));
                        break;

                    case R.id.menu:
                        startActivity(new Intent(shop.this, menu.class));
                        break;
                }
                return true;
            }
        });

        opentime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] time = opentime.getText().toString().split(":");
                int hour = Integer.parseInt(time[0]);
                int minute = Integer.parseInt(time[1]);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(shop.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        String hour = i+"";
                        String min = i1+"";
                        if(i<10){
                            hour = "0"+i;
                        }
                        if(i1<10){
                            min = "0"+i1;
                        }
                        opentime.setText(hour+":"+min);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select open time");
                mTimePicker.show();
            }
        });

        closetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] time = closetime.getText().toString().split(":");
                int hour = Integer.parseInt(time[0]);
                int minute = Integer.parseInt(time[1]);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(shop.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        String hour = i+"";
                        String min = i1+"";
                        if(i<10){
                            hour = "0"+i;
                        }
                        if(i1<10){
                            min = "0"+i1;
                        }
                        closetime.setText(hour+":"+min);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select close time");
                mTimePicker.show();
            }
        });

        dbrshop.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                radius.setText(dataSnapshot.child("radius").getValue().toString());
                charge.setText(dataSnapshot.child("delivery_charge").getValue().toString());
                etphone.setText(dataSnapshot.child("phone").getValue().toString());
                opentime.setText(dataSnapshot.child("opentime").getValue().toString());
                closetime.setText(dataSnapshot.child("closetime").getValue().toString());

                if(dataSnapshot.child("status").getValue().toString().equals("open")){
                    shopswitch.setChecked(true);
                }
                else {
                    shopswitch.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        shopswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b == true){
                    dbrshop.child("status").setValue("open");
                    Toast.makeText(shop.this, "Shop opened", Toast.LENGTH_SHORT).show();
                }
                else {
                    dbrshop.child("status").setValue("closed");
                    Toast.makeText(shop.this, "Shop closed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String rd = radius.getText().toString().trim();
                String ch = charge.getText().toString().trim();
                String phone = etphone.getText().toString().trim();

                if(!ch.isEmpty() && !rd.isEmpty() && !phone.isEmpty()) {
                    if(etphone.getText().toString().length() < 10){
                        Toast.makeText(shop.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        dbrshop.child("delivery_charge").setValue(ch);
                        dbrshop.child("radius").setValue(rd);
                        dbrshop.child("phone").setValue(phone);
                        dbrshop.child("opentime").setValue(opentime.getText().toString());
                        dbrshop.child("closetime").setValue(closetime.getText().toString());
                        Toast.makeText(shop.this, "Saved", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(shop.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    @Override
    protected void onResume() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.shop);
        view.performClick();
        super.onResume();
    }
}
