package com.example.uzimaemployee;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PcrReport extends AppCompatActivity implements  AdapterView.OnItemSelectedListener {
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String user_id;
    private ImageView userImage;
    private TextView nameText, ageText, genderText;
    private Button subButton;
    private FloatingActionButton floatRecord, floatClock;
    private Spinner spinnerDiastol,spinnerSystol , spinnerHistory;
    private EditText tempText, emtRemarks , emtHospital;
    private ProgressDialog progressDialog;
    String distressed_id , diastol_read , systol_read, history_read,deployment_id,incident_D, patient , patAge , patGender ,hospital_taken;
    String ambulance,role,writer,employeeId;

    Bitmap bmp , scaleBmp;
    int pageWidth = 1200;

    Date dateObj;

    DateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcr_report);

        //firebase setup
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        progressDialog=new ProgressDialog(this);

        //toolbar setup
        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("PCR");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PcrReport.this, DeploymentDetails.class));
            }
        });

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);


        Intent intent = getIntent();
//get the attached extras from the intent
//we should use the same key as we used to attach the data.
        distressed_id = intent.getStringExtra("DISTRESSED_ID");
        deployment_id = intent.getStringExtra("DEPLOYMENT_ID");
        incident_D = intent.getStringExtra("INCIDENT");


        //Map out Widgets
        nameText = findViewById(R.id.patient_name);
        ageText = findViewById(R.id.patient_age);
        genderText = findViewById(R.id.patient_gender);
        subButton = findViewById(R.id.send_btn);
        floatClock = findViewById(R.id.clock_arrival);
        floatRecord = findViewById(R.id.record_audio);
        spinnerDiastol = findViewById(R.id.spinner_blood1);
        spinnerSystol = findViewById(R.id.spinner_blood2);
        spinnerHistory = findViewById(R.id.history_spinner);
        tempText = findViewById(R.id.temperature_text);
        emtRemarks = findViewById(R.id.emt_remarks);
        emtHospital = findViewById(R.id.text_hospital);
        userImage = findViewById(R.id.user_image);

        bmp = BitmapFactory.decodeResource(getResources(),R.drawable.uzimalogo);
        scaleBmp = Bitmap.createScaledBitmap(bmp , 165,201,false);

        loadUserDetails();
        fetchCurrentUserDetails();


        ArrayAdapter<CharSequence> diastolAdapter = ArrayAdapter.createFromResource(this,R.array.bp,android.R.layout.simple_spinner_item);
        diastolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDiastol.setAdapter(diastolAdapter);
        spinnerDiastol.setOnItemSelectedListener(this);



        ArrayAdapter<CharSequence> systolAdapter = ArrayAdapter.createFromResource(this,R.array.bp,android.R.layout.simple_spinner_item);
        systolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSystol.setAdapter(systolAdapter);
        spinnerSystol.setOnItemSelectedListener(this);


        ArrayAdapter<CharSequence> historyAdapter = ArrayAdapter.createFromResource(this,R.array.historical_illness,android.R.layout.simple_spinner_item);
        historyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHistory.setAdapter(historyAdapter);
        spinnerHistory.setOnItemSelectedListener(this);


        floatClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clockDeparture();
            }
        });
        floatRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openRecorder = new Intent(PcrReport.this, VoiceRecorder.class);
                openRecorder.putExtra("DISTRESSED_ID",distressed_id);
                openRecorder.putExtra("DISTRESSED_NAME", patient);
                startActivity(openRecorder);
            }
        });

        subButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                sendToDatabase();
                createPDF();
            }
        });






    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

        Spinner spin1 = (Spinner)parent;
        if(spin1.getId() == R.id.spinner_blood1)
        {
            String txt1 = parent.getItemAtPosition(position).toString();
            diastol_read=txt1;

        }

        Spinner spin2 = (Spinner)parent;
        if(spin2.getId() == R.id.spinner_blood2)
        {
            String txt2 = parent.getItemAtPosition(position).toString();
            systol_read=txt2;

        }

        Spinner spin3 = (Spinner)parent;
        if(spin3.getId() == R.id.history_spinner)
        {
            String txt3 = parent.getItemAtPosition(position).toString();
            history_read=txt3;

        }





    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

        Toast.makeText(PcrReport.this,"Please fill the required forms",Toast.LENGTH_SHORT).show();

    }

    public void loadUserDetails(){

        DocumentReference docRef3 = firebaseFirestore.collection("users").document(distressed_id);
        docRef3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name2 = task.getResult().getString("name");
                        String age = task.getResult().getString("user_age");
                        String sex = task.getResult().getString("gender");
                        String image =  task.getResult().getString("image");



                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.user_img);
                        Glide.with(PcrReport.this).setDefaultRequestOptions(placeholderRequest).load(image).into(userImage);



                        nameText.setText(name2);
                        ageText.setText(age);
                        genderText.setText(sex);

                        patient = name2;
                        patAge = age;
                        patGender = sex;


                    } else {

                        Toast.makeText(PcrReport.this,"Does not exist",Toast.LENGTH_SHORT).show();


                    }
                } else {

                    Toast.makeText(PcrReport.this,"Error fetching documents",Toast.LENGTH_SHORT).show();


                }
            }
        });




    }

    public void fetchCurrentUserDetails(){

        DocumentReference docRef3 = firebaseFirestore.collection("Employee_Details").document(user_id);
        docRef3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        //ambulance,role,writer,company,employeeId;

                        ambulance = task.getResult().getString("ambulance");
                        role = task.getResult().getString("employee_role:");
                        writer = task.getResult().getString("first_name");
                        employeeId = task.getResult().getString("employee_id");


                    } else {

                        Toast.makeText(PcrReport.this,"Does not exist",Toast.LENGTH_SHORT).show();


                    }
                } else {

                    Toast.makeText(PcrReport.this,"Error fetching documents",Toast.LENGTH_SHORT).show();


                }
            }
        });


    }


    public void clockDeparture(){



        progressDialog.setMessage("Clocking departure...");
        progressDialog.show();

        final String hospitalTaken = emtHospital.getText().toString();

        hospital_taken = hospitalTaken;



        Map<String, Object> userMap= new HashMap<>();

        userMap.put("patient_name",null);
        userMap.put("patient_image" , null);
        userMap.put("patient_age",null);
        userMap.put("patient_gender",null);
        userMap.put("incident",null);
        userMap.put("diastol_read",null);
        userMap.put("syastol_read",null);
        userMap.put("historical_illnesses",null);
        userMap.put("temperature",null);
        userMap.put("emt_remarks",null);
        userMap.put("departure_time", FieldValue.serverTimestamp());
        userMap.put("arrival_time",null);
        userMap.put("writer",writer);
        userMap.put("role",role);
        userMap.put("employee_id",employeeId);
        userMap.put("ambulance",ambulance);
        userMap.put("hospital",hospitalTaken);





        firebaseFirestore.collection("Pcr_Reports").document(deployment_id)
                .set(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(PcrReport.this,"Ambulance request sent",Toast.LENGTH_LONG).show();

                        informDispatcher(hospitalTaken , writer , employeeId ,deployment_id ,ambulance);




                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();

                        Toast.makeText(PcrReport.this,"FIRESTORE ERROR: COULD NOT SEND",Toast.LENGTH_LONG).show();

                        finish();

                    }


                });



    }

    private void informDispatcher(String hospitalTaken, String writer, String employeeId, String deployment_id, String ambulance) {


        //2.notify dispatcher

        String message2 = " Ambulance plate:"+ambulance+". \n Moving from emergency scene heading towards "+hospitalTaken+"\n Dispatch id: "+deployment_id;
        Map<String , Object> adminNotification = new HashMap<>();
        adminNotification.put("from",user_id);
        adminNotification.put("description", message2);
        adminNotification.put("status" , "on-route");
        adminNotification.put("condition" , "new");
        adminNotification.put("timestamp" , FieldValue.serverTimestamp());

        firebaseFirestore.collection("Dispatcher_Notification").document()
                .set(adminNotification).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(PcrReport.this, "Dispatcher has been informed", Toast.LENGTH_LONG).show();


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(PcrReport.this, "Could not inform dispatcher"+e.getMessage(), Toast.LENGTH_LONG).show();


            }
        });
    }

    public void sendToDatabase(){

        progressDialog.setMessage("sending PCR report...");
        progressDialog.show();





        final String emtRmks =  emtRemarks.getText().toString();
        final String temperatureRead =  tempText.getText().toString();



        DocumentReference docRef = firebaseFirestore.collection("users").document(distressed_id);
        final DocumentReference docRef2 = firebaseFirestore.collection("Pcr_Reports").document(deployment_id);



        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        String name2 = task.getResult().getString("name");
                        String age = task.getResult().getString("user_age");
                        String sex = task.getResult().getString("gender");
                        String image = task.getResult().getString("image");




                        docRef2
                                .update(


                                        "arrival_time",FieldValue.serverTimestamp(),
                                        "diastol_read",diastol_read,
                                        "emt_remarks",emtRmks,
                                        "historical_illnesses",history_read,
                                        "patient_age",age,
                                        "patient_gender",sex,
                                        "patient_image" , image,
                                        "patient_name",name2,
                                        "syastol_read",systol_read,
                                        "temperature",temperatureRead,
                                        "incident",incident_D

                                )

                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        progressDialog.dismiss();

                                        Toast.makeText(PcrReport.this,"Success Yes!!",Toast.LENGTH_LONG).show();

                                        notifyDispatcher(deployment_id);


                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();

                                        Toast.makeText(PcrReport.this,"Failure, check code please!!",Toast.LENGTH_LONG).show();


                                    }
                                });


