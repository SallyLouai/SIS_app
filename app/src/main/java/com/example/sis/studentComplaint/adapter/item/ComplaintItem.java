package com.example.sis.studentComplaint.adapter.item;

import android.util.Log;

import com.example.sis.User;

import androidx.annotation.NonNull;

public class ComplaintItem {
    private String ID;
    private String ComplaintDate;
    private String StudentComments;
    private String ComplaintType;
    private String StudentFeedback;
    private String AddDate;
    private String Reference;

    public ComplaintItem() {

    }

    public ComplaintItem(String ID, String complaintDate, String studentComments, String complaintType, String studentFeedback, String addDate, String reference) {
        this.ID = ID;
        this.ComplaintDate = complaintDate;
        this.StudentComments = studentComments;
        this.ComplaintType = complaintType;
        this.StudentFeedback = studentFeedback;
        this.AddDate = addDate;
        this.Reference = reference;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getComplaintDate() {
        return ComplaintDate;
    }

    public void setComplaintDate(String complaintDate) {
        ComplaintDate = complaintDate;
    }

    public String getStudentComments() {
        return StudentComments;
    }

    public void setStudentComments(String studentComments) {
        StudentComments = studentComments;
    }

    public String getComplaintType() {
        return ComplaintType;
    }

    public void setComplaintType(String complaintType) {
        ComplaintType = complaintType;
    }

    public String getStudentFeedback() {
        return StudentFeedback;
    }

    public void setStudentFeedback(String studentFeedback) {
        StudentFeedback = studentFeedback;
    }

    public String getAddDate() {
        return AddDate;
    }

    public void setAddDate(String addDate) {
        AddDate = addDate;
    }

    public String getReference() {
        return Reference;
    }

    public void setReference(String reference) {
        Reference = reference;
    }

    @NonNull
    @Override
    public String toString() {
        return ID+ "@__@" +ComplaintDate+ "@__@" +StudentComments+ "@__@" +ComplaintType + "@__@" + StudentFeedback + "@__@" + AddDate + "@__@" + Reference ;
    }

    public ComplaintItem fromString(String data){

        String[] d = data.split("@__@");
        try {
            this.ID = d[0];
            this.ComplaintDate = d[1];
            this.StudentComments = d[2];
            this.ComplaintType = d[3];
            this.StudentFeedback = d[4];
            this.AddDate = d[5];
            this.Reference = d[6];
            return this;
        }catch (ArrayIndexOutOfBoundsException a){
            Log.e("ArrayOutOf",a.getMessage());
        }
        return this;
    }
}
