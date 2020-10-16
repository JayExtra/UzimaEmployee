package com.example.uzimaemployee;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonIOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {


    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String user_id;
    private Geocoder geocoder;
    private ProgressBar mProgressBar;

    private TextView doneTV , onGoingTV , totalTV ,totalPcr , viewPcr;

    private static final String TAG = "On going:";


    String driverId;
    long millisecons_d;
    long millisecons_a;

    String a , result , result2;

    int highScoreSoFar = 0;
    long b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        //firebase setup
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //toolbar setup
        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Stats");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StatisticsActivity.this, MainActivity.class));
                finish();
            }
        });


        //prepare widgets

        doneTV = findViewById(R.id.done_textview);
        onGoingTV = findViewById(R.id.on_going_text);
        totalTV = findViewById(R.id.total_deployments_text);
        totalPcr = findViewById(R.id.pcr_total_text);
        viewPcr = findViewById(R.id.text_view_reports);

        viewPcr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StatisticsActivity.this , PcrReports.class));
                finish();
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();

        getTotalDispatch();
        getDoneDispatch();
        getOngoingDispatch();
        //fetch driver id
        getDriverId();

    }

    private void getTotalPcrReports() {

        //get total reports
        firebaseFirestore.collection("Pcr_Reports")
                .whereEqualTo("employee_id" ,driverId)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if(error!= null){
                            Toast.makeText(StatisticsActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, error.toString());
                            return;
                        }


                        List<String> documents = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("writer") != null) {
                                documents.add(doc.getString("writer"));
                            }
                        }
                        Log.d(TAG, "Total number of reports: " + documents);

                        int size = documents.size();

                        Log.d(TAG, "Total number of reports: " + size);

                        String dSize = Integer.toString(size);

                        totalPcr.setText(dSize);

                    }
                });

    }

    private void getDriverId() {

        DocumentReference docRef = firebaseFirestore.collection("Employee_Details").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        driverId = task.getResult().getString("employee_id");
                        Log.d(TAG, "Driver Id:" + driverId);

                        getTotalPcrReports();
                        calculateFastDispatch();


                    } else {
                        Log.d(TAG, "No such document");

                        Toast.makeText(StatisticsActivity.this , "This employee does not exist",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());

                    Toast.makeText(StatisticsActivity.this , "get failed with"+task.getException(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getOngoingDispatch() {

        String status = "on-going";


        firebaseFirestore.collection("Dispatch_Records")
                .whereEqualTo("dispatch_status" ,status)
                .whereEqualTo("driver_identity",user_id)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if(error!= null){
                            Toast.makeText(StatisticsActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, error.toString());
                            return;
                        }
                        

                        List<String> documents = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("deployed_driver") != null) {
                                documents.add(doc.getString("deployed_driver"));
                            }
                        }
                        Log.d(TAG, "Current on-going dispatches: " + documents);

                        int size = documents.size();

                        Log.d(TAG, "on-going dispatches size: " + size);

                        String dSize = Integer.toString(size);

                        onGoingTV.setText(dSize);

                    }
                });



    }

    private void getDoneDispatch() {
        String status = "done";


        firebaseFirestore.collection("Dispatch_Records")
                .whereEqualTo("dispatch_status" ,status)
                .whereEqualTo("driver_identity",user_id)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if(error!= null){
                            Toast.makeText(StatisticsActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, error.toString());
                            return;
                        }

                        List<String> documents = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("deployed_driver") != null) {
                                documents.add(doc.getString("deployed_driver"));
                            }
                        }
                        Log.d(TAG, "Current done dispatches: " + documents);

                        int size = documents.size();

                        Log.d(TAG, "Done dispatches size: " + size);

                        String dSize = Integer.toString(size);

                        doneTV.setText(dSize);

                    }
                });
    }

    private void getTotalDispatch() {

        firebaseFirestore.collection("Dispatch_Records")
                .whereEqualTo("driver_identity",user_id)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if(error!= null){
                            Toast.makeText(StatisticsActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, error.toString());
                            return;
                        }

                        List<String> documents = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("deployed_driver") != null) {
                                documents.add(doc.getString("deployed_driver"));
                            }
                        }
                        Log.d(TAG, "Current total dispatches: " + documents);

                        int size = documents.size();

                        Log.d(TAG, "Total dispatches size: " + size);

                        String dSize = Integer.toString(size);

                        totalTV.setText(dSize);

                    }
                });
    }

    private void calculateFastDispatch(){


        firebaseFirestore.collection("Dispatch_Times")
                .whereEqualTo("u_id" , user_id)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if(error!= null){
                            Toast.makeText(StatisticsActivity.this, "Error while calculating!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, error.toString());
                            return;
                        }

                        List<Long> times  = new ArrayList<>();
                        ArrayList depIds = new ArrayList<>();


                        ArrayList calculate = new ArrayList<>();

                        JSONObject data = new JSONObject();

                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("u_id") != null) {

                                depIds.add(doc.getId());

                                String depId = doc.getId();
                              millisecons_d = doc.getTimestamp("departure_time").getSeconds();
                                 millisecons_a = doc.getTimestamp("arrival_time").getSeconds();

                                 long t_difference = millisecons_a - millisecons_d;

                                 long minutes = t_difference/60;


                                times.add(minutes);


                                try{

                                    data.put(a = depId ,b = minutes);


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                                calculate.add(data);


                            }




                            //Log.d(TAG,"Time diferences are:" + times);
                            //Log.d(TAG,"Deployment ids are:" + depIds);

                            Log.d(TAG,"Final:" + calculate);


                        }

                    }
                });







    }
}
