package com.example.uzimaemployee.Constants;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

public class Fuels {

    @DocumentId
    public String docId;

    public String station;
    public String transaction_id;
    public String receipt_image;
    public Date timestamp;
    public Long amount;
    public Long litres;


    public Fuels(){

    }

    public Fuels(String docId, String station, String transaction_id, String receipt_image, Date timestamp, Long amount, Long litres) {
        this.docId = docId;
        this.station = station;
        this.transaction_id = transaction_id;
        this.receipt_image = receipt_image;
        this.timestamp = timestamp;
        this.amount = amount;
        this.litres = litres;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getReceipt_image() {
        return receipt_image;
    }

    public void setReceipt_image(String receipt_image) {
        this.receipt_image = receipt_image;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getLitres() {
        return litres;
    }

    public void setLitres(Long litres) {
        this.litres = litres;
    }
}
