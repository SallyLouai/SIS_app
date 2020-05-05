package com.example.sis;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.sis.Network.APIEndPoint;
import com.example.sis.Network.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Timetable extends AppCompatActivity {
    private SharedPreferences pref;
    private NetworkUtils networkUtils;
    APIEndPoint apiEndPoint;
    private User user;

    private Spinner centers;
    private EditText course_code;
    private ArrayList<Cours> courses ;
    private WebView wbv;
    private WebView wbv_printer;

    String selectedCenter = "2019";
    String selectedSeme = "1";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("loading");
        progressDialog.setMessage("getting data from server please wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(getResources().getDrawable(R.drawable.logo));
        progressDialog.setCancelable(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pref = getSharedPreferences("user_details",MODE_PRIVATE);
        user = new User(pref.getString("UserLogin", null ) );

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

        wbv_printer = view.findViewById(R.id.wbv_printer);

        networkUtils = new NetworkUtils();
        apiEndPoint = networkUtils.getApiEndPoint();
        centers = findViewById(R.id.cours_spiner);
        course_code = findViewById(R.id.course_code);
        wbv = findViewById(R.id.timewbv);
        wbv.getSettings().setBuiltInZoomControls(true);

        centers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCenter = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void fetchCours(){
        progressDialog.show();
        Call<ResponseBody> call = apiEndPoint.Timetable("application/json", user.getStdID(), selectedCenter, course_code.getText().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String html="";
                    if (response.code() == 200) {
                        Document document = Jsoup.parse(response.body().string());
                        Element data_cours = document.select("p").first();

                        Gson gson = new Gson();

                        Type collectionType = new TypeToken<ArrayList<Cours>>(){}.getType();
                        courses = gson.fromJson(data_cours.text(), collectionType);

                        html += "<table class='table_' cellspacing='0' cellpadding='0' >";
                        html += "<thead>";
                        html += "<tr>";
                        html += "<td>Course Code</td>";
                        html += "<td>Course Name</td>";
                        html += "<td>Part</td>";
                        html += "<td>Class No</td>";
                        html += "<td>Period</td>";
                        html += "<td>Status</td>";
                        html += "<td>Tutor</td>";
                        html += "<td>Hall</td>";
                        html += "<td>Reserved</td>";
                        html += "<td>Max Seat Allowed</td>";
                        html += "<td>Class Gender</td>";
                        html += "</tr>";
                        html += "</thead>";

                        html += "<tbody>";
                        int i = 0;
                        for( Cours c : courses ){

                            html += "<tr class='cls_"+i+"'>";
                            html += "<td>"+c.Code+"</td>";
                            html += "<td>"+c.Name+"</td>";
                            html += "<td>"+c.Part+"</td>";
                            html += "<td>"+c.class_no+"</td>";
                            html += "<td>"+c.period+"</td>";
                            html += "<td>"+c.status+"</td>";
                            html += "<td>"+c.tutor+"</td>";
                            html += "<td>"+c.hall+"</td>";
                            html += "<td>"+c.reserved+"</td>";
                            html += "<td>"+c.max_seat+"</td>";
                            html += "<td>"+c.class_gender+"</td>";
                            html += "</tr>";

                            if(i== 0)
                                i=1;
                            else
                                i=0;

                        }
                        html += "</tbody>";
                        html += "</table>";
                        html +="<style>table td{padding: 10px; } table thead tr td,table tfoot tr td{background: #3465A4; color: #fff; } table tbody tr.cls_0 td{background: #eee; }</style>";
                    } else if (response.code() == 404) {
                        html = "<br><h3>No Result ....</h3>";
                    }

                    wbv.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                    wbv_printer.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("onFailure", "MSG " + t.getMessage());
            }
        });
    }

    public void search(View v){
        fetchCours();
    }
    public void print(View view){

        PrintManager printManager = (PrintManager) this.getSystemService(getBaseContext().PRINT_SERVICE);

        PrintDocumentAdapter printAdapter =
                wbv_printer.createPrintDocumentAdapter("SIS");

        String jobName = "Grade_";

        printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());
    }

    public void onOptionsItemSelected(View item){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        finish();
    }

}