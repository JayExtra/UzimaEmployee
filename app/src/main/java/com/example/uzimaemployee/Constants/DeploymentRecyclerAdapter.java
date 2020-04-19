package com.example.uzimaemployee.Constants;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uzimaemployee.DeploymentDetails;
import com.example.uzimaemployee.Deployments;
import com.example.uzimaemployee.R;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DeploymentRecyclerAdapter extends RecyclerView.Adapter<DeploymentRecyclerAdapter.ViewHolder> {

    public List<DeploymentsConstructor> deploymentsList;
    public Context context;
    String dispatchId , latitude , longitude;
    List<Address>adresses;
    Geocoder geocoder;


    public DeploymentRecyclerAdapter(List<DeploymentsConstructor>deploymentList){
        this.deploymentsList = deploymentList;
    }


    public DeploymentRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_emergency_item, parent , false);

        context = parent.getContext();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeploymentRecyclerAdapter.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);


        final String emergencyPostId = deploymentsList.get(position).EmergencyId;

        dispatchId = emergencyPostId;


        String title_text = deploymentsList.get(position).getDistressed_incident();

        String dispatch_status = deploymentsList.get(position).getDispatch_status();
        holder.setVisib(dispatch_status);

        holder.setTitle(title_text);

        String desc_text = deploymentsList.get(position).getDistressed_description();

        holder.setDesc(desc_text);

        GeoPoint locgeoPoint = deploymentsList.get(position).getDistressed_Location();


        if (locgeoPoint == null){

            Toast.makeText(context,"Location error",Toast.LENGTH_SHORT).show();

        }else{


            double lat = locgeoPoint.getLatitude();
            double lng = locgeoPoint.getLongitude();

            //convert to string
            latitude = Double.toString(lat);
            longitude= Double.toString(lng);

            //setup geocoder

            geocoder = new Geocoder(context, Locale.getDefault());

            try {

                adresses = geocoder.getFromLocation(lat,lng,1);
                String address = adresses.get(0).getAddressLine(0);

                String fulladdress=  address+"";

                holder.setLocation(fulladdress);



                //Toast.makeText(MainActivity.this, "Your Location:"+mainAdress, Toast.LENGTH_SHORT).show();



            } catch (IOException e) {
                e.printStackTrace();
            }
        }






        holder.deployText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent openPost = new Intent(context, DeploymentDetails.class);
                openPost.putExtra("DOCUMENT_ID",emergencyPostId);
                openPost.putExtra("LATITUDE",latitude);
                openPost.putExtra("LONGITUDE",longitude);

                context.startActivity(openPost);

            }
        });

        holder.viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent openPost2 = new Intent(context, DeploymentDetails.class);
                openPost2.putExtra("DOCUMENT_ID",emergencyPostId);
                openPost2.putExtra("LATITUDE",latitude);
                openPost2.putExtra("LONGITUDE",longitude);

                context.startActivity(openPost2);

            }
        });







    }

    @Override
    public int getItemCount() {

        if(deploymentsList != null) {

            return deploymentsList.size();

        } else {

            return 0;

        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView titleText , descText , locationText;
        private View mView;
        private CardView emergencyCardview;
        private String status = "done";
        private TextView deployText;
        private Button viewButton;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            deployText = mView.findViewById(R.id.text_deploy);
            viewButton = mView.findViewById(R.id.view_button);


        }

        public void setTitle( String ttleText){

            titleText = mView.findViewById(R.id.title_emp);
            titleText.setText(ttleText);

        }

        public void setDesc( String desText){

            descText = mView.findViewById(R.id.description_emp);
            descText.setText(desText);

        }
        public void setLocation( String address){

            locationText = mView.findViewById(R.id.location_emp);
            locationText.setText(address);

        }



        public void setVisib( String statusTxt){

            if (statusTxt == status){

               deployText.setVisibility(View.INVISIBLE);
               viewButton.setVisibility(View.VISIBLE);


            }else{
                deployText.setVisibility(View.VISIBLE);
                viewButton.setVisibility(View.INVISIBLE);
            }



        }




    }


}
