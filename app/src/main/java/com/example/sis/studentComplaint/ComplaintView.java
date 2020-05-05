package com.example.sis.studentComplaint;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sis.Network.APIEndPoint;
import com.example.sis.Network.NetworkUtils;
import com.example.sis.R;
import com.example.sis.User;
import com.example.sis.studentComplaint.adapter.item.ComplaintItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class ComplaintView extends AppCompatActivity {
    private WebView webView;
    private String html;
    private NetworkUtils networkUtils;
    SharedPreferences pref;
    private User user;
    private ComplaintItem complaintItem;
    private WebView wbv_printer;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_view);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("loading");
        progressDialog.setMessage("getting data from server please wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(getResources().getDrawable(R.drawable.logo));
        progressDialog.setCancelable(false);
        // get user data
        pref = getSharedPreferences("user_details",MODE_PRIVATE);
        user = new User(pref.getString("UserLogin", null));
        // set custom actionbar
        ActionBar actionBar = this.getSupportActionBar();
        String classTitle = Objects.requireNonNull(actionBar.getTitle()).toString();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.custom_action_bar);
        View view = actionBar.getCustomView();
        TextView activity_title = view.findViewById(R.id.activity_title);
        activity_title.setText(classTitle);
        TextView user_id_name = view.findViewById(R.id.user_id_name);
        user_id_name.setText( user.getStdCode() + "  |  " + user.getStdArName());


        Intent intent=getIntent();
        String complaintData=intent.getStringExtra("complaint");
        complaintItem=new ComplaintItem();
        assert complaintData != null;
        complaintItem.fromString(complaintData);
        webView=findViewById(R.id.ComplaintWebView);
        webView.getSettings().setBuiltInZoomControls(true);
        // web view for printer
        wbv_printer = findViewById(R.id.wbv_printer);
        networkUtils=new NetworkUtils();
        getComplaintStatus();
    }

    @EverythingIsNonNull
    void getComplaintStatus(){
        progressDialog.show();
        APIEndPoint apiEndPoint=networkUtils.getApiEndPoint();
        Call<ResponseBody> call=apiEndPoint.GetStudentComplaintStatuses("application/json",user.getToken(),complaintItem.getID());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    html="";
                    assert response.body() != null;
                    Document document = Jsoup.parse(response.body().string());
                    Element p = document.select("body").first();
                    // to extract JSON FROM HTML
                    JSONObject json = new JSONObject(p.text());
                    String respValue=json.getString("RespVal");
                    String res=json.getString("RespMsg");
                    if(Integer.valueOf(respValue) != 0){
                        JSONArray ComplaintStatuses=json.getJSONArray("ComplaintStatuses");
                        html= "<p style='padding-bottom:10px;padding-top:10px;color:#000;text-align:left;font-weight: bold;' >Complaint details</p>" +
                                "<table cellspacing='4' cellpadding='0'>" +
                                "<tr>" +
                                "<td calspan='4'><p class=\"HeadClass\">Complaint Date&#9;</p></td>" +
                                "<td class='val'>"+complaintItem.getComplaintDate()+"</td>"+
                                "</tr><tr>" +
                                "<td calspan='4'><p class=\"HeadClass\">Student Comments&#9;</p></td>" +
                                "<td class='val'>"+complaintItem.getStudentComments()+"</td>" +
                                "</tr><tr>" +
                                "<td calspan='4'><p class=\"HeadClass\">Complaint Type&#9;</p></td>" +
                                "<td class='val'>"+complaintItem.getComplaintType()+"</td>" +
                                "</tr><tr>" +
                                "<td calspan='4'><p class=\"HeadClass\">Student Feedback&#9;</p></td>" +
                                "<td class='val'>"+complaintItem.getStudentFeedback()+"</td>" +
                                "</tr><tr>" +
                                "<td calspan='4'><p class=\"HeadClass\">Add Date&#9;</p></td>" +
                                "<td class='val'>"+complaintItem.getAddDate()+"</td>" +
                                "</tr><tr>" +
                                "<td calspan='4'><p class=\"HeadClass\">Reference&#9;</p></td>" +
                                "<td class='val'>"+complaintItem.getReference()+"</td>" +
                                "</tr>" +
                                "</table>" +
                                "<p style='padding-bottom:10px;padding-top:10px;color:#000;text-align:left;font-weight: bold;' >Complaint status</p>" +
                                "<table cellspacing='4' cellpadding='0'>";
                                for(int i=0;i<ComplaintStatuses.length();i++){
                                    JSONObject object=ComplaintStatuses.getJSONObject(i);
                                    html+="<tr>" +
                                            "<td calspan='4'><p class=\"HeadClass\">Complaint Status</p></td>" +
                                            "<td><p class='val'>"+object.getString("ComplaintStatus")+"</p></td>" +
                                            "</tr><tr>" +
                                            "<td calspan='4'><p class=\"HeadClass\">Decision Date</p></td>" +
                                            "<td><p class='val'>"+object.getString("DecisionDate")+"</p></td>" +
                                            "</tr><tr>" +
                                            "<td calspan='4'><p class=\"HeadClass\">Decision Comments</p></td>" +
                                            "<td><p class='val'>"+object.getString("DecisionComments")+"</p></td>" +
                                            "</tr><tr>" +
                                            "<td calspan='4'><p class=\"HeadClass\">Decision User Name</p>" +
                                            "<td><p class='val'>"+object.getString("DecisionUserName")+"</p></td>" +
                                            "</tr><tr>" +
                                            "<td calspan='4'><p class=\"HeadClass\">Recipient User Name</p>" +
                                            "<td><p class='val'>"+object.getString("RecipientUserName")+"</p></td>" +
                                            "</tr><tr>" +
                                            "<td calspan='4'><p class=\"HeadClass\">DecisionNotes</p></td>" +
                                            "<td><p class='val'>"+object.getString("DecisionNotes")+"</p></td>" +
                                            "</tr>";
                                }

                                html+=  "</table>" +
                                        "<style>" +
                                        ".HeadClass{color:#000;text-align:left;font-weight: bold;}" +
                                        "table {background: #fff;text-align:left;}" +
                                        "table tr td {text-align:left;} " +
                                        ".val{padding-left:20px;}" +
                                        "</style>";


                        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                        webView.zoomOut();
                    }else{
                        // error message
                        html="<br><h3>"+res+"..</h3>";
                        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                        wbv_printer.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                        webView.zoomOut();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createPdf(View view){
        if (html.isEmpty()){
            Toast.makeText(this,"Please wait a moment",Toast.LENGTH_LONG).show();
        }else {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDateTime now = LocalDateTime.now();
            String print="<div style=\"display:block;\"><header>" +
                    "<div style=\"float:left;padding-left:20px;padding-top:20px;\"><p>Arab Open University</p><p>Kingdom Of Saudi Arabia</p></div>" +
                    "<div style=\"float:right;padding-right:20px;padding-top:20px;\"><p>الجامعة العربية المفتوحة </p><p>المملكة العربية السعودية</p></div>" +
                    "<div style=\"text-align:center;padding-top:10px;padding-bottom:10px;\">" +
                    "<img height=\"120px\" width=\"120px\"src=\"file:///android_asset/logo.png\" alt=\"Arab Open University\"></div>" +
                    "</header></div>\n";
            print+="<p style=\"text-align:center;padding-top:10px;padding-bottom:10px;font-weight:bold;\">Student Name :"+user.getStdArName()+"</p>";
            print+="<p style=\"text-align:center;font-weight:bold;\">Student ID :"+user.getStdID()+"</p>";
            print +=html;
            print+="<p style=\"text-align:center;font-weight:bold;\">Date : "+dtf.format(now)+"</p>";
            print+= "<style>table{ border: none; padding: 10px;} </style>";

            wbv_printer.loadDataWithBaseURL("file:///android_asset/", print, "text/html", "UTF-8", null);
            PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);
            PrintDocumentAdapter printAdapter = wbv_printer.createPrintDocumentAdapter("SIS");
            String jobName = getString(R.string.app_name) + "Grade_";
            printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
        }
    }

    // go back to choose activity
    public void onOptionsItemSelected(View v){
        super.onBackPressed();
    }
}
