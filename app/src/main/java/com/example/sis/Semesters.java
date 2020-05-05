package com.example.sis;

import android.util.Log;

import java.io.Serializable;

public class Semesters implements Serializable {

    private String ID;
    private String Semester;


    public Semesters() {
        super();
    }

    public Semesters(String d) {
        super();
        fromString(d);
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setSemester(String semester) {
        Semester = semester;
    }

    public String getID() {
        return ID;
    }

    public String getSemester() {
        return Semester;
    }

    public String toString() {
        return ID + "@__@" + Semester;
    }

    public Semesters fromString(String data) {

        String[] d = data.split("@__@");
        try {
            this.ID = d[0];
            this.Semester = d[1];
            return this;
        } catch (ArrayIndexOutOfBoundsException a) {
            Log.e("ArrayOutOf", a.getMessage());
        }
    return this;
    }
}
