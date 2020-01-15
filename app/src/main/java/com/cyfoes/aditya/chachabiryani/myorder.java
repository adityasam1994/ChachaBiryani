package com.cyfoes.aditya.chachabiryani;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cyfoes.aditya.chachabiryani.admin.myaccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class myorder extends AppCompatActivity {

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    LinearLayout mainlayout;
    DatabaseReference dbrorder = FirebaseDatabase.getInstance().getReference("myorders");
    DatabaseReference dbrshop = FirebaseDatabase.getInstance().getReference("shop");
    String shopphone="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myorder);

        getSupportActionBar().hide();

        mainlayout = (LinearLayout) findViewById(R.id.mainlayout);

        if (firebaseAuth.getCurrentUser() != null) {
            //createcards();
            getshopphone();
        } else {
            Toast.makeText(this, "Login first to view your orders", Toast.LENGTH_SHORT).show();
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.orders);
        view.performClick();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.homeicon:
                        startActivity(new Intent(myorder.this, MainActivity.class));
                        break;

                    case R.id.login:
                        if (firebaseAuth.getCurrentUser() != null) {
                            startActivity(new Intent(myorder.this, myaccount.class));
                        } else {
                            startActivity(new Intent(myorder.this, login.class));
                        }
                        break;

                    case R.id.cart:
                        startActivity(new Intent(myorder.this, cart.class));
                        break;
                }
                return true;
            }
        });
    }

    private void getshopphone() {
        dbrshop.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("phone")) {
                    shopphone = dataSnapshot.child("phone").getValue().toString();
                }
                createcards();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createcards() {
        final ArrayList<String> carddates = new ArrayList<>();
        dbrorder.addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mainlayout.removeAllViews();
                int count = 0;
                if (dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())) {
                    for (final DataSnapshot dorder : dataSnapshot.child(firebaseAuth.getCurrentUser().getUid()).getChildren()) {
                        count++;
                        LayoutInflater inflater = LayoutInflater.from(myorder.this);
                        LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.myorder_item, null, false);
                        lay.setId(count);
                        lay.setTag(dorder.getKey().toString());

                        String mydate = getmydate(dorder.child("date").getValue().toString(),dorder.child("time").getValue().toString());
                        carddates.add(mydate+"/"+count);

                        mainlayout.addView(lay);

                        TextView name = lay.findViewById(R.id.name);
                        TextView price = lay.findViewById(R.id.price);
                        TextView date = lay.findViewById(R.id.date);
                        TextView code = lay.findViewById(R.id.ordercode);
                        ImageView status = lay.findViewById(R.id.status);

                        String ord = dorder.child("order").getValue().toString();
                        String[] sp = ord.split(":::");
                        String order = "";

                        final ArrayList<String> list = new ArrayList<String>();
                        for (String s : sp) {
                            list.add(s);
                        }
                        for (String s : list) {
                            String[] splitext = s.split("/");
                            if (order.equals("")) {
                                order = splitext[2];
                            } else {
                                order = order + " + " + splitext[2];
                            }
                        }

                        double cost = 0;
                        String[] spp = ord.split(":::");
                        for (String s : spp) {
                            String[] splittext = s.split("/");
                            cost = cost + Double.parseDouble(splittext[3]);
                        }

                        name.setText(order);
                        //price.setText("\u20B9 " + cost);
                        price.setText("\u20B9 " + (Double.parseDouble(dorder.child("cost").getValue().toString()) + Double.parseDouble(dorder.child("delivery_charge").getValue().toString())));
                        date.setText(dorder.child("date").getValue().toString());
                        code.setText(dorder.getKey().toString());

                        if (dorder.child("order_status").getValue().toString().equals("pending")) {
                            status.setImageResource(R.drawable.pending);
                        } else if (dorder.child("order_status").getValue().toString().equals("accepted")) {
                            status.setImageResource(R.drawable.accept);
                            status.setBackgroundResource(R.drawable.ring);
                        } else if (dorder.child("order_status").getValue().toString().equals("rejected")
                                || dorder.child("order_status").getValue().toString().equals("cancelled")) {
                            status.setImageResource(R.drawable.cross);
                            status.setBackgroundResource(R.drawable.redring);
                        } else {
                            status.setImageResource(R.drawable.checkmark);
                        }
                        lay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Dialog dialog = new Dialog(myorder.this);
                                dialog.setContentView(R.layout.myorder_popup);
                                dialog.show();

                                Window window = dialog.getWindow();
                                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                TextView code = dialog.findViewById(R.id.code);
                                TextView date = dialog.findViewById(R.id.date);
                                final TextView price = dialog.findViewById(R.id.price);
                                TextView status = dialog.findViewById(R.id.status);
                                TextView payment = dialog.findViewById(R.id.payment);
                                TextView pmode = dialog.findViewById(R.id.pmode);
                                Button btnpay = dialog.findViewById(R.id.btnpay);
                                Button btncall = dialog.findViewById(R.id.btncall);
                                LinearLayout order_layout = dialog.findViewById(R.id.order_item_layout);

                                if(dorder.child("payment_method").getValue().toString().equals("online")
                                        && dorder.child("payment_status").getValue().toString().equals("pending")
                                        && dorder.child("order_status").getValue().toString().equals("accepted")){
                                    btnpay.setVisibility(View.VISIBLE);
                                }
                                else {
                                    btnpay.setVisibility(View.GONE);
                                }

                                status.setText(dorder.child("order_status").getValue().toString().toUpperCase());
                                payment.setText(dorder.child("payment_status").getValue().toString().toUpperCase());

                                btncall.setOnClickListener(new View.OnClickListener() {
                                    @SuppressLint("MissingPermission")
                                    @Override
                                    public void onClick(View v) {
                                        if (!shopphone.equals("")) {
                                            Intent intent = new Intent(Intent.ACTION_CALL);
                                            intent.setData(Uri.parse("tel:" + shopphone));

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                                    startActivity(intent);
                                                } else {
                                                    requstcallpermission();
                                                }
                                            } else {
                                                startActivity(intent);
                                            }
                                        }
                                    }
                                });

                                if(dorder.child("payment_method").getValue().toString().equals("online")){
                                    pmode.setText("ONLINE");
                                }
                                else if(dorder.child("payment_method").getValue().toString().equals("cod")){
                                    pmode.setText("CASH ON DELIVERY");
                                }

                                code.setText(dorder.getKey().toString());
                                price.setText("\u20B9 " + (Double.parseDouble(dorder.child("cost").getValue().toString()) + Double.parseDouble(dorder.child("delivery_charge").getValue().toString())));
                                date.setText(dorder.child("date").getValue().toString() + " [" + dorder.child("time").getValue().toString() + "]");

                                final String order = dorder.child("order").getValue().toString();

                                String[] sp = order.split(":::");
                                ArrayList<String> list = new ArrayList<>();
                                for (String s : sp) {
                                    list.add(s);
                                }
                                double cost = Double.parseDouble(getString(R.string.delivery_charge));
                                for (String s : list) {
                                    String[] splittext = s.split("/");

                                    cost = cost + Double.parseDouble(splittext[3]);

                                    LayoutInflater inflater = LayoutInflater.from(myorder.this);
                                    LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.order_item, null, false);
                                    order_layout.addView(lay);

                                    TextView itemname = lay.findViewById(R.id.itemname);
                                    TextView itemweight = lay.findViewById(R.id.itemweight);

                                    itemname.setText(splittext[2]);
                                    itemweight.setText(splittext[1] + " Kg");
                                }

                                btnpay.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(myorder.this, checksumrefund.class);
                                        intent.putExtra("orderid", dorder.getKey().toString());
                                        intent.putExtra("cost", price.getText().toString().substring(2) + "");
                                        intent.putExtra("order", order);
                                        intent.putExtra("address", dorder.child("address").getValue().toString());
                                        intent.putExtra("latitude", dorder.child("latitude").getValue().toString());
                                        intent.putExtra("longitude", dorder.child("longitude").getValue().toString());
                                        intent.putExtra("date", dorder.child("date").getValue().toString());
                                        intent.putExtra("time", dorder.child("time").getValue().toString());
                                        intent.putExtra("custid", firebaseAuth.getCurrentUser().getUid().toString());
                                        startActivity(intent);
                                    }
                                });
                            }
                        });
                        if(count == dataSnapshot.child(firebaseAuth.getCurrentUser().getUid()).getChildrenCount()){
                            ArrayList<String> newcards = sortcards(carddates);
                            for(String s: newcards){
                                String[] spl = s.split("/");
                                LinearLayout l = (LinearLayout)findViewById(Integer.parseInt(spl[1]));
                                mainlayout.removeView(l);
                                mainlayout.addView(l);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private ArrayList<String> sortcards(ArrayList<String> carddates) {
        Collections.sort(carddates, Collections.<String>reverseOrder());
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
        if (requestCode == 456) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Call permission is required to make calls", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.orders);
        view.performClick();
        super.onResume();
    }
}
