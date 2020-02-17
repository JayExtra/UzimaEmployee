package com.example.uzimaemployee.Constants;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class EmergencyId {

    @Exclude
    public String EmergencyId;

    public<T extends EmergencyId> T withId(@NonNull final String id){


        this.EmergencyId = id;

        return (T) this;

    }






}
