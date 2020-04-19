package com.example.uzimaemployee;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class VoiceRecorder extends AppCompatActivity {

    private ImageButton recordButton;


    private Boolean isRecording = false;

    private String distressed_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_recorder);

        Intent intent = getIntent();
        distressed_id = intent.getStringExtra("DISTRESSED_ID");


        //toolbar setup
        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Record Audio");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openRecorder = new Intent(VoiceRecorder.this, PcrReport.class);
                openRecorder.putExtra("DISTRESSED_ID",distressed_id);
                startActivity(openRecorder);
            }
        });




        //map widgets

        recordButton = findViewById(R.id.record_button);


        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isRecording){

                    //stop recording

                    isRecording=false;
                    recordButton.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped));

                }else{

                    isRecording=true;
                    recordButton.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording));

                }

            }
        });
    }
}
