package com.cyfoes.aditya.chachabiryani.admin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cyfoes.aditya.chachabiryani.R;
import com.cyfoes.aditya.chachabiryani.admin_home;
import com.cyfoes.aditya.chachabiryani.shop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

public class menu extends AppCompatActivity {

    Button popbtn, addmenubtn;
    Uri filePath;
    ImageView image;
    LinearLayout uploadimage;
    Button save, delete;
    EditText name, price;
    CheckBox available;
    DatabaseReference dbrorder = FirebaseDatabase.getInstance().getReference("menu");
    DatabaseReference dbrshop = FirebaseDatabase.getInstance().getReference("shop");
    StorageReference spref = FirebaseStorage.getInstance().getReferenceFromUrl("gs://chachabiryani-7c5e6.appspot.com");
    ProgressDialog pd;
    LinearLayout menulayout;
    Switch aSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        getSupportActionBar().hide();


        //aSwitch = (Switch)findViewById(R.id.switchon);
        menulayout = (LinearLayout) findViewById(R.id.menulayout);
        createcards();
        //checkstatus();

        addmenubtn = (Button) findViewById(R.id.addmenubtn);
        pd = new ProgressDialog(this);

        /*aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b == true){
                    dbrshop.child("status").setValue("open");
                }
                else {
                    dbrshop.child("status").setValue("closed");
                }
            }
        });*/

        addmenubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(menu.this);
                dialog.setContentView(R.layout.menu_popup);
                dialog.show();
                Window window = dialog.getWindow();
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                uploadimage = dialog.findViewById(R.id.upload_image);
                save = dialog.findViewById(R.id.savebtn);
                name = dialog.findViewById(R.id.name);
                price = dialog.findViewById(R.id.price);
                available = dialog.findViewById(R.id.checkbox);
                image = dialog.findViewById(R.id.imagepreview);
                delete = dialog.findViewById(R.id.delbtn);
                final String cd = generatecode();

