package com.example.uzimaemployee;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class DeploymentDetails extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String user_id;
    private Geocoder geocoder;
    private ProgressBar mProgressBar;

    private TextView titleText, incidentText , patientPhone, callNumber, descriptionText , userName , depStatus;
    private Button navButton , pcrButton,finishButton , sendArrivalBtn;
    private static final int REQUEST_CALL =1;
    String incident_d;

    private static final String TAG = "Deployment Details";

    String dUi , descText , dPerson;


    private String emergency_post_id , latitude , longitude;
    String number, distrUid;
    private ProgressDialog progressDialog;
    Dialog myDialog;
    Button yesBtn, noBtn;

    Handler handler = new Handler();
    Runnable runnable;

    int delay = 3*1000; //Delay for 10 seconds.  One second = 1000 milliseconds.

    private ListenerRegistration deploymentListener;



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
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

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
        incidentText =findViewById(R.id.incident_text);
        patientPhone =findViewById(R.id.phone_number);
        callNumber =findViewById(R.id.call_number);
        descriptionText =findViewById(R.id.description_text);
        navButton =findViewById(R.id.navigate_button);
        pcrButton =findViewById(R.id.pcr_button);
        finishButton =findViewById(R.id.button_finish);
        userName =findViewById(R.id.user_name);
        depStatus = findViewById(R.id.text_status);
        mProgressBar = findViewById(R.id.details_progressbar);

        sendArrivalBtn = findViewById(R.id.btn_arrival);

        //FETCH DETAILS
        emergency_post_id = getIntent().getStringExtra("DOCUMENT_ID");
        latitude = getIntent().getStringExtra("LATITUDE");
        longitude = getIntent().getStringExtra("LONGITUDE");

        //fetch details
        //fetchFromDatabase();

        //checkDeploymentStatus();

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



        sendArrivalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                sendNotification();
                updateTime();
            }
        });

        


    }

    private void updateTime() {

        DocumentReference dispRef = firebaseFirestore.collection("Dispatch_Times").document(emergency_post_id);

// Set the "isCapital" field of the city 'DC'
        dispRef
                .update("arrival_time", FieldValue.serverTimestamp(),
                        "u_id" , user_id
                )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Time successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);

                        Toast.makeText(DeploymentDetails.this , "Error updating document"+e,Toast.LENGTH_SHORT).show();
                    }
                });


    }

    @Override
    protected void onStart() {
        super.onStart();

        progressDialog.setMessage("Loading details...");
        progressDialog.show();

        firebaseFirestore.collection("Dispatch_Records").document(emergency_post_id).addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {


                }
                if (documentSnapshot.exists()) {

                    progressDialog.dismiss();



                    String person_name = documentSnapshot.getString("distressed_person");
                    String d_title =  documentSnapshot.getString("distressed_incident");
                    String d_status = documentSnapshot.getString("dispatch_status");
                    String d_num =  documentSnapshot.getString("distressed_number");
                    String d_desc =  documentSnapshot.getString("distressed_description");
                    String d_uid = documentSnapshot.getString("distressed_uid");

                    userName.setText(person_name);
                    titleText.setText(d_title);
                    incidentText.setText(d_title);
                    depStatus.setText(d_status);
                    patientPhone.setText(d_num);
                    descriptionText.setText(d_desc);

                    number = d_num;

                    distrUid = d_uid;
                    incident_d = d_title;
                    descText = d_desc;
                    dPerson = person_name;


                } else{

                    Toast.makeText(DeploymentDetails.this, "Document doesn't exist!", Toast.LENGTH_SHORT).show();


                }
            }
        });
    }

    @Override
    protected void onResume() {
        //start handler as activity become visible

        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                //do something

                checkDeploymentStatus();

                handler.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

// If onPause() is not included the threads will double up when you
// reload the activity

    @Override
    protected void onPause() {
       handler.removeCallbacks(runnable);//stop handler when activity not visible
        super.onPause();
    }

    private void checkDeploymentStatus() {

        DocumentReference dcRef = firebaseFirestore.collection("Dispatch_Records").document(emergency_post_id);
        dcRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    DocumentSnapshot document = task.getResult();

                    if(document.exists()){

                        String status = task.getResult().getString("dispatch_status");

                        String status2 = "done";

                        if(status.equals(status2)){

                            Log.d("HERE:", "onComplete: It is equal.Check done");
                            finishButton.setVisibility(View.INVISIBLE);
                            sendArrivalBtn.setVisibility(View.INVISIBLE);
                            pcrButton.setVisibility(View.INVISIBLE);
                            navButton.setVisibility(View.INVISIBLE);

                        }else{

                            Log.d("HERE:", "onComplete: It is not equal.Check done");
                            finishButton.setVisibility(View.VISIBLE);
                            sendArrivalBtn.setVisibility(View.VISIBLE);
                        }

                    }else{

                        Toast.makeText(DeploymentDetails.this , "Sorry , this dispatch has expired or does not exist",Toast.LENGTH_SHORT).show();

                    }


                }else{

                    Toast.makeText(DeploymentDetails.this , "Could not perform checks on dispatch",Toast.LENGTH_SHORT).show();

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(DeploymentDetails.this , "Error:" + e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void sendNotification() {

       //check the id before sending notification. Id may be that of dispatcher
        DocumentReference dcRef = firebaseFirestore.collection("Employee_Details").document(distrUid);
        dcRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {

                                               /* Log.d(TAG, "onComplete email: " + task.getResult().getString("email"));
                                                Log.d(TAG, "onComplete name: " + task.getResult().getString("first_name"));

                                                Log.d("Id check:", "onComplete: "+d_uid);*/

                        Toast.makeText(DeploymentDetails.this, "Dispatcher id identified", Toast.LENGTH_LONG).show();


                        //sends notificaion to admin only if dispatch information is from admin

                        sendToAdminOnly();



                    } else {

                        Toast.makeText(DeploymentDetails.this, "User id identified", Toast.LENGTH_LONG).show();

                        //this will send notification to user and admin

                        sendToAdminUser();



                    }


                } else {


                    Toast.makeText(DeploymentDetails.this, "Could not identify if user or admin", Toast.LENGTH_LONG).show();
                    mProgressBar.setVisibility(View.INVISIBLE);


                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(DeploymentDetails.this , "Error at identification:"+e.getMessage(),Toast.LENGTH_LONG).show();
                mProgressBar.setVisibility(View.INVISIBLE);

            }
        });
    }

    private void sendToAdminUser() {

        //2.notify dispatcher

        String message2 = " Arrived at emergency scene.\n Dispatch id: "+emergency_post_id+"\n Distressed individual:"+dPerson;
        Map<String , Object> adminNotification = new HashMap<>();
        adminNotification.put("from",user_id);
        adminNotification.put("description", message2);
        adminNotification.put("status" , "arrived");
        adminNotification.put("condition" , "new");
        adminNotification.put("timestamp" , FieldValue.serverTimestamp());

        firebaseFirestore.collection("Dispatcher_Notification").document()
                .set(adminNotification).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(DeploymentDetails.this, "Dispatcher has been informed", Toast.LENGTH_LONG).show();

                //1.NotifyUser

                String myMessage = "I have arrived";
                Map<String, Object> userNotification = new HashMap<>();
                userNotification.put("from", user_id);
                userNotification.put("message", myMessage);
                userNotification.put("timestamp",FieldValue.serverTimestamp());


                firebaseFirestore.collection("users/"+distrUid+"/Notifications").document()
                        .set(userNotification).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(DeploymentDetails.this, "User has been informed", Toast.LENGTH_LONG).show();

                        mProgressBar.setVisibility(View.INVISIBLE);




                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(DeploymentDetails.this, "Could not inform user"+e.getMessage(), Toast.LENGTH_LONG).show();
                        mProgressBar.setVisibility(View.INVISIBLE);

                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(DeploymentDetails.this, "Could not inform dispatcher"+e.getMessage(), Toast.LENGTH_LONG).show();
                mProgressBar.setVisibility(View.INVISIBLE);

            }
        });



    }

    private void sendToAdminOnly() {

        //2.notify dispatcher

        String message2 = " Arrived at emergency scene.\n Dispatch id: "+emergency_post_id+"\n Distressed individual:"+dPerson;
        Map<String , Object> adminNotification = new HashMap<>();
        adminNotification.put("from",user_id);
        adminNotification.put("description", message2);
        adminNotification.put("status" , "arrived");
        adminNotification.put("condition" , "new");
        adminNotification.put("timestamp" , FieldValue.serverTimestamp());

        firebaseFirestore.collection("Dispatcher_Notification").document()
                .set(adminNotification).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(DeploymentDetails.this, "Dispatcher has been informed", Toast.LENGTH_LONG).show();
                mProgressBar.setVisibility(View.INVISIBLE);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(DeploymentDetails.this, "Could not inform dispatcher"+e.getMessage(), Toast.LENGTH_LONG).show();
                mProgressBar.setVisibility(View.INVISIBLE);


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
                        depStatus.setText(d_status);
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




