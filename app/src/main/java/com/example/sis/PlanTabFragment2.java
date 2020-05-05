package com.example.sis;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.sis.Network.APIEndPoint;
import com.example.sis.Network.NetworkUtils;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanTabFragment2 extends Fragment {
    private NetworkUtils networkUtils;
    private APIEndPoint apiEndPoint;
    public PrintManager printManager;
    public User user;
    public View view;
    public LayoutInflater linflater;
    private Spinner spiner_type;
    private Spinner spiner_schools;
    private ArrayList<Cours> courses ;
    private WebView wbv;
    private WebView wbv_printer;
    private String html="";
    private String selectedType = "All";
    private String selectedGroup = "";
    private ArrayList<String> groupsList;
    private ArrayList<String> Types;
    private ProgressDialog progressDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        linflater = inflater;
        View v = inflater.inflate(R.layout.plan_tab_fragment_2, container, false);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("loading");
        progressDialog.setMessage("getting data from server please wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(getResources().getDrawable(R.drawable.logo));
        progressDialog.setCancelable(false);

        view = v;
        groupsList=new ArrayList<>();
        Types=new ArrayList<>();
        Types.add("All");

        networkUtils = new NetworkUtils();
        apiEndPoint = networkUtils.getApiEndPoint();
        spiner_type = v.findViewById(R.id.spiner_type);
        spiner_schools = v.findViewById(R.id.spinner_school);
        wbv = v.findViewById(R.id.plan_wbv);
        wbv.getSettings().setBuiltInZoomControls(true);
        wbv_printer = v.findViewById(R.id.wbv_printer2);
        groupsList=fetchGroups();

        // set default spinner positions ..
        spiner_schools.setSelection(0);
        if(groupsList.size()>0 && Types.size()>0)
            printPlan(groupsList.get(0),Types.get(0));

        spiner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!groupsList.isEmpty() && !Types.isEmpty()){
                    selectedType=Types.get(position);
                    printPlan(selectedGroup,selectedType);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        spiner_schools.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               if(!groupsList.isEmpty() && !Types.isEmpty()){
                   selectedGroup=groupsList.get(position);
                   printPlan(selectedGroup,selectedType);
               }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ImageButton print = v.findViewById(R.id.plan_details_print);
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CreatePDF();
                }
                /*PrintDocumentAdapter printAdapter = wbv_printer.createPrintDocumentAdapter("SIS");
                String jobName = "Plan_";
                printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());*/
            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    private ArrayList<String> fetchGroups(){
        final ArrayList<String> groupsNames=new ArrayList<>();

        Call<ResponseBody> call = apiEndPoint.studentPlanCourse("application/json", user.getToken());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    // get response and store it in Document object
                    assert response.body() != null;
                    Document document = Jsoup.parse(response.body().string());
                    Element p = document.select("body").first();
                    // to extract JSON FROM HTML
                    JSONObject json = new JSONObject(p.text());
                    String respValue=json.getString("RespVal");
                    String res=json.getString("RespMsg");
                    if(Integer.valueOf(respValue) != 0) {
                        JSONArray jsonArray = json.getJSONArray("Courses");
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject object=jsonArray.getJSONObject(i);
                            if (!groupsNames.contains(object.getString("GroupName"))){
                                groupsNames.add(object.getString("GroupName"));
                                Types.add(object.getString("Status"));
                            }
                        }
                            try {
                                if(getActivity() != null){
                                    spiner_schools.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, groupsNames));
                                    spiner_type.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, Types));
                                }

                            }catch (NullPointerException n){
                                Log.e("Student Plan error ", Objects.requireNonNull(n.getMessage()));
                                }
                            }else{
                        html = "<br><h3>"+res+" ....</h3>";
                        wbv.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                        wbv_printer.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("onFailure", "MSG " + t.getMessage());
            }
        });
        return groupsNames;
    }

    private void printPlan(final String GName,final String Type){
        progressDialog.show();
        Call<ResponseBody> call = apiEndPoint.studentPlanCourse("application/json", user.getToken());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                html = "";
                try {
                    // get response and store it in Document object
                    assert response.body() != null;
                    Document document = Jsoup.parse(response.body().string());
                    Element p = document.select("body").first();
                    // to extract JSON FROM HTML
                    JSONObject json = new JSONObject(p.text());
                    String respValue = json.getString("RespVal");
                    String res = json.getString("RespMsg");
                    if (Integer.valueOf(respValue) != 0) {
                        html += "<table class='table_' cellspacing='0' cellpadding='0' >";
                        html += "<thead>";
                        html += "<tr>";
                        html += "<td>Group Name</td>";
                        html += "<td>Course Code</td>";
                        html += "<td>Course Name</td>";
                        html += "<td>Credits</td>";
                        html += "<td>Status</td>";
                        html += "<td>Grade</td>";
                        html += "<td>Completion Year</td>";
                        html += "<td>Completion Sem</td>";
                        html += "</tr>";
                        html += "</thead>";
                        html += "<tbody>";
                        JSONArray jsonArray = json.getJSONArray("Courses");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            if (object.getString("GroupName").equals(GName)){
                                switch (Type){
                                    case "All":
                                        html += "<tr>";
                                        html += "<td>"+object.getString("GroupName")+"</td>";
                                        html += "<td>"+object.getString("CourseCode")+"</td>";
                                        html += "<td>"+object.getString("CourseName")+"</td>";
                                        html += "<td>"+object.getString("Credits")+"</td>";
                                        html += "<td>"+object.getString("Status")+"</td>";
                                        html += "<td>"+object.getString("Grade")+"</td>";
                                        html += "<td>"+object.getString("CompletionYear")+"</td>";
                                        html += "<td>"+object.getString("CompletionSem")+"</td>";
                                        html += "</tr>";
                                        break;
                                    default:
                                        if(object.getString("Status").equals(Type)){
                                            html += "<tr>";
                                            html += "<td>"+object.getString("GroupName")+"</td>";
                                            html += "<td>"+object.getString("CourseCode")+"</td>";
                                            html += "<td>"+object.getString("CourseName")+"</td>";
                                            html += "<td>"+object.getString("Credits")+"</td>";
                                            html += "<td>"+object.getString("Status")+"</td>";
                                            html += "<td>"+object.getString("Grade")+"</td>";
                                            html += "<td>"+object.getString("CompletionYear")+"</td>";
                                            html += "<td>"+object.getString("CompletionSem")+"</td>";
                                            html += "</tr>";
                                        }
                                }

                            }
                        }
                    }else{
                        html = "<br><h3>"+res+" ....</h3>";
                    }
                    // footer
                    html += "</tbody>";
                    html += "</table>";
                    html +="<style>table td{padding: 10px; } table thead tr td,table tfoot tr td{background: #3465A4; color: #fff; } table tbody tr.cls_0 td{background: #eee; }</style>";
                    wbv.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                    //printManager
                    //wbv_printer.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
                } catch (JSONException | IOException e) {
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
    private void CreatePDF(){
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

        PrintManager printManager = (PrintManager) Objects.requireNonNull(getActivity()).getSystemService(Context.PRINT_SERVICE);

        PrintDocumentAdapter printAdapter = wbv_printer.createPrintDocumentAdapter("SIS");

        String jobName = getString(R.string.app_name) + "Grade_";

        if (printManager != null) {
            printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
        }
    }
}
