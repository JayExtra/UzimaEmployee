package com.example.uzimaemployee.Constants;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.uzimaemployee.Deployments;
import com.example.uzimaemployee.EditEmployeeProfile;
import com.example.uzimaemployee.EmployeeProfile;
import com.example.uzimaemployee.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsRecyclerAdapter extends RecyclerView.Adapter<NotificationsRecyclerAdapter.ViewHolder> {

    public List<NotificationsConstructor> notificationsList;
    public Context context;
    public FirebaseFirestore mFirebaseFirestore;
    public FirebaseAuth firebaseAuth;
    Dialog myDialog;
    Button cancel , proceed;
    EditText mCustomMessage;
    Spinner mMessageSpinner;
    String message , fromID;

    public NotificationsRecyclerAdapter(List<NotificationsConstructor> notificationsList) {

        this.notificationsList = notificationsList;

    }

    @NonNull
    @Override
    public NotificationsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_notification_item, parent , false);

        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationsRecyclerAdapter.ViewHolder holder, int position) {

        //instantiate firebase elements
        FirebaseApp.initializeApp(context);
        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        myDialog=new Dialog(context);


        //fetch details

        //fetch message

        String from_id = notificationsList.get(position).getFrom();
        fromID = from_id;



        holder.messageText.setText(notificationsList.get(position).getDescription());

        mFirebaseFirestore.collection("Employee_Details").document(from_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String name = documentSnapshot.getString("first_name");
                String role = documentSnapshot.getString("employee_role");

                holder.fromText.setText(name);
                holder.roleText.setText(role);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(context , "Error in loading notifications",Toast.LENGTH_SHORT).show();

            }
        });

        holder.buttonDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String current_id= firebaseAuth.getCurrentUser().getUid();
                DocumentReference docRef = mFirebaseFirestore.collection("Dispatch_Notifications").document(current_id);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            if(document.exists()){

                                //proceed


                                holder.mProgressBar.setVisibility(View.VISIBLE);

                                myDialog.setContentView(R.layout.popup4);

                                cancel = (Button) myDialog.findViewById(R.id.button_cancel);
                                proceed = (Button) myDialog.findViewById(R.id.button_proceed);
                                mMessageSpinner = (Spinner) myDialog.findViewById(R.id.spinner_message);

                                //setupp spinner adapter

                                ArrayAdapter<CharSequence> messageAdapter = ArrayAdapter.createFromResource(context,R.array.reasons_array,android.R.layout.simple_spinner_item);
                                messageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                mMessageSpinner.setAdapter(messageAdapter);
                                mMessageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                                        Spinner spin = (Spinner)adapterView;
                                        if(spin.getId() == R.id.spinner_message)
                                        {
                                            String txt = adapterView.getItemAtPosition(position).toString();
                                            message =txt;

                                            Toast.makeText(myDialog.getContext(),"Incident selected is"+message,Toast.LENGTH_SHORT).show();

                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> adapterView) {

                                        Toast.makeText(context , "Please select a message so as to proceed",Toast.LENGTH_SHORT).show();

                                    }
                                });


                                proceed.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        final String current_id = firebaseAuth.getCurrentUser().getUid();

                                        //notify dispatcher

                                        Map<String, Object> notification = new HashMap<>();
                                        notification.put("from", current_id);
                                        notification.put("description", message);
                                        notification.put("status" , "denied");

                                        mFirebaseFirestore.collection("Dispatcher_Notification").document()
                                                .set(notification)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        Toast.makeText(context , "Dispatcher has been informed",Toast.LENGTH_SHORT).show();

                                                        //delete in temporary request storage


                                                        mFirebaseFirestore.collection("Dispatch_Notifications").document(current_id)
                                                                .delete()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(context , "Delete Success",Toast.LENGTH_SHORT).show();
                                                                        holder.mProgressBar.setVisibility(View.INVISIBLE);

                                                                        myDialog.dismiss();


                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(context , "Delete Failed",Toast.LENGTH_SHORT).show();

                                                                        holder.mProgressBar.setVisibility(View.INVISIBLE);

                                                                        myDialog.dismiss();

                                                                    }
                                                                });


                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(context,"Error:"+e.getMessage(),Toast.LENGTH_SHORT).show();
                                                    }
                                                });








                                    }
                                });

                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Toast.makeText(context,"Cancelling...",Toast.LENGTH_SHORT).show();


                                        holder.mProgressBar.setVisibility(View.INVISIBLE);

                                        myDialog.dismiss();

                                    }
                                });



                                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                myDialog.show();








                            }else{

                                Toast.makeText(context , "No existing information on the dispatch" +
                                        " ,The dispatch was already accepted or please contact the dispatcher",Toast.LENGTH_LONG).show();

                                holder.mProgressBar.setVisibility(View.INVISIBLE);



                            }
                        }else{
                            Toast.makeText(context , "Failure to complete the dispatch request",Toast.LENGTH_SHORT).show();
                            holder.mProgressBar.setVisibility(View.INVISIBLE);



                        }



                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(context , "Error at cancel:" +e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });


            }
        });

        holder.buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String my_id = firebaseAuth.getCurrentUser().getUid();

                holder.mProgressBar.setVisibility(View.VISIBLE);




                DocumentReference docRef = mFirebaseFirestore.collection("Dispatch_Notifications").document(my_id);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                final String d_ambulance = task.getResult().getString("deployed_ambulance");
                                final String d_company = task.getResult().getString("deployed_company");
                                final String d_driver = task.getResult().getString("deployed_driver");
                                final String d_date = task.getResult().getString("deployment_date");
                                final String d_status = task.getResult().getString("dispatch_status");
                                final GeoPoint d_location = task.getResult().getGeoPoint("distressed_Location");
                                final String d_county = task.getResult().getString("distressed_county");
                                final String d_description = task.getResult().getString("distressed_description");
                                final String d_email = task.getResult().getString("distressed_email");
                                final String d_incident = task.getResult().getString("distressed_incident");
                                final String d_number = task.getResult().getString("distressed_number");
                                final String d_person = task.getResult().getString("distressed_person");
                                final String d_uid = task.getResult().getString("distressed_uid");
                                final String drv_id = task.getResult().getString("driver_id");
                                final String drv_identity = task.getResult().getString("driver_identity");
                                final String drv_number = task.getResult().getString("driver_number");
                                final String drv_status = task.getResult().getString("driver_status");


                                //1.NotifyUser

                                String myMessage = "My name is "+d_driver+" from "+d_company+" ambulance plate: "+d_ambulance+ ". I have been dispatched towards you. Please stay calm and find a safe location. ";
                                Map<String, Object> userNotification = new HashMap<>();
                                userNotification.put("from", my_id);
                                userNotification.put("message", myMessage);
                                userNotification.put("timestamp",FieldValue.serverTimestamp());


                                mFirebaseFirestore.collection("users/"+d_uid+"/Notifications").document()
                                        .set(userNotification)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Toast.makeText(context , "Distressed individual has been informed",Toast.LENGTH_SHORT).show();

                                                //2.notify dispatcher

                                                String message2 = "Proceeding towards the emergency scene";
                                                Map<String , Object> adminNotification = new HashMap<>();
                                                adminNotification.put("from",my_id);
                                                adminNotification.put("description", message2);
                                                adminNotification.put("status" , "accepted");
                                                adminNotification.put("condition" , "new");
                                                adminNotification.put("timestamp" , FieldValue.serverTimestamp());

                                                mFirebaseFirestore.collection("Dispatcher_Notification").document()
                                                        .set(adminNotification)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(context, "Informed dispatcher",Toast.LENGTH_SHORT ).show();

                                                                //3. Update the dispatch records

                                                                Map<String,Object> dispatchInfo= new HashMap<>();
                                                                dispatchInfo.put("deployed_ambulance" , d_ambulance);
                                                                dispatchInfo.put("deployed_company" , d_company);
                                                                dispatchInfo.put("deployed_driver" , d_driver);
                                                                dispatchInfo.put("deployment_date" , d_date);
                                                                dispatchInfo.put("dispatch_status" , d_status);
                                                                dispatchInfo.put("distressed_Location" , d_location);
                                                                dispatchInfo.put("distressed_county" , d_county);
                                                                dispatchInfo.put("distressed_description" , d_description);
                                                                dispatchInfo.put("distressed_email" , d_email);
                                                                dispatchInfo.put("distressed_incident" , d_incident);
                                                                dispatchInfo.put("distressed_number" , d_number);
                                                                dispatchInfo.put("distressed_person" , d_person);
                                                                dispatchInfo.put("distressed_uid" , d_uid);
                                                                dispatchInfo.put("driver_id" , drv_id);
                                                                dispatchInfo.put("driver_identity" , drv_identity);
                                                                dispatchInfo.put("driver_number" , drv_number);
                                                                dispatchInfo.put("driver_status" , drv_status);

                                                                mFirebaseFirestore.collection("Dispatch_Records").document().set(dispatchInfo)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {

                                                                                Toast.makeText(context,"Success in updating dispatch_records",Toast.LENGTH_SHORT).show();

                                                                                //4.delete temporary dispatch record notification

                                                                                mFirebaseFirestore.collection("Dispatch_Notifications").document(my_id)
                                                                                        .delete()
                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                Toast.makeText(context , "Delete Success",Toast.LENGTH_SHORT).show();
                                                                                                holder.mProgressBar.setVisibility(View.INVISIBLE);

                                                                                               Intent intent = new Intent(context , Deployments.class);
                                                                                                context.startActivity(intent);
                                                                                               ((Activity)context).finish();
                                                                                            }
                                                                                        })
                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                Toast.makeText(context , "Delete Failed on step3",Toast.LENGTH_SHORT).show();

                                                                                                holder.mProgressBar.setVisibility(View.INVISIBLE);


                                                                                            }
                                                                                        });





                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {

                                                                        Toast.makeText(context,"Error on Dispatch Records:"+e.getMessage(),Toast.LENGTH_SHORT).show();


                                                                    }
                                                                });


                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                        Toast.makeText(context ,  "Failed to inform dispatcher",Toast.LENGTH_SHORT).show();

                                                    }
                                                });





                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context,"Error:"+e.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });












//19-10-1996
                            } else {


                                Toast.makeText(context , "No existing information on the dispatch" +
                                        " ,The dispatch was already accepted or please contact the dispatcher",Toast.LENGTH_LONG).show();

                                holder.mProgressBar.setVisibility(View.INVISIBLE);




                            }
                        } else {

                            Toast.makeText(context , "Failure to complete the dispatch request",Toast.LENGTH_SHORT).show();
                            holder.mProgressBar.setVisibility(View.INVISIBLE);





                        }



                    }
                });






            }
        });




    }

    @Override
    public int getItemCount() {

        if(notificationsList != null) {

            return notificationsList.size();

        } else {

            return 0;

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView messageText , fromText , roleText;
        private View mView;
        private Button buttonDeny, buttonAccept;
        private ProgressBar mProgressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            messageText = mView.findViewById(R.id.msg_txt);
            roleText = mView.findViewById(R.id.role_txt);
            fromText = mView.findViewById(R.id.name_text);
            buttonAccept = mView.findViewById(R.id.dep_btn);
            buttonDeny = mView.findViewById(R.id.deny_btn);
            mProgressBar =mView.findViewById(R.id.single_item_bar);


        }
    }
}
