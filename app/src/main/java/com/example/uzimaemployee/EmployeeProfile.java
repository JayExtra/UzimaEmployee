package com.example.uzimaemployee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.uzimaemployee.DatePicker.DatePickerFragment;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class EmployeeProfile extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private FloatingActionButton editFloating;
    private TextView nameText, employeeidText, ambulanceText, companyText,roleText, emailText, useridTxt, secondNameText , countyText, jDate;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private ProgressDialog progressDialog;
    private String bloodGrp, ageTxt;
    private Uri mainImageURI=null;
    public boolean isChanged = false;
    private ImageView employeeImage;

    Dialog myDialog1;
    TextView setReminder , dlExpryText;
    ImageView dlImage;
    RadioButton radioDlSelector;
    Button closeDlDialog , updateDl;
    String employee_id;
    EditText dateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_profile);



        //firebase setup
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();




        //setup widgets
        editFloating = findViewById(R.id.edit_button);
        nameText = findViewById(R.id.employee_name);
        employeeidText = findViewById(R.id.employee_id);
        companyText = findViewById(R.id.employee_company);
        ambulanceText = findViewById(R.id.employee_ambulance);
        roleText = findViewById(R.id.employee_role);
        employeeImage = findViewById(R.id.employee_image);
        useridTxt = findViewById(R.id.user_id_txt);
        secondNameText =findViewById(R.id.employee_sname);
        progressDialog=new ProgressDialog(this);

        countyText = findViewById(R.id.tv_region);
        jDate = findViewById(R.id.j_date_tv);


        myDialog1 = new Dialog(this);




        //toolbar setup
        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        getSupportActionBar().setTitle("Your profile");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmployeeProfile.this, MainActivity.class));
                finish();
            }
        });


        //call all the data from the database

        fetchFromDatabase();

        //edit button

        editFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EmployeeProfile.this,EditEmployeeProfile.class));
                finish();
            }
        });
    }

    public void fetchFromDatabase(){

        progressDialog.setMessage("Loading details...");
        progressDialog.show();


//******checking on data within the database on whether it already exist then fetch the user details and place them in the required fields************



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
                        String company = task.getResult().getString("company");
                        String ambulance = task.getResult().getString("ambulance");
                        String image2 = task.getResult().getString("image");
                        String county = task.getResult().getString("county");

                        employee_id = e_id;


                        mainImageURI = Uri.parse(image2);

                        //attach to the various views
                        nameText.setText(f_name);
                        secondNameText.setText(s_name);
                        employeeidText.setText(e_id);
                        companyText.setText(company);
                        ambulanceText.setText(ambulance);
                        roleText.setText(e_role);
                        useridTxt.setText(user_id);
                        countyText.setText(county);

                        loadJoiningDate( e_id);



                        //******replacing the dummy image with real profile picture******

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.user_img);

                        Glide.with(EmployeeProfile.this).setDefaultRequestOptions(placeholderRequest).load(image2).into(employeeImage);


