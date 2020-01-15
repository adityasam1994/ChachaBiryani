package com.cyfoes.aditya.chachabiryani.admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cyfoes.aditya.chachabiryani.R;
import com.cyfoes.aditya.chachabiryani.admin_home;
import com.cyfoes.aditya.chachabiryani.shop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class admin_completed extends AppCompatActivity {

    LinearLayout mainlayout;
    DatabaseReference dbrorder = FirebaseDatabase.getInstance().getReference("myorders");
    DatabaseReference dbrusers = FirebaseDatabase.getInstance().getReference("users");
    LinearLayout emptylayout;
    ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_completed);

        getSupportActionBar().hide();

        emptylayout = (LinearLayout)findViewById(R.id.emptylayout);
        scrollView = (ScrollView) findViewById(R.id.scrollview);

        mainlayout = (LinearLayout)findViewById(R.id.mainlayout);
        createcards();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.completed);
        view.performClick();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.pending:
                        startActivity(new Intent(admin_completed.this, admin_pending.class));
                        break;

                    case R.id.accepted:
                        startActivity(new Intent(admin_completed.this, admin_home.class));
                        break;

                    case R.id.menu:
                        startActivity(new Intent(admin_completed.this, menu.class));
                        break;

                    case R.id.shop:
                        startActivity(new Intent(admin_completed.this, shop.class));
                        break;
                }
                return true;
            }
        });
    }

    private void createcards() {
        dbrorder.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<String> carddates = new ArrayList<>();
                mainlayout.removeAllViews();
                int count = 0;
                for(final DataSnapshot dusers: dataSnapshot.getChildren()){
                    for(final DataSnapshot dorders: dusers.getChildren()){
                        if(dorders.child("order_status").getValue().toString().equals("completed")){
                            count++;
                            LayoutInflater inflater = LayoutInflater.from(admin_completed.this);
                            LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.admin_pending_item, null, false);

                            lay.setId(count);
                            lay.setTag(dorders.getKey().toString());

                            String mydate = getmydate(dorders.child("date").getValue().toString(),dorders.child("time").getValue().toString());
                            carddates.add(mydate+"/"+count);

                            mainlayout.addView(lay);

                            TextView code = lay.findViewById(R.id.code);
                            TextView name = lay.findViewById(R.id.name);
                            //LinearLayout item_layout = lay.findViewById(R.id.item_layout);
                            TextView price = lay.findViewById(R.id.price);
                            TextView date = lay.findViewById(R.id.date);
                            final TextView customername = lay.findViewById(R.id.customername);
                            final TextView phone = lay.findViewById(R.id.phone);
                            TextView distance = lay.findViewById(R.id.distance);
                            ImageView payment = lay.findViewById(R.id.payment);

                            payment.setImageResource(R.drawable.checkmark);

                            code.setText(dorders.getKey().toString());
                            date.setText(dorders.child("date").getValue().toString()+" ["+dorders.child("time").getValue().toString()+"]");

                            dbrusers.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    customername.setText(dataSnapshot.child(dusers.getKey()).child("name").getValue().toString());
                                    if(dataSnapshot.child(dusers.getKey()).hasChild("phone")) {
                                        phone.setText("+91 "+dataSnapshot.child(dusers.getKey()).child("phone").getValue().toString());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            if(dorders.child("payment_status").getValue().toString().equals("done")){
                                payment.setImageResource(R.drawable.checkmark);
                            }
                            else {
                                payment.setImageResource(R.drawable.pending);
                            }

                            String ord = dorders.child("order").getValue().toString();
                            String[] sp = ord.split(":::");
                            String order="";

                            final ArrayList<String> list = new ArrayList<String>();
                            for(String s:sp){
                                list.add(s);
                            }
                            for(String s:list){
                                String[] splitext = s.split("/");
                                if(order.equals("")){
                                    order = splitext[2];
                                }
                                else {
                                    order = order + " + " +splitext[2];
                                }
                            }

                            name.setText(order);

                            double cost = Double.parseDouble(getString(R.string.delivery_charge));
                            String[] spp = ord.split(":::");
                            for(String s:spp){
                                String[] splittext = s.split("/");
                                cost = cost + Double.parseDouble(splittext[3]);
                            }

                            //price.setText("\u20B9 "+cost);

                            price.setText("\u20B9 "+(Double.parseDouble(dorders.child("cost").getValue().toString()) +Double.parseDouble(dorders.child("delivery_charge").getValue().toString())));

                            final double lat=Double.parseDouble(dorders.child("latitude").getValue().toString());
                            final double lng=Double.parseDouble(dorders.child("longitude").getValue().toString());

                            Location locc = new Location("");
                            locc.setLatitude(lat);
                            locc.setLongitude(lng);

                            Location locp = new Location("");
                            locp.setLatitude(26.7199734);
                            locp.setLongitude(83.3867575);

                            double dist = locc.distanceTo(locp);
                            String rounded = String.format("%.2f", dist/1000);
                            distance.setText(rounded+" Km");

                            lay.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Dialog dialog = new Dialog(admin_completed.this);
                                    dialog.setContentView(R.layout.accepted_popup);
                                    dialog.show();

                                    Window window = dialog.getWindow();
                                    window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    TextView code = dialog.findViewById(R.id.code);
                                    LinearLayout order_layout = dialog.findViewById(R.id.order_item_layout);
                                    final TextView customername = dialog.findViewById(R.id.customername);
                                    TextView address = dialog.findViewById(R.id.address);
                                    TextView distance = dialog.findViewById(R.id.distance);
                                    TextView price = dialog.findViewById(R.id.price);
                                    ImageView gps = dialog.findViewById(R.id.gps);
                                    final TextView phone = dialog.findViewById(R.id.phone);
                                    final Button btncall = dialog.findViewById(R.id.btncall);
                                    Button markcomplete = dialog.findViewById(R.id.markcomplete);

                                    markcomplete.setVisibility(View.GONE);
                                    code.setText(dorders.getKey().toString());
                                    address.setText(dorders.child("address").getValue().toString());

                                    final double lat=Double.parseDouble(dorders.child("latitude").getValue().toString());
                                    final double lng=Double.parseDouble(dorders.child("longitude").getValue().toString());

                                    gps.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Uri intenturi = Uri.parse("google.navigation:q=" + lat +"," +lng);
                                            Intent mapintent = new Intent(Intent.ACTION_VIEW, intenturi);
                                            mapintent.setPackage("com.google.android.apps.maps");
                                            startActivity(mapintent);
                                        }
                                    });


                                    Location locc = new Location("");
                                    locc.setLatitude(lat);
                                    locc.setLongitude(lng);

                                    Location locp = new Location("");
                                    locp.setLatitude(26.7199734);
                                    locp.setLongitude(83.3867575);

                                    double dist = locc.distanceTo(locp);
                                    String rounded = String.format("%.2f", dist/1000);
                                    distance.setText(rounded+" Km");

                                    dbrusers.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                            customername.setText(dataSnapshot.child(dusers.getKey()).child("name").getValue().toString());
                                            if(dataSnapshot.child(dusers.getKey()).hasChild("phone")){
                                                phone.setText("+91"+dataSnapshot.child(dusers.getKey()).child("phone").getValue().toString());

                                                btncall.setOnClickListener(new View.OnClickListener() {
                                                    @SuppressLint("MissingPermission")
                                                    @Override
                                                    public void onClick(View v) {
                                                        Intent intent=new Intent(Intent.ACTION_CALL);
                                                        intent.setData(Uri.parse("tel:"+dataSnapshot.child(dusers.getKey()).child("phone").getValue().toString()));

                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                            if(checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                                                                startActivity(intent);
                                                            }
                                                            else {
                                                                requstcallpermission();
                                                            }
                                                        }
                                                        else {
                                                            startActivity(intent);
                                                        }
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    String order = dorders.child("order").getValue().toString();

                                    String[] sp = order.split(":::");
                                    ArrayList<String> list = new ArrayList<>();
                                    for (String s: sp){
                                        list.add(s);
                                    }
                                    double cost = Double.parseDouble(getString(R.string.delivery_charge));
                                    for(String s:list){
                                        String[] splittext = s.split("/");

                                        cost = cost + Double.parseDouble(splittext[3]);

                                        LayoutInflater inflater = LayoutInflater.from(admin_completed.this);
                                        LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.order_item, null, false);
                                        order_layout.addView(lay);

                                        TextView itemname = lay.findViewById(R.id.itemname);
                                        TextView itemweight = lay.findViewById(R.id.itemweight);

                                        itemname.setText(splittext[2]);
                                        itemweight.setText(splittext[1]+" Kg");
                                    }

                                    //price.setText("\u20B9 "+cost);

                                    price.setText("\u20B9 "+(dorders.child("cost").getValue().toString()+ " + " +dorders.child("delivery_charge").getValue().toString()));
                                }
                            });
                        }
                    }
                }
                ArrayList<String> newcards = sortcards(carddates);
                for (String s : newcards) {
                    String[] spl = s.split("/");
                    LinearLayout l = (LinearLayout) findViewById(Integer.parseInt(spl[1]));
                    mainlayout.removeView(l);
                    mainlayout.addView(l);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private ArrayList<String> sortcards(ArrayList<String> carddates) {
        Collections.sort(carddates);
        return carddates;
    }


    private String getmydate(String s, String t) {
        String[] sp = s.split("-");
        String d = sp[2]+sp[1]+sp[0];
        String[] tp = t.split(":");
        String newtime = tp[0]+tp[1];
        return (d+newtime);
    }

    private void requstcallpermission() {
        String[] reqloc = new String[]{Manifest.permission.CALL_PHONE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(reqloc, 456);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 456){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Call permission is required to make calls", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.completed);
        view.performClick();
        super.onResume();
    }
}
