package com.example.sis;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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

import okhttp3.ResponseBody;
import retrofit2.Call;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import retrofit2.Callback;
import retrofit2.Response;

public class Fees extends AppCompatActivity {
    private SharedPreferences pref;
    private NetworkUtils networkUtils;
    private User user;
    private String html;
    private WebView fees_wbv;
    private WebView wbv_printer;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fees);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("loading");
        progressDialog.setMessage("getting data from server please wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(getResources().getDrawable(R.drawable.logo));
        progressDialog.setCancelable(false);

        // get user information
        pref = getSharedPreferences("user_details",MODE_PRIVATE);
        user = new User(pref.getString("UserLogin", null ) );
        // set custom action bar
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
        // for printer
        wbv_printer = view.findViewById(R.id.wbv_printer);
        // to make requests
        networkUtils = new NetworkUtils();
        APIEndPoint apiEndPoint = networkUtils.getApiEndPoint();
        // web view for results
        fees_wbv = findViewById(R.id.fees_wbv);
        // zoom in/out controls
        fees_wbv.getSettings().setBuiltInZoomControls(true);
        progressDialog.show();
        Call<ResponseBody> call = apiEndPoint.GetFees("application/json", user.getToken());
        call.enqueue(new Callback<ResponseBody>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    html = "";
                    // get response and store it in Document object
                    assert response.body() != null;
                    Document document = Jsoup.parse(response.body().string());
                    Element p = document.select("body").first();
                    // to extract JSON FROM HTML
                    JSONObject json = new JSONObject(p.text());
                    String respValue=json.getString("RespVal");
                    String res=json.getString("RespMsg");
                    if(Integer.valueOf(respValue) != 0){

                        html+="<table class='table_' cellspacing='0' cellpadding='0'>" +
                                "<thead>" +
                                "<tr><td>FeeType</td>" +
                                "<td>DB</td>" +
                                "<td>CR</td>" +
                                "<td>Balance</td></tr>"+
                                "</thead><tbody>";
                        JSONArray jsonArrayFees=json.getJSONArray("Fees");
                        for(int i=0;i<jsonArrayFees.length();i++) {
                            JSONObject fee = jsonArrayFees.getJSONObject(i);
                            html+=  "<tr><td>"
                                    +fee.getString("FeeType")+"</td>"+"<td>"
                                    +fee.getString("Db")+"</td><td>"
                                    +fee.getString("Cr")+"</td><td>"
                                    +fee.getString("Balance")+
                                    "</td></tr>";
                        }
                        html+="</tbody><tfoot><tr><td colspan='6'></td></tfoot></table>" +
                                "<style>" +
                                "table tfoot tr td{background: #3465A4; color: #fff; }" +
                                "table{width:100% }" +
                                "table td{padding: 10px; }" +
                                "table thead tr td,table tfoot tr td{background: #3465A4; color: #fff; }" +
                                "table tbody tr.cls_0 td{background: #eee; }" +
                                "tr:nth-child(even) {background-color: #dddddd;}" +
                                "</style>";

                        // view in a web view
                        fees_wbv.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                        fees_wbv.zoomOut();

                    }else {
                        // if there's something wrong ....
                        html="<br><h3>"+res+"..</h3>";
                        fees_wbv.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                        fees_wbv.zoomOut();
                    }
                }catch (JSONException | IOException ex) {
                    ex.printStackTrace();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("onFailure", "MSG " + t.getMessage());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createPdf(View view){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime now = LocalDateTime.now();
        // university header
        String print="<div style=\"display:block;\"><header>" +
                "<div style=\"float:left;padding-left:20px;padding-top:20px;\"><p>Arab Open University</p><p>Kingdom Of Saudi Arabia</p></div>" +
                "<div style=\"float:right;padding-right:20px;padding-top:20px;\"><p>الجامعة العربية المفتوحة </p><p>المملكة العربية السعودية</p></div>" +
                "<div style=\"text-align:center;padding-top:10px;padding-bottom:10px;\">" +
                "<img height=\"120px\" width=\"120px\"src=\"file:///android_asset/logo.png\" alt=\"Arab Open University\"></div>" +
                "</header></div>\n";
        // student name , student ID
        print+="<p style=\"text-align:center;padding-top:10px;padding-bottom:10px;font-weight:bold;\">Student Name :"+user.getStdArName()+"</p>";
        print+="<p style=\"text-align:center;font-weight:bold;\">Student ID :"+user.getStdID()+"</p>";
        // html response
        print +=html;
        //
        print+="<p style=\"text-align:center;font-weight:bold;\">Date : "+dtf.format(now)+"</p>";
        print+= "<style>table {margin: auto; width: 100%; border: none; padding: 10px;} </style>";

        PrintManager printManager = (PrintManager) this.getSystemService(getBaseContext().PRINT_SERVICE);
        wbv_printer.loadDataWithBaseURL("file:///android_asset/", print, "text/html", "UTF-8", null);

        PrintDocumentAdapter printAdapter =
                wbv_printer.createPrintDocumentAdapter("SIS");

        String jobName = getString(R.string.app_name) + "Fees_";

        printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());
    }


    public void onOptionsItemSelected(View v){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        finish();
    }

}
