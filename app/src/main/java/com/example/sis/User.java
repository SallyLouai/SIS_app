package com.example.sis;

import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;


public class User implements Serializable {


    private String  RespVal;
    private String  RespMsg;
    public String   Token;
    private String  StdID;
    private String  StdCode;
    private String  StdArName;
    private String  StdEnName;
    public String   StdUniversityEmail;
    private String  StdPersEmail;
    private String  StdCivilID;
    private String  StdTrack;
    private String  Password;

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    /*
        public String mobile;
        public String adresse;
        */
    private String programe_en_name;

    public User(){
        super();
    }
    public User(String d){
        super();
        fromString(d);
    }


    public String getRespVal() {
        return RespVal;
    }

    public String getRespMsg() {
        return RespMsg;
    }

    public String getToken() {
        return Token;
    }

    public String getStdID() {
        return StdID;
    }

    public String getStdCode() {
        return StdCode;
    }

    public String getStdArName() {
        return StdArName;
    }

    public String getStdEnName() {
        return StdEnName;
    }

    public String getStdUniversityEmail() {
        return StdUniversityEmail;
    }

    public String getStdPersEmail() {
        return StdPersEmail;
    }

    public String getStdCivilID() {
        return StdCivilID;
    }

    public String getStdTrack() {
        return StdTrack;
    }

    public String toString(){
        return RespVal+ "@__@" +RespMsg+ "@__@" +Token+ "@__@" +StdID + "@__@" + StdCode + "@__@" + StdArName + "@__@" + StdEnName + "@__@" + StdUniversityEmail + "@__@" + StdPersEmail + "@__@" + StdCivilID + "@__@" + StdTrack +"@__@" + Password;
    }

    public User fromString(String data){

        String[] d = data.split("@__@");
        try {
            this.RespVal = d[0];
            this.RespMsg = d[1];
            this.Token = d[2];
            this.StdID = d[3];
            this.StdCode = d[4];
            this.StdArName = d[5];
            this.StdEnName = d[6];
            this.StdUniversityEmail = d[7];
            this.StdPersEmail = d[8];
            this.StdCivilID = d[9];
            this.StdTrack = d[10];
            this.Password=d[11];
            return this;
        }catch (ArrayIndexOutOfBoundsException a){
            Log.e("ArrayOutOf",a.getMessage());
        }

        return this;
    }
}