                delete.setVisibility(View.GONE);

                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                takepicture();
                            } else {
                                Dexter.withActivity(menu.this)
                                        .withPermission(android.Manifest.permission.CAMERA)
                                        .withListener(new PermissionListener() {
                                            @Override
                                            public void onPermissionGranted(PermissionGrantedResponse response) {/* ... */}

                                            @Override
                                            public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}

                                            @Override
                                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                                        }).check();
                            }
                        } else {
                            takepicture();
                        }
                    }
                });

                uploadimage.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                takepicture();
                            } else {
                                Dexter.withActivity(menu.this)
                                        .withPermission(android.Manifest.permission.CAMERA)
                                        .withListener(new PermissionListener() {
                                            @Override
                                            public void onPermissionGranted(PermissionGrantedResponse response) {/* ... */}

                                            @Override
                                            public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}

                                            @Override
                                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                                        }).check();
                            }
                        } else {
                            takepicture();
                        }
                    }
                });

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pd.setMessage("Uploading...");
                        pd.show();
                        final String dname = name.getText().toString().trim();
                        final String dprice = price.getText().toString().trim();
                        String avail = "unavailable";
                        if (available.isChecked()) {
                            avail = "available";
                        }
                        if (!dname.isEmpty() && !dprice.isEmpty() && filePath != null) {
                            final String finalAvail = avail;
                            spref.child("menu_images/" + cd).putFile(filePath)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            dbrorder.child(cd).child("name").setValue(dname);
                                            dbrorder.child(cd).child("price").setValue(dprice);
                                            dbrorder.child(cd).child("available").setValue(finalAvail);
                                            pd.dismiss();
                                            dialog.dismiss();
                                            createcards();
                                            Toast.makeText(menu.this, "Menu item upload successfull!", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                            pd.setMessage("Uploading..." + progress + " %");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(menu.this, "Failed to upload menu item!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            pd.dismiss();
                            Toast.makeText(menu.this, "All fields are reqired!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.accepted:
                        startActivity(new Intent(menu.this, admin_home.class));
                        break;

                    case R.id.completed:
                        startActivity(new Intent(menu.this, admin_completed.class));
                        break;

                    case R.id.pending:
                        startActivity(new Intent(menu.this, admin_completed.class));
                        break;

                    case R.id.shop:
                        startActivity(new Intent(menu.this, shop.class));
                        break;
                }
                return true;
            }
        });
    }

    /*private void checkstatus() {
        dbrshop.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("status")){
                    if(dataSnapshot.child("status").getValue().toString().equals("open")){
                        aSwitch.setChecked(true);
                    }
                    else {
                        aSwitch.setChecked(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    private void createcards() {
        dbrorder.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menulayout.removeAllViews();
                for (final DataSnapshot d : dataSnapshot.getChildren()) {
                    LayoutInflater inflater = LayoutInflater.from(menu.this);
                    LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.menu_items, null, false);
                    menulayout.addView(lay);

                    final ImageView imageView = lay.findViewById(R.id.item_image);
                    final TextView pname = lay.findViewById(R.id.name);
                    final TextView pprice = lay.findViewById(R.id.price);
                    Button btnadd = lay.findViewById(R.id.btnadd);
                    Button btnremove = lay.findViewById(R.id.btnremove);
                    final Uri[] myuri = new Uri[1];
                    btnadd.setText("EDIT");
                    final String[] id = {""};

                    pname.setText(d.child("name").getValue().toString());
                    pprice.setText("\u20B9 " + d.child("price").getValue().toString()+"/Kg");
                    id[0] = d.getKey().toString();

                    spref.child("menu_images/" + id[0]).getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    myuri[0] = uri;
                                    Picasso.get().load(uri).resize(300, 300).into(imageView);
                                }
                            });

                    if (d.child("available").getValue().toString().equals("unavailable")) {
                        btnadd.setTextColor(Color.parseColor("#787878"));
                        pprice.setTextColor(Color.parseColor("#787878"));
                        btnadd.setBackgroundResource(R.drawable.cancel_button);

                        ColorMatrix matrix = new ColorMatrix();
                        matrix.setSaturation(0);

                        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                        imageView.setColorFilter(filter);
                    }

                    btnadd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Dialog dialog = new Dialog(menu.this);
                            dialog.setContentView(R.layout.menu_popup);
                            dialog.show();

                            Window window = dialog.getWindow();
                            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                            name = dialog.findViewById(R.id.name);
                            image = dialog.findViewById(R.id.imagepreview);
                            uploadimage = dialog.findViewById(R.id.upload_image);
                            price = dialog.findViewById(R.id.price);
                            available = dialog.findViewById(R.id.checkbox);
                            save = dialog.findViewById(R.id.savebtn);
                            delete = dialog.findViewById(R.id.delbtn);



                            uploadimage.setVisibility(View.INVISIBLE);
                            available.setChecked(false);

                            name.setText(d.child("name").getValue().toString());
                            price.setText(d.child("price").getValue().toString());
                            if (d.child("available").getValue().toString().equals("available")) {
                                available.setChecked(true);
                            }
                            Picasso.get().load(myuri[0]).resize(300, 300).into(image);


                            image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                            takepicture();
                                        } else {
                                            Dexter.withActivity(menu.this)
                                                    .withPermission(android.Manifest.permission.CAMERA)
                                                    .withListener(new PermissionListener() {
                                                        @Override
                                                        public void onPermissionGranted(PermissionGrantedResponse response) {/* ... */}

                                                        @Override
                                                        public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}

                                                        @Override
                                                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                                                    }).check();
                                        }
                                    } else {
                                        takepicture();
                                    }
                                }
                            });

                            delete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(menu.this);
                                    builder.setTitle("Deleting item");
                                    builder.setMessage("Are you sure you want to delete '" + d.child("name").getValue().toString() + "' from you menu?");
                                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog, int which) {
                                                    pd.setMessage("Deleting item...");
                                                    pd.show();
                                                    spref.child("menu_images/" + id[0]).delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    dbrorder.child(id[0]).setValue(null);
                                                                    pd.dismiss();
                                                                    dialog.dismiss();
                                                                    createcards();
                                                                    Toast.makeText(menu.this, "Item deteted!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    pd.dismiss();
                                                                    Toast.makeText(menu.this, "Failed to delete item!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            }).create().show();
                                }
                            });

                            save.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    pd.setMessage("Uploading...");
                                    pd.show();
                                    final String dname = name.getText().toString().trim();
                                    final String dprice = price.getText().toString().trim();
                                    String avail = "unavailable";
                                    if (available.isChecked()) {
                                        avail = "available";
                                    }
                                    if (!dname.isEmpty() && !dprice.isEmpty()) {
                                        final String finalAvail = avail;
                                        if (!myuri[0].toString().contains("https://firebasestorage.googleapis.com")) {
                                            spref.child("menu_images/" + id[0]).putFile(filePath)
                                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            dbrorder.child(id[0]).child("name").setValue(dname);
                                                            dbrorder.child(id[0]).child("price").setValue(dprice);
                                                            dbrorder.child(id[0]).child("available").setValue(finalAvail);
                                                            pd.dismiss();
                                                            dialog.dismiss();
                                                            createcards();
                                                            Toast.makeText(menu.this, "Menu item update successfull!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                                            pd.setMessage("Updating..." + progress + " %");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            pd.dismiss();
                                                            Toast.makeText(menu.this, "Failed to update menu item!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            dbrorder.child(id[0]).child("name").setValue(dname);
                                            dbrorder.child(id[0]).child("price").setValue(dprice);
                                            dbrorder.child(id[0]).child("available").setValue(finalAvail);
                                            pd.dismiss();
                                            dialog.dismiss();
                                            createcards();
                                            Toast.makeText(menu.this, "Menu item update successfull!", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        pd.dismiss();
                                        Toast.makeText(menu.this, "All fields are reqired!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String generatecode() {
        final String ALPHA_NUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        int count = 10;
        String code = "";
        while (count-- != 0) {
            int charecter = (int) (Math.random() * ALPHA_NUM.length());
            builder.append(ALPHA_NUM.charAt(charecter));
            code = builder.toString();
        }
        return code;
    }

    private void takepicture() {
        CropImage.activity()
                .setAspectRatio(1, 1)
                .setRequestedSize(480, 400)
                .start(menu.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filePath = result.getUri();
                image.setVisibility(View.VISIBLE);
                image.setImageURI(filePath);
                uploadimage.setVisibility(View.INVISIBLE);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(menu.this, "Failed" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        View view = bottomNavigationView.findViewById(R.id.menu);
        view.performClick();
        super.onResume();
    }
}
