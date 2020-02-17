package com.example.uzimaemployee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private ImageView mainImage;
    private TextView usernameTxt, roleText, secondName;
    private Uri mainImageURI=null;
    private FloatingActionButton clockFloat;
    private CardView ambCard;

    private static final int REQUEST_LOCATION = 1;

    private LocationManager locationManager;
    String latitude, longitude;
    Dialog myDialog;
    Button buttonYes;
    TextView messagetXt;
    //Spinner team, shift;
    public Context context;
    String firstName, ambulanceData,companyData,employeeId,userID, employeeName,employeeID, statusData,shiftData,teamData;
    Timestamp timeinData,timeoutData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize Firebase app and get Firebase instance
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //initialize the progress dialog
        progressDialog = new ProgressDialog(this);
        myDialog=new Dialog(this);
        mainImage = findViewById(R.id.main_activity_image);
        usernameTxt = findViewById(R.id.main_name);
         secondName = findViewById(R.id.second_name);
        roleText = findViewById(R.id.main_role);
        clockFloat = findViewById(R.id.floating_clock);
        ambCard = findViewById(R.id.ambulance_card);

        //Request permission to access user location
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        getUserLocation();


        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));


        //check if user account is setup
        checkUserData();


        clockFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start clock in process

                startClockin();
            }
        });

        ambCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this ,Deployments.class));
            }
        });




    }


    public void checkUserData(){

        progressDialog.setMessage("Loading details...");
        progressDialog.show();




        DocumentReference docRef = firebaseFirestore.collection("Employee_Details").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        Toast.makeText(MainActivity.this, "Welcome back", Toast.LENGTH_LONG).show();




                        String f_name = task.getResult().getString("first_name");
                        String s_name = task.getResult().getString("second_name");
                        String e_id = task.getResult().getString("employee_id");
                        String e_role = task.getResult().getString("employee_role:");
                        String company = task.getResult().getString("company");
                        String ambulance = task.getResult().getString("ambulance");
                        String image2 = task.getResult().getString("image");

                        secondName.setText(s_name);
                        usernameTxt.setText(f_name);
                        roleText.setText(e_role);


                        mainImageURI = Uri.parse(image2);

                        //******replacing the dummy image with real profile picture******

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.user_img);

                        Glide.with(MainActivity.this).setDefaultRequestOptions(placeholderRequest).load(image2).into(mainImage);






