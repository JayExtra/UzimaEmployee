package com.example.uzimaemployee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
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
import com.example.uzimaemployee.LocationService.LocationService;
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
import com.google.firebase.firestore.GeoPoint;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private ImageView mainImage;
    private ImageView backImage;
    private TextView usernameTxt, roleText, secondName ;
    private Uri mainImageURI=null;
    private FloatingActionButton clockFloat;
    private CardView ambCard;
    private Button mPdf , mAudio;

    String empId;


    private static final int REQUEST_LOCATION = 1;

    private LocationManager locationManager;
    String latitude, longitude;
    Dialog myDialog ,myDialog2 , myDialog3;
    Button buttonYes , buttonShare , buttonOk;
    TextView messagetXt ,documentPathText , warningText;
    //Spinner team, shift;
    public Context context;
    String firstName, ambulanceData,companyData,employeeId,userID, employeeName,employeeID, statusData,shiftData,teamData;
    Timestamp timeinData,timeoutData;

    String filePath;

    private int FILE_PICKER_REQUEST_CODE = 1000;

    double lat , lng;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 20*1000;

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
        myDialog2=new Dialog(this);
        myDialog3=new Dialog(this);

        mainImage = findViewById(R.id.main_activity_image);
        usernameTxt = findViewById(R.id.main_name);
         secondName = findViewById(R.id.second_name);
        roleText = findViewById(R.id.main_role);
        clockFloat = findViewById(R.id.floating_clock);
        ambCard = findViewById(R.id.ambulance_card);
        mPdf = findViewById(R.id.share_pdf);
        mAudio = findViewById(R.id.share_audio);


        //Request permission to access user location
        if(Build.VERSION.SDK_INT >= 23){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //Request Location
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION} , 1 );


            }else{
                //Req location
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startService();
                        getDriverId();
                    }
                }, 2000);

            }
        }else{
            //start the location service
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startService();
                    getDriverId();
                }
            }, 3000);
        }
        //getUserLocation();






        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        !=PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1001);
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());



        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));


        //check if user account is setup
        checkUserData();






        clockFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start clock in process

                startClockin();
            }
        });

        ambCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this ,Deployments.class));
                finish();
            }
        });

        mAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //shareAudio();
                openFilePicker();

            }
        });

        mPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //open file picker
                openFilePicker();

            }
        });




    }


    @Override
    protected void onResume() {
        //start handler as activity become visible

        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                //do something

                checkSuspensionDriver();

                handler.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

// If onPause() is not included the threads will double up when you
// reload the activity

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }


    private void checkSuspensionDriver(){






        DocumentReference docRef = firebaseFirestore.collection("Suspended_Employees").document(empId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        deleteShift();

                        String reason = task.getResult().getString("reason");

                        //String message = "Your account has been temporarily suspended. "+reason;

                        myDialog3.setContentView(R.layout.warning_dialog);

                        warningText = myDialog3.findViewById(R.id.warning_message_txt);
                        buttonOk = myDialog3.findViewById(R.id.button_ok_dialog);

                        warningText.setText(reason);


                        buttonOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                myDialog3.dismiss();
                            }
                        });




                        myDialog3.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        myDialog3.show();



                    } else {

                        Toast.makeText(MainActivity.this, "This user doesn't exist", Toast.LENGTH_SHORT).show();

                    }
                } else {

                    Toast.makeText(MainActivity.this, "Failed to retrieve id", Toast.LENGTH_SHORT).show();

                }
            }
        });









    }

    private void deleteShift() {

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


    private void getDriverId(){

        DocumentReference docRef = firebaseFirestore.collection("Employee_Details").document(user_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String employeeId = task.getResult().getString("employee_id");
                        empId = employeeId;

                    } else {

                        Toast.makeText(MainActivity.this, "This user doesn't exist", Toast.LENGTH_SHORT).show();

                    }
                } else {

                    Toast.makeText(MainActivity.this, "Failed to retrieve id", Toast.LENGTH_SHORT).show();

                }
            }
        });







    }

    private void shareAudio() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
                +  File.separator + "com.example.uzimaemployee" + File.separator);
        intent.setDataAndType(uri, "*/*");
        startActivity(Intent.createChooser(intent, "Open folder"));
    }

    private void sharePdf(){

        //launch share dialog

        //launch dialog
        myDialog2.setContentView(R.layout.share_dialog);
        documentPathText =  (TextView) myDialog2.findViewById(R.id.document_path);
        backImage = myDialog2.findViewById(R.id.back_arrow);

        documentPathText.setText(filePath);

        buttonShare=(Button) myDialog2.findViewById(R.id.share_button);

        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               //share pdf

                //Uri path = FileProvider.getUriForFile(MainActivity.this, "com.example.uzimaemployee.fileprovider", new File(filePath));

                File file = new File(filePath);

                if(!file.exists()){

                    Toast.makeText(MainActivity.this , "This file does not exist" , Toast.LENGTH_SHORT).show();
                    return;

                }

                Intent shareIntent = new Intent();
                shareIntent.setType("doc/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+file));
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Pdf file for patient.");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share..."));


            }
        });

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog2.dismiss();
            }
        });

        myDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog2.show();


    }


    private void openFilePicker() {

         new MaterialFilePicker()
                // Pass a source of context. Can be:
                //    .withActivity(Activity activity)
                //    .withFragment(Fragment fragment)
                //    .withSupportFragment(androidx.fragment.app.Fragment fragment)
                .withActivity(MainActivity.this)
                // With cross icon on the right side of toolbar for closing picker straight away
                //.withCloseMenu(true)
                // Showing hidden files
                .withHiddenFiles(true)
                // Want to choose only jpg images
                //.withFilter(Pattern.compile(".*\\.pdf$"))
                // Don't apply filter to directories names
                //.withFilterDirectories(false)

                .withRequestCode(FILE_PICKER_REQUEST_CODE)
                .start();
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




                        String f_name = task.getResult().getString("first_name");
                        String s_name = task.getResult().getString("second_name");
                        String e_id = task.getResult().getString("employee_id");
                        String e_role = task.getResult().getString("employee_role:");
                        String company = task.getResult().getString("company");
                        String ambulance = task.getResult().getString("ambulance");
                        String image2 = task.getResult().getString("image");

                        secondName.setText(s_name);
                        usernameTxt.setText(f_name);
                        roleText.setText(e_role);

                        if (image2 == null){

                            RequestOptions placeholderRequest = new RequestOptions();
                            placeholderRequest.placeholder(R.drawable.user_img);

                        }else{

                            mainImageURI = Uri.parse(image2);

                            //******replacing the dummy image with real profile picture******

                            RequestOptions placeholderRequest = new RequestOptions();
                            placeholderRequest.placeholder(R.drawable.user_img);

                            Glide.with(MainActivity.this).setDefaultRequestOptions(placeholderRequest).load(image2).into(mainImage);


                        }


//19-10-1996
                    } else {


                        //launch dialog
                        myDialog.setContentView(R.layout.popup2);
                        messagetXt =  (TextView) myDialog.findViewById(R.id.message_text);
                       // String messsage = "It seems that your details are not in our system, please proceed to setup your profile";

                       // messagetXt.setText(messsage);
                        buttonYes=(Button) myDialog.findViewById(R.id.button_proceed);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                               startActivity(new Intent(MainActivity.this,EmployeeCreation.class));
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

            case R.id.logout:
                progressDialog.setMessage("Signing out...");
                progressDialog.show();

                Map<String , Object> tokenMapRemove = new HashMap<>();
                tokenMapRemove.put("token_id" ,FieldValue.delete());

                firebaseFirestore.collection("Employee_Details").document(user_id).update(tokenMapRemove).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mAuth.signOut();
                        finish();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(MainActivity.this,"Error" + e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });

                return true;

            case R.id.item1:

                startActivity(new Intent(MainActivity.this, EmployeeProfile.class ));
                finish();
                Toast.makeText(MainActivity.this,"Loading profile...",Toast.LENGTH_SHORT).show();
                return true;


            case R.id.item2:

                startActivity(new Intent(MainActivity.this, Notifications.class ));
                finish();
                Toast.makeText(MainActivity.this,"Fetching notifications...",Toast.LENGTH_SHORT).show();

                return true;


            case R.id.contact_sup:
                //Toast.makeText(this, "contact was selected", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this , ContactSupport.class));
                finish();
                return true;

            case R.id.pcr_reports:
                startActivity(new Intent(MainActivity.this , PcrReports.class));
                return true;


            case R.id.my_stats:
                startActivity(new Intent(MainActivity.this , StatisticsActivity.class));
                return true;

            case R.id.my_vehicle:
                startActivity(new Intent(MainActivity.this , FuelServicing.class));
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

                        Toast.makeText(MainActivity.this, "Shift already clocked in", Toast.LENGTH_LONG).show();

                        myDialog.dismiss();


