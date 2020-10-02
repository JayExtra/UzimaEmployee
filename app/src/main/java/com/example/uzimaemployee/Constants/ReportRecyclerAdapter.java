package com.example.uzimaemployee.Constants;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uzimaemployee.R;
import com.example.uzimaemployee.SingleReport;

import java.util.Date;
import java.util.List;

public class ReportRecyclerAdapter extends  RecyclerView.Adapter<ReportRecyclerAdapter.ViewHolder>{

    public List<Report> reportList;
    public Context context;

    public ReportRecyclerAdapter (List<Report> reportList){

        this.reportList = reportList;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_report_item, parent , false);

        context = parent.getContext();

        return new ReportRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String report_id = reportList.get(position).getDocId();

        holder.idText.setText(reportList.get(position).getDocId());


        long milliseconds= reportList.get(position).getDeparture_time().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(milliseconds)).toString();//gets the date in which the house was posted
        holder.timeText.setText(dateString);


        holder.casualtyText.setText(reportList.get(position).getPatient_name());
        holder.incidentText.setText(reportList.get(position).getIncident());
        holder.hospitalText.setText(reportList.get(position).getHospital());



        holder.reportCardview.setOnClickListener(view -> {
            Intent intent = new Intent(context , SingleReport.class);
            intent.putExtra("REPORT_ID" ,report_id);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        if(reportList != null) {

            return reportList.size();

        } else {

            return 0;

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView idText , timeText , casualtyText , incidentText ,hospitalText ;
        private View mView;
        private CardView reportCardview;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            idText = mView.findViewById(R.id.report_id);
            timeText = mView.findViewById(R.id.time_text);
            casualtyText = mView.findViewById(R.id.casualty_text);
            incidentText = mView.findViewById(R.id.incident_text);
            hospitalText = mView.findViewById(R.id.hospital_text);
            reportCardview = mView.findViewById(R.id.report_card);



        }
    }
}
