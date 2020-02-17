package com.example.uzimaemployee.Constants;

import com.google.firebase.firestore.GeoPoint;

public class DeploymentsConstructor extends  EmergencyId{

    public GeoPoint distressed_Location;
    public String distressed_incident;
    public String distressed_description;
    public String dispatch_status;

    public DeploymentsConstructor(){


    }

    public DeploymentsConstructor(GeoPoint distressed_Location, String distressed_incident, String distressed_description, String dispatch_status) {
        this.distressed_Location = distressed_Location;
        this.distressed_incident = distressed_incident;
        this.distressed_description = distressed_description;
        this.dispatch_status = dispatch_status;
    }





    public String getDispatch_status() {
        return dispatch_status;
    }

    public void setDispatch_status(String dispatch_status) {
        this.dispatch_status = dispatch_status;
    }




    public GeoPoint getDistressed_Location() {
        return distressed_Location;
    }

    public void setDistressed_Location(GeoPoint distressed_Location) {
        this.distressed_Location = distressed_Location;
    }

    public String getDistressed_incident() {
        return distressed_incident;
    }

    public void setDistressed_incident(String distressed_incident) {
        this.distressed_incident = distressed_incident;
    }

    public String getDistressed_description() {
        return distressed_description;
    }

    public void setDistressed_description(String distressed_description) {
        this.distressed_description = distressed_description;
    }
}
