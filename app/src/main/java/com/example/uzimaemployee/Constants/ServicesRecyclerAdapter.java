package com.example.uzimaemployee.Constants;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uzimaemployee.R;

import java.util.Date;
import java.util.List;

public class ServicesRecyclerAdapter extends RecyclerView.Adapter<ServicesRecyclerAdapter.ViewHolder> {


    public List<Services> servicesList;
    public Context context;

    Dialog myDialog;
    Button closeDialog;
    ImageView receiptImage;


    public ServicesRecyclerAdapter(List<Services>servicesList){
        this.servicesList = servicesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_services_item, parent , false);

        context = parent.getContext();


        return new ServicesRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        myDialog=new Dialog(context);

        //bind the values to their views

        holder.docId.setText(servicesList.get(position).getDocId());
        holder.receiptId.setText(servicesList.get(position).getTransaction_id());
        holder.serviceTxt.setText(servicesList.get(position).getService_center());
        holder.amntTxt.setText("Ksh. "+servicesList.get(position).getAmount().toString());
        holder.servtypeTxt.setText(servicesList.get(position).getService_type());

        long milliseconds= servicesList.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(milliseconds)).toString();//gets the date in which the house was posted
        holder.dateTxt.setText(dateString);


        holder.viewReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.setContentView(R.layout.fuel_receipt_dialog);
                closeDialog = (Button) myDialog.findViewById(R.id.button_close_dialog);
                receiptImage = (ImageView) myDialog.findViewById(R.id.receipt_image);


                Glide.with(context).load(servicesList.get(position).getReceipt_image()).into(receiptImage);

                closeDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myDialog.dismiss();
                    }
                });

                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myDialog.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        if(servicesList != null) {

            return servicesList.size();

        } else {

            return 0;

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView docId , serviceTxt , amntTxt , servtypeTxt , receiptId , dateTxt;
        private Button viewReceipt;
        private View mView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            docId = mView.findViewById(R.id.service_tv);
            serviceTxt = mView.findViewById(R.id.service_center_tv);
            dateTxt = mView.findViewById(R.id.date_tv);


            amntTxt = mView.findViewById(R.id.amount_tv);
            servtypeTxt = mView.findViewById(R.id.service_type_tv);
            receiptId = mView.findViewById(R.id.receipt_id_tv);

            viewReceipt = mView.findViewById(R.id.button_view_receipt_service);
        }
    }
}
