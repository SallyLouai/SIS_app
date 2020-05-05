package com.example.sis;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import com.example.sis.Network.APIEndPoint;
import com.example.sis.Network.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Grades extends AppCompatActivity {
    private SharedPreferences pref;
    private NetworkUtils networkUtils;
    APIEndPoint apiEndPoint;
    private User user;
    private Spinner spinner_semesters;
    private ArrayList<Semesters> StudentSemesters;
    private WebView wbv;
    private WebView wbv_printer;
    String html;
    String selectedYear = "2019";
    String selectedSeme = "1";
    private ProgressDialog progressDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

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

        networkUtils = new NetworkUtils();
        apiEndPoint = networkUtils.getApiEndPoint();

        // web view for printer
        wbv_printer = view.findViewById(R.id.wbv_printer);
        // Semesters spinner
        spinner_semesters = findViewById(R.id.spinner_years);
        // web view for grades
        wbv = findViewById(R.id.wbv);
        wbv.getSettings().setBuiltInZoomControls(true);

        // for student semesters
        StudentSemesters=getSemesters();
        spinner_semesters.setSelection(0);
        if(!StudentSemesters.isEmpty()){
            fetchCours(StudentSemesters.get(0).getID());
        }
        spinner_semesters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = StudentSemesters.get(position).getID();
                fetchCours(selectedYear);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    public ArrayList<Semesters> getSemesters() {
        final ArrayList<Semesters> semesters =new ArrayList<Semesters>();
        final ArrayList<String> sem=new ArrayList<String>();

        Call<ResponseBody> call = apiEndPoint.GetStudentSemesters("application/json", user.getToken());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    assert response.body() != null;
                    Document document = Jsoup.parse(response.body().string());
                    Element p = document.select("body").first();
                    // to extract JSON FROM HTML
                    JSONObject json = new JSONObject(p.text());
                    String respValue=json.getString("RespVal");
                    String res=json.getString("RespMsg");
                    // if the result is Correct
                    /**
                     * 1- Take the list of semesters
                     * 2- store them on Semester Object
                     * 3- store the semester on an arraylist
                     * */
                    if(Integer.valueOf(respValue) != 0){
                        JSONArray jsonArrayGrades=json.getJSONArray("Semesters");
                        Gson gson = new Gson();
                        // Get the type of User class ..
                        Type collectionType = new TypeToken<Semesters>() {}.getType();
                        // put user data in User object
                        // get data from json object and the Type of the User class
                        for(int i=0;i<jsonArrayGrades.length();i++) {
                            JSONObject semester=jsonArrayGrades.getJSONObject(i);
                            Semesters semesters1=gson.fromJson(semester.toString(), collectionType);
                            semesters.add(semesters1);
                            sem.add(semesters1.getSemester());
                        }
                        spinner_semesters.setAdapter(new ArrayAdapter<String>(Grades.this, android.R.layout.simple_spinner_dropdown_item, sem));
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("onFailure", "MSG " + t.getMessage());
            }
        });

        return semesters;
    }

    public void fetchCours(String semester){

        if(!user.getToken().isEmpty() && !selectedSeme.isEmpty()){
            //selectedSeme=
            progressDialog.show();
        Call<ResponseBody> call = apiEndPoint.Grades("application/json", user.getToken(), semester);
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
                    JSONArray jsonArrayGrades=json.getJSONArray("Grades");

                    html += "<table class='table_' cellspacing='0' cellpadding='0' >";
                    html += "<thead>";
                    html += "<tr>";
                    html += "<td>Course Code</td>";
                    html += "<td>Course Name</td>";
                    html += "<td>Part</td>";
                    html += "<td>Hours</td>";
                    html += "<td>Attendance</td>";
                    html += "<td>MTA</td>";
                    html += "<td>QZs</td>";
                    html += "<td>FNL</td>";
                    html += "<td>TOT</td>";
                    html += "<td>Grade</td>";
                    html += "<td>Points</td>";
                    html += "</tr>";
                    html += "</thead>";
                    html += "<tbody>";

                    int semesterHours=0;
                    int points=0;
                    int avg=0;

                    for(int i=0;i<jsonArrayGrades.length();i++){
                        JSONObject grade=jsonArrayGrades.getJSONObject(i);
                        String[] s=grade.getString("GradeDetails").split(";");
                        // Attendance extract all numbers then get only the first number (because the attendance could be only 1 character)
                        String attendance=String.valueOf(s[0].replaceAll("(\\D+)","").charAt(0));
                        // MTA get all characters except /number then get all numbers
                        String MTA=       s[1].replaceAll("[^?\\D+](\\d+)","").replaceAll("(\\D+)","");
                        // quiz get all characters from first except 1 then get all characters except (/number) then get the grade
                        String quiz=      s[2].replaceAll("(^\\D+)(\\d+)(\\D+)","").replaceAll("[^?\\D+](\\d+)","").replaceAll("(\\D+)","");
                        // final
                        String Final=     s[3].replaceAll("[^?\\D+](\\d+)","").replaceAll("(\\D+)","");
                        int total=Integer.valueOf(attendance)+Integer.valueOf(MTA)+Integer.valueOf(quiz)+Integer.valueOf(Final);

                        html += "<tr class='cls_"+i+"'>";
                        html += "<td>"+grade.getString("Course")+"</td>"; // course code
                        html += "<td>"+grade.getString("Course")+"</td>"; // course full name
                        html += "<td>"+1+"</td>"; // part
                        html += "<td>"+grade.get("Credits")+"</td>"; // hours
                        html += "<td>"+attendance+"</td>"; // Attendance
                        html += "<td>"+MTA+"</td>"; //MTA
                        html += "<td>"+quiz+"</td>"; //QZs
                        html += "<td>"+Final+"</td>";//FNL
                        html += "<td>"+total+"</td>"; // total grade
                        html += "<td>"+grade.getString("Grade")+"</td>"; // A,B+,B
                        html += "<td>"+getPoints(grade.getString("Grade"),Integer.valueOf(grade.getString("Credits")))+"</td>"; // points of grade
                        html += "</tr>";

                        semesterHours+=Integer.valueOf(grade.getString("Credits"));
                    }

                    html += "</tbody>";
                    html += "<tfoot>";
                    html += "<tr>";
                    html += "<td colspan='3'>Sum of semester hours:</td>";
                    html += "<td>"+semesterHours+"</td>";
                    html += "<td colspan='2'>Sum of semester point:</td>";
                    html += "<td>"+points+"</td>";
                    html += "<td colspan='3'>Semester average:</td>";
                    html += "<td>"+avg+"</td>";
                    html += "</tr>";
                    html += "<tr>";
                    html += "<td colspan='3'>QHRS:</td>";
                    html += "<td></td>";
                    html += "<td colspan='2'>QPTS:</td>";
                    html += "<td>36</td>";
                    html += "<td colspan='3'>GPA:</td>";
                    html += "<td>4</td>";
                    html += "</tr>";
                    html += "</tfoot>";
                    html += "</table>";
                    html +="<style>" +
                            "table td{padding: 10px; }" +
                            "table thead tr td,table tfoot tr td{background: #3465A4; color: #fff; }" +
                            "table tbody tr.cls_0 td{background: #eee; " +
                            "} </style>" ;
                    // view in a web view
                    wbv.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                    //wbv_printer.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                    wbv.zoomOut();
                    // if there's something wrong ....
                    } else{
                        html="<br><h3>"+res+"..</h3>";
                        wbv.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                        wbv_printer.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                        wbv.zoomOut();
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
            wbv.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
            //wbv_printer.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);

            wbv.zoomOut();
            progressDialog.dismiss();
        }
    }

    public double getPoints(String grade,int hours){
        switch (hours){
            case 3:
                switch (grade){
                    case "A":return 12;
                    case "B+":return 10.5;
                    case "B":return 12;
                    case "C+":return 12;
                    case "C":return 12;
                    case "D":return 12;
                }
                break;
            case 4:
                switch (grade){
                    case "A":return 16;
                    case "B+":return 10.5;
                    case "B":return 12;
                    case "C+":return 12;
                    case "C":return 12;
                    case "D":return 12;
                }
                break;
            case 5:
                switch (grade){
                    case "A":return 20;
                    case "B+":return 10.5;
                    case "B":return 12;
                    case "C+":return 12;
                    case "C":return 12;
                    case "D":return 12;
                }
                break;
            case 8:
                switch (grade){
                    case "A":return 32;
                    case "B+":return 10.5;
                    case "B":return 12;
                    case "C+":return 12;
                    case "C":return 12;
                    case "D":return 12;
                }
                break;
        }
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createPdf(View view){
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


        wbv_printer.loadDataWithBaseURL("file:///android_asset/", print, "text/html", "UTF-8", null);

        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);

        PrintDocumentAdapter printAdapter = wbv_printer.createPrintDocumentAdapter("SIS");

        String jobName = getString(R.string.app_name) + "Grade_";

        printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
    }

    public void onOptionsItemSelected(View v){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        finish();
    }
}