//19-10-1996
                    } else {

                        Toast.makeText(EmployeeProfile.this, "SUCCESS", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(EmployeeProfile.this,EditEmployeeProfile.class));
                        finish();



                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(EmployeeProfile.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }



            }
        });

    }

    private void loadJoiningDate(String e_id) {

        DocumentReference docRef = firebaseFirestore.collection("Employee").document(e_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Timestamp milliseconds = task.getResult().getTimestamp("joining_date");
                        String dateString = milliseconds.toDate().toString();
                        jDate.setText(dateString);


                    } else {

                        Toast.makeText(EmployeeProfile.this , "This user does not exist",Toast.LENGTH_SHORT).show();

                    }
                } else {

                    Toast.makeText(EmployeeProfile.this , "Failed to fetch joining date",Toast.LENGTH_SHORT).show();


                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dl_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.drivers_license:
                startDlDialog();
                return true;



        }
        return super.onOptionsItemSelected(item);
    }



    private void startDlDialog() {



        myDialog1.setContentView(R.layout.dl_dialog);


        closeDlDialog = myDialog1.findViewById(R.id.close_dialog_dl);
        dlExpryText = myDialog1.findViewById(R.id.dl_expiry);
        setReminder = myDialog1.findViewById(R.id.set_reminder_txt);
        dlImage = myDialog1.findViewById(R.id.dl_image);
        radioDlSelector = myDialog1.findViewById(R.id.update_radio_button);
        updateDl=myDialog1.findViewById(R.id.update_dl);
        dateText = myDialog1.findViewById(R.id.date_text);



        DocumentReference docRef = firebaseFirestore.collection("Employee").document(employee_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        Timestamp dlExpiry = task.getResult().getTimestamp("dl_expiry");
                        String dl_image = task.getResult().getString("drivers_license");


                        String dlExpDate = dlExpiry.toDate().toString();


                        dlExpryText.setText(dlExpDate);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.user_img);

                        Glide.with(EmployeeProfile.this).setDefaultRequestOptions(placeholderRequest).load(dl_image).into(dlImage);




                        //******replacing the dummy image with real profile picture******

//19-10-1996
                    } else {

                        Toast.makeText(EmployeeProfile.this, "SUCCESS", Toast.LENGTH_LONG).show();

                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(EmployeeProfile.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }



            }
        });


        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });


        setReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Date endDate = new DateTime(scheduled_date).plusMinutes(60).toDate();

                //Calendar calendar = Calendar.getInstance();

                //calendar.setTime(scheduled_date);

                //long endDate = calendar.getTimeInMillis();


                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setData(CalendarContract.Events.CONTENT_URI);
                intent.putExtra(CalendarContract.Events.TITLE,"Drivers Liccense Expiry Date");
                intent.putExtra(CalendarContract.Events.DESCRIPTION,"Date for licence expiry");
                intent.putExtra(CalendarContract.Events.EVENT_LOCATION,"None");
                intent.putExtra(CalendarContract.Events.ALL_DAY,true);
               // intent.putExtra(CalendarContract.Events.DTSTART,scheduled_date);
                //intent.putExtra(CalendarContract.Events.DTEND,endDate);


                if(intent.resolveActivity(getPackageManager())!=null){

                    startActivity(intent);


                }else{
                    Toast.makeText(EmployeeProfile.this , "You have no app that can handle this type of action",Toast.LENGTH_SHORT).show();


                }
            }
        });




        closeDlDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog1.dismiss();
            }
        });


        radioDlSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDl.setVisibility(View.VISIBLE);
                dateText.setVisibility(View.VISIBLE);
            }
        });

        updateDl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               String date =  dateText.getText().toString();



                if(!TextUtils.isEmpty(date)){

                    if(isChanged){
                        StorageReference image_path = storageReference.child("Driver_Images").child(user_id + ".jpg");

                        image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                storeImage(task,date);

                            }
                        });


                    }else{

                        storeImage(null , date);


                    }



                }else{

                    dateText.setError("Please fill in the required date");

                }

            }
        });

        dlImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BringImagePicker();
            }
        });



        myDialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog1.show();




    }

    private void storeImage(Task<UploadTask.TaskSnapshot> task, String date) {

        if(task!=null){
            storageReference.child("Driver_Images").child(user_id+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    DocumentReference imageRef = firebaseFirestore.collection("Employee").document(employee_id);

// Set the "isCapital" field of the city 'DC'
                    imageRef
                            .update("drivers_license", uri.toString(),
                                    "dl_expiry" , new Date(date))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Toast.makeText(EmployeeProfile.this , "Drivers license updated",Toast.LENGTH_SHORT).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(EmployeeProfile.this , "Drivers license failed to updated"+e.getMessage(),Toast.LENGTH_SHORT).show();


                                }
                            });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(EmployeeProfile.this , "Drivers license uri failed to download"+e.getMessage(),Toast.LENGTH_SHORT).show();


                }
            });


        }else{


            storageReference.child("Driver_Images").child(user_id+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    uri=mainImageURI;

                    DocumentReference imageRef = firebaseFirestore.collection("Employee").document(employee_id);

// Set the "isCapital" field of the city 'DC'
                    imageRef
                            .update("drivers_license", uri.toString(),
                                    "dl_expiry" , new Date(date))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Toast.makeText(EmployeeProfile.this , "Drivers license updated",Toast.LENGTH_SHORT).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(EmployeeProfile.this , "Drivers license failed to updated"+e.getMessage(),Toast.LENGTH_SHORT).show();


                                }
                            });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(EmployeeProfile.this , "Drivers license uri failed to download"+e.getMessage(),Toast.LENGTH_SHORT).show();


                }
            });



        }


    }

    private void BringImagePicker() {

        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {

//****replace image with new image******
                mainImageURI = result.getUri();
                dlImage.setImageURI(mainImageURI);

                isChanged=true;


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error= result.getError();
                Toast.makeText(EmployeeProfile.this,"Error!!:" + error,Toast.LENGTH_SHORT).show();



            }
        }


    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String currentDateString = java.text.DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());
        dateText.setText(currentDateString);
    }
}
