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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uzimaemployee.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class FuelsRecyclerAdapter extends RecyclerView.Adapter<FuelsRecyclerAdapter.ViewHolder>{

    public List<Fuels> fuelsList;
   // public List<Fuels> allFuelList;
    public Context context;

    Dialog myDialog;
    Button closeDialog;
    ImageView receiptImage;

    public FuelsRecyclerAdapter(List<Fuels>fuelsList){
        this.fuelsList = fuelsList;
        //this.allFuelList = new ArrayList<>(fuelsList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_fuel_item, parent , false);

        context = parent.getContext();


        return new FuelsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        myDialog=new Dialog(context);

        //bind the values to their views

        holder.docId.setText(fuelsList.get(position).getDocId());
        holder.receiptId.setText(fuelsList.get(position).getTransaction_id());
        holder.stationTxt.setText(fuelsList.get(position).getStation());
        holder.amntTxt.setText("Ksh. "+fuelsList.get(position).getAmount().toString());
        holder.ltrsTxt.setText(fuelsList.get(position).getLitres().toString()+" ltrs");

        long milliseconds= fuelsList.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(milliseconds)).toString();//gets the date in which the house was posted
        holder.dateTxt.setText(dateString);


        holder.viewReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.setContentView(R.layout.fuel_receipt_dialog);
                closeDialog = (Button) myDialog.findViewById(R.id.button_close_dialog);
                receiptImage = (ImageView) myDialog.findViewById(R.id.receipt_image);


                Glide.with(context).load(fuelsList.get(position).getReceipt_image()).into(receiptImage);

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
        if(fuelsList != null) {

            return fuelsList.size();

        } else {

            return 0;

        }
    }

   // @Override
   // public Filter getFilter() {
   //     return filter;
  //  }


//runs on background thread
   // Filter filter = new Filter() {
      //  @Override
      //  protected FilterResults performFiltering(CharSequence charSequence) {
        //   List<Fuels> filteredList = new ArrayList<>();
//
         //   if(charSequence.toString().isEmpty()){
                //filteredList.addAll(allFuelList);

         //   }else{
          //      for(Fuels fuel : allFuelList){

          //          if(fuel.getDocId().toLowerCase().contains(charSequence.toString().toLowerCase())){
                       // filteredList.add(fuel);
           //         }

           //     }
          //  }

        //    FilterResults filterResults = new FilterResults();
         //   filterResults.values = filteredList;
         //   return null;
     //   }

        //runs on ui thread
       // @Override
      //  protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

        //    fuelsList.clear();
         //   fuelsList.addAll((Collection<? extends Fuels>) filterResults.values);
          //  notifyDataSetChanged();

//
   //     }
  //  };

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView docId , stationTxt , amntTxt , ltrsTxt , receiptId , dateTxt;
        private Button viewReceipt;
        private View mView;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            docId = mView.findViewById(R.id.entry_id);
            stationTxt = mView.findViewById(R.id.station_tv);
            dateTxt = mView.findViewById(R.id.date_tv);


            amntTxt = mView.findViewById(R.id.amount_tv);
            ltrsTxt = mView.findViewById(R.id.litres_tv);
            receiptId = mView.findViewById(R.id.receipt_tv);

            viewReceipt = mView.findViewById(R.id.button_view_receipt);

        }
    }
}
