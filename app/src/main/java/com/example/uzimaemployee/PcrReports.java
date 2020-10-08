package com.example.uzimaemployee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.example.uzimaemployee.Constants.Report;
import com.example.uzimaemployee.Constants.ReportRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class PcrReports extends AppCompatActivity {

    private RecyclerView reportRecycler;
    private ReportRecyclerAdapter mReportRecyclerAdapter;

    private List<Report> reports_list;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    private ProgressBar mProgressBar;

    private String user_id , driver_id;


    //importing firebase
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcr_reports);


        //firebase setup
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //Toolbar settings
        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Pcr Reports");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PcrReports.this, MainActivity.class));
                finish();
            }
        });


        reports_list = new ArrayList<>();
        reportRecycler =findViewById(R.id.reports_adapter);
        mReportRecyclerAdapter = new ReportRecyclerAdapter(reports_list);
        reportRecycler.setLayoutManager(new LinearLayoutManager(this));
        reportRecycler.setAdapter(mReportRecyclerAdapter);

        mProgressBar=findViewById(R.id.load_reports_progress);




        //fetch driver id
        fetchDriverId();

        mProgressBar.setVisibility(View.VISIBLE);
        //Req location
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               loadFirstPosts();
            }
        }, 5000);



        reportRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        reportRecycler.setVisibility(View.VISIBLE);



        //retrieve firebase posts


        Query firstQuery = firebaseFirestore.collection("Pcr_Reports")
                .whereEqualTo("employee_id" , driver_id)
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



                        Report mReport = doc.getDocument().toObject(Report.class);

                        if(isFirstPageFirstLoad) {

                            reports_list.add(mReport);

                        }else{

                            reports_list.add(0,mReport);

                        }

                        mReportRecyclerAdapter.notifyDataSetChanged();


                    }

                }

                isFirstPageFirstLoad = false;

            }
        });
    }

    public void loadMorePosts(){

        //retrieve firebase posts

        Query nextQuery = firebaseFirestore.collection("Pcr_Reports")
                .whereEqualTo("employee_id" , driver_id)
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


                            Report mReport = doc.getDocument().toObject(Report.class);
                            reports_list.add(mReport);

                            mReportRecyclerAdapter.notifyDataSetChanged();


                        }

                    }

                }

            }
        });



    }

    public void fetchDriverId(){


        DocumentReference docRef = firebaseFirestore.collection("Employee_Details").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String driverId = task.getResult().getString("employee_id");

                        driver_id = driverId;

                        Log.d("Here:", "onComplete:"+driverId);

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
