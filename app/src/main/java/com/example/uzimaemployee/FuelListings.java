package com.example.uzimaemployee;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uzimaemployee.Constants.DeploymentRecyclerAdapter;
import com.example.uzimaemployee.Constants.DeploymentsConstructor;
import com.example.uzimaemployee.Constants.Fuels;
import com.example.uzimaemployee.Constants.FuelsRecyclerAdapter;
import com.example.uzimaemployee.Constants.Report;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class FuelListings extends AppCompatActivity {

    private List<Fuels> fuelsList;
    private RecyclerView fuelsRecyclerView;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String user_id;
    private FuelsRecyclerAdapter mFuelsRecyclerAdapter;
    private ProgressBar mProgressBar;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_listings);

        //firebase setup
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();


        //toolbar setup
        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Fuel Entries");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FuelListings.this, FuelServicing.class));
                finish();
            }
        });



        //setup widgets
        fuelsRecyclerView = findViewById(R.id.fuel_list_rv);
        mProgressBar = findViewById(R.id.fuel_progress_bar);

        //Recycler view setup

        fuelsList = new ArrayList<>();
        mFuelsRecyclerAdapter = new FuelsRecyclerAdapter(fuelsList);
        fuelsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fuelsRecyclerView.setAdapter(mFuelsRecyclerAdapter);





        mProgressBar.setVisibility(View.VISIBLE);
        //Req location
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadFirstPosts();
            }
        }, 2000);



        fuelsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom =  !recyclerView.canScrollVertically(1);

                if (reachedBottom){

                    //String desc = lastVisible.getString("title");
                    // Toast.makeText(PcrReports.this,"WE have reaced bottom of" + desc,Toast.LENGTH_SHORT).show();

                    loadMorePosts();


                }
            }
        });


    }

    public void loadFirstPosts(){

        mProgressBar.setVisibility(View.INVISIBLE);
        fuelsRecyclerView.setVisibility(View.VISIBLE);



        //retrieve firebase posts


        Query firstQuery = firebaseFirestore.collection("Fuel_Entries")
                .whereEqualTo("driver_id" , user_id)
                .limit(3);
        //.orderBy("arrival_time",Query.Direction.DESCENDING)
        // .limit(3);

        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (isFirstPageFirstLoad) {


                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                }


                for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){

                    if(doc.getType() == DocumentChange.Type.ADDED){



                        Fuels fuels = doc.getDocument().toObject(Fuels.class);

                        if(isFirstPageFirstLoad) {

                            fuelsList.add(fuels);

                        }else{

                            fuelsList.add(0,fuels);

                        }

                        mFuelsRecyclerAdapter.notifyDataSetChanged();


                    }

                }

                isFirstPageFirstLoad = false;

            }
        });
    }


    public void loadMorePosts(){

        //retrieve firebase posts

        Query nextQuery = firebaseFirestore.collection("Fuel_Entries")
                .whereEqualTo("driver_id" , user_id)
                //.orderBy("arrival_time",Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);

        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if(!queryDocumentSnapshots.isEmpty()){

                    //start here
                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                    for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){

                        if(doc.getType() == DocumentChange.Type.ADDED){


                            Fuels fuels = doc.getDocument().toObject(Fuels.class);
                            fuelsList.add(fuels);

                            mFuelsRecyclerAdapter.notifyDataSetChanged();


                        }

                    }

                }

            }
        });



    }
}