//19-10-1996
                    } else {


                        //launch dialog
                        myDialog.setContentView(R.layout.popup2);
                        messagetXt =  (TextView) myDialog.findViewById(R.id.message_text);
                       // String messsage = "It seems that your details are not in our system, please proceed to setup your profile";

                       // messagetXt.setText(messsage);
                        buttonYes=(Button) myDialog.findViewById(R.id.button_proceed);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                               startActivity(new Intent(MainActivity.this,EmployeeCreation.class));
                                myDialog.dismiss();

                            }
                        });

                        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        myDialog.show();


                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }



            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                startActivity(new Intent(MainActivity.this, EmployeeProfile.class));
                return true;

            case R.id.logout:
                progressDialog.setMessage("Signing out...");
                progressDialog.show();

                mAuth.signOut();
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                return true;

            case R.id.contact_sup:
                Toast.makeText(this, "contact was selected", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.clock_out:
                clockOut();
                return true;

            default:

                return super.onOptionsItemSelected(item);
        }

    }


    public void startClockin(){

        progressDialog.setMessage("Loading details...");
        progressDialog.show();

        DocumentReference docRef = firebaseFirestore.collection("Daily_Shift").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {

                        Toast.makeText(MainActivity.this, "Welcome back", Toast.LENGTH_LONG).show();

                        myDialog.dismiss();


//19-10-1996
                    } else {


                        //launch dialog
                        myDialog.setContentView(R.layout.popup);
                        messagetXt =  (TextView) myDialog.findViewById(R.id.message_text);
                        //shift = (Spinner)myDialog.findViewById(R.id.shift_spinner);
                        //team = (Spinner)myDialog.findViewById(R.id.team_spinner);







                        buttonYes=(Button) myDialog.findViewById(R.id.button_proceed);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                final Double lat =  Double.parseDouble(latitude);
                                final Double lng =  Double.parseDouble(longitude);

                                final GeoPoint geoPoint=new GeoPoint(lat,lng);



                                DocumentReference docRef = firebaseFirestore.collection("Employee_Details").document(user_id);
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            progressDialog.dismiss();
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {

                                                String f_name = task.getResult().getString("first_name");
                                                String s_name = task.getResult().getString("second_name");
                                                String e_id = task.getResult().getString("employee_id");
                                                String e_role = task.getResult().getString("employee_role:");
                                                String company = task.getResult().getString("company");
                                                String ambulance = task.getResult().getString("ambulance");





                                                String status = "available";

                                                Map<String, Object> shiftMap= new HashMap<>();

                                                shiftMap.put("first_name",f_name);
                                                shiftMap.put("second_name",s_name);
                                                shiftMap.put("employee_id",e_id);
                                                shiftMap.put("role",e_role);
                                                shiftMap.put("company",company);
                                                shiftMap.put("ambulance",ambulance);
                                                shiftMap.put("time_in", FieldValue.serverTimestamp());
                                                shiftMap.put("time_out",null);
                                                shiftMap.put("user_id",user_id);
                                                shiftMap.put("Status",status);
                                                shiftMap.put("Location",geoPoint);

                                                firebaseFirestore.collection("Daily_Shift").document(user_id)
                                                        .set(shiftMap)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(MainActivity.this,"Shift clocked in",Toast.LENGTH_LONG).show();

                                                                myDialog.dismiss();

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {

                                                                Toast.makeText(MainActivity.this,"FIRESTORE ERROR: Could not store",Toast.LENGTH_LONG).show();

                                                                finish();

                                                            }


                                                        });















//19-10-1996
                                            } else {

                                                Toast.makeText(MainActivity.this, "DATA DOES NOT EXISTS,PLEASE CREATE YOUR MEDICAL ID", Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(MainActivity.this,EditEmployeeProfile.class));



                                            }
                                        } else {

                                            String error = task.getException().getMessage();
                                            Toast.makeText(MainActivity.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                                        }



                                    }
                                });




                               // myDialog.dismiss();

                            }
                        });

                        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        myDialog.show();


                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }



            }
        });



    }

    public void clockOut(){

        String status = "unavailable";

        Map<String, Object> shiftMap= new HashMap<>();

        shiftMap.put("Status",status);
        shiftMap.put("time_out", FieldValue.serverTimestamp());

        firebaseFirestore.collection("Daily_Shift").document(user_id)
                .update(shiftMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this,"  Success!!",Toast.LENGTH_LONG).show();

                        startShiftCollection();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(MainActivity.this,"FIRESTORE ERROR: Could not store",Toast.LENGTH_LONG).show();

                        finish();

                    }


                });




    }

    public void startShiftCollection(){


        DocumentReference docRef = firebaseFirestore.collection("Daily_Shift").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        Toast.makeText(MainActivity.this, "Processing....", Toast.LENGTH_LONG).show();

                         firstName = task.getResult().getString("first_name");
                         employeeId = task.getResult().getString("employee_id");
                         timeinData = task.getResult().getTimestamp("time_in");
                         timeoutData = task.getResult().getTimestamp("time_out");
                         statusData = task.getResult().getString("Status");
                         companyData = task.getResult().getString("company");
                         ambulanceData = task.getResult().getString("ambulance");
                         userID = task.getResult().getString("user_id");


                        //******create new shift database here
                        startDatabaseCreation();

//19-10-1996
                    } else {


                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }



            }
        });






    }

    public void startDatabaseCreation(){

        Map<String, Object> shiftMap= new HashMap<>();


        shiftMap.put("name", firstName);
        shiftMap.put("employee_id", employeeId);
        shiftMap.put("Status",statusData);
        shiftMap.put("time_in", timeinData);
        shiftMap.put("time_out", timeoutData);
        shiftMap.put("company", companyData);
        shiftMap.put("ambulance", ambulanceData);
        shiftMap.put("user_id", user_id);

        firebaseFirestore.collection("Daily_Shift_Records").document()
                .set(shiftMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {


                        Toast.makeText(MainActivity.this,"  Success!!",Toast.LENGTH_LONG).show();

                       deleteRecord();
;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(MainActivity.this,"FIRESTORE ERROR: Could not store",Toast.LENGTH_LONG).show();

                        finish();

                    }


                });




    }

    public void deleteRecord(){

        firebaseFirestore.collection("Daily_Shift").document(user_id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this,"  Success on Delete!!",Toast.LENGTH_LONG).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"  Success on Delete!!",Toast.LENGTH_LONG).show();
                    }
                });
    }



    private void getUserLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Check if gps is enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            OnGPS();

        } else {
            getLocation();
        }
    }

    private void getLocation() {

        //Check Permissions again

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,

                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location LocationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location LocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location LocationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (LocationGps != null) {
                double lat = LocationGps.getLatitude();
                double longi = LocationGps.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);

                Toast.makeText(MainActivity.this, "Your Location:" + "\n" + "Latitude= " + latitude + "\n" + "Longitude= " + longitude, Toast.LENGTH_SHORT).show();
            } else if (LocationNetwork != null) {
                double lat = LocationNetwork.getLatitude();
                double longi = LocationNetwork.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);

                Toast.makeText(MainActivity.this, "Your Location:" + "\n" + "Latitude= " + latitude + "\n" + "Longitude= " + longitude, Toast.LENGTH_SHORT).show();
            } else if (LocationPassive != null) {
                double lat = LocationPassive.getLatitude();
                double longi = LocationPassive.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);

                Toast.makeText(MainActivity.this, "Your Location:" + "\n" + "Latitude= " + latitude + "\n" + "Longitude= " + longitude, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Can't Get Your Location", Toast.LENGTH_SHORT).show();
            }

            //Thats All Run Your App
        }


    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }



}
