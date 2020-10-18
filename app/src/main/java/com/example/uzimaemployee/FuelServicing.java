package com.example.uzimaemployee;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FuelServicing extends AppCompatActivity {

    Dialog myDialog1 , myDialog2;

    //fueling
    ImageView backFuelButton , backServiceButton;
    EditText stationTxt , transIdTxt , litresTxt , amountTxt;
    Button receiptBtn , submitFuelButton;
    TextView receiptUrl;


    //servicing
    EditText garageTxtVw , serviceDesc , transIdServ , amountServ;
    Button receiptServeBtn , submitServButton;
    TextView receiptServUrl;


    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    private Uri postImageUri1 = null;
    private Uri postImageUri12 = null;

    String user_id , ambulancePlate;
    String TAG = "FuelServicing";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_servicing);

        //initialize Firebase app and get Firebase instance
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        //toolbar setup
        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Vehicle");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FuelServicing.this, MainActivity.class));
            }
        });

        myDialog1 = new Dialog(this);
        myDialog2 = new Dialog(this);


        //fetch ambulance details

        fetchAmbulance();
    }

    private void fetchAmbulance() {

        DocumentReference docRef = firebaseFirestore.collection("Employee_Details").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                       ambulancePlate = task.getResult().getString("ambulance");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.vehicle_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.service_entry:
                startServiceEntry();
                        return true;

            case R.id.fuel_entry:
                startFuelEntry();
                return true;

            case R.id.fuel_entry_list:
                Toast.makeText(this , "fuel list selected",Toast.LENGTH_SHORT).show();
                return true;

            case R.id.service_entry_list:
                Toast.makeText(this , "service list selected",Toast.LENGTH_SHORT).show();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void startFuelEntry() {
        myDialog1.setContentView(R.layout.fuel_entry_popup);



        backFuelButton = myDialog1.findViewById(R.id.back_fuel_img);
        stationTxt = myDialog1.findViewById(R.id.station_text_view);
        transIdTxt = myDialog1.findViewById(R.id.transaction_text_view);
        litresTxt = myDialog1.findViewById(R.id.litres_text_view);
        amountTxt = myDialog1.findViewById(R.id.amount_spent_text_view);
        receiptUrl=myDialog1.findViewById(R.id.image_url_text);

        receiptBtn = myDialog1.findViewById(R.id.button_receipt);
        submitFuelButton = myDialog1.findViewById(R.id.submit_fuel);



        backFuelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog1.dismiss();
            }
        });

        receiptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                receiptUrl.setVisibility(View.VISIBLE);

                BringImagePicker();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        receiptUrl.setText(postImageUri1.toString());
                    }
                }, 20000);


            }
        });

        submitFuelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String station = stationTxt.getText().toString();
                String transactionId = transIdTxt.getText().toString();
                String litres = litresTxt.getText().toString();
                String amountFuel = amountTxt.getText().toString();

                if (station.isEmpty()){
                    stationTxt.setError("Please indicate the fueling station");
                }else if(transactionId.isEmpty()){
                    transIdTxt.setError("Please indicate the transaction id of te payment made");

                }else if(litres.isEmpty()){

                    litresTxt.setError("Please indicate the litres filled into the vehicle");
                }else if(amountFuel.isEmpty()){

                    amountTxt.setError("Please indicate the amount spent in fueling");

                }else if(postImageUri1==null){

                    Toast.makeText(getApplicationContext() , "Please select the receipt image",Toast.LENGTH_SHORT).show();

                }

                else{

                    Toast.makeText(getApplicationContext() , "sending....",Toast.LENGTH_SHORT).show();

                    sendToFuelDatabase(station , transactionId , litres , amountFuel);


                }

            }
        });
        myDialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog1.show();

    }

    private void sendToFuelDatabase(String station, String transactionId, String litres, String amountFuel) {

        if(postImageUri1!=null){


            final String randomName = UUID.randomUUID().toString();


            StorageReference filePath= storageReference.child("Fuel_Receipts").child(randomName+".jpg");
            filePath.putFile(postImageUri1).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){


                        storageReference.child("Fuel_Receipts").child(randomName+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri download_uri) {


                                long litresVehicle = Long.parseLong(litres);
                                int amount = Integer.parseInt(amountFuel);

                                Map<String , Object> fuelMap = new HashMap<>();
                                fuelMap.put("station",station);
                                fuelMap.put("transaction_id" , transactionId);
                                fuelMap.put("amount" , amount);
                                fuelMap.put("litres",litresVehicle);
                                fuelMap.put("timestamp" , FieldValue.serverTimestamp());
                                fuelMap.put("receipt_image",download_uri.toString());

                                firebaseFirestore.collection("Fuel_Entries").document()
                                        .set(fuelMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(getApplicationContext() , "Fueling details added successfully",Toast.LENGTH_SHORT).show();

                                                updateGrandTotal(amount);
                                                updateMonthTotal(amount);
                                                createPersonalTotal(amount);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext() , "Fueling details upload failed"+e.getMessage(),Toast.LENGTH_SHORT).show();

                                    }
                                });

                            }

                        });

                    }else {

                        Toast.makeText(getApplicationContext() , "Could not upload receipt image",Toast.LENGTH_SHORT).show();


                    }
                }
            });

        }else{

            Toast.makeText(getApplicationContext() , "Please ensure that you have the receipt image",Toast.LENGTH_SHORT).show();
        }


    }

    private void createPersonalTotal(int amount) {

        //get todays month
        Calendar cal=Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(cal.getTime());

        DocumentReference docRef = firebaseFirestore.collection("Ambulances/"+ambulancePlate+"/Expenses").document("Monthly");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        DocumentReference monthRef = firebaseFirestore.collection("Ambulances/"+ambulancePlate+"/Expenses").document("Monthly");
// Atomically increment the population of the city by 50.
                        monthRef.update(month_name, FieldValue.increment(amount));

                    } else {
                        Log.d(TAG, "No such document");

                        Map<String , Object> monthMap = new HashMap<>();
                        monthMap.put(month_name , amount);
                        firebaseFirestore.collection("Ambulances/"+ambulancePlate+"/Expenses").document("Monthly")
                                .set(monthMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                Toast.makeText(getApplicationContext() , "Done..",Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext() , "Failed at personal month increament.."+e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }


    private void updateMonthTotal(int amount) {

        //get todays month
        Calendar cal=Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(cal.getTime());

        DocumentReference monthRef = firebaseFirestore.collection("Fueling_Total").document("Monthly");

// Atomically increment the population of the city by 50.
        monthRef.update(month_name, FieldValue.increment(amount));
    }

    private void updateGrandTotal(int amount) {
        DocumentReference washingtonRef = firebaseFirestore.collection("Fueling_Total").document("Grand_Total");
        washingtonRef.update("total", FieldValue.increment(amount));
    }







    //servicing totals:


    private void startServiceEntry() {

        myDialog2.setContentView(R.layout.service_entry_popup);

        backServiceButton = myDialog2.findViewById(R.id.back_service_img);

        garageTxtVw = myDialog2.findViewById(R.id.garage_text_view);
        serviceDesc = myDialog2.findViewById(R.id.service_type_text_view);
        transIdServ = myDialog2.findViewById(R.id.transaction_text_view);
        amountServ = myDialog2.findViewById(R.id.amount_text_view);
        receiptServeBtn = myDialog2.findViewById(R.id.button_receipt_img);
        submitServButton = myDialog2.findViewById(R.id.button_submit_service);
        receiptServUrl = myDialog2.findViewById(R.id.receipt_url_text);



        backServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog2.dismiss();
            }
        });

        receiptServeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                receiptServUrl.setVisibility(View.VISIBLE);
                BringImagePicker();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        receiptServUrl.setText(postImageUri12.toString());
                    }
                }, 20000);



            }
        });

        submitServButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String garage = garageTxtVw.getText().toString();
                String transactionId = transIdServ.getText().toString();
                String service = serviceDesc.getText().toString();
                String amount_service = amountServ.getText().toString();

                if (garage.isEmpty()){
                    garageTxtVw.setError("Please indicate the service station taken to");
                }else if(transactionId.isEmpty()){
                    transIdServ.setError("Please indicate the transaction id of te payment made");

                }else if(service.isEmpty()){

                    serviceDesc.setError("Please indicate the type of service done to vehicle");
                }else if(amount_service.isEmpty()){

                    amountServ.setError("Please indicate the amount spent in servicing vehicle");

                }else if(postImageUri12==null){

                    Toast.makeText(getApplicationContext() , "Please select the receipt image",Toast.LENGTH_SHORT).show();

                }

                else{

                    Toast.makeText(getApplicationContext() , "sending....",Toast.LENGTH_SHORT).show();

                    sendToStorageDatabase(garage , transactionId , service , amount_service);


                }

            }
        });




        myDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog2.show();

    }

    private void sendToStorageDatabase(String garage, String transactionId, String service, String amount_service) {

        if(postImageUri12!=null){


            final String randomName = UUID.randomUUID().toString();


            StorageReference filePath= storageReference.child("Servicing_Receipts").child(randomName+".jpg");
            filePath.putFile(postImageUri12).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){


                        storageReference.child("Servicing_Receipts").child(randomName+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri download_uri) {


                                int amount = Integer.parseInt(amount_service);

                                Map<String , Object> fuelMap = new HashMap<>();
                                fuelMap.put("service_center",garage);
                                fuelMap.put("transaction_id" , transactionId);
                                fuelMap.put("amount" , amount);
                                fuelMap.put("service_type",service);
                                fuelMap.put("timestamp" , FieldValue.serverTimestamp());
                                fuelMap.put("receipt_image",download_uri.toString());

                                firebaseFirestore.collection("Servicing_Entries").document()
                                        .set(fuelMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(getApplicationContext() , "Service details added successfully",Toast.LENGTH_SHORT).show();

                                                updateServiceGrandTotal(amount);
                                                updateServiceMonthTotal(amount);
                                                createServicePersonalTotal(amount);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext() , "Service details upload failed"+e.getMessage(),Toast.LENGTH_SHORT).show();

                                    }
                                });

                            }

                        });

                    }else {

                        Toast.makeText(getApplicationContext() , "Could not upload receipt image",Toast.LENGTH_SHORT).show();


                    }
                }
            });

        }else{

            Toast.makeText(getApplicationContext() , "Please ensure that you have the receipt image",Toast.LENGTH_SHORT).show();
        }



    }

    private void createServicePersonalTotal(int amount) {

        //get todays month
        Calendar cal=Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(cal.getTime());

        DocumentReference docRef = firebaseFirestore.collection("Ambulances/"+ambulancePlate+"/Expenses").document("Servicing_Monthly");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        DocumentReference monthRef = firebaseFirestore.collection("Ambulances/"+ambulancePlate+"/Expenses").document("Servicing_Monthly");
// Atomically increment the population of the city by 50.
                        monthRef.update(month_name, FieldValue.increment(amount));

                    } else {
                        Log.d(TAG, "No such document");

                        Map<String , Object> monthMap = new HashMap<>();
                        monthMap.put(month_name , amount);
                        firebaseFirestore.collection("Ambulances/"+ambulancePlate+"/Expenses").document("Servicing_Monthly")
                                .set(monthMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                Toast.makeText(getApplicationContext() , "Done..",Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext() , "Failed at personal month increament servicing.."+e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });



    }

    private void updateServiceMonthTotal(int amount) {

        //get todays month
        Calendar cal=Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(cal.getTime());

        DocumentReference monthRef = firebaseFirestore.collection("Servicing_Total").document("Monthly");

// Atomically increment the population of the city by 50.
        monthRef.update(month_name, FieldValue.increment(amount));

    }

    private void updateServiceGrandTotal(int amount) {

        DocumentReference washingtonRef = firebaseFirestore.collection("Servicing_Total").document("Grand_Total");
        washingtonRef.update("total", FieldValue.increment(amount));
    }


    private void BringImagePicker() {

        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(2, 1)
                .setMinCropResultSize(520,520)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {

                postImageUri1=result.getUri();
                postImageUri12 = result.getUri();
                //imageSelect.setImageURI(postImageUri1);



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error= result.getError();

            }
        }


    }

}
