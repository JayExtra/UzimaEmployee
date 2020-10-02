package com.example.uzimaemployee.Constants;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

public class Report {

    @DocumentId
    public String docId;

    public Date departure_time;
    public String patient_name;
    public String incident;
    public String hospital;

    public Report(){}

    public Report(String docId, Date departure_time, String patient_name, String incident, String hospital) {
        this.docId = docId;
        this.departure_time = departure_time;
        this.patient_name = patient_name;
        this.incident = incident;
        this.hospital = hospital;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public Date getDeparture_time() {
        return departure_time;
    }

    public void setDeparture_time(Date departure_time) {
        this.departure_time = departure_time;
    }

    public String getPatient_name() {
        return patient_name;
    }

    public void setPatient_name(String patient_name) {
        this.patient_name = patient_name;
    }

    public String getIncident() {
        return incident;
    }

    public void setIncident(String incident) {
        this.incident = incident;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }
}