//19-10-1996
                    } else {


                        //launch dialog
                        myDialog.setContentView(R.layout.popup);
                        messagetXt =  (TextView) myDialog.findViewById(R.id.message_text);
                        //shift = (Spinner)myDialog.findViewById(R.id.shift_spinner);
                        //team = (Spinner)myDialog.findViewById(R.id.team_spinner);







                        buttonYes=(Button) myDialog.findViewById(R.id.button_proceed);

                        buttonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                 lat =  Double.parseDouble(latitude);
                                 lng =  Double.parseDouble(longitude);

                                final GeoPoint geoPoint=new GeoPoint(lat,lng);



                                DocumentReference docRef = firebaseFirestore.collection("Employee_Details").document(user_id);
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            progressDialog.dismiss();
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {

                                                String f_name = task.getResult().getString("first_name");
                                                String s_name = task.getResult().getString("second_name");
                                                String e_id = task.getResult().getString("employee_id");
                                                String e_role = task.getResult().getString("employee_role:");
                                                String ambulance = task.getResult().getString("ambulance");
                                                String phnNum = task.getResult().getString("phone_number");





                                                String status = "available";

                                                Map<String, Object> shiftMap= new HashMap<>();

                                                shiftMap.put("first_name",f_name);
                                                shiftMap.put("second_name",s_name);
                                                shiftMap.put("employee_id",e_id);
                                                shiftMap.put("role",e_role);
                                                shiftMap.put("ambulance",ambulance);
                                                shiftMap.put("time_in", FieldValue.serverTimestamp());
                                                shiftMap.put("time_out",null);
                                                shiftMap.put("user_id",user_id);
                                                shiftMap.put("Status",status);
                                                shiftMap.put("Location",geoPoint);
                                                shiftMap.put("phone_number" , phnNum);

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



//19-10-1996
                                            } else {

                                                Toast.makeText(MainActivity.this, "DATA DOES NOT EXISTS,PLEASE CREATE YOUR MEDICAL ID", Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(MainActivity.this,EditEmployeeProfile.class));



                                            }
                                        } else {

                                            String error = task.getException().getMessage();
                                            Toast.makeText(MainActivity.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                                        }



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

                         firstName = task.getResult().getString("first_name");
                         employeeId = task.getResult().getString("employee_id");
                         timeinData = task.getResult().getTimestamp("time_in");
                         timeoutData = task.getResult().getTimestamp("time_out");
                         statusData = task.getResult().getString("Status");
                         companyData = task.getResult().getString("company");
                         ambulanceData = task.getResult().getString("ambulance");
                         userID = task.getResult().getString("user_id");


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


        shiftMap.put("name", firstName);
        shiftMap.put("employee_id", employeeId);
        shiftMap.put("Status",statusData);
        shiftMap.put("time_in", timeinData);
        shiftMap.put("time_out", timeoutData);
        shiftMap.put("ambulance", ambulanceData);
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


    void startService(){
        LocationBroadcastReceiver receiver = new LocationBroadcastReceiver();
        IntentFilter filter = new IntentFilter("ACTION_LOC");
        registerReceiver(receiver , filter);

        Intent intent = new Intent(MainActivity.this , LocationService.class);
        startService(intent);
    }

    public class LocationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("ACTION_LOC")){

                lat = intent.getDoubleExtra("latitude" , 0f);
                lng = intent.getDoubleExtra("longitude" , 0f);

                longitude=Double.toString(lng);
                latitude = Double.toString(lat);

                Toast.makeText(MainActivity.this ,  "Help! Location:.\nLatitude:" +latitude+ "\nLongitude:" +longitude ,Toast.LENGTH_SHORT).show();


            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            // Do anything with file
           Toast.makeText(MainActivity.this , "Path:"+filePath ,Toast.LENGTH_LONG).show();
           sharePdf();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       switch (requestCode){


           case 1001:{


               if(grantResults[0]==PackageManager.PERMISSION_GRANTED){

                   Toast.makeText(this , "Permission granted",Toast.LENGTH_LONG).show();

               }else{
                   Toast.makeText(this , "Permission not granted",Toast.LENGTH_LONG).show();

                   finish();
               }
           }
       }


    }
}
