package com.example.uzimaemployee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class EditEmployeeProfile extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private FloatingActionButton addButton;
    private Uri mainImageURI=null;
    private ImageView setupImage;
    private EditText nameField,emailField,employeeIdField;
    public boolean isChanged = false;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private ProgressDialog progressDialog;
    private Spinner ageSpinner, sexSpinner, roleSpinner;
    private String ageTxt, roleTxt, sexTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_employee_profile);

        //setup firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        //Map the widgets
        setupImage =findViewById(R.id.setup_image);
        addButton =findViewById(R.id.floating_add);
        nameField=findViewById(R.id.edit_name);
        emailField=findViewById(R.id.edit_email);
        employeeIdField=findViewById(R.id.edit_id);
        ageSpinner = findViewById(R.id.age_spinner);
        sexSpinner = findViewById(R.id.sex_spinner);
        roleSpinner = findViewById(R.id.role_spinner);

        progressDialog=new ProgressDialog(this);


        //setup the various spinners

        ArrayAdapter<CharSequence> ageAdapter = ArrayAdapter.createFromResource(this,R.array.age,android.R.layout.simple_spinner_item);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ageSpinner.setAdapter(ageAdapter);
        ageSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource(this,R.array.sex,android.R.layout.simple_spinner_item);
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexSpinner.setAdapter(sexAdapter);
        sexSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this,R.array.role,android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);
        roleSpinner.setOnItemSelectedListener(this);




        //toolbar setup
        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        getSupportActionBar().setTitle("Edit your profile");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditEmployeeProfile.this, MainActivity.class));
                finish();
            }
        });




        //image selector
        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(EditEmployeeProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(EditEmployeeProfile.this, "You do not have permission", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(EditEmployeeProfile.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {

                        BringImagePicker();

                    }
                }else{
                    BringImagePicker();
                }


            }
        });

        //send user data to the database

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SendToDatabase();

            }
        });







    }






    //send data to database method

    //writing values into database
    private void SendToDatabase() {

        final String name= nameField.getText().toString();
        final String email = emailField.getText().toString();
        final String employeeId= employeeIdField.getText().toString();

        if(!TextUtils.isEmpty(name)&&!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(employeeId)&&mainImageURI!=null){

            progressDialog.setMessage("Uploading details...");
            progressDialog.show();

            if (isChanged) {        ///checks first if the profile image is changed if it is then proceeds


//***sending image to fire base storage together with user details*********


                user_id = mAuth.getCurrentUser().getUid();


                StorageReference image_path = storageReference.child("employee_pictures").child(user_id + ".jpg");

                image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                        if (task.isSuccessful()) {

                            //****call on method that will store the user data on the firestore database****

                            storeFirestore(task,name,email,employeeId);


                        } else {

                            String error = task.getException().getMessage();

                            Toast.makeText(EditEmployeeProfile.this, "(IMAGE ERROR):" + error, Toast.LENGTH_LONG).show();

                            finish();

                        }


                    }
                });


            }else {  //if profile image is not changed pass task as null therefore the user selects image again


                storeFirestore(null, name,email,employeeId);
                Toast.makeText(EditEmployeeProfile.this, "FIRESTORE ERROR 1:", Toast.LENGTH_LONG).show();

            }


        }

    }



















    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

        Spinner spin = (Spinner)parent;
        Spinner spin2 = (Spinner)parent;
        Spinner spin3 = (Spinner)parent;

        if(spin.getId() == R.id.age_spinner)
        {
            String txt = parent.getItemAtPosition(position).toString();
            ageTxt =txt;

        }
        if(spin2.getId() == R.id.sex_spinner)
        {
            String txt = parent.getItemAtPosition(position).toString();
            sexTxt = txt;

        }
        if(spin3.getId() == R.id.role_spinner)
        {
            String txt = parent.getItemAtPosition(position).toString();
            roleTxt = txt;

        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

        Toast.makeText(EditEmployeeProfile.this,"No field should be left unselected!!",Toast.LENGTH_SHORT).show();

    }

    public void BringImagePicker(){

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
                setupImage.setImageURI(mainImageURI);

                isChanged=true;


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error= result.getError();
                Toast.makeText(EditEmployeeProfile.this,"Error!!:" + error,Toast.LENGTH_SHORT).show();



            }
        }


    }

    //storage of values into firestore database
    //******adding user details  into firestore database ***********

    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot>task, final String name,
                                final String email, final String employeeId) {

        //****hash map for storing user details in fire base cloud storage**************


        if (task!=null) {


            storageReference.child("employee_pictures").child(user_id+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri download_uri) {


                    Map<String, Object> userMap= new HashMap<>();

                    userMap.put("image",download_uri.toString());
                    userMap.put("name",name);
                    userMap.put("role",roleTxt);
                    userMap.put("employee_id",employeeId);
                    userMap.put("sex:",sexTxt);
                    userMap.put("email",email);
                    userMap.put("age",ageTxt);
                    userMap.put("user_id",user_id);




                    firebaseFirestore.collection("Employee_Details").document(user_id)
                            .set(userMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(EditEmployeeProfile.this,"Your Id was successfully updated",Toast.LENGTH_LONG).show();

                                    startActivity(new Intent(EditEmployeeProfile.this,EmployeeProfile.class));

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(EditEmployeeProfile.this,"FIRESTORE ERROR: COULD NOT FETCH IMAGE",Toast.LENGTH_LONG).show();

                                    finish();

                                }


                            });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Toast.makeText(EditEmployeeProfile.this,"FIRESTORE ERROR 2",Toast.LENGTH_LONG).show();

                }
            });


        } else{

            storageReference.child("employee_pictures").child(user_id+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri download_uri) {
                    // Got the download URL for profile picture

                    download_uri=mainImageURI;


                    Map<String, Object> userMap= new HashMap<>();

                    userMap.put("image",download_uri.toString());
                    userMap.put("name",name);
                    userMap.put("role",roleTxt);
                    userMap.put("employee_id",employeeId);
                    userMap.put("sex:",sexTxt);
                    userMap.put("email",email);
                    userMap.put("age",ageTxt);
                    userMap.put("user_id",user_id);

                    firebaseFirestore.collection("Employee_Details").document(user_id)
                            .set(userMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(EditEmployeeProfile.this,"Employee id  Updated",Toast.LENGTH_LONG).show();

                                    startActivity(new Intent(EditEmployeeProfile.this,EmployeeProfile.class));


                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(EditEmployeeProfile.this,"FIRESTORE ERROR 3",Toast.LENGTH_LONG).show();

                                    finish();

                                }


                            });




                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors

                    Toast.makeText(EditEmployeeProfile.this,"FIRESTORE ERROR 4 ",Toast.LENGTH_LONG).show();

                }
            });


        }






    }
}
