package com.example.uzimaemployee.Constants;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

public class Services {

    @DocumentId
    private String docId ;


    private String service_center;
    private String service_type;
    private String transaction_id;
    private String receipt_image;
    private Date timestamp;
    private Long amount;

    public Services(){};

    public Services(String docId, String service_center, String service_type, String transaction_id, String receipt_image, Date timestamp, Long amount) {
        this.docId = docId;
        this.service_center = service_center;
        this.service_type = service_type;
        this.transaction_id = transaction_id;
        this.receipt_image = receipt_image;
        this.timestamp = timestamp;
        this.amount = amount;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getService_center() {
        return service_center;
    }

    public void setService_center(String service_center) {
        this.service_center = service_center;
    }

    public String getService_type() {
        return service_type;
    }

    public void setService_type(String service_type) {
        this.service_type = service_type;
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
}
