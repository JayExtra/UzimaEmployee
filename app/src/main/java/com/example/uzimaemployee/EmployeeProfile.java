package com.example.uzimaemployee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EmployeeProfile extends AppCompatActivity {
    private FloatingActionButton editFloating;
    private TextView nameText, employeeidText, ageText, sextText,roleText, emailText, useridTxt;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private ProgressDialog progressDialog;
    private String bloodGrp, ageTxt;
    private Uri mainImageURI=null;
    private ImageView employeeImage;

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
        ageText = findViewById(R.id.employee_age);
        sextText = findViewById(R.id.employee_sex);
        roleText = findViewById(R.id.employee_role);
        employeeImage = findViewById(R.id.employee_image);
        emailText = findViewById(R.id.employee_email);
        useridTxt = findViewById(R.id.user_id_txt);
        progressDialog=new ProgressDialog(this);




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
            }
        });


        //call all the data from the database

        fetchFromDatabase();

        //edit button

        editFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EmployeeProfile.this,EditEmployeeProfile.class));
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

                        String name2 = task.getResult().getString("name");
                        String employeeId = task.getResult().getString("employee_id");
                        String role = task.getResult().getString("role");
                        String email = task.getResult().getString("email");
                        String sexTxt = task.getResult().getString("sex:");
                        String user_id = task.getResult().getString("user_id");
                        String age = task.getResult().getString("age");
                        String image2 = task.getResult().getString("image");








                        mainImageURI = Uri.parse(image2);

                        //attach to the various views

                        nameText.setText(name2);
                        employeeidText.setText(employeeId);
                        ageText.setText(age);
                        sextText.setText(sexTxt);
                        roleText.setText(role);
                        emailText.setText(email);
                        useridTxt.setText(user_id);







                        //******replacing the dummy image with real profile picture******

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.user_img);

                        Glide.with(EmployeeProfile.this).setDefaultRequestOptions(placeholderRequest).load(image2).into(employeeImage);


//19-10-1996
                    } else {

                        Toast.makeText(EmployeeProfile.this, "DATA DOES NOT EXISTS,PLEASE CREATE YOUR MEDICAL ID", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(EmployeeProfile.this,EditEmployeeProfile.class));



                    }
                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(EmployeeProfile.this, "(FIRESTORE RETRIEVE ERROR):" + error, Toast.LENGTH_LONG).show();


                }



            }
        });

    }
}
