package com.example.uzimaemployee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private ImageView mainImage;
    private TextView usernameTxt, roleText;
    private Uri mainImageURI=null;
    private FloatingActionButton clockFloat;
    Dialog myDialog;
    Button buttonYes;
    TextView messagetXt;
    Spinner team, shift;
    public Context context;
    String shiftTxt, teamTxt, employeeName,employeeID, statusData,shiftData,teamData;
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
        roleText = findViewById(R.id.main_role);
        clockFloat = findViewById(R.id.floating_clock);


        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));


        //check if user account is setup
        checkUserData();


        //clock in process
        startClockin();

        clockFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start clock in process

                startClockin();
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




                        String name2 = task.getResult().getString("name");
                        employeeName = name2;
                        employeeID = task.getResult().getString("employee_id");
                        String role = task.getResult().getString("role");
                        String image2 = task.getResult().getString("image");


                        mainImageURI = Uri.parse(image2);

                        //******replacing the dummy image with real profile picture******

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.user_img);

                        Glide.with(MainActivity.this).setDefaultRequestOptions(placeholderRequest).load(image2).into(mainImage);


                        usernameTxt.setText(name2);
                        roleText.setText(role);




//19-10-1996
                    } else {


                        //launch dialog
                        myDialog.setContentView(R.layout.popup2);
                        messagetXt =  (TextView) myDialog.findViewById(R.id.message_text);

                        //messagetXt.setText("It seems that your details are not in our system, please proceed to setup your profile");
                        buttonYes=(Button) myDialog.findViewById(R.id.button_proceed);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                               startActivity(new Intent(MainActivity.this,EditEmployeeProfile.class));
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
                        shift = (Spinner)myDialog.findViewById(R.id.shift_spinner);
                        team = (Spinner)myDialog.findViewById(R.id.team_spinner);

                        //spinner adapters

                        ArrayAdapter<CharSequence> shiftAdapter = ArrayAdapter.createFromResource(myDialog.getContext(),R.array.shift,android.R.layout.simple_spinner_item);
                        shiftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        shift.setAdapter(shiftAdapter);

                        shift.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

                                String txt = parent.getItemAtPosition(position).toString();

                                shiftTxt = txt;

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                                Toast.makeText(MainActivity.this,"  Error: Please select your current shift",Toast.LENGTH_LONG).show();

                            }
                        });


                        ArrayAdapter<CharSequence> teamAdapter = ArrayAdapter.createFromResource(myDialog.getContext(),R.array.team,android.R.layout.simple_spinner_item);
                        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        team.setAdapter(teamAdapter);

                        team.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                                String txt2 = parent.getItemAtPosition(position).toString();
                                teamTxt = txt2;

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                                Toast.makeText(MainActivity.this,"  Error: Please select your current team",Toast.LENGTH_LONG).show();


                            }
                        });





                        buttonYes=(Button) myDialog.findViewById(R.id.button_proceed);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String status = "available";

                                Map<String, Object> shiftMap= new HashMap<>();

                                shiftMap.put("employee_name",employeeName);
                                shiftMap.put("employee_id",employeeID);
                                shiftMap.put("shift",shiftTxt);
                                shiftMap.put("team",teamTxt);
                                shiftMap.put("time_in", FieldValue.serverTimestamp());
                                shiftMap.put("time_out",null);
                                shiftMap.put("user_id",user_id);
                                shiftMap.put("Status",status);

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

                         shiftData = task.getResult().getString("shift");
                         teamData = task.getResult().getString("team");
                         timeinData = task.getResult().getTimestamp("time_in");
                         timeoutData = task.getResult().getTimestamp("time_out");
                         statusData = task.getResult().getString("Status");


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


        shiftMap.put("name", employeeName);
        shiftMap.put("employee_id", employeeID);
        shiftMap.put("Status",statusData);
        shiftMap.put("time_in", timeinData);
        shiftMap.put("time_out", timeoutData);
        shiftMap.put("shift", shiftData);
        shiftMap.put("team", teamData);
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

}
