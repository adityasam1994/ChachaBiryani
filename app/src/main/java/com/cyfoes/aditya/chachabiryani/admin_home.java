package com.cyfoes.aditya.chachabiryani;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.cyfoes.aditya.chachabiryani.admin.admin_completed;
import com.cyfoes.aditya.chachabiryani.admin.admin_pending;
import com.cyfoes.aditya.chachabiryani.admin.menu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class admin_home extends AppCompatActivity {

    LinearLayout mainlayout;
    DatabaseReference dbrorder = FirebaseDatabase.getInstance().getReference("myorders");
    DatabaseReference dbruser = FirebaseDatabase.getInstance().getReference("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        checkPermissions();

        mainlayout = (LinearLayout) findViewById(R.id.mainlayout);
        createcards();

        getSupportActionBar().hide();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.pending:
                        startActivity(new Intent(admin_home.this, admin_pending.class));
                        break;

                    case R.id.completed:
                        startActivity(new Intent(admin_home.this, admin_completed.class));
                        break;

                    case R.id.menu:
                        startActivity(new Intent(admin_home.this, menu.class));
                        break;

                    case R.id.shop:
                        startActivity(new Intent(admin_home.this, shop.class));
                        break;
                }
                return true;
            }
        });

        checkfororders();

    }

    private void createcards() {
        dbrorder.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<String> carddates = new ArrayList<>();
                mainlayout.removeAllViews();
                int count = 0;
                for (final DataSnapshot dusers : dataSnapshot.getChildren()) {
                    for (final DataSnapshot dorders : dusers.getChildren()) {
                        if (dorders.child("order_status").getValue().toString().equals("accepted")) {
                            count++;
                            LayoutInflater inflater = LayoutInflater.from(admin_home.this);
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

                            code.setText(dorders.getKey().toString());
                            date.setText(dorders.child("date").getValue().toString() + " [" + dorders.child("time").getValue().toString() + "]");

                            dbruser.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    customername.setText(dataSnapshot.child(dusers.getKey()).child("name").getValue().toString());
                                    if (dataSnapshot.child(dusers.getKey()).hasChild("phone")) {
                                        phone.setText("+91 " + dataSnapshot.child(dusers.getKey()).child("phone").getValue().toString());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            if (dorders.child("payment_status").getValue().toString().equals("done")) {
                                payment.setImageResource(R.drawable.checkmark);
                            } else {
                                payment.setImageResource(R.drawable.pending);
                            }

                            String ord = dorders.child("order").getValue().toString();
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

                            name.setText(order);

                            double cost = Double.parseDouble(getString(R.string.delivery_charge));
                            String[] spp = ord.split(":::");
                            for (String s : spp) {
                                String[] splittext = s.split("/");
                                cost = cost + Double.parseDouble(splittext[3]);
                            }

                            //price.setText("\u20B9 "+cost);

                            price.setText("\u20B9 " + (Double.parseDouble(dorders.child("cost").getValue().toString()) + Double.parseDouble(dorders.child("delivery_charge").getValue().toString())));

                            final double lat = Double.parseDouble(dorders.child("latitude").getValue().toString());
                            final double lng = Double.parseDouble(dorders.child("longitude").getValue().toString());

                            Location locc = new Location("");
                            locc.setLatitude(lat);
                            locc.setLongitude(lng);

                            Location locp = new Location("");
                            locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                            locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                            double dist = locc.distanceTo(locp);
                            String rounded = String.format("%.2f", dist / 1000);
                            distance.setText(rounded + " Km");

                            lay.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final Dialog dialog = new Dialog(admin_home.this);
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

                                    code.setText(dorders.getKey().toString());
                                    address.setText(dorders.child("address").getValue().toString());

                                    final double lat = Double.parseDouble(dorders.child("latitude").getValue().toString());
                                    final double lng = Double.parseDouble(dorders.child("longitude").getValue().toString());

                                    gps.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Uri intenturi = Uri.parse("google.navigation:q=" + lat + "," + lng);
                                            Intent mapintent = new Intent(Intent.ACTION_VIEW, intenturi);
                                            mapintent.setPackage("com.google.android.apps.maps");
                                            startActivity(mapintent);
                                        }
                                    });


                                    Location locc = new Location("");
                                    locc.setLatitude(lat);
                                    locc.setLongitude(lng);

                                    Location locp = new Location("");
                                    locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                                    locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                                    double dist = locc.distanceTo(locp);
                                    String rounded = String.format("%.2f", dist / 1000);
                                    distance.setText(rounded + " Km");

                                    dbruser.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                            customername.setText(dataSnapshot.child(dusers.getKey()).child("name").getValue().toString());
                                            if (dataSnapshot.child(dusers.getKey()).hasChild("phone")) {
                                                phone.setText("+91" + dataSnapshot.child(dusers.getKey()).child("phone").getValue().toString());

                                                btncall.setOnClickListener(new View.OnClickListener() {
                                                    @SuppressLint("MissingPermission")
                                                    @Override
                                                    public void onClick(View v) {
                                                        Intent intent = new Intent(Intent.ACTION_CALL);
                                                        intent.setData(Uri.parse("tel:" + dataSnapshot.child(dusers.getKey()).child("phone").getValue().toString()));

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
                                    for (String s : sp) {
                                        list.add(s);
                                    }
                                    double cost = Double.parseDouble(getString(R.string.delivery_charge));
                                    for (String s : list) {
                                        String[] splittext = s.split("/");

                                        cost = cost + Double.parseDouble(splittext[3]);

                                        LayoutInflater inflater = LayoutInflater.from(admin_home.this);
                                        LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.order_item, null, false);
                                        order_layout.addView(lay);

                                        TextView itemname = lay.findViewById(R.id.itemname);
                                        TextView itemweight = lay.findViewById(R.id.itemweight);

                                        itemname.setText(splittext[2]);
                                        itemweight.setText(splittext[1] + " Kg");
                                    }

                                    //price.setText("\u20B9 "+cost);

                                    price.setText("\u20B9 " + dorders.child("cost").getValue().toString() + " + " + dorders.child("delivery_charge").getValue().toString());

                                    markcomplete.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            final AlertDialog.Builder builder = new AlertDialog.Builder(admin_home.this);
                                            builder.setTitle("Mark complete");
                                            builder.setMessage("Are you sure you want to mark this order as completed?");
                                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dbrorder.child(dusers.getKey().toString()).child(dorders.getKey().toString()).child("order_status").setValue("completed");
                                                    dbrorder.child(dusers.getKey().toString()).child(dorders.getKey().toString()).child("payment_status").setValue("done");
                                                    dialog.dismiss();
                                                    Toast.makeText(admin_home.this, "Order completed", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.cancel();
                                                            Toast.makeText(admin_home.this, "Okay", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                            builder.create().show();

                                        }
                                    });
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
        if (requestCode == 456) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Call permission is required to make calls", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void checkfororders() {
        dbrorder.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot dusers : dataSnapshot.getChildren()) {
                    for (final DataSnapshot dorders : dusers.getChildren()) {
                        if (dorders.hasChild("order_status") && dorders.hasChild("address") && dorders.hasChild("latitude")
                                && dorders.hasChild("longitude") && dorders.hasChild("cost") && dorders.hasChild("delivery_charge")
                                && dorders.hasChild("order")) {

                            if (dorders.child("order_status").getValue().toString().equals("pending")) {
                                final Dialog dialog = new Dialog(admin_home.this);
                                dialog.setContentView(R.layout.order_popup);
                                dialog.show();

                                Window window = dialog.getWindow();
                                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                TextView code = dialog.findViewById(R.id.code);
                                LinearLayout order_layout = dialog.findViewById(R.id.order_item_layout);
                                final TextView customername = dialog.findViewById(R.id.customername);
                                TextView address = dialog.findViewById(R.id.address);
                                TextView distance = dialog.findViewById(R.id.distance);
                                TextView price = dialog.findViewById(R.id.price);
                                Button accept = dialog.findViewById(R.id.btnaccept);
                                Button reject = dialog.findViewById(R.id.btncancel);
                                ImageView gps = dialog.findViewById(R.id.gps);
                                final TextView phone = dialog.findViewById(R.id.phone);
                                final Button btncall = dialog.findViewById(R.id.btncall);

                                code.setText(dorders.getKey().toString());
                                address.setText(dorders.child("address").getValue().toString());

                                final double lat = Double.parseDouble(dorders.child("latitude").getValue().toString());
                                final double lng = Double.parseDouble(dorders.child("longitude").getValue().toString());

                                gps.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Uri intenturi = Uri.parse("google.navigation:q=" + lat + "," + lng);
                                        Intent mapintent = new Intent(Intent.ACTION_VIEW, intenturi);
                                        mapintent.setPackage("com.google.android.apps.maps");
                                        startActivity(mapintent);
                                    }
                                });


                                Location locc = new Location("");
                                locc.setLatitude(lat);
                                locc.setLongitude(lng);

                                Location locp = new Location("");
                                locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                                locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                                double dist = locc.distanceTo(locp);
                                String rounded = String.format("%.2f", dist / 1000);
                                distance.setText(rounded + " Km");

                                dbruser.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                        customername.setText(dataSnapshot.child(dusers.getKey()).child("name").getValue().toString());
                                        if (dataSnapshot.child(dusers.getKey()).hasChild("phone")) {
                                            phone.setText("+91" + dataSnapshot.child(dusers.getKey()).child("phone").getValue().toString());

                                            btncall.setOnClickListener(new View.OnClickListener() {
                                                @SuppressLint("MissingPermission")
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent(Intent.ACTION_CALL);
                                                    intent.setData(Uri.parse("tel:" + dataSnapshot.child(dusers.getKey()).child("phone").getValue().toString()));

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
                                for (String s : sp) {
                                    list.add(s);
                                }
                                double cost = Double.parseDouble(getString(R.string.delivery_charge));
                                for (String s : list) {
                                    String[] splittext = s.split("/");

                                    cost = cost + Double.parseDouble(splittext[3]);

                                    LayoutInflater inflater = LayoutInflater.from(admin_home.this);
                                    LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.order_item, null, false);
                                    order_layout.addView(lay);

                                    TextView itemname = lay.findViewById(R.id.itemname);
                                    TextView itemweight = lay.findViewById(R.id.itemweight);

                                    itemname.setText(splittext[2]);
                                    itemweight.setText(splittext[1] + " Kg");
                                }

                                //price.setText("\u20B9 "+cost);

                                price.setText("\u20B9 " + dorders.child("cost").getValue().toString() + " + " + dorders.child("delivery_charge").getValue().toString());

                                accept.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dbrorder.child(dusers.getKey()).child(dorders.getKey()).child("order_status").setValue("accepted");
                                        dialog.dismiss();
                                        Toast.makeText(admin_home.this, "Order accepted!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                reject.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dbrorder.child(dusers.getKey()).child(dorders.getKey()).child("order_status").setValue("rejected");
                                        dialog.dismiss();
                                        Toast.makeText(admin_home.this, "Order rejected!", Toast.LENGTH_SHORT).show();
                                        //startActivity(new Intent(admin_home.this, checksumrefund.class));

                                    }
                                });
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

    private void checkPermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        android.Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();
    }

    @Override
    protected void onResume() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.accepted);
        view.performClick();
        super.onResume();
    }
}
