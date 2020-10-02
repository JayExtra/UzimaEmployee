package com.example.uzimaemployee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SingleReport extends AppCompatActivity {

    //importing firebase
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private TextView nameText , genderText , ageText , bpText , tempText , historyText
            , incidentText ,remarksText , arrivalText , departureText , hospitalText;

    private String user_id , deployment_id;
    private ImageView patImage;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_report);

        //firebase setup
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();


        //FETCH DETAILS
        deployment_id = getIntent().getStringExtra("REPORT_ID");

        //Toolbar settings
        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(deployment_id);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SingleReport.this, PcrReports.class));
                finish();
            }
        });



        //setup widgets
        nameText = findViewById(R.id.name_txtvw);
        ageText = findViewById(R.id.age_txtvw);
        genderText = findViewById(R.id.gender_txtvw);
        bpText = findViewById(R.id.bp_txtvw);
        tempText = findViewById(R.id.temp_txtvw);
        historyText = findViewById(R.id.hist_txtvw);
        incidentText = findViewById(R.id.incident_txtvw);
        remarksText = findViewById(R.id.emt_rmks);
        arrivalText = findViewById(R.id.arrival_txtvw);
        departureText = findViewById(R.id.departure_txtvw);
        patImage = findViewById(R.id.user_image);
        mProgressBar = findViewById(R.id.single_report_progress);
        hospitalText = findViewById(R.id.hospital_txtvw);



        mProgressBar.getIndeterminateDrawable().setColorFilter(
                Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);



        //load pcr record
        mProgressBar.setVisibility(View.VISIBLE);
        //Req location
        new Handler().postDelayed(() -> loadDetails(), 2000);








    }


    public void loadDetails(){

        mProgressBar.setVisibility(View.INVISIBLE);


        DocumentReference docRef = firebaseFirestore.collection("Pcr_Reports").document(deployment_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String p_name = task.getResult().getString("patient_name");
                        String p_age = task.getResult().getString("patient_age");
                        String p_gender = task.getResult().getString("patient_gender");
                        String p_image = task.getResult().getString("patient_image");
                        String p_systol = task.getResult().getString("syastol_read");
                        String p_diastol = task.getResult().getString("diastol_read");
                        String p_temperature = task.getResult().getString("temperature");
                        String p_history = task.getResult().getString("historical_illnesses");
                        String p_incident = task.getResult().getString("incident");

                        String p_hospital = task.getResult().getString("hospital");
                        String e_remarks = task.getResult().getString("emt_remarks");
                        Date d_time = task.getResult().getTimestamp("departure_time").toDate();
                        Date a_time = task.getResult().getTimestamp("arrival_time").toDate();


                        String finalBp = p_diastol+"/"+p_systol;

                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");
                        String aDate = formatter.format(Date.parse(String.valueOf(a_time)));

                        SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");
                        String dDate = formatter2.format(Date.parse(String.valueOf(d_time)));


                        nameText.setText(p_name);
                        ageText.setText(p_age);
                        genderText.setText(p_gender);
                        bpText.setText(finalBp);
                        tempText.setText(p_temperature);
                        historyText.setText(p_history);
                        incidentText.setText(p_incident);
                        remarksText.setText(e_remarks);
                        arrivalText.setText(aDate);
                        departureText.setText(dDate);
                        hospitalText.setText(p_hospital);


                        if(p_image!=null){

                            Glide.with(SingleReport.this).load(p_image).into(patImage);

                        }else{

                            patImage.setVisibility(View.GONE);
                        }

//19-10-1996
                    } else {


                        Log.d("Here:", "onComplete: Driver not in the system");




                    }
                } else {

                    Log.d("Here:", "onComplete: Failed to fetch driver");


                }



            }
        });



    }
}
