package com.example.uzimaemployee;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VoiceRecorder extends AppCompatActivity {

    private ImageButton recordButton;


    private Boolean isRecording = false;

    private String distressed_id;

    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21 ;
    private MediaRecorder mediaRecorder;
    private String recordFile;

    private Chronometer mChronometer;

    private TextView mRecordName ;
    private String mPatient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_recorder);

        Intent intent = getIntent();
        distressed_id = intent.getStringExtra("DISTRESSED_ID");
        mPatient = intent.getStringExtra("DISTRESSED_NAME");


        //toolbar setup
        Toolbar toolbar = findViewById(R.id.employee_interface_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Record Audio");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isRecording){

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(VoiceRecorder.this);
                    alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Toast.makeText(VoiceRecorder.this, "Recording stopped...",Toast.LENGTH_SHORT).show();
                            isRecording = false;
                            Intent openRecorder = new Intent(VoiceRecorder.this, PcrReport.class);
                            openRecorder.putExtra("DISTRESSED_ID",distressed_id);
                            startActivity(openRecorder);
                        }
                    });

                    alertDialog.setNegativeButton("CANCEL" , null);
                    alertDialog.setTitle("Audio still recording");
                    alertDialog.setMessage("Are you sure you want to stop the recording?");
                    alertDialog.create().show();

                } else{

                    Intent openRecorder = new Intent(VoiceRecorder.this, PcrReport.class);
                    openRecorder.putExtra("DISTRESSED_ID",distressed_id);
                    startActivity(openRecorder);


                }



            }
        });




        //map widgets

        recordButton = findViewById(R.id.record_button);
        mChronometer = findViewById(R.id.record_timer);
        mRecordName = findViewById(R.id.record_file_name);


        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isRecording){

                    //stop recording

                    stopRecording();

                    isRecording=false;
                    recordButton.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped));
                    Toast.makeText(VoiceRecorder.this, "Saving your audio...",Toast.LENGTH_SHORT).show();


                }else{

                    //start recording audio
                    if(checkPermissions()){

                        isRecording=true;
                        recordButton.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording));
                        startRecording();



                    }

                }

            }
        });
    }

    private void startRecording() {

        mChronometer.setBase(SystemClock.elapsedRealtime());

        mChronometer.start();

        String recordPath = VoiceRecorder.this.getExternalFilesDir("/").getAbsolutePath();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        Date now = new Date();
        recordFile = "Recording_"+mPatient+"_"+ formatter.format(now) + "3gp";

        mRecordName.setText("Recording..file name:" + recordFile);
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFile);
        mediaRecorder.setAudioEncoder( MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
    }

    private void stopRecording() {

        //stop timer
        mChronometer.stop();

        //change text on page in file saved
        mRecordName.setText("Recording stopped , File Saved"+ recordFile);

        //Stop media recorder and set it to null for further use to record new audio
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    private boolean checkPermissions() {

        if(ActivityCompat.checkSelfPermission
                (this, recordPermission)== PackageManager.PERMISSION_GRANTED){

            return true;

        } else{

          ActivityCompat.requestPermissions(VoiceRecorder.this , new String[]{recordPermission},PERMISSION_CODE);
            return false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(isRecording){
            stopRecording();

            Toast.makeText(VoiceRecorder.this, "Recording stopped...",Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(isRecording){

            stopRecording();
            Toast.makeText(VoiceRecorder.this, "Recording stopped...",Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (isRecording){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(VoiceRecorder.this);
            alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //stopRecording();
                    Toast.makeText(VoiceRecorder.this, "Recording stopped...",Toast.LENGTH_SHORT).show();
                    isRecording = false;
                    startActivity(new Intent(VoiceRecorder.this,MainActivity.class));
                }
            });
            alertDialog.setNegativeButton("CANCEL" , null);
            alertDialog.setTitle("Audio still recording");
            alertDialog.setMessage("Are you sure you want to stop the recording?");
            alertDialog.create().show();

        }
    }
}
