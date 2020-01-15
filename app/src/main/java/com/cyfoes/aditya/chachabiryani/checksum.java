package com.cyfoes.aditya.chachabiryani;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class checksum extends AppCompatActivity implements PaytmPaymentTransactionCallback {

    String custid = "", orderId = "", mid = "", cost = "", address = "", date = "", time = "", latitude = "", longitude = "", cart = "", payment="";
    Double delivery_charge = 0.0;
    SharedPreferences sharedPreferences;
    DatabaseReference dbruser = FirebaseDatabase.getInstance().getReference("users");
    DatabaseReference dbrorder = FirebaseDatabase.getInstance().getReference("myorders");
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    TextView txtcode, txtprice, txtcustomername, txtphone, txtaddress;
    LinearLayout order_item_layout;
    Button btncancel;
    Boolean order_accepted=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checksum);
        getSupportActionBar().hide();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        txtcode = (TextView)findViewById(R.id.code);
        txtaddress = (TextView)findViewById(R.id.address);
        txtcustomername = (TextView)findViewById(R.id.customername);
        txtphone = (TextView)findViewById(R.id.phone);
        txtprice = (TextView)findViewById(R.id.price);
        btncancel = (Button)findViewById(R.id.btncancel);
        order_item_layout = (LinearLayout)findViewById(R.id.order_item_layout);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.cart);
        view.performClick();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.homeicon:
                        startActivity(new Intent(checksum.this, MainActivity.class));
                        break;

                    case R.id.login:
                        if (firebaseAuth.getCurrentUser() != null) {
                            startActivity(new Intent(checksum.this, myaccount.class));
                        } else {
                            startActivity(new Intent(checksum.this, login.class));
                        }
                        break;

                    case R.id.orders:
                        startActivity(new Intent(checksum.this, myorder.class));
                        break;
                }
                return true;
            }
        });

        sharedPreferences = getSharedPreferences("orders", Context.MODE_PRIVATE);

        Intent intent = getIntent();
        orderId = intent.getExtras().getString("orderid");
        custid = intent.getExtras().getString("custid");
        cost = intent.getExtras().getString("cost");
        address = intent.getExtras().getString("address");
        latitude = intent.getExtras().getString("latitude");
        longitude = intent.getExtras().getString("longitude");
        date = intent.getExtras().getString("date");
        time = intent.getExtras().getString("time");
        cart = intent.getExtras().getString("order");
        payment = intent.getExtras().getString("payment");
        delivery_charge = Double.parseDouble(intent.getExtras().getString("delivery_charge"));
        mid = "aYZduM01989035476127"; /// your marchant key

        txtcode.setText(orderId);
        txtprice.setText("\u20B9 "+cost);
        txtaddress.setText(address);

        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbrorder.child(firebaseAuth.getCurrentUser().getUid()).child(orderId).setValue(null);
                Toast.makeText(checksum.this, "Order Cancelled", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(checksum.this, MainActivity.class));
            }
        });

        dbruser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                txtcustomername.setText(dataSnapshot.child(firebaseAuth.getCurrentUser().getUid()).child("name").getValue().toString());
                if (dataSnapshot.child(firebaseAuth.getCurrentUser().getUid()).hasChild("phone")) {
                    txtphone.setText("+91" + dataSnapshot.child(firebaseAuth.getCurrentUser().getUid()).child("phone").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        String[] sp = cart.split(":::");
        ArrayList<String> list = new ArrayList<>();
        for (String s : sp) {
            list.add(s);
        }
        double cost = delivery_charge;
        for (String s : list) {
            String[] splittext = s.split("/");

            cost = cost + Double.parseDouble(splittext[3]);

            LayoutInflater inflater = LayoutInflater.from(checksum.this);
            LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.order_item, null, false);
            order_item_layout.addView(lay);

            TextView itemname = lay.findViewById(R.id.itemname);
            TextView itemweight = lay.findViewById(R.id.itemweight);

            itemname.setText(splittext[2]);
            itemweight.setText(splittext[1] + " Kg");
        }

        dbrorder.child(firebaseAuth.getCurrentUser().getUid()).child(orderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("order_status").getValue().toString().equals("accepted")){
                    if(order_accepted == false) {
                        if (payment.equals("cod")) {
                            Toast.makeText(checksum.this, "Order accepted", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(checksum.this, MainActivity.class));
                            order_accepted = true;
                        } else if (payment.equals("paytm")) {
                            Toast.makeText(checksum.this, "Order accepted, Proceeding to payment", Toast.LENGTH_SHORT).show();
                            order_accepted = true;
                            sendUserDetailTOServerdd dl = new sendUserDetailTOServerdd();
                            dl.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
                }
                else if(dataSnapshot.child("order_status").getValue().toString().equals("rejected")){
                    Toast.makeText(checksum.this, "Order rejected by the shop", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(checksum.this, MainActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*sendUserDetailTOServerdd dl = new sendUserDetailTOServerdd();
        dl.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);*/
    }

    public class sendUserDetailTOServerdd extends AsyncTask<ArrayList<String>, Void, String> {
        private ProgressDialog dialog = new ProgressDialog(checksum.this);
        //private String orderId , mid, custid, amt;
        String url = "https://chachabiryani.000webhostapp.com/paytm/generateChecksum.php";
        String varifyurl = "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";
        // "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID"+orderId;
        String CHECKSUMHASH = "";

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        protected String doInBackground(ArrayList<String>... alldata) {
            JSONParser jsonParser = new JSONParser(checksum.this);
            String param =
                    "MID=" + mid +
                            "&ORDER_ID=" + orderId +
                            "&CUST_ID=" + custid +
                            "&CHANNEL_ID=WAP&TXN_AMOUNT=" + cost + "&WEBSITE=WEBSTAGING" +
                            "&CALLBACK_URL=" + varifyurl + "&INDUSTRY_TYPE_ID=Retail";
            JSONObject jsonObject = jsonParser.makeHttpRequest(url, "POST", param);
            // yaha per checksum ke saht order id or status receive hoga..
            Log.e("CheckSum result >>", jsonObject.toString());
            if (jsonObject != null) {
                Log.e("CheckSum result >>", jsonObject.toString());
                try {
                    CHECKSUMHASH = jsonObject.has("CHECKSUMHASH") ? jsonObject.getString("CHECKSUMHASH") : "";
                    Log.e("CheckSum result >>", CHECKSUMHASH);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return CHECKSUMHASH;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(" setup acc ", "  signup result  " + result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            PaytmPGService Service = PaytmPGService.getStagingService();
            // when app is ready to publish use production service
            // PaytmPGService  Service = PaytmPGService.getProductionService();
            // now call paytm service here
            //below parameter map is required to construct PaytmOrder object, Merchant should replace below map values with his own values
            HashMap<String, String> paramMap = new HashMap<String, String>();
            //these are mandatory parameters
            paramMap.put("MID", mid); //MID provided by paytm
            paramMap.put("ORDER_ID", orderId);
            paramMap.put("CUST_ID", custid);
            paramMap.put("CHANNEL_ID", "WAP");
            paramMap.put("TXN_AMOUNT", cost);
            paramMap.put("WEBSITE", "WEBSTAGING");
            paramMap.put("CALLBACK_URL", varifyurl);
            //paramMap.put( "EMAIL" , "abc@gmail.com");   // no need
            // paramMap.put( "MOBILE_NO" , "9144040888");  // no need
            paramMap.put("CHECKSUMHASH", CHECKSUMHASH);
            //paramMap.put("PAYMENT_TYPE_ID" ,"CC");    // no need
            paramMap.put("INDUSTRY_TYPE_ID", "Retail");
            PaytmOrder Order = new PaytmOrder(paramMap);
            Log.e("checksum ", "param " + paramMap.toString());
            Service.initialize(Order, null);
            // start payment service call here
            Service.startPaymentTransaction(checksum.this, true, true,
                    checksum.this);
        }
    }

    @Override
    public void onTransactionResponse(Bundle inResponse) {
        if (inResponse.getString("STATUS").equals("TXN_SUCCESS")) {
            /*dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(orderId).child("order").setValue(cart);
            dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(orderId).child("payment_method").setValue("online");
            dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(orderId).child("order_status").setValue("pending");
            dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(orderId).child("address").setValue(address);
            dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(orderId).child("latitude").setValue(latitude);
            dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(orderId).child("longitude").setValue(longitude);
            dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(orderId).child("date").setValue(date);
            dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(orderId).child("time").setValue(time);
            dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(orderId).child("cost").setValue(Double.parseDouble(cost)-Double.parseDouble(getString(R.string.delivery_charge)));
            dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(orderId).child("delivery_charge").setValue(Double.parseDouble(getString(R.string.delivery_charge)));*/
            dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(orderId).child("transaction_id").setValue(inResponse.getString("TXNID"));
            dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(orderId).child("bundle").setValue(inResponse.toString());
            dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(orderId).child("payment_status").setValue("done");

            sharedPreferences.edit().putString("myorder", "").commit();
            Toast.makeText(this, "Order placed!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(checksum.this, MainActivity.class));
        } else {
            Toast.makeText(this, "Transaction failed!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(checksum.this, cart.class));
        }
    }

    @Override
    public void networkNotAvailable() {
        Toast.makeText(this, "Network error!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(checksum.this, cart.class));
    }

    @Override
    public void clientAuthenticationFailed(String inErrorMessage) {
        Toast.makeText(this, "Some error occured during transaction. Please try again!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(checksum.this, com.cyfoes.aditya.chachabiryani.cart.class));
    }

    @Override
    public void someUIErrorOccurred(String inErrorMessage) {
        Toast.makeText(this, "Some error occured during transaction. Please try again!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(checksum.this, com.cyfoes.aditya.chachabiryani.cart.class));
    }

    @Override
    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
        Toast.makeText(this, "Some error occured during transaction. Please try again!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(checksum.this, com.cyfoes.aditya.chachabiryani.cart.class));
    }

    @Override
    public void onBackPressedCancelTransaction() {
        Toast.makeText(this, "Transaction Cancelled", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(checksum.this, com.cyfoes.aditya.chachabiryani.cart.class));
    }

    @Override
    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
        Toast.makeText(this, "Transaction Cancelled", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(checksum.this, com.cyfoes.aditya.chachabiryani.cart.class));
    }

    /*@Override
    protected void onResume() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.cart);
        view.performClick();
        super.onResume();
    }*/
}
