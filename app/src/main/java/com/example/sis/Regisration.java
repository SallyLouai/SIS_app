package com.example.sis;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.sis.Network.APIEndPoint;
import com.example.sis.Network.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Regisration extends AppCompatActivity {
    private SharedPreferences pref;
    private NetworkUtils networkUtils;
    APIEndPoint apiEndPoint;
    private User user;
    private int Done;

    private Spinner cours_spiner;
    private Spinner filieres_spiner;

    private int selected_course_id;
    private int selected_filiere_id;
    private String selected_semester = "First";

    private ArrayList<Cours> All_Course;
    private ArrayList<Filiere> All_Filiere;

    private EditText academyyear;
    private Spinner sems;

    private ProgressDialog progressDialog;


    private ListView registr_lsview;
    ArrayList<register_item> items = new ArrayList<register_item>();
    private MyCustomAdapter myadpter;
    private int Position_Last_Item = -1;

    private WebView wbv_printer;
    private TextView chose_course;
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regisration);
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

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("loading");
        progressDialog.setMessage("getting data from server please wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(getResources().getDrawable(R.drawable.logo));
        progressDialog.setCancelable(false);

        chose_course = findViewById(R.id.textView20);
        button = findViewById(R.id.button);

        networkUtils = new NetworkUtils();
        apiEndPoint = networkUtils.getApiEndPoint();

        filieres_spiner = findViewById(R.id.filieres_spiner);
        cours_spiner = findViewById(R.id.cours_spiner);

        academyyear = findViewById(R.id.academyyear);
        sems = findViewById(R.id.sems_spiner);
        sems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch ( Integer.parseInt(id+"") ){
                    case 0:
                        selected_semester = "First";
                        break;
                    case 1:
                        selected_semester = "Second";
                        break;
                    case 2:
                        selected_semester = "Summer";
                        break;
                    default:
                        selected_semester = "First";
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //regwbv = findViewById(R.id.regwbv);
        //regwbv.getSettings().setBuiltInZoomControls(true);
        wbv_printer = view.findViewById(R.id.wbv_printer);
        registr_lsview = findViewById(R.id.registr_lsview);

        registr_lsview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if( position > 0 ) {
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Regisration.this);
                        builder.setMessage("في حال اخترت التعديل وترغب بإلغاء العملية يرجى النقر على زر الإلغاء بالأسفل !")
                                .setIcon(R.drawable.down_all)
                                .setTitle("تحديث المقرر")
                                .setPositiveButton("إسقاط", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                    Position_Last_Item = items.get(position).id;
                                    //items.remove(position);
                                    //myadpter.notifyDataSetChanged();
                                    progressDialog.show();
                                    Call<ResponseBody> call = apiEndPoint.Registration(
                                            "application/json",
                                            user.getStdID(),
                                            selected_course_id,
                                            academyyear.getText().toString(),
                                            Position_Last_Item+"",
                                            selected_filiere_id,
                                            "remove"
                                    );
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @SuppressLint("SetTextI18n")
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            try {
                                                if(response.code() == 200) {
                                                    message("Operation-Done");
                                                    init();
                                                }else
                                                    message("Operation-Failed");
                                            }catch (Exception e) {
                                                e.printStackTrace();
                                                message("Operation-Failed");
                                            }
                                        }
                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            Log.d("onFailure", "MSG " + t.getMessage());
                                            message("Operation-Failed");
                                        }
                                    });
                                    }
                                })
                                .setNegativeButton("تعديل", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                    register_item ri = items.get(position);

                                    //Log.d("obnFailure", "MSG : "+position+" + " +ri.toString());


                                    Position_Last_Item = ri.id;
                                    selected_course_id = ri.c_id;
                                    get_filiers();
                                    chose_course.setText("Choose Course : "+ri.code + " "+ri.cours_name);
                                    cours_spiner.setVisibility(View.GONE);
                                    button.setVisibility(View.VISIBLE);
                                    }
                                })
                                .show();
                    } catch (Exception e) {
                    }
                }
            }
        });

        /*items.add(new register_item());
        myadpter = new MyCustomAdapter(items);
        registr_lsview.setAdapter(myadpter);*/

        get_courses();
        cours_spiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_course_id = All_Course.get( Integer.parseInt(id+"") ).id;

                filieres_spiner.setAdapter(null);
                get_filiers();

                //Log.d("obnFailure", "MSG : "+id+" positin "+position+" slctd "+selected_course_id);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        filieres_spiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position >0) {
                    selected_filiere_id = All_Filiere.get(Integer.parseInt((id-1) + "")).id;
                    add_to_listview();
                    //registre_filiere("get_without_add_cours");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void send (View v){
        init_edit();
    }

    public void get_filiers(){
        //progressDialog.show();
        Call<ResponseBody> call = apiEndPoint.Registration(
                "application/json",
                user.getStdID(),
                selected_course_id,
                academyyear.getText().toString(),
                selected_semester,
                selected_filiere_id,
                "get_filiers"
        );
        call.enqueue(new Callback<ResponseBody>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.code() == 200) {
                        Document document = Jsoup.parse(response.body().string());
                        Element data_filieres = document.select("p").first();

                        Gson gson = new Gson();
                        Type collectionType2 = new TypeToken<ArrayList<Filiere>>(){}.getType();
                        All_Filiere = gson.fromJson(data_filieres.text(), collectionType2);

                        ArrayList<String> filiere_adapter = new ArrayList<>();
                        filiere_adapter.add("------------");
                        for (Filiere f : All_Filiere){
                            filiere_adapter.add(f.name);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Regisration.this,
                                android.R.layout.simple_spinner_item, filiere_adapter);
                        filieres_spiner.setAdapter(adapter);


                        Log.d("onFailure", "MSG data_branches ; " + data_filieres.text() );

                    }else{
                        Log.d("onFailure", "MSG data_branches ; " + user.getStdID() + " -- "+
                                selected_filiere_id + " -- "+
                                selected_course_id);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("onFailure", "MSG " + t.getMessage());
            }
        });
    }

    public void get_courses(){
        progressDialog.show();
        Call<ResponseBody> call = apiEndPoint.Registration(
                "application/json",
                user.getStdID(),
                selected_course_id,
                academyyear.getText().toString(),
                selected_semester,
                selected_filiere_id,
                "get_courses"
        );
        call.enqueue(new Callback<ResponseBody>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.code() == 200) {
                        Document document = Jsoup.parse(response.body().string());
                        Gson gson = new Gson();

                        Element data_items_register = document.select("span.registred").first();
                        Type collectionType = new TypeToken<ArrayList<register_item>>(){}.getType();
                        items = gson.fromJson(data_items_register.text(), collectionType);
                        items.add(0,new register_item());
                        myadpter = new MyCustomAdapter(items);
                        registr_lsview.setAdapter(myadpter);
                        myadpter.notifyDataSetChanged();

                        Element data_courses = document.select("p").first();
                        Type collectionType2 = new TypeToken<ArrayList<Cours>>(){}.getType();
                        All_Course = gson.fromJson(data_courses.text(), collectionType2);

                        ArrayList<String> course_adapter = new ArrayList<>();
                        for (Cours c : All_Course){
                            course_adapter.add(c.Code + " " +c.Name);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Regisration.this,
                                android.R.layout.simple_spinner_item, course_adapter);
                        cours_spiner.setAdapter(adapter);

                    }else{
                        Log.d("onFailure", "MSG 400 ");
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    Log.d("onFailure", "MSG " + e.getMessage() );
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("onFailure", "MSG " + t.getMessage());
            }
        });
    }

    public int add_to_listview(){
        boolean exist = false;
        int hours = 0;

        Cours c = new Cours();//= All_Course.get( selected_course_id );
        Filiere f = new Filiere();// = All_Filiere.get( selected_filiere_id );

        for (Cours cc : All_Course){
            if( cc.id == selected_course_id )
                c = cc;
        }
        for (Filiere ff : All_Filiere){
            if( ff.id == selected_filiere_id )
                f = ff;
        }
        if( f.hours > 0 )
            hours = f.hours;

        final AlertDialog.Builder builder = new AlertDialog.Builder(Regisration.this);
        builder.setIcon(R.drawable.office_icon)
                .setTitle("إضافة / تحديث مقرر");

        for ( register_item ri : items ){
            if( ri.c_id == selected_course_id && Position_Last_Item == -1 ) //&& ri.f_id == selected_filiere_id  )
                exist = true;

            if( ri.hours != null)
                hours += Integer.parseInt( ri.hours );
        }

        if(hours > 21) {
            builder.setMessage("لقد وصلت إلى الحد المسموح به من عدد الساعات \n لا يمكن إضافة وحدة أخرى !").show();
            return 0;
        }

        if(exist==true) {
            builder.setMessage("أنت مسجل في هدا المقرر بالفعل !").show();
            return 0;
        }

        String msg = "لقد اخترت : " +
                "\n" ;
            if( Position_Last_Item == -1 ) {
                msg += " مقرر : " + c.Name ;
            }
                msg += "\n" +
                " شعبة : " + f.name +
                "\n" +
                " فترة : " + f.times +
                "\n" +
                " المركز : " + f.center +
                "\n" +
                " عدد الساعات : " + f.hours +
                "\n" +
                " الجزء : " + f.part +
                "\n" +
                " القاعة : " + f.salle +
                " \n";

        if( Position_Last_Item != -1 ) {
            msg += "هل ترغب بالتعديل ؟";
        }else{
            msg += "هل ترغب باﻹضافة ؟";
            msg += "\n \n" +
                    "بضغطك على نعم فأنت تقر وتوافق على الشروط !.";
        }

        builder.setMessage(msg)
                .setPositiveButton("نعم", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        progressDialog.show();
                        Call<ResponseBody> call = apiEndPoint.Registration(
                                "application/json",
                                user.getStdID(),
                                selected_course_id,
                                academyyear.getText().toString(),
                                Position_Last_Item+"",
                                selected_filiere_id,
                                "save"
                        );
                        call.enqueue(new Callback<ResponseBody>() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    if(response.code() == 200) {
                                        message("Operation-Done");
                                        init();
                                    }else
                                        message("Operation-Failed");
                                }catch (Exception e) {
                                    e.printStackTrace();
                                    message("Operation-Failed");
                                }
                            }
                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.d("onFailure", "MSG " + t.getMessage());
                                message("Operation-Failed");
                            }
                        });

                        Position_Last_Item = -1;
                    }
                })
                .setNegativeButton("لا", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Position_Last_Item = -1;
                    }
                })
                .setNeutralButton("الشروط", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        AlertDialog.Builder b = new AlertDialog.Builder(Regisration.this);
                        b.setMessage("أتعهد بان أستلم حقيبتي التعليمية و إلا سوف يسقط حقي بالحصول عليها\n" +
                                "\n" +
                                "كما اقر بأنني على دراية تامة بأن حقيبة المقرر الدي أقوم بتسجيله بساعات معتمدة تساوي 8 ساعات قد تحتوي على جزء المقرر الأول و الثاني وبالتالي سيسقط حقي باستلام الحقيبة في جزء المقرر الثاني تلقائيا إدا لم ألتزم باستلام الحقيبة في الجزء الأول")
                                .setTitle("الشروط")
                                .setPositiveButton("الرجوع", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                      //  Position_Last_Item = -1;
                                        builder.show();
                                    }
                                }).show();
                    }
                })
                .show();

        return 0;
    }

    public void message(String cas){
        switch (cas){
            case "Failed-Register":

                break;
            case "Operation-Done":

                break;
            case "Operation-Failed":

                break;

        }
    }

    public void init_edit (){
        Position_Last_Item = -1;
        selected_course_id = All_Course.get(0).id;
        cours_spiner.setSelection(0);
        chose_course.setText("Choose Course");
        cours_spiner.setVisibility(View.VISIBLE);
        button.setVisibility(View.GONE);
    }

    public void init(){
        init_edit();
        All_Course = null;
        All_Filiere = null;
        items = null;
        registr_lsview.setAdapter(myadpter);
        selected_course_id = 0;
        selected_filiere_id = 0;
        Position_Last_Item = -1;

        get_courses();
    }

    public void onOptionsItemSelected(View item){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createPdf(View v){
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

        String html = "";
        html += "<table class='table_' cellspacing='0' cellpadding='0' >";
        html += "<thead>";
        html += "<tr>";
        html += "<td>المقرر</td>";
        html += "<td>العنوان</td>";
        html += "<td>الشعبة</td>";
        html += "<td>الفترة</td>";
        html += "<td>المركز</td>";
        html += "<td>عدد الساعات</td>";
        html += "<td>الجزء</td>";
        html += "<td>القاعة</td>";
        html += "</tr>";
        html += "</thead>";

        html += "<tbody>";
        int i = 0;
        int frst= 0;
        for( register_item ri : items ){

            if( frst != 0 ){
                html += "<tr class='cls_"+i+"'>";
                html += "<td>"+ri.code+"</td>";
                html += "<td>"+ri.cours_name+"</td>";
                html += "<td>"+ri.filiere_name+"</td>";
                html += "<td>"+ri.times+"</td>";
                html += "<td>"+ri.center+"</td>";
                html += "<td>"+ri.hours+"</td>";
                html += "<td>"+ri.part+"</td>";
                html += "<td>"+ri.salle+"</td>";
                html += "</tr>";

                if(i== 0)
                    i=1;
                else
                    i=0;
            }else{
                frst = 1;
            }

        }
        html += "</tbody>";
        html += "</table>";
        html +="<style>html,body,table{direction: rtl;}table{width:100%;}table td{padding: 10px; } table thead tr td,table tfoot tr td{background: #3465A4; color: #fff; } table tbody tr.cls_0 td{background: #eee; }</style>";
        print +=html;
        print+="<p style=\"text-align:center;font-weight:bold;\">Date : "+dtf.format(now)+"</p>";
        print+= "<style>table {margin: auto; width: 50%; border: none; padding: 10px;} </style>";
        wbv_printer.loadDataWithBaseURL("file:///android_asset/", print, "text/html", "UTF-8", null);



        PrintManager printManager = (PrintManager) this.getSystemService(getBaseContext().PRINT_SERVICE);

        PrintDocumentAdapter printAdapter =
                wbv_printer.createPrintDocumentAdapter("SIS");

        String jobName = getString(R.string.app_name) + "Registration_";

        printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());
    }

    /*------------------------------*/

    class register_item{
        public int id;
        public int c_id;
        public int f_id;
        public String code;
        public String cours_name;
        public String filiere_name;
        public String times;
        public String center;
        public String hours;
        public String part;
        public String salle;

        public register_item(){
        }

        public register_item(int i, int ci, int fi, String c, String cn, String fn, String t, String cntr, String h, String p, String s){
            id = i;
            c_id = ci;
            f_id = fi;
            code = c;
            cours_name = cn;
            filiere_name = fn;
            times = t;
            center = cntr;
            hours = h;
            part = p;
            salle = s;
        }

        public String toString(){
            return  "id : " + id + "c_id : " + c_id + "f_id : " + f_id + "code : " + code + "cours_name : " + cours_name + "filiere_name : " + filiere_name + "times : " + times + "center : " + center + "hours : " + hours + "part : " + part + "salle : " + salle;
        }
    }


    class MyCustomAdapter extends BaseAdapter {
        ArrayList<register_item> Items = new ArrayList<register_item>();
        MyCustomAdapter(ArrayList<register_item> itms) {
            this.Items = itms;
        }
        @Override
        public int getCount() {
            return this.Items.size();
        }

        @Override
        public String getItem(int position) {
            return this.Items.get(position).code;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {

            LayoutInflater linflater = getLayoutInflater();
            View v = linflater.inflate(R.layout.register_item, null);

            TextView code = (TextView) v.findViewById(R.id.registr_code);
            //TextView cours_name = (TextView) v.findViewById(R.id.registr_cours_name);
            TextView filiere_name = (TextView) v.findViewById(R.id.registr_filiere_name);
            TextView times = (TextView) v.findViewById(R.id.registr_times);
            TextView center = (TextView) v.findViewById(R.id.registr_center);
            TextView hours = (TextView) v.findViewById(R.id.registr_hours);
            TextView part = (TextView) v.findViewById(R.id.registr_part);
            TextView salle = (TextView) v.findViewById(R.id.registr_salle);

            if( i == 0 ){
                LinearLayout wrap_fees_item = (LinearLayout) v.findViewById(R.id.wrap_plan_item);
                wrap_fees_item.setBackgroundColor( getResources().getColor(R.color.colorPrimary) );
                wrap_fees_item.setPadding(10,10,10,10);

                code.setTextColor( getResources().getColor(R.color.colorWhite) );
                //cours_name.setTextColor( getResources().getColor(R.color.colorWhite) );
                filiere_name.setTextColor( getResources().getColor(R.color.colorWhite) );
                times.setTextColor( getResources().getColor(R.color.colorWhite) );
                center.setTextColor( getResources().getColor(R.color.colorWhite) );
                hours.setTextColor( getResources().getColor(R.color.colorWhite) );
                part.setTextColor( getResources().getColor(R.color.colorWhite) );
                salle.setTextColor( getResources().getColor(R.color.colorWhite) );

                return  v;
            }

            code.setText( this.Items.get(i).code);
            //cours_name.setText( this.Items.get(i).cours_name);
            filiere_name.setText( this.Items.get(i).filiere_name);
            times.setText( this.Items.get(i).times);
            center.setText( this.Items.get(i).center);
            hours.setText( this.Items.get(i).hours);
            part.setText( this.Items.get(i).part);
            salle.setText( this.Items.get(i).salle);

            return v;
        }
    }
}
