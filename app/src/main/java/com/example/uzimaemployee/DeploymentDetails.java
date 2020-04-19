package com.example.uzimaemployee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DeploymentDetails extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String user_id;
    private Geocoder geocoder;

    private TextView titleText, statusText, incidentText , patientPhone, callNumber, descriptionText , userName;
    private Button navButton , pcrButton,finishButton;
    private static final int REQUEST_CALL =1;
    String incident_d;


    private String emergency_post_id , latitude , longitude;
    String number, distrUid;
    private ProgressDialog progressDialog;
    Dialog myDialog;
    Button yesBtn, noBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deployment_details);

        //firebase setup
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //toolbar setup
        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Details");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DeploymentDetails.this, Deployments.class));
            }
        });


        //setup of widgets
        myDialog=new Dialog(this);
        progressDialog = new ProgressDialog(this);
        titleText =findViewById(R.id.title_text);
        statusText =findViewById(R.id.status_text);
        incidentText =findViewById(R.id.incident_text);
        patientPhone =findViewById(R.id.phone_number);
        callNumber =findViewById(R.id.call_number);
        descriptionText =findViewById(R.id.description_text);
        navButton =findViewById(R.id.navigate_button);
        pcrButton =findViewById(R.id.pcr_button);
        finishButton =findViewById(R.id.button_finish);
        userName =findViewById(R.id.user_name);

        //FETCH DETAILS
        emergency_post_id = getIntent().getStringExtra("DOCUMENT_ID");
        latitude = getIntent().getStringExtra("LATITUDE");
        longitude = getIntent().getStringExtra("LONGITUDE");

        //fetch details
        fetchFromDatabase();

        //navigation

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startNavigation();

            }
        });

        pcrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent openPost = new Intent(DeploymentDetails.this, PcrReport.class);
                openPost.putExtra("DISTRESSED_ID",distrUid);
                openPost.putExtra("DEPLOYMENT_ID",emergency_post_id );
                openPost.putExtra("INCIDENT",incident_d );

                startActivity(openPost);

            }
        });


        callNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCall();
            }
        });


        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //launch dialog
                myDialog.setContentView(R.layout.popup3);


                // messagetXt.setText(messsage);
                yesBtn=(Button) myDialog.findViewById(R.id.yes_button);
                noBtn=(Button) myDialog.findViewById(R.id.button_no);

                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        finishDeployment();
                        myDialog.dismiss();

                    }
                });


                noBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        myDialog.dismiss();

                    }
                });

                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myDialog.show();




            }
        });













    }


    public void fetchFromDatabase(){

        progressDialog.setMessage("Loading details...");
        progressDialog.show();


        final String status = "done";



        DocumentReference docRef = firebaseFirestore.collection("Dispatch_Records").document(emergency_post_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        Toast.makeText(DeploymentDetails.this, "Welcome back", Toast.LENGTH_LONG).show();




                        String person_name = task.getResult().getString("distressed_person");
                        String d_title = task.getResult().getString("distressed_incident");
                        String d_status = task.getResult().getString("dispatch_status");
                        String d_num = task.getResult().getString("distressed_number");
                        String d_desc = task.getResult().getString("distressed_description");
                        String d_uid =task.getResult().getString("distressed_uid");

                        distrUid = d_uid;
                        incident_d = d_title;


                        if(d_status == status){

                            finishButton.setVisibility(View.INVISIBLE);

                        }


                        userName.setText(person_name);
                        titleText.setText(d_title);
                        incidentText.setText(d_title);
                        statusText.setText(d_status);
                        patientPhone.setText(d_num);
                        descriptionText.setText(d_desc);

                        number = d_num;









//19-10-1996
                    } else {
                        Toast.makeText(DeploymentDetails.this, "Document does not exist", Toast.LENGTH_LONG).show();





                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(DeploymentDetails.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }



            }
        });





    }

    public void startNavigation(){

        //start navigation towards the emergency

        Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+","+longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {

            startActivity(mapIntent);

        }



    }



    public void startCall(){


        if(ContextCompat.checkSelfPermission(DeploymentDetails.this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(DeploymentDetails.this,new String[]{Manifest.permission.CALL_PHONE},REQUEST_CALL);

        }else{
            String dial= "tel:" + number;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }

    }


    public void finishDeployment(){

        //update the availability shift record

        String status = "available";
        final String status2 = "done";

        DocumentReference docRef = firebaseFirestore.collection("Daily_Shift").document(user_id);
        final DocumentReference docRef2 = firebaseFirestore.collection("Dispatch_Records").document(emergency_post_id);

// Set the "isCapital" field of the city 'DC'
        docRef
                .update("Status", status)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(DeploymentDetails.this, "Success on Update", Toast.LENGTH_LONG).show();

                        docRef2.update("dispatch_status",status2).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(DeploymentDetails.this, "Success on Update of Dispatch", Toast.LENGTH_LONG).show();


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(DeploymentDetails.this, "Failure on Update of Dispatch", Toast.LENGTH_LONG).show();


                            }
                        });





                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(DeploymentDetails.this, "Failure on Update", Toast.LENGTH_LONG).show();


                    }
                });







    }




    }




