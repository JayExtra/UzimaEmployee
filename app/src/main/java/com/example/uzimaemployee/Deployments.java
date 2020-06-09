package com.example.uzimaemployee;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;

import com.example.uzimaemployee.Constants.DeploymentRecyclerAdapter;
import com.example.uzimaemployee.Constants.DeploymentsConstructor;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class Deployments extends AppCompatActivity {

    private List<DeploymentsConstructor> deploymentsList;
    private RecyclerView deploymentsRecyclerView;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private Geocoder geocoder;
    private DeploymentRecyclerAdapter deploymentRecyclerAdapter;
    private String user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deployments);

        //firebase setup
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //toolbar setup
        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Deployments");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Deployments.this, MainActivity.class));
                finish();
            }
        });


        //setup widgets
        deploymentsRecyclerView = findViewById(R.id.deployment_recycler);

        //Recycler view setup

        deploymentsList = new ArrayList<>();
        deploymentRecyclerAdapter = new DeploymentRecyclerAdapter(deploymentsList);
        deploymentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deploymentsRecyclerView.setAdapter(deploymentRecyclerAdapter);


        //call method
        firebaseFirestore.collection("Dispatch_Records").whereEqualTo("driver_identity",user_id)
                .addSnapshotListener(Deployments.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if(!queryDocumentSnapshots.isEmpty()){

                    for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){

                        if(doc.getType() == DocumentChange.Type.ADDED){


                            String dispatchId = doc.getDocument().getId();
                            DeploymentsConstructor deployment = doc.getDocument().toObject(DeploymentsConstructor.class).withId(dispatchId);
                            deploymentsList.add(deployment);
                            deploymentRecyclerAdapter.notifyDataSetChanged();


                        }

                    }




                }

            }
        });








    }








}
