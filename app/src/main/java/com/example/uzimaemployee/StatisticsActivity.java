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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {


    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String user_id;
    private Geocoder geocoder;
    private ProgressBar mProgressBar;

    private TextView doneTV , onGoingTV , totalTV;

    private static final String TAG = "On going:";


    String driverId;

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


    }


    @Override
    protected void onStart() {
        super.onStart();

        getTotalDispatch();
        getDoneDispatch();
        getOngoingDispatch();

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
}
