package com.cyfoes.aditya.chachabiryani.admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cyfoes.aditya.chachabiryani.MainActivity;
import com.cyfoes.aditya.chachabiryani.R;
import com.cyfoes.aditya.chachabiryani.cart;
import com.cyfoes.aditya.chachabiryani.myorder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class myaccount extends AppCompatActivity implements OnMapReadyCallback, LocationListener{

    DatabaseReference dbruser = FirebaseDatabase.getInstance().getReference("users");
    DatabaseReference dbrshop = FirebaseDatabase.getInstance().getReference("shop");
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    TextView realname, editname, editphone, phone;
    Button addaddress;
    ImageView btnlogout;
    String userid;
    LinearLayout mainlayout;
    GoogleMap gmap;
    double lati = 0, longi = 0;
    String myloaction = "";
    private LocationManager locationManager;
    FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleApiClient googleApiClient;
    final static int REQUEST_LOCATION = 199;
    Double shopradius=0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myaccount);

        getSupportActionBar().hide();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        requestlocation();

        startgps();

        btnlogout = (ImageView)findViewById(R.id.btnlogout);
        realname = (TextView)findViewById(R.id.realname);
        editname = (TextView)findViewById(R.id.editname);
        addaddress = (Button)findViewById(R.id.addaddress);
        mainlayout = (LinearLayout)findViewById(R.id.mainlayout);
        phone = (TextView)findViewById(R.id.phone);
        editphone = (TextView)findViewById(R.id.editphone);

        userid = firebaseAuth.getCurrentUser().getUid().toString();
        setname();

        editphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editphonedialog();
            }
        });

        editname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editnamedialog();
            }
        });

        addaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addaddressdialog();
            }
        });

        dbrshop.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String rd = dataSnapshot.child("radius").getValue().toString();
                shopradius = Double.parseDouble(rd)*1000;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(myaccount.this);
                builder.setTitle("Logging out");
                builder.setMessage("Are you sure you want to logout?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseAuth.signOut();
                        startActivity(new Intent(myaccount.this, MainActivity.class));
                        Toast.makeText(myaccount.this, "Signed out", Toast.LENGTH_SHORT).show();
                    }
                })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create().show();
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.login);
        view.performClick();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.orders:
                        startActivity(new Intent(myaccount.this, myorder.class));
                        break;

                    case R.id.homeicon:
                            startActivity(new Intent(myaccount.this, MainActivity.class));
                        break;

                    case R.id.cart:
                        startActivity(new Intent(myaccount.this, cart.class));
                        break;
                }
                return true;
            }
        });
    }

    private void editphonedialog() {
        final Dialog dialog = new Dialog(myaccount.this);
        dialog.setContentView(R.layout.account_popup);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView tl = dialog.findViewById(R.id.title);
        TextView namehead = dialog.findViewById(R.id.namehead);
        TextView addresshead = dialog.findViewById(R.id.addresshead);
        final EditText ettext = dialog.findViewById(R.id.ettext);
        Button btnsave = dialog.findViewById(R.id.btnsave);
        Button btndel = dialog.findViewById(R.id.btndel);
        FrameLayout googleframe = dialog.findViewById(R.id.google_frame);
        Button gps = dialog.findViewById(R.id.gps);
        EditText name = dialog.findViewById(R.id.name);
        TextView findonmap = dialog.findViewById(R.id.findonmap);

        ettext.setHint("Your number here");

        btndel.setVisibility(View.GONE);
        gps.setVisibility(View.GONE);
        googleframe.setVisibility(View.GONE);
        findonmap.setVisibility(View.GONE);
        addresshead.setVisibility(View.GONE);
        namehead.setText("PHONE");

        tl.setText("PHONE");
        name.setVisibility(View.GONE);
        ettext.setInputType(InputType.TYPE_CLASS_PHONE);
        dbruser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(userid)){
                    if(dataSnapshot.child(userid).hasChild("phone")){
                        ettext.setText(dataSnapshot.child(userid).child("phone").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbruser.child(userid).child("phone").setValue(ettext.getText().toString().trim());
                dialog.dismiss();
                setname();
                Toast.makeText(myaccount.this, "Phone number Saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startgps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = locationManager.getBestProvider(criteria, true);
                locationManager.requestLocationUpdates(provider, 0, 0, (LocationListener) this);

            } else {
                requestlocation();
            }
        }
    }

    private void requestlocation(){
        String[] reqloc = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(reqloc, 123);
        }
    }

    @SuppressLint("MissingPermission")
    private void getlocation(){
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    lati = location.getLatitude();
                    longi = location.getLongitude();

                    Geocoder geo = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses.isEmpty()) {

                        } else {
                            if (addresses.size() > 0) {
                                String ad = addresses.get(0).getAddressLine(0);
                                myloaction = ad;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    getlocation();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 123){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Location permissions are required to access location", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showGPSdialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(myaccount.this);
        builder.setTitle("GPS Disabled!");
        builder.setMessage("GPS should be enabled to get your location");
        builder.setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(myaccount.this, "Okay", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.login);
        view.performClick();
        startgps();
        super.onResume();
    }

    private BitmapDescriptor bitmatdescriptorfromVector(Context applicationContext, int vector_res_id) {
        Drawable vectorDrawable = ContextCompat.getDrawable(applicationContext, vector_res_id);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void addaddressdialog() {
        final Boolean[] validaddress = {false};
        lati=0;longi=0;
        final Dialog dialog = new Dialog(myaccount.this);
        dialog.setContentView(R.layout.account_popup);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView tl = dialog.findViewById(R.id.title);
        final EditText ettext = dialog.findViewById(R.id.ettext);
        Button btnsave = dialog.findViewById(R.id.btnsave);
        Button btndel = dialog.findViewById(R.id.btndel);
        Button gps = dialog.findViewById(R.id.gps);
        TextView findonmap = dialog.findViewById(R.id.findonmap);
        final EditText name = dialog.findViewById(R.id.name);
        TextView text91 = dialog.findViewById(R.id.text91);

        name.setHint("e.g. Home,Work etc.");
        ettext.setHint("Your Address here");

        tl.setText("ADD ADDRESS");
        btndel.setVisibility(View.GONE);

        text91.setVisibility(View.GONE);
        //ettext.setText(myloaction);

        final MapView mapView = dialog.findViewById(R.id.gmap);
        MapsInitializer.initialize(getApplicationContext());

        mapView.onCreate(dialog.onSaveInstanceState());
        mapView.onResume();

        gps.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lati = 0;
                    longi = 0;
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lati = location.getLatitude();
                                longi = location.getLongitude();
                                final LatLng currentlocation = new LatLng(lati, longi);

                                Location locc = new Location("");
                                locc.setLatitude(lati);
                                locc.setLongitude(longi);

                                Location locp = new Location("");
                                locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                                locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                                double dist = locc.distanceTo(locp);

                                if(dist > shopradius){
                                    validaddress[0] = false;
                                    Toast.makeText(myaccount.this, "Sorry! This address is out of range of our shop", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    validaddress[0] = true;
                                }


                                mapView.getMapAsync(new OnMapReadyCallback() {
                                    @Override
                                    public void onMapReady(final GoogleMap googleMap) {
                                        googleMap.clear();
                                        final MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.position(currentlocation);
                                        markerOptions.title("My address");
                                        googleMap.addMarker(markerOptions);
                                        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                        googleMap.setBuildingsEnabled(true);

                                        final CameraPosition cameraPosition = new CameraPosition.Builder().
                                                target(currentlocation)
                                                .tilt(45)
                                                .zoom(16)
                                                .build();
                                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                    /*googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                                        @Override
                                        public void onCameraIdle() {
                                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                        }
                                    });*/

                                        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                            @Override
                                            public void onMapClick(LatLng latLng) {
                                                googleMap.clear();
                                                final MarkerOptions markerOptions = new MarkerOptions();
                                                markerOptions.position(latLng);
                                                markerOptions.title("My address");
                                                googleMap.addMarker(markerOptions);
                                                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                                googleMap.setBuildingsEnabled(true);

                                                final CameraPosition cameraPosition = new CameraPosition.Builder().
                                                        target(latLng)
                                                        .tilt(45)
                                                        .zoom(16)
                                                        .build();
                                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                            /*googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                                                @Override
                                                public void onCameraIdle() {
                                                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                }
                                            });*/

                                                lati = latLng.latitude;
                                                longi = latLng.longitude;

                                                Location locc = new Location("");
                                                locc.setLatitude(lati);
                                                locc.setLongitude(longi);

                                                Location locp = new Location("");
                                                locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                                                locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                                                double dist = locc.distanceTo(locp);

                                                if(dist > shopradius){
                                                    validaddress[0] = false;
                                                    Toast.makeText(myaccount.this, "Sorry! This address is out of range of out shop", Toast.LENGTH_SHORT).show();
                                                }
                                                else {
                                                    validaddress[0] = true;
                                                }


                                                Geocoder geo = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
                                                try {
                                                    List<Address> addresses = geo.getFromLocation(latLng.latitude, latLng.longitude, 1);
                                                    if (addresses.isEmpty()) {
                                                        Toast.makeText(myaccount.this, "Can't get this address", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        if (addresses.size() > 0) {
                                                            String ad = addresses.get(0).getAddressLine(0);
                                                            ettext.setText(ad);
                                                        }
                                                    }
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                });

                                Geocoder geo = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
                                try {
                                    List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    if (addresses.isEmpty()) {

                                    } else {
                                        if (addresses.size() > 0) {
                                            String ad = addresses.get(0).getAddressLine(0);
                                            ettext.setText(ad);
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                getlocation();
                            }
                        }
                    });
                }
                else {
                    enableLoc();
                }
            }
        });

        findonmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lati=0;longi=0;
                String add = ettext.getText().toString();
                Geocoder geo = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> address = geo.getFromLocationName(add, 5);
                    if(address.size() > 0){
                        Address location=address.get(0);
                        lati=location.getLatitude();
                        longi=location.getLongitude();

                        Location locc = new Location("");
                        locc.setLatitude(lati);
                        locc.setLongitude(longi);

                        Location locp = new Location("");
                        locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                        locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                        double dist = locc.distanceTo(locp);

                        if(dist > shopradius){
                            validaddress[0] = false;
                            Toast.makeText(myaccount.this, "Sorry! This address is out of range of out shop", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            validaddress[0] = true;
                        }

                        mapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(final GoogleMap googleMap) {
                                googleMap.clear();
                                final LatLng currentlocation = new LatLng(lati, longi);

                                final MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(currentlocation);
                                markerOptions.title("My address");
                                googleMap.addMarker(markerOptions);
                                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                googleMap.setBuildingsEnabled(true);

                                final CameraPosition cameraPosition = new CameraPosition.Builder().
                                        target(currentlocation)
                                        .tilt(45)
                                        .zoom(16)
                                        .build();
                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                               /* googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                                    @Override
                                    public void onCameraIdle() {
                                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                    }
                                });*/

                                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                    @Override
                                    public void onMapClick(LatLng latLng) {
                                        googleMap.clear();
                                        final MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.position(latLng);
                                        markerOptions.title("My address");
                                        googleMap.addMarker(markerOptions);
                                        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                        googleMap.setBuildingsEnabled(true);

                                        final CameraPosition cameraPosition = new CameraPosition.Builder().
                                                target(latLng)
                                                .tilt(45)
                                                .zoom(16)
                                                .build();
                                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                        /*googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                                            @Override
                                            public void onCameraIdle() {
                                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                            }
                                        });*/

                                        lati = latLng.latitude;
                                        longi = latLng.longitude;

                                        Location locc = new Location("");
                                        locc.setLatitude(lati);
                                        locc.setLongitude(longi);

                                        Location locp = new Location("");
                                        locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                                        locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                                        double dist = locc.distanceTo(locp);

                                        if(dist > shopradius){
                                            validaddress[0] = false;
                                            Toast.makeText(myaccount.this, "Sorry! This address is out of range of out shop", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            validaddress[0] = true;
                                        }

                                        Geocoder geo = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
                                        try {
                                            List<Address> addresses = geo.getFromLocation(latLng.latitude, latLng.longitude, 1);
                                            if (addresses.isEmpty()) {
                                                Toast.makeText(myaccount.this, "Can't get this address", Toast.LENGTH_SHORT).show();
                                            } else {
                                                if (addresses.size() > 0) {
                                                    String ad = addresses.get(0).getAddressLine(0);
                                                    ettext.setText(ad);
                                                }
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        });
                    }
                    else {
                        Toast.makeText(myaccount.this, "Can't find this address!", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lati == 0 && longi == 0) {
                    String add = ettext.getText().toString();
                    Geocoder geo = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> address = geo.getFromLocationName(add, 5);
                        if (address.size() > 0) {
                            Address location = address.get(0);
                            lati = location.getLatitude();
                            longi = location.getLongitude();

                            Location locc = new Location("");
                            locc.setLatitude(lati);
                            locc.setLongitude(longi);

                            Location locp = new Location("");
                            locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                            locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                            double dist = locc.distanceTo(locp);

                            if(dist > shopradius){
                                validaddress[0] = false;
                                Toast.makeText(myaccount.this, "Sorry! This address is out of range of out shop", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                validaddress[0] = true;
                            }
                        }
                        else {
                            Toast.makeText(myaccount.this, "Address not found!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                String code = generatecode();
                if(!ettext.getText().toString().trim().isEmpty() && !name.getText().toString().trim().isEmpty()
                        && lati != 0 && longi != 0) {
                    if (validaddress[0] == false) {
                        Toast.makeText(myaccount.this, "Sorry! This address is out of range of our shop", Toast.LENGTH_SHORT).show();
                    } else {
                        dbruser.child(userid).child("addresses").child(code).child("title").setValue(name.getText().toString().trim());
                        dbruser.child(userid).child("addresses").child(code).child("address").setValue(ettext.getText().toString().trim());
                        dbruser.child(userid).child("addresses").child(code).child("latitude").setValue(lati);
                        dbruser.child(userid).child("addresses").child(code).child("longitude").setValue(longi);
                        dialog.dismiss();
                        setname();
                        Toast.makeText(myaccount.this, "Address added", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(myaccount.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private String generatecode() {
        final String ALPHA_NUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        int count = 8;
        String code = "";
        while (count-- != 0) {
            int charecter = (int) (Math.random() * ALPHA_NUM.length());
            builder.append(ALPHA_NUM.charAt(charecter));
            code = builder.toString();
        }
        return code;
    }

    private void editnamedialog() {
        final Dialog dialog = new Dialog(myaccount.this);
        dialog.setContentView(R.layout.account_popup);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView tl = dialog.findViewById(R.id.title);
        final EditText ettext = dialog.findViewById(R.id.ettext);
        TextView namehead = dialog.findViewById(R.id.namehead);
        TextView addresshead = dialog.findViewById(R.id.addresshead);
        Button btnsave = dialog.findViewById(R.id.btnsave);
        Button btndel = dialog.findViewById(R.id.btndel);
        LinearLayout address_layout = dialog.findViewById(R.id.address_layout);
        FrameLayout googleframe = dialog.findViewById(R.id.google_frame);
        Button gps = dialog.findViewById(R.id.gps);
        EditText name = dialog.findViewById(R.id.name);
        TextView findonmap = dialog.findViewById(R.id.findonmap);
        TextView text91 = dialog.findViewById(R.id.text91);

        ettext.setHint("Your name here");

        btndel.setVisibility(View.GONE);
        gps.setVisibility(View.GONE);
        googleframe.setVisibility(View.GONE);
        findonmap.setVisibility(View.GONE);
        namehead.setText("Name");
        addresshead.setVisibility(View.GONE);

        text91.setVisibility(View.GONE);
        tl.setText("NAME");
        tl.setHint("Your name here");
        name.setVisibility(View.GONE);
        dbruser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(userid)){
                    if(dataSnapshot.child(userid).hasChild("name")){
                        ettext.setText(dataSnapshot.child(userid).child("name").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbruser.child(userid).child("name").setValue(ettext.getText().toString().trim());
                dialog.dismiss();
                setname();
                Toast.makeText(myaccount.this, "Name Saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setname() {
        final Boolean[] validaddress = {false};
        dbruser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mainlayout.removeAllViews();
                lati = 0;longi=0;
                if(dataSnapshot.hasChild(userid)){
                    if(dataSnapshot.child(userid).hasChild("name")){
                        realname.setText(dataSnapshot.child(userid).child("name").getValue().toString());
                    }
                    if(dataSnapshot.child(userid).hasChild("phone")){
                        phone.setText("+91 "+dataSnapshot.child(userid).child("phone").getValue().toString());
                    }
                    else {
                        dbruser.child(firebaseAuth.getCurrentUser().getUid()).child("phone").setValue(firebaseAuth.getCurrentUser().getPhoneNumber().substring(3));
                        phone.setText(firebaseAuth.getCurrentUser().getPhoneNumber().toString());
                    }

                    if(dataSnapshot.child(userid).hasChild("addresses")){
                        for(final DataSnapshot daddress:dataSnapshot.child(userid).child("addresses").getChildren()){
                            LayoutInflater inflater = LayoutInflater.from(myaccount.this);
                            LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.acoount_element, null, false);
                            mainlayout.addView(lay);

                            TextView title = lay.findViewById(R.id.tvtitle);
                            TextView content = lay.findViewById(R.id.tvcontent);
                            TextView tvedit = lay.findViewById(R.id.tvedit);

                            final String ttl = daddress.child("title").getValue().toString();
                            final String cnt = daddress.child("address").getValue().toString();

                            title.setText(ttl);
                            content.setText(cnt);

                            tvedit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final Dialog dialog = new Dialog(myaccount.this);
                                    dialog.setContentView(R.layout.account_popup);
                                    dialog.show();

                                    Window window = dialog.getWindow();
                                    window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    final TextView tl = dialog.findViewById(R.id.title);
                                    final EditText ettext = dialog.findViewById(R.id.ettext);
                                    final Button btnsave = dialog.findViewById(R.id.btnsave);
                                    Button btndel = dialog.findViewById(R.id.btndel);
                                    Button gps = dialog.findViewById(R.id.gps);
                                    TextView findonmap = dialog.findViewById(R.id.findonmap);
                                    final EditText nn = dialog.findViewById(R.id.name);
                                    TextView text91 = dialog.findViewById(R.id.text91);

                                    nn.setHint("e.g. Home,Work etc.");
                                    ettext.setHint("Your Address here");

                                    text91.setVisibility(View.GONE);
                                    tl.setText("EDIT ADDRESS");
                                    nn.setText(ttl);
                                    ettext.setText(cnt);

                                    lati = Double.parseDouble(daddress.child("latitude").getValue().toString());
                                    longi = Double.parseDouble(daddress.child("longitude").getValue().toString());

                                    Location locc = new Location("");
                                    locc.setLatitude(lati);
                                    locc.setLongitude(longi);

                                    Location locp = new Location("");
                                    locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                                    locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                                    double dist = locc.distanceTo(locp);

                                    if(dist > shopradius){
                                        validaddress[0] = false;
                                        Toast.makeText(myaccount.this, "Sorry! This address is out of range of our shop", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        validaddress[0] = true;
                                    }

                                    btnsave.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if(!tl.getText().toString().trim().isEmpty() && !ettext.getText().toString().trim().isEmpty()
                                                    && lati != 0 && longi != 0){
                                                if(lati != Double.parseDouble(daddress.child("latitude").getValue().toString()) &&
                                                        longi != Double.parseDouble(daddress.child("longitude").getValue().toString())) {
                                                    if (validaddress[0] == false) {
                                                        Toast.makeText(myaccount.this, "Sorry! This address is out of range of our shop", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        dbruser.child(userid).child("addresses").child(daddress.getKey().toString()).child("address").setValue(ettext.getText().toString().trim());
                                                        dbruser.child(userid).child("addresses").child(daddress.getKey().toString()).child("latitude").setValue(lati);
                                                        dbruser.child(userid).child("addresses").child(daddress.getKey().toString()).child("longitude").setValue(longi);

                                                        dialog.dismiss();
                                                        setname();
                                                        Toast.makeText(myaccount.this, "Update successfull!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                                if(!nn.getText().toString().equals(daddress.child("title").getValue().toString())){
                                                    dbruser.child(userid).child("addresses").child(daddress.getKey().toString()).child("title").setValue(nn.getText().toString().trim());
                                                    dialog.dismiss();
                                                    setname();
                                                    Toast.makeText(myaccount.this, "Update successfull!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    });

                                    btndel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(myaccount.this);
                                            builder.setTitle("Deleting Address");
                                            builder.setMessage("Are you sure you want to delete this address?");
                                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dbruser.child(userid).child("addresses").child(daddress.getKey().toString()).setValue(null);
                                                    dialog.dismiss();
                                                    setname();
                                                    Toast.makeText(myaccount.this, "Address deleted", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                        }
                                                    }).create().show();

                                        }
                                    });

                                    final MapView mapView = dialog.findViewById(R.id.gmap);
                                    MapsInitializer.initialize(getApplicationContext());

                                    mapView.onCreate(dialog.onSaveInstanceState());
                                    mapView.onResume();
                                    mapView.getMapAsync(new OnMapReadyCallback() {
                                        @Override
                                        public void onMapReady(final GoogleMap googleMap) {
                                            googleMap.clear();
                                            final LatLng currentlocation = new LatLng(Double.parseDouble(daddress.child("latitude").getValue().toString()),
                                                    Double.parseDouble(daddress.child("longitude").getValue().toString()));
                                            final MarkerOptions markerOptions = new MarkerOptions();
                                            markerOptions.position(currentlocation);
                                            markerOptions.title("My address");
                                            googleMap.addMarker(markerOptions);
                                            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                            googleMap.setBuildingsEnabled(true);

                                            final CameraPosition cameraPosition = new CameraPosition.Builder().
                                                    target(currentlocation)
                                                    .tilt(45)
                                                    .zoom(16)
                                                    .build();
                                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                            /*googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                                                @Override
                                                public void onCameraIdle() {
                                                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                }
                                            });*/

                                            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                                @Override
                                                public void onMapClick(LatLng latLng) {
                                                    googleMap.clear();
                                                    final MarkerOptions markerOptions = new MarkerOptions();
                                                    markerOptions.position(latLng);
                                                    markerOptions.title("My address");
                                                    googleMap.addMarker(markerOptions);
                                                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                                    googleMap.setBuildingsEnabled(true);

                                                    final CameraPosition cameraPosition = new CameraPosition.Builder().
                                                            target(latLng)
                                                            .tilt(45)
                                                            .zoom(16)
                                                            .build();
                                                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                    /*googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                                                        @Override
                                                        public void onCameraIdle() {
                                                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                        }
                                                    });*/

                                                    lati = latLng.latitude;
                                                    longi = latLng.longitude;

                                                    Location locc = new Location("");
                                                    locc.setLatitude(lati);
                                                    locc.setLongitude(longi);

                                                    Location locp = new Location("");
                                                    locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                                                    locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                                                    double dist = locc.distanceTo(locp);

                                                    if(dist > shopradius){
                                                        validaddress[0] = false;
                                                        Toast.makeText(myaccount.this, "Sorry! This address is out of range of our shop", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else {
                                                        validaddress[0] = true;
                                                    }

                                                    Geocoder geo = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
                                                    try {
                                                        List<Address> addresses = geo.getFromLocation(latLng.latitude, latLng.longitude, 1);
                                                        if (addresses.isEmpty()) {
                                                            Toast.makeText(myaccount.this, "Can't get this address", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            if (addresses.size() > 0) {
                                                                String ad = addresses.get(0).getAddressLine(0);
                                                                ettext.setText(ad);
                                                            }
                                                        }
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }
                                    });

                                    gps.setOnClickListener(new View.OnClickListener() {
                                        @SuppressLint("MissingPermission")
                                        @Override
                                        public void onClick(View v) {
                                            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                                    @Override
                                                    public void onSuccess(Location location) {
                                                        if (location != null) {
                                                            lati = location.getLatitude();
                                                            longi = location.getLongitude();
                                                            final LatLng currentlocation = new LatLng(lati, longi);

                                                            Location locc = new Location("");
                                                            locc.setLatitude(lati);
                                                            locc.setLongitude(longi);

                                                            Location locp = new Location("");
                                                            locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                                                            locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                                                            double dist = locc.distanceTo(locp);

                                                            if(dist > shopradius){
                                                                validaddress[0] = false;
                                                                Toast.makeText(myaccount.this, "Sorry! This address is out of range of our shop", Toast.LENGTH_SHORT).show();
                                                            }
                                                            else {
                                                                validaddress[0] = true;
                                                            }

                                                            mapView.getMapAsync(new OnMapReadyCallback() {
                                                                @Override
                                                                public void onMapReady(final GoogleMap googleMap) {
                                                                    googleMap.clear();
                                                                    final MarkerOptions markerOptions = new MarkerOptions();
                                                                    markerOptions.position(currentlocation);
                                                                    markerOptions.title("My address");
                                                                    googleMap.addMarker(markerOptions);
                                                                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                                                    googleMap.setBuildingsEnabled(true);

                                                                    final CameraPosition cameraPosition = new CameraPosition.Builder().
                                                                            target(currentlocation)
                                                                            .tilt(45)
                                                                            .zoom(16)
                                                                            .build();
                                                                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                                    /*googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                                                                        @Override
                                                                        public void onCameraIdle() {
                                                                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                                        }
                                                                    });
*/
                                                                    googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                                                        @Override
                                                                        public void onMapClick(LatLng latLng) {
                                                                            googleMap.clear();
                                                                            final MarkerOptions markerOptions = new MarkerOptions();
                                                                            markerOptions.position(latLng);
                                                                            markerOptions.title("My address");
                                                                            googleMap.addMarker(markerOptions);
                                                                            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                                                            googleMap.setBuildingsEnabled(true);

                                                                            final CameraPosition cameraPosition = new CameraPosition.Builder().
                                                                                    target(latLng)
                                                                                    .tilt(45)
                                                                                    .zoom(16)
                                                                                    .build();
                                                                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                                       /* googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                                                                            @Override
                                                                            public void onCameraIdle() {
                                                                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                                            }
                                                                        });*/

                                                                            lati = latLng.latitude;
                                                                            longi = latLng.longitude;

                                                                            Location locc = new Location("");
                                                                            locc.setLatitude(lati);
                                                                            locc.setLongitude(longi);

                                                                            Location locp = new Location("");
                                                                            locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                                                                            locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                                                                            double dist = locc.distanceTo(locp);

                                                                            if(dist > shopradius){
                                                                                validaddress[0] = false;
                                                                                Toast.makeText(myaccount.this, "Sorry! This address is out of range of our shop", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                            else {
                                                                                validaddress[0] = true;
                                                                            }

                                                                            Geocoder geo = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
                                                                            try {
                                                                                List<Address> addresses = geo.getFromLocation(latLng.latitude, latLng.longitude, 1);
                                                                                if (addresses.isEmpty()) {
                                                                                    Toast.makeText(myaccount.this, "Can't get this address", Toast.LENGTH_SHORT).show();
                                                                                } else {
                                                                                    if (addresses.size() > 0) {
                                                                                        String ad = addresses.get(0).getAddressLine(0);
                                                                                        ettext.setText(ad);
                                                                                    }
                                                                                }
                                                                            } catch (IOException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            });

                                                            Geocoder geo = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
                                                            try {
                                                                List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                                                if (addresses.isEmpty()) {

                                                                } else {
                                                                    if (addresses.size() > 0) {
                                                                        String ad = addresses.get(0).getAddressLine(0);
                                                                        ettext.setText(ad);
                                                                    }
                                                                }
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                        } else {
                                                            getlocation();
                                                        }
                                                    }
                                                });
                                            }
                                            else {
                                                enableLoc();
                                            }
                                        }
                                    });


                                    findonmap.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String add = ettext.getText().toString();
                                            Geocoder geo = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
                                            try {
                                                List<Address> address = geo.getFromLocationName(add, 5);
                                                if(address != null){
                                                    Address location=address.get(0);
                                                    lati=location.getLatitude();
                                                    longi=location.getLongitude();

                                                    Location locc = new Location("");
                                                    locc.setLatitude(lati);
                                                    locc.setLongitude(longi);

                                                    Location locp = new Location("");
                                                    locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                                                    locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                                                    double dist = locc.distanceTo(locp);

                                                    if(dist > shopradius){
                                                        validaddress[0] = false;
                                                        Toast.makeText(myaccount.this, "Sorry! This address is out of range of our shop", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else {
                                                        validaddress[0] = true;
                                                    }

                                                    mapView.getMapAsync(new OnMapReadyCallback() {
                                                        @Override
                                                        public void onMapReady(final GoogleMap googleMap) {
                                                            googleMap.clear();
                                                            final LatLng currentlocation = new LatLng(lati, longi);

                                                            final MarkerOptions markerOptions = new MarkerOptions();
                                                            markerOptions.position(currentlocation);
                                                            markerOptions.title("My address");
                                                            googleMap.addMarker(markerOptions);
                                                            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                                            googleMap.setBuildingsEnabled(true);

                                                            final CameraPosition cameraPosition = new CameraPosition.Builder().
                                                                    target(currentlocation)
                                                                    .tilt(45)
                                                                    .zoom(16)
                                                                    .build();
                                                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                           /* googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                                                                @Override
                                                                public void onCameraIdle() {
                                                                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                                }
                                                            });*/

                                                            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                                                @Override
                                                                public void onMapClick(LatLng latLng) {
                                                                    googleMap.clear();
                                                                    final MarkerOptions markerOptions = new MarkerOptions();
                                                                    markerOptions.position(latLng);
                                                                    markerOptions.title("My address");
                                                                    googleMap.addMarker(markerOptions);
                                                                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                                                    googleMap.setBuildingsEnabled(true);

                                                                    final CameraPosition cameraPosition = new CameraPosition.Builder().
                                                                            target(latLng)
                                                                            .tilt(45)
                                                                            .zoom(16)
                                                                            .build();
                                                                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                                  /*  googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                                                                        @Override
                                                                        public void onCameraIdle() {
                                                                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                                        }
                                                                    });*/

                                                                    lati = latLng.latitude;
                                                                    longi = latLng.longitude;

                                                                    Location locc = new Location("");
                                                                    locc.setLatitude(lati);
                                                                    locc.setLongitude(longi);

                                                                    Location locp = new Location("");
                                                                    locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                                                                    locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                                                                    double dist = locc.distanceTo(locp);

                                                                    if(dist > shopradius){
                                                                        validaddress[0] = false;
                                                                        Toast.makeText(myaccount.this, "Sorry! This address is out of range of our shop", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    else {
                                                                        validaddress[0] = true;
                                                                    }

                                                                    Geocoder geo = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
                                                                    try {
                                                                        List<Address> addresses = geo.getFromLocation(latLng.latitude, latLng.longitude, 1);
                                                                        if (addresses.isEmpty()) {
                                                                            Toast.makeText(myaccount.this, "Can't get this address", Toast.LENGTH_SHORT).show();
                                                                        } else {
                                                                            if (addresses.size() > 0) {
                                                                                String ad = addresses.get(0).getAddressLine(0);
                                                                                ettext.setText(ad);
                                                                            }
                                                                        }
                                                                    } catch (IOException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                                else {
                                                    Toast.makeText(myaccount.this, "Can't find this address!", Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(myaccount.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error","Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(myaccount.this, REQUEST_LOCATION);

                                //finish();
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        //gmap.clear();
        final LatLng currentlocation = new LatLng(lati, longi);

        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentlocation);
        markerOptions.title("My location");
        gmap.addMarker(markerOptions);
        gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        gmap.setBuildingsEnabled(true);

        final CameraPosition cameraPosition = new CameraPosition.Builder().
                target(currentlocation)
                .tilt(45)
                .zoom(16)
                .build();
        gmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        gmap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                gmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


}