//19-10-1996
                    } else {

                        progressDialog.dismiss();

                        Toast.makeText(PcrReport.this, "DATA DOES NOT EXISTS,PLEASE CREATE YOUR MEDICAL ID", Toast.LENGTH_LONG).show();



                    }
                } else {

                    progressDialog.dismiss();

                    String error = task.getException().getMessage();
                    Toast.makeText(PcrReport.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }



            }
        });





    }

    private void notifyDispatcher(String deployment_id) {

        //2.notify dispatcher

        String message2 = " Deployment "+deployment_id+" Success. \n Patient has arrived at hospital and handed over";
        Map<String , Object> adminNotification = new HashMap<>();
        adminNotification.put("from",user_id);
        adminNotification.put("description", message2);
        adminNotification.put("status" , "Done");
        adminNotification.put("condition" , "new");
        adminNotification.put("timestamp" , FieldValue.serverTimestamp());

        firebaseFirestore.collection("Dispatcher_Notification").document()
                .set(adminNotification).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(PcrReport.this, "Dispatcher has been informed", Toast.LENGTH_LONG).show();


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(PcrReport.this, "Could not inform dispatcher"+e.getMessage(), Toast.LENGTH_LONG).show();


            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void createPDF(){

        Toast.makeText(PcrReport.this,"Downloading pdf...",Toast.LENGTH_SHORT).show();


        dateObj = new Date();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

            PdfDocument mypdfDocument = new PdfDocument();
            Paint myPaint = new Paint();
            Paint pageTitle = new Paint();

            PdfDocument.PageInfo myPageInfo1 = new PdfDocument.PageInfo.Builder(1200,2010,1).create();
            PdfDocument.Page myPage1 = mypdfDocument.startPage(myPageInfo1);
            Canvas canvas = myPage1.getCanvas();

            canvas.drawBitmap(scaleBmp,0,0,myPaint);

            pageTitle.setTextAlign(Paint.Align.CENTER);
            pageTitle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            pageTitle.setTextSize(70);
            canvas.drawText("Uzima",pageWidth/2,270,pageTitle);

            pageTitle.setTextAlign(Paint.Align.CENTER);
            pageTitle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
            pageTitle.setTextSize(60);
            canvas.drawText("Call: +254759783805",pageWidth/2,500,pageTitle);

            myPaint.setTextAlign(Paint.Align.LEFT);
            myPaint.setTextSize(35f);
            myPaint.setColor(Color.BLACK);
            canvas.drawText("Patient Name:"+ patient,20,590,myPaint);
            canvas.drawText("Patient Age:"+ patAge,20,640,myPaint);
            canvas.drawText("Patient Gender:"+ patGender,20,690,myPaint);

            dateFormat = new SimpleDateFormat("dd/MM/yy");
            canvas.drawText("Date:"+ dateFormat.format(dateObj),pageWidth-20,590,myPaint);

            dateFormat = new SimpleDateFormat("HH/mm/ss");
            canvas.drawText("Time:"+ dateFormat.format(dateObj),pageWidth-20,640,myPaint);

            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setStrokeWidth(2);
            canvas.drawRect(20,780,pageWidth-20,260,myPaint);

            myPaint.setTextAlign(Paint.Align.LEFT);
            myPaint.setStyle(Paint.Style.FILL);
            canvas.drawText("Blood Pressure",40,830,myPaint);
            canvas.drawText("Temperature",200,830,myPaint);
            canvas.drawText("Medical History",700,830,myPaint);

            canvas.drawLine(180,790,180,840,myPaint);
            canvas.drawLine(680,790,680,840,myPaint);
            canvas.drawLine(880,790,880,840,myPaint);
            canvas.drawLine(1030,790,1030,840,myPaint);

            if (spinnerDiastol.getSelectedItemPosition()!= 0){

                canvas.drawText(""+diastol_read+"/"+systol_read,40,950,myPaint);
                canvas.drawText(""+tempText.getText().toString(),200,950,myPaint);
                canvas.drawText(""+history_read,700,950,myPaint);

            }

            canvas.drawLine(680,1200,pageWidth-20,1200, myPaint);
            canvas.drawText("EMT remarks:" ,700,1250,myPaint);
            myPaint.setTextAlign(Paint.Align.RIGHT);

            myPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(emtRemarks.getText().toString(),700,1300,myPaint);
            canvas.drawText("EMT name:" + writer,700,1350,myPaint);
            canvas.drawText("Role:" + role,700,1400,myPaint);
            canvas.drawText("Hospital:" + hospital_taken,700,1450,myPaint);
            canvas.drawText("Ambulance:" + ambulance,700,1500,myPaint);




            mypdfDocument.finishPage(myPage1);


            File file = new File(Environment.getExternalStorageDirectory(),"/"+patient+".pdf");


            try{
                mypdfDocument.writeTo(new FileOutputStream(file));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Toast.makeText(PcrReport.this,"PDF downloaded",Toast.LENGTH_SHORT).show();


            mypdfDocument.close();


        }else{
            Toast.makeText(PcrReport.this,"This feature as available for android KITKAT and higher",Toast.LENGTH_LONG).show();

        }


    }
}

