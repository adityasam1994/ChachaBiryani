package com.cyfoes.aditya.chachabiryani;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cyfoes.aditya.chachabiryani.admin.myaccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.payumoney.core.PayUmoneySdkInitializer;
import com.payumoney.core.entity.TransactionResponse;
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class cart extends AppCompatActivity {

    PayUmoneySdkInitializer.PaymentParam.Builder builder = new PayUmoneySdkInitializer.PaymentParam.Builder();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    PayUmoneySdkInitializer.PaymentParam paymentParam = null;
    LinearLayout cart_item_layout;
    SharedPreferences sharedPreferences;
    DatabaseReference dbruser = FirebaseDatabase.getInstance().getReference("users");
    DatabaseReference dbrorder = FirebaseDatabase.getInstance().getReference("myorders");
    DatabaseReference dbrmenu = FirebaseDatabase.getInstance().getReference("menu");
    DatabaseReference dbrshop = FirebaseDatabase.getInstance().getReference("shop");
    TextView addresstext, cartprice, changeaddress;
    Button checkout;
    RadioGroup radioGroup;
    String payment_method = "cod";
    double homelati = 0;
    double homelongi = 0;
    LinearLayout emptycart;
    ScrollView scrollView;
    double totalcost = 0;
    Boolean name = false;
    ProgressDialog pd;
    Boolean shopopen;
    Double delivery_charge=300.0, dradius = 0.0;
    Boolean underradius=false;

    String TAG = "mainActivity", txnid = "txt12346", amount = "20", phone = "9144040888",
            prodname = "BlueApp Course", firstname = "kamal", email = "kamal.bunkar07@gmail.com",
            merchantId = "6788021", merchantkey = "dCwHchUc";  //   first test key only

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        getSupportActionBar().hide();

        pd = new ProgressDialog(this);
        cart_item_layout = (LinearLayout) findViewById(R.id.cart_item_layout);
        addresstext = (TextView) findViewById(R.id.addresstext);
        cartprice = (TextView) findViewById(R.id.cartprice);
        sharedPreferences = getSharedPreferences("orders", Context.MODE_PRIVATE);
        checkout = (Button) findViewById(R.id.checkout);
        radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        emptycart = (LinearLayout) findViewById(R.id.emptylayout);
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        changeaddress = (TextView) findViewById(R.id.changeaddress);

        //createcards();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.cart);
        view.performClick();

        //sethomeaddress();
        checkshopstatus();

        //setprice();

        smspermission();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.homeicon:
                        startActivity(new Intent(cart.this, MainActivity.class));
                        break;

                    case R.id.login:
                        if (firebaseAuth.getCurrentUser() != null) {
                            startActivity(new Intent(cart.this, myaccount.class));
                        } else {
                            startActivity(new Intent(cart.this, login.class));
                        }
                        break;

                    case R.id.orders:
                        startActivity(new Intent(cart.this, myorder.class));
                        break;
                }
                return true;
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radiocod:
                        payment_method = "cod";
                        break;

                    case R.id.radiopaytm:
                        payment_method = "paytm";
                        break;

                    /*case R.id.radiopayu:
                        payment_method = "payu";
                        break;*/
                }
            }
        });

        final String date = getdate(System.currentTimeMillis() + "");
        final String time = gettime(System.currentTimeMillis() + "");

        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkout.getText().toString().equals("CHECKOUT*")) {
                    Toast.makeText(cart.this, "Kindly remove unavailable items from your cart!", Toast.LENGTH_SHORT).show();
                } else {
                    if (addresstext.getText().toString().equals("No address")) {
                        Toast.makeText(cart.this, "Kindly add an address in your account", Toast.LENGTH_SHORT).show();
                    } else {
                        if (name == false) {
                            Toast.makeText(cart.this, "Kindly add your name in My Account", Toast.LENGTH_SHORT).show();
                        } else {
                            if (shopopen == false) {
                                Toast.makeText(cart.this, "Shop is closed, order cannot be placed", Toast.LENGTH_SHORT).show();
                            } else {
                                if (underradius == false) {
                                    Toast.makeText(cart.this, "The selected address is out of the range of our shop", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (checkout.getText().toString().equals("SHOP CLOSED")) {
                                        Toast.makeText(cart.this, "Shop is closed now, order cannot be placed", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String code = generatecode();
                                        if (payment_method.equals("cod")) {
                                            String cart = sharedPreferences.getString("myorder", "");
                                            if (!cart.equals("") && homelati != 0 && homelongi != 0 && !addresstext.getText().toString().equals("My Address")) {
                                        /*dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("order").setValue(cart);
                                        dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("payment_method").setValue("cod");
                                        dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("payment_status").setValue("pending");
                                        dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("order_status").setValue("pending");
                                        dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("address").setValue(addresstext.getText().toString());
                                        dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("latitude").setValue(homelati);
                                        dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("longitude").setValue(homelongi);
                                        dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("date").setValue(date);
                                        dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("time").setValue(time);
                                        dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("cost").setValue(totalcost - delivery_charge);
                                        dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("delivery_charge").setValue(delivery_charge);*/
                                                String order = cart;
                                                String payment_method = "cod";
                                                String cost = String.valueOf((totalcost - delivery_charge));
                                                place_order po = new place_order(cart, "cod", cost, delivery_charge + "", homelati + "", homelongi + "", "pending", "pending",
                                                        addresstext.getText().toString(), date + "", time + "");

                                                dbrorder.child(firebaseAuth.getCurrentUser().getUid()).child(code).setValue(po);

                                                sharedPreferences.edit().putString("myorder", "").commit();
                                                Intent intent = new Intent(cart.this, checksum.class);
                                                intent.putExtra("orderid", code);
                                                intent.putExtra("cost", totalcost + "");
                                                intent.putExtra("order", cart);
                                                intent.putExtra("address", addresstext.getText().toString());
                                                intent.putExtra("latitude", homelati + "");
                                                intent.putExtra("longitude", homelongi + "");
                                                intent.putExtra("date", date + "");
                                                intent.putExtra("time", time + "");
                                                intent.putExtra("custid", firebaseAuth.getCurrentUser().getUid().toString());
                                                intent.putExtra("payment", "cod");
                                                intent.putExtra("delivery_charge", delivery_charge + "");
                                                startActivity(intent);
                                                //createcards();
                                                //Toast.makeText(cart.this, "Order placed", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        if (payment_method.equals("paytm")) {
                                            String cart = sharedPreferences.getString("myorder", "");

/*
                                    dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("order").setValue(cart);
                                    dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("payment_method").setValue("online");
                                    dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("payment_status").setValue("pending");
                                    dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("order_status").setValue("pending");
                                    dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("address").setValue(addresstext.getText().toString());
                                    dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("latitude").setValue(homelati);
                                    dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("longitude").setValue(homelongi);
                                    dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("date").setValue(date);
                                    dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("time").setValue(time);
                                    dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("cost").setValue(totalcost - delivery_charge);
                                    dbrorder.child(firebaseAuth.getCurrentUser().getUid().toString()).child(code).child("delivery_charge").setValue(delivery_charge);
*/

                                            String order = cart;
                                            String payment_method = "online";
                                            String cost = String.valueOf((totalcost - delivery_charge));
                                            place_order po = new place_order(cart, "online", cost, delivery_charge + "", homelati + "", homelongi + "", "pending", "pending",
                                                    addresstext.getText().toString(), date + "", time + "");

                                            dbrorder.child(firebaseAuth.getCurrentUser().getUid()).child(code).setValue(po);

                                            sharedPreferences.edit().putString("myorder", "").commit();
                                            Intent intent = new Intent(cart.this, checksum.class);
                                            intent.putExtra("orderid", code);
                                            intent.putExtra("cost", totalcost + "");
                                            intent.putExtra("order", cart);
                                            intent.putExtra("address", addresstext.getText().toString());
                                            intent.putExtra("latitude", homelati + "");
                                            intent.putExtra("longitude", homelongi + "");
                                            intent.putExtra("date", date + "");
                                            intent.putExtra("time", time + "");
                                            intent.putExtra("payment", "paytm");
                                            intent.putExtra("delivery_charge", delivery_charge + "");
                                            intent.putExtra("custid", firebaseAuth.getCurrentUser().getUid().toString());
                                            startActivity(intent);
                                        }
                                        if (payment_method.endsWith("payu")) {
                                            pd.setMessage("Please wait...");
                                            pd.show();
                                            phone = firebaseAuth.getCurrentUser().getPhoneNumber().toString();
                                            amount = totalcost + "";
                                            txnid = code;
                                            startpay();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        changeaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeaddressdialog();
            }
        });
    }

    private void checkshopstatus() {
        dbrshop.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String ch = dataSnapshot.child("delivery_charge").getValue().toString();
                dradius = Double.parseDouble(dataSnapshot.child("radius").getValue().toString())*1000;
                delivery_charge = Double.parseDouble(ch);
                createcards();
                sethomeaddress();

                if(dataSnapshot.hasChild("status")){
                    if(dataSnapshot.child("status").getValue().toString().equals("closed")){
                        shopopen=false;
                    }
                    else {
                        shopopen=true;
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
                            checkout.setText("SHOP CLOSED");
                        }
                        else if(cdate.after(cldate)){
                            checkout.setText("SHOP CLOSED");
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

    public void startpay() {

        builder.setAmount(amount)                          // Payment amount
                .setTxnId(txnid)                     // Transaction ID
                .setPhone(phone)                   // User Phone number
                .setProductName(prodname)                   // Product Name or description
                .setFirstName(firstname)                              // User First name
                .setEmail(email)              // User Email ID
                .setsUrl("https://www.payumoney.com/mobileapp/payumoney/success.php")     // Success URL (surl)
                .setfUrl("https://www.payumoney.com/mobileapp/payumoney/failure.php")     //Failure URL (furl)
                .setUdf1("")
                .setUdf2("")
                .setUdf3("")
                .setUdf4("")
                .setUdf5("")
                .setUdf6("")
                .setUdf7("")
                .setUdf8("")
                .setUdf9("")
                .setUdf10("")
                .setIsDebug(true)                              // Integration environment - true (Debug)/ false(Production)
                .setKey(merchantkey)                        // Merchant key
                .setMerchantId(merchantId);


        try {
            paymentParam = builder.build();
            // generateHashFromServer(paymentParam );
            getHashkey();

        } catch (Exception e) {
            Log.e(TAG, " error s " + e.toString());
        }

    }

    public void getHashkey() {
        ServiceWrapper service = new ServiceWrapper(null);
        Call<String> call = service.newHashCall(merchantkey, txnid, amount, prodname,
                firstname, email);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.e(TAG, "hash res " + response.body());
                String merchantHash = response.body();
                if (merchantHash.isEmpty() || merchantHash.equals("")) {
                    Toast.makeText(cart.this, "Could not generate hash", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "hash empty");
                } else {
                    // mPaymentParams.setMerchantHash(merchantHash);
                    paymentParam.setMerchantHash(merchantHash);
                    // Invoke the following function to open the checkout page.
                    // PayUmoneyFlowManager.startPayUMoneyFlow(paymentParam, StartPaymentActivity.this,-1, true);
                    PayUmoneyFlowManager.startPayUMoneyFlow(paymentParam, cart.this, R.style.AppTheme_default, false);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "hash error " + t.toString());
            }
        });
    }

    private void smspermission() {
        if (ContextCompat.checkSelfPermission(cart.this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(cart.this, new String[]{android.Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
// PayUMoneySdk: Success -- payuResponse{"id":225642,"mode":"CC","status":"success","unmappedstatus":"captured","key":"9yrcMzso","txnid":"223013","transaction_fee":"20.00","amount":"20.00","cardCategory":"domestic","discount":"0.00","addedon":"2018-12-31 09:09:43","productinfo":"a2z shop","firstname":"kamal","email":"kamal.bunkar07@gmail.com","phone":"9144040888","hash":"b22172fcc0ab6dbc0a52925ebbd0297cca6793328a8dd1e61ef510b9545d9c851600fdbdc985960f803412c49e4faa56968b3e70c67fe62eaed7cecacdfdb5b3","field1":"562178","field2":"823386","field3":"2061","field4":"MC","field5":"167227964249","field6":"00","field7":"0","field8":"3DS","field9":" Verification of Secure Hash Failed: E700 -- Approved -- Transaction Successful -- Unable to be determined--E000","payment_source":"payu","PG_TYPE":"AXISPG","bank_ref_no":"562178","ibibo_code":"VISA","error_code":"E000","Error_Message":"No Error","name_on_card":"payu","card_no":"401200XXXXXX1112","is_seamless":1,"surl":"https://www.payumoney.com/sandbox/payment/postBackParam.do","furl":"https://www.payumoney.com/sandbox/payment/postBackParam.do"}
//PayUMoneySdk: Success -- merchantResponse438104
// on successfull txn
        //  request code 10000 resultcode -1
        //tran {"status":0,"message":"payment status for :438104","result":{"postBackParamId":292490,"mihpayid":"225642","paymentId":438104,"mode":"CC","status":"success","unmappedstatus":"captured","key":"9yrcMzso","txnid":"txt12345","amount":"20.00","additionalCharges":"","addedon":"2018-12-31 09:09:43","createdOn":1546227592000,"productinfo":"a2z shop","firstname":"kamal","lastname":"","address1":"","address2":"","city":"","state":"","country":"","zipcode":"","email":"kamal.bunkar07@gmail.com","phone":"9144040888","udf1":"","udf2":"","udf3":"","udf4":"","udf5":"","udf6":"","udf7":"","udf8":"","udf9":"","udf10":"","hash":"0e285d3a1166a1c51b72670ecfc8569645b133611988ad0b9c03df4bf73e6adcca799a3844cd279e934fed7325abc6c7b45b9c57bb15047eb9607fff41b5960e","field1":"562178","field2":"823386","field3":"2061","field4":"MC","field5":"167227964249","field6":"00","field7":"0","field8":"3DS","field9":" Verification of Secure Hash Failed: E700 -- Approved -- Transaction Successful -- Unable to be determined--E000","bank_ref_num":"562178","bankcode":"VISA","error":"E000","error_Message":"No Error","cardToken":"","offer_key":"","offer_type":"","offer_availed":"","pg_ref_no":"","offer_failure_reason":"","name_on_card":"payu","cardnum":"401200XXXXXX1112","cardhash":"This field is no longer supported in postback params.","card_type":"","card_merchant_param":null,"version":"","postUrl":"https:\/\/www.payumoney.com\/mobileapp\/payumoney\/success.php","calledStatus":false,"additional_param":"","amount_split":"{\"PAYU\":\"20.0\"}","discount":"0.00","net_amount_debit":"20","fetchAPI":null,"paisa_mecode":"","meCode":"{\"vpc_Merchant\":\"TESTIBIBOWEB\"}","payuMoneyId":"438104","encryptedPaymentId":null,"id":null,"surl":null,"furl":null,"baseUrl":null,"retryCount":0,"merchantid":null,"payment_source":null,"pg_TYPE":"AXISPG"},"errorCode":null,"responseCode":null}---438104

        // Result Code is -1 send from Payumoney activity
        Log.e("StartPaymentActivity", "request code " + requestCode + " resultcode " + resultCode);
        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_OK && data != null) {
            TransactionResponse transactionResponse = data.getParcelableExtra(PayUmoneyFlowManager.INTENT_EXTRA_TRANSACTION_RESPONSE);

            if (transactionResponse != null && transactionResponse.getPayuResponse() != null) {

                if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.SUCCESSFUL)) {
                    pd.dismiss();
                    //Success Transaction
                } else {
                    //Failure Transaction
                    pd.dismiss();
                    Toast.makeText(this, "" + transactionResponse.getTransactionStatus(), Toast.LENGTH_SHORT).show();
                }

                // Response from Payumoney
                String payuResponse = transactionResponse.getPayuResponse();

                // Response from SURl and FURL
                String merchantResponse = transactionResponse.getTransactionDetails();
                Log.e(TAG, "tran " + payuResponse + "---" + merchantResponse);
            } /* else if (resultModel != null && resultModel.getError() != null) {
                Log.d(TAG, "Error response : " + resultModel.getError().getTransactionResponse());
            } else {
                Log.d(TAG, "Both objects are null!");
            }*/
        }
    }

    private void changeaddressdialog() {
        final Dialog dialog = new Dialog(cart.this);
        dialog.setContentView(R.layout.address_popup);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final LinearLayout addressl = dialog.findViewById(R.id.address_layout);

        dbruser.child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("addresses")) {
                    for (final DataSnapshot daddress : dataSnapshot.child("addresses").getChildren()) {
                        LayoutInflater inflater = LayoutInflater.from(cart.this);
                        LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.address_popup_item, null, false);
                        addressl.addView(lay);

                        TextView name = lay.findViewById(R.id.addresshead);
                        TextView address = lay.findViewById(R.id.address);

                        name.setText(daddress.child("title").getValue().toString());
                        address.setText(daddress.child("address").getValue().toString());

                        lay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Location locc = new Location("");
                                locc.setLatitude(Double.parseDouble(daddress.child("latitude").getValue().toString()));
                                locc.setLongitude(Double.parseDouble(daddress.child("longitude").getValue().toString()));

                                Location locp = new Location("");
                                locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                                locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                                double dist = locc.distanceTo(locp);

                                if(dist > dradius){
                                    Toast.makeText(cart.this, "This address is out of our delivery range", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    addresstext.setText(daddress.child("address").getValue().toString());
                                    homelati = Double.parseDouble(daddress.child("latitude").getValue().toString());
                                    homelongi = Double.parseDouble(daddress.child("longitude").getValue().toString());
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static String getdate(String longDate) {
        String returnDate = null;
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat(
                    "dd-MM-yyyy", Locale.US);
            Calendar calender = Calendar.getInstance();
            TimeZone currentTimeZone = calender.getTimeZone();
            timeFormat.setTimeZone(currentTimeZone);
            Date date = new Date(Long.parseLong(longDate));
            returnDate = timeFormat.format(date);
        } catch (Exception ex) {
            returnDate = longDate;
        }
        return returnDate;
    }

    public static String gettime(String longDate) {
        String returnDate = null;
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat(
                    "HH:mm", Locale.US);
            Calendar calender = Calendar.getInstance();
            TimeZone currentTimeZone = calender.getTimeZone();
            timeFormat.setTimeZone(currentTimeZone);
            Date date = new Date(Long.parseLong(longDate));
            returnDate = timeFormat.format(date);
        } catch (Exception ex) {
            returnDate = longDate;
        }
        return returnDate;
    }


    private String generatecode() {
        final String ALPHA_NUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        int count = 7;
        String code = "";
        while (count-- != 0) {
            int charecter = (int) (Math.random() * ALPHA_NUM.length());
            builder.append(ALPHA_NUM.charAt(charecter));
            code = builder.toString();
        }
        return code;
    }

    private void setprice() {
        String myorder = sharedPreferences.getString("myorder", "");
        if (!myorder.equals("")) {
            double cost = delivery_charge;
            String[] sp = myorder.split(":::");
            for (String s : sp) {
                String[] splittext = s.split("/");
                cost = cost + Double.parseDouble(splittext[3]);
            }
            cartprice.setText("" + cost);
            totalcost = cost;
        }
    }

    private void sethomeaddress() {
        dbruser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (firebaseAuth.getCurrentUser() != null) {
                    if (dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())) {
                        if (dataSnapshot.child(firebaseAuth.getCurrentUser().getUid()).hasChild("name")) {
                            name = true;
                        } else {
                            name = false;
                        }
                        if (dataSnapshot.child(firebaseAuth.getCurrentUser().getUid()).hasChild("addresses")) {
                            int count = 0;
                            for (DataSnapshot d : dataSnapshot.child(firebaseAuth.getCurrentUser().getUid()).child("addresses").getChildren()) {
                                if (count == 0) {
                                    addresstext.setText(d.child("address").getValue().toString());
                                    homelati = Double.parseDouble(d.child("latitude").getValue().toString());
                                    homelongi = Double.parseDouble(d.child("longitude").getValue().toString());

                                    Location locc = new Location("");
                                    locc.setLatitude(homelati);
                                    locc.setLongitude(homelongi);

                                    Location locp = new Location("");
                                    locp.setLatitude(Double.parseDouble(getString(R.string.latitude)));
                                    locp.setLongitude(Double.parseDouble(getString(R.string.longitude)));

                                    double dist = locc.distanceTo(locp);
                                    if(dist > dradius){
                                        underradius = false;
                                    }
                                    else {
                                        underradius = true;
                                    }
                                }
                                count++;
                            }
                        } else {
                            addresstext.setText("No address");
                        }
                    }
                } else {
                    Toast.makeText(cart.this, "Login first to access the cart", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(cart.this, login.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createcards() {
        checkout.setText("CHECKOUT");
        if (sharedPreferences.getString("myorder", "").equals("")) {
            scrollView.setVisibility(View.GONE);
            emptycart.setVisibility(View.VISIBLE);
        } else {
            emptycart.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        }
        cart_item_layout.removeAllViews();
        String myorder = sharedPreferences.getString("myorder", "");
        if (!myorder.equals("")) {
            String[] sp = myorder.split(":::");

            final ArrayList<String> list = new ArrayList<String>();
            for (String s : sp) {
                list.add(s);
            }

            for (String s : list) {
                final int ind = list.indexOf(s);

                LayoutInflater inflater = LayoutInflater.from(cart.this);
                final LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.cart_item, null, false);
                cart_item_layout.addView(lay);

                TextView itemname = lay.findViewById(R.id.itemname);
                TextView itemweight = lay.findViewById(R.id.itemweight);
                TextView itemprice = lay.findViewById(R.id.itemprice);
                final ImageView info = lay.findViewById(R.id.info);

                info.setVisibility(View.INVISIBLE);

                final String[] splittext = s.split("/");

                dbrmenu.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot dcode : dataSnapshot.getChildren()) {
                            if (dcode.getKey().toString().equals(splittext[0])) {
                                if (dcode.child("available").getValue().toString().equals("unavailable")) {
                                    info.setVisibility(View.VISIBLE);
                                    checkout.setText("CHECKOUT*");
                                    lay.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(cart.this);
                                            builder.setTitle("Item unavailable!");
                                            builder.setMessage("'" + splittext[2] + "' is currently unavailable, Do you want to remove it from cart?");
                                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    list.remove(ind);

                                                    String nlist = "";
                                                    for (String l : list) {
                                                        if (nlist.equals("")) {
                                                            nlist = l;
                                                        } else {
                                                            nlist = nlist + ":::" + l;
                                                        }
                                                    }
                                                    sharedPreferences.edit().putString("myorder", nlist).commit();
                                                    createcards();
                                                }
                                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Toast.makeText(cart.this, "Okay", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            builder.show();
                                        }
                                    });
                                } else {
                                    info.setVisibility(View.INVISIBLE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                itemname.setText(splittext[2]);
                itemweight.setText(splittext[1] + " Kg");
                itemprice.setText("\u20B9 " + splittext[3]);
            }

            LayoutInflater inflater = LayoutInflater.from(cart.this);
            final LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.cart_item, null, false);
            cart_item_layout.addView(lay);

            TextView itemname = lay.findViewById(R.id.itemname);
            TextView itemweight = lay.findViewById(R.id.itemweight);
            TextView itemprice = lay.findViewById(R.id.itemprice);
            final ImageView info = lay.findViewById(R.id.info);

            itemname.setText("Delivery Charge");
            itemweight.setVisibility(View.INVISIBLE);
            itemprice.setText("\u20B9 " + delivery_charge);
            info.setVisibility(View.GONE);
        }
        setprice();
    }

    private void showalert(String s) {

    }

    @Override
    protected void onResume() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.cart);
        view.performClick();
        super.onResume();
    }
}
