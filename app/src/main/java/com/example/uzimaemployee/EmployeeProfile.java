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
    private TextView nameText, employeeidText, ambulanceText, companyText,roleText, emailText, useridTxt, secondNameText;
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
        companyText = findViewById(R.id.employee_company);
        ambulanceText = findViewById(R.id.employee_ambulance);
        roleText = findViewById(R.id.employee_role);
        employeeImage = findViewById(R.id.employee_image);
        useridTxt = findViewById(R.id.user_id_txt);
        secondNameText =findViewById(R.id.employee_sname);
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
                finish();
            }
        });


        //call all the data from the database

        fetchFromDatabase();

        //edit button

        editFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EmployeeProfile.this,EmployeeCreation.class));
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


                        mainImageURI = Uri.parse(image2);

                        //attach to the various views
                        nameText.setText(f_name);
                        secondNameText.setText(s_name);
                        employeeidText.setText(e_id);
                        companyText.setText(company);
                        ambulanceText.setText(ambulance);
                        roleText.setText(e_role);
                        useridTxt.setText(user_id);



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
}
