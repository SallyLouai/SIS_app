package com.example.sis;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.example.sis.Network.APIEndPoint;
import com.example.sis.Network.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentTimeTable extends AppCompatActivity {
    private NetworkUtils networkUtils;
    APIEndPoint apiEndPoint;
    SharedPreferences pref;
    private User user;
    private WebView webView_printer;
    private WebView timetableWebView;
    private String html;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("loading");
        progressDialog.setMessage("getting data from server please wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(getResources().getDrawable(R.drawable.logo));
        progressDialog.setCancelable(false);

        pref = getSharedPreferences("user_details",MODE_PRIVATE);
        user = new User(pref.getString("UserLogin", null ) );
        // action bar
        ActionBar actionBar = this.getSupportActionBar();
        String classTitle = actionBar.getTitle().toString();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.custom_action_bar);
        View view = actionBar.getCustomView();
        TextView activity_title = view.findViewById(R.id.activity_title);
        activity_title.setText(classTitle);
        TextView user_id_name = view.findViewById(R.id.user_id_name);
        user_id_name.setText( user.getStdCode() + "  |  " + user.getStdArName());


        networkUtils = new NetworkUtils();
        apiEndPoint = networkUtils.getApiEndPoint();

        // web view for printer
        webView_printer = view.findViewById(R.id.wbv_printer);
        // web view for grades
        timetableWebView = findViewById(R.id.wbv);
        timetableWebView.getSettings().setBuiltInZoomControls(true);
        fetchCours();
    }

    public void fetchCours(){
        progressDialog.show();
        if(!user.getToken().isEmpty()){
            //selectedSeme=
            Call<ResponseBody> call = apiEndPoint.GetStudentCourses("application/json", user.getToken());
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try{
                        html="";
                        // get response and store it in Document object
                        assert response.body() != null;
                        Document document = Jsoup.parse(response.body().string());
                        Element p = document.select("body").first();
                        // to extract JSON FROM HTML
                        JSONObject json = new JSONObject(p.text());
                        String respValue=json.getString("RespVal");
                        String res=json.getString("RespMsg");
                        if(Integer.valueOf(respValue) != 0){
                            JSONArray jsonArrayGrades=json.getJSONArray("Courses");

                            html += "<table class='table_' cellspacing='0' cellpadding='0' >";
                            html += "<thead>";
                            html += "<tr>";
                            html += "<td>Course Code</td>";
                            html += "<td>Hours</td>";
                            html += "<td>Section</td>";
                            html += "<td>Location</td>";
                            html += "<td>Schedule</td>";
                            html += "<td>Time</td>";
                            html += "</tr>";
                            html += "</thead>";
                            html += "<tbody>";

                            for(int i=0;i<jsonArrayGrades.length();i++){
                                JSONObject course=jsonArrayGrades.getJSONObject(i);
                                html += "<tr class='cls_"+i+"'>";
                                html += "<td>"+course.getString("CourseCode")+"</td>";  // course code
                                html += "<td>"+course.getString("Credits")+"</td>";     // hours
                                html += "<td>"+course.getString("Section")+"</td>";     // Section
                                html += "<td>"+course.getString("Location")+"</td>";    // Location
                                html += "<td>"+course.getString("Schedule")+"</td>";    // Schedule
                                html += "<td>"+course.getString("Time")+"</td>";        // Time
                                html += "</tr>";
                            }

                            html += "</tbody>";
                            html += "<tfoot>";
                            html += "<tr>";
                            html += "<td colspan='6'></td>";
                            html +="</tfoot>";
                            html += "</table>";
                            html +="<style>" +
                                    "table td{padding: 10px; }" +
                                    "table thead tr td,table tfoot tr td{background: #3465A4; color: #fff; }" +
                                    "table tbody tr.cls_0 td{background: #eee; " +
                                    "}" +
                                    "</style>";


                            // view courses in a web view
                            timetableWebView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                            webView_printer.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                            timetableWebView.zoomOut();
                            // if there's something wrong ....
                        } else{
                            html="<br><h3>"+res+"..</h3>";
                            timetableWebView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                            //webView_printer.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);

                            timetableWebView.zoomOut();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("onFailure", "MSG " + t.getMessage());
                }
            }); }else{
             html = "<br><h3>No Result ....</h3>";
            timetableWebView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
            //webView_printer.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);

            timetableWebView.zoomOut();
            progressDialog.dismiss();

        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createPdf(View view) {
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
        print+= "<style>table {margin: auto; width: 50%; border: none; padding: 10px;} </style>";

        webView_printer.loadDataWithBaseURL("file:///android_asset/", print, "text/html", "UTF-8", null);

        PrintManager printManager = (PrintManager) this.getSystemService(getBaseContext().PRINT_SERVICE);

        PrintDocumentAdapter printAdapter = webView_printer.createPrintDocumentAdapter("TimeTable");

        String jobName = getString(R.string.app_name) + "TimeTable_";

        printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());
    }
    public void onOptionsItemSelected(View item){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        finish();
    }
}
