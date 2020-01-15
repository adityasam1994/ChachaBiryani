package com.cyfoes.aditya.chachabiryani;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cyfoes.aditya.chachabiryani.admin.myaccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    LinearLayout menulayout;
    DatabaseReference dbrorder = FirebaseDatabase.getInstance().getReference("menu");
    DatabaseReference dbrshop = FirebaseDatabase.getInstance().getReference("shop");
    StorageReference spref = FirebaseStorage.getInstance().getReferenceFromUrl("gs://chachabiryani-7c5e6.appspot.com");
    ProgressDialog pd;
    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    LinearLayout bottom_draw;
    Button add_item;
    TextView cross, shopclose;
    SharedPreferences sharedPreferences;
    RadioGroup radiogroup;
    Boolean shopopen;
    FrameLayout frame;
    TextView shopnotopen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        sharedPreferences = getSharedPreferences("orders", Context.MODE_PRIVATE);

        pd = new ProgressDialog(this);

        shopnotopen = (TextView)findViewById(R.id.shopnotopen);
        frame = (FrameLayout)findViewById(R.id.frame);
        shopclose = (TextView)findViewById(R.id.shopclosed);
        bottom_draw = (LinearLayout)findViewById(R.id.bottom_draw);
        menulayout = (LinearLayout) findViewById(R.id.menulayout);
        add_item = (Button)findViewById(R.id.add_item);
        cross = (TextView)findViewById(R.id.cross);
        radiogroup = (RadioGroup)findViewById(R.id.radiogroup);

        createcards();
        checkshopstatus();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.orders:
                        startActivity(new Intent(MainActivity.this, myorder.class));
                        break;

                    case R.id.login:
                        if(firebaseAuth.getCurrentUser() != null){
                            startActivity(new Intent(MainActivity.this, myaccount.class));
                        }
                        else {
                            startActivity(new Intent(MainActivity.this, login.class));
                        }
                        break;

                    case R.id.cart:
                        startActivity(new Intent(MainActivity.this, cart.class));
                        break;
                }
                return true;
            }
        });
    }

    private void checkshopstatus() {
        dbrshop.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("status")){
                    if(dataSnapshot.child("status").getValue().toString().equals("closed")){
                        shopopen=true;
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0,1);
                        shopclose.setLayoutParams(params);
                        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                        frame.setLayoutParams(params1);
                    }
                    else {
                        shopopen=false;
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
                        frame.setLayoutParams(params);
                        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                        shopclose.setLayoutParams(params1);
                    }
                }
                if(dataSnapshot.hasChild("opentime")){
                    String otime = dataSnapshot.child("opentime").getValue().toString();
                    String ctime = dataSnapshot.child("closetime").getValue().toString();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                    String currentDate = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                    try {
                        Date odate = dateFormat.parse(otime);
                        Date cldate = dateFormat.parse(ctime);
                        Date cdate = dateFormat.parse(currentDate);

                        if(cdate.before(odate)){
                            shopnotopen.setVisibility(View.VISIBLE);
                            shopnotopen.setText("SHOP WILL BE OPENED AT "+otime);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            shopnotopen.setLayoutParams(params);
                        }
                        else if(cdate.after(cldate)){
                            shopnotopen.setVisibility(View.VISIBLE);
                            shopnotopen.setText("SHOP WILL BE OPENED TOMORROW AT "+otime);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            shopnotopen.setLayoutParams(params);
                        }
                        else {
                            shopnotopen.setVisibility(View.INVISIBLE);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                            shopnotopen.setLayoutParams(params);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createcards() {
        dbrorder.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menulayout.removeAllViews();
                for (final DataSnapshot d : dataSnapshot.getChildren()) {
                    String myorder = sharedPreferences.getString("myorder", "");
                    String[] sp = myorder.split(":::");
                    final ArrayList<String> list = new ArrayList<String>();

                    for(String s:sp){
                        list.add(s);
                    }

                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.menu_items, null, false);
                    menulayout.addView(lay);

                    final ImageView imageView = lay.findViewById(R.id.item_image);
                    final TextView name = lay.findViewById(R.id.name);
                    final TextView price = lay.findViewById(R.id.price);
                    final Button btnadd = lay.findViewById(R.id.btnadd);
                    final Button btnremove = lay.findViewById(R.id.btnremove);
                    final String[] id = {""};
                    final int[] count = {0};

                    for(String s : list){
                        final int ind = list.indexOf(s);
                        String[] splittext = s.split("/");
                        if(splittext[0].equals(d.getKey().toString())){
                            btnadd.setVisibility(View.INVISIBLE);
                            btnremove.setVisibility(View.VISIBLE);

                            btnremove.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    list.remove(ind);

                                    String nlist="";
                                    for(String l : list){
                                        if(nlist.equals("")){
                                            nlist = l;
                                        }
                                        else {
                                            nlist = nlist + ":::" + l;
                                        }
                                    }
                                    sharedPreferences.edit().putString("myorder", nlist).commit();
                                    createcards();
                                }
                            });
                        }
                    }

                    btnadd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottom_draw.setVisibility(View.VISIBLE);
                            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_top);
                            bottom_draw.startAnimation(animation);

                            final TextView totalprice = (TextView)findViewById(R.id.totalprice);
                            TextView item_name = (TextView)findViewById(R.id.item_name);
                            item_name.setText(d.child("name").getValue().toString());
                            EditText itemweight = (EditText)findViewById(R.id.weight);
                            final int priceperkg = Integer.parseInt(d.child("price").getValue().toString());

                            totalprice.setText("\u20B9 "+"0.0");

                            itemweight.setText(null);

                            final int[] weight = {0};

                            itemweight.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                }

                                @Override
                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                    if(charSequence.length() != 0) {
                                        double pri = priceperkg * Double.parseDouble(charSequence.toString());
                                        totalprice.setText("\u20B9 " + pri);
                                        weight[0] = Integer.parseInt(charSequence.toString());
                                    }
                                    else {
                                        totalprice.setText( "\u20B9 "+"0.0");
                                        weight[0] = 0;
                                    }
                                }

                                @Override
                                public void afterTextChanged(Editable editable) {

                                }
                            });

                            /*radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(RadioGroup group, int checkedId) {
                                    switch (checkedId){
                                        case R.id.onekg:
                                            totalprice.setText("INR "+priceperkg);
                                            weight[0] = 1;
                                            break;

                                        case R.id.twokg:
                                            totalprice.setText("INR "+priceperkg*2);
                                            weight[0] = 2;
                                            break;

                                        case R.id.fourkg:
                                            totalprice.setText("INR "+priceperkg*4);
                                            weight[0] = 4;
                                            break;
                                    }
                                }
                            });*/

                            cross.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    bottom_draw.setVisibility(View.VISIBLE);
                                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_bottom);
                                    bottom_draw.startAnimation(animation);
                                    if(animation.hasEnded()){
                                        bottom_draw.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });

                            add_item.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (weight[0] != 0) {
                                        String sporder = sharedPreferences.getString("myorder", "");
                                        if (sporder.equals("")) {
                                            String orderitem = d.getKey().toString() + "/" + weight[0] + "/" + d.child("name").getValue().toString() + "/" + totalprice.getText().toString().substring(2);
                                            sharedPreferences.edit().putString("myorder", orderitem).commit();
                                        } else {
                                            String orderitem = sporder + ":::" + d.getKey().toString() + "/" + weight[0] + "/" + d.child("name").getValue().toString() + "/" + totalprice.getText().toString().substring(2);
                                            sharedPreferences.edit().putString("myorder", orderitem).commit();
                                        }

                                        bottom_draw.setVisibility(View.VISIBLE);
                                        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_bottom);
                                        btnadd.setVisibility(View.INVISIBLE);
                                        btnremove.setVisibility(View.VISIBLE);
                                        createcards();
                                        bottom_draw.startAnimation(animation);
                                        if (animation.hasEnded()) {
                                            bottom_draw.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    else {
                                        Toast.makeText(MainActivity.this, "Please enter some amount of item", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });

                    name.setText(d.child("name").getValue().toString());
                    price.setText("\u20B9 " + d.child("price").getValue().toString()+"/Kg");
                    id[0] = d.getKey().toString();

                    if(d.child("available").getValue().toString().equals("unavailable")){
                        btnadd.setText("UNAVAILABLE");
                        btnadd.setTextColor(Color.parseColor("#787878"));
                        price.setTextColor(Color.parseColor("#787878"));
                        btnadd.setBackgroundResource(R.drawable.cancel_button);
                        btnadd.setEnabled(false);

                        ColorMatrix matrix = new ColorMatrix();
                        matrix.setSaturation(0);

                        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                        imageView.setColorFilter(filter);
                    }

                    spref.child("menu_images/" + id[0]).getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.get().load(uri).resize(300, 300).into(imageView);
                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.homeicon);
        view.performClick();
        super.onResume();
    }
}
