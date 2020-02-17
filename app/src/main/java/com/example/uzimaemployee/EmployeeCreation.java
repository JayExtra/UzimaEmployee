package com.example.uzimaemployee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class EmployeeCreation extends AppCompatActivity {

    private EditText employeeId;
    private Button confrmButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private ProgressDialog progressDialog;
    String   f_name, s_name,e_role,e_id,e_image,c_id, co_name , e_ambulance,companyID;
    private String user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_creation);

        //setup firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        progressDialog = new ProgressDialog(this);

        //toolbar setup
        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        getSupportActionBar().setTitle("Connect t o profile");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmployeeCreation.this, MainActivity.class));
            }
        });

        //setup widgets

        employeeId  = findViewById(R.id.emp_id);
        confrmButton = findViewById(R.id.confirm_button);



        confrmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //get od first
                getCompanyId();


                //setup details first...
                setupProfile();


            }
        });



    }


    public void setupProfile(){


        String empId = employeeId.getText().toString();

        progressDialog.setMessage("Creating profile...");
        progressDialog.show();




        DocumentReference docRef = firebaseFirestore.collection("Company_Profiles/"+companyID+"/Drivers").document(empId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        Toast.makeText(EmployeeCreation.this, " Creating....", Toast.LENGTH_LONG).show();




                        final String name1 = task.getResult().getString("first_name");
                        final String name2 = task.getResult().getString("second_name");
                        final String empRole = task.getResult().getString("role");
                        final String employeeId = task.getResult().getString("employee_id");
                        final String image2 = task.getResult().getString("drivers_image");
                        String company_id = task.getResult().getString("company");
                        final String ambulance = task.getResult().getString("ambulance");





                        //fetch company details

                        progressDialog.setMessage("Fetching Company details...");
                        progressDialog.show();




                        DocumentReference docRef = firebaseFirestore.collection("Company_Profiles").document(company_id);
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {

                                        Toast.makeText(EmployeeCreation.this, "Company details fetched", Toast.LENGTH_LONG).show();

                                        String company_name = task.getResult().getString("company_name");



                                        Map<String, Object> userMap= new HashMap<>();

                                        userMap.put("image",image2);
                                        userMap.put("first_name",name1);
                                        userMap.put("second_name",name2);
                                        userMap.put("employee_id",employeeId);
                                        userMap.put("employee_role:",empRole);
                                        userMap.put("company",company_name);
                                        userMap.put("ambulance",ambulance);




                                        firebaseFirestore.collection("Employee_Details").document(user_id)
                                                .set(userMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(EmployeeCreation.this,"Your details were created successfully",Toast.LENGTH_LONG).show();

                                                        startActivity(new Intent(EmployeeCreation.this,EmployeeProfile.class));

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                        Toast.makeText(EmployeeCreation.this,"FIRESTORE ERROR: COULD NOT FETCH IMAGE",Toast.LENGTH_LONG).show();

                                                        finish();

                                                    }


                                                });






//19-10-1996
                                    } else {



                                    }
                                } else {

                                    String error = task.getException().getMessage();
                                    Toast.makeText(EmployeeCreation.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                                }



                            }
                        });












//19-10-1996
                    } else {

                        Toast.makeText(EmployeeCreation.this, "Error: Seems like your profile is not with us, please contact your company", Toast.LENGTH_LONG).show();



                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(EmployeeCreation.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }



            }
        });






    }


    public void getCompanyId(){



        Query firstQuery = firebaseFirestore.collection("Company_Profiles");

        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {



                for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){

                    if(doc.getType() == DocumentChange.Type.ADDED){

                        String companyId = doc.getDocument().getId();

                        companyID = companyId;

                        Toast.makeText(EmployeeCreation.this,"id:"+companyID,Toast.LENGTH_LONG).show();






                    }

                }



            }
        });
    }



}

