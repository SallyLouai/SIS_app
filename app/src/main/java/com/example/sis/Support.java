package com.example.sis;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.io.File;

public class Support extends AppCompatActivity {
    private SharedPreferences pref;
    private User user;





    //private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    DownloadManager manager;
    Context context;
    long downloadID;
    int download_id=1;
    int num_web;

    boolean  check_net;
    Button btn_test;


    String std_gu_app_com ="Students Guide appeals and complaints";
    String ques_ansew = "Question and answer";
    String std_tut_plag = "Students tutorial in plagiarism";

    String std_app_com = "Student appeals and complaints";
    String update_result = "الإعتراض على النتائج والتعديل";


    String std_gu_app_com_uri="https://sisonline.arabou.edu.kw/ksaeng/images/SSS%20Students%20Guide%20V3.pdf";
    String ques_ansew_uri="https://sisonline.arabou.edu.kw/ksaeng/images/Q&A.pdf";
    String std_tut_plag_uri="https://www.arabou.edu.kw/images/university/Files/plagiarism_mat.pdf";
    String std_app_com_uri="https://sisonline.arabou.edu.kw/ksaeng/pdf/Academic_Appeal__complaints-july-2018.pdf";
    String update_result_uri="https://sisonline.arabou.edu.kw/ksaeng/images/%d8%a7%d9%84%d8%a7%d8%b9%d8%aa%d8%b1%d8%a7%d8%b6%20%d8%b9%d9%84%d9%89%20%d8%a7%d9%84%d9%86%d8%aa%d8%a7%d8%a6%d8%ac%20%d9%88%d8%a7%d9%84%d8%aa%d8%b9%d8%af%d9%8a%d9%84.pdf";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
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

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final SupportPagerAdapter adapter = new SupportPagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    public void click_std_guid_app_andcom(View view) {
        check_found(std_gu_app_com_uri, std_gu_app_com);
    }
    public void click_ques_ansew(View view) {
        check_found(ques_ansew_uri, ques_ansew);
    }
    public void click_std_tut_plag(View view) {
        check_found(std_tut_plag_uri, std_tut_plag);
    }
    public void click_std_guid_study(View view) {
        num_web = 1;
        Intent intent=new Intent(getApplicationContext(),query_result.class);
        intent.putExtra("web",num_web);
        startActivity(intent);
    }
    public void click_std_pro_under(View view) {
        Toast.makeText(getApplicationContext(), "not found", Toast.LENGTH_SHORT).show();
        //check_found(std_pro_under_uri, std_pro_under,5);
    }
    public void click_std_app_com(View view) {
        check_found(std_app_com_uri, std_app_com);
    }
    public void click_update_result(View view) {
        check_found(update_result_uri, update_result);
    }
    public void click_uni_reg_std_byl(View view) {
        num_web = 2;
        Intent intent=new Intent(getApplicationContext(),query_result.class);
        intent.putExtra("web",num_web);
        startActivity(intent);
    }
    public void click_appeals_system(View view) {
        num_web =3;
        Intent intent=new Intent(getApplicationContext(),query_result.class);
        intent.putExtra("web",num_web);
        startActivity(intent);
    }
    public void click_compliant_sys(View view) {
        num_web = 4;
        Intent intent=new Intent(getApplicationContext(),query_result.class);
        intent.putExtra("web",num_web);
        startActivity(intent);
    }
    public void click_inq_sys(View view) {
        num_web = 5;
        Intent intent=new Intent(getApplicationContext(),query_result.class);
        intent.putExtra("web",num_web);
        startActivity(intent);
    }
    public void click_dis_dys_support(View view) {
        num_web = 6;
        Intent intent=new Intent(getApplicationContext(),query_result.class);
        intent.putExtra("web",num_web);
        startActivity(intent);
    }
    public void click_exam(View view) {
        num_web = 7;
        Intent intent=new Intent(getApplicationContext(),query_result.class);
        intent.putExtra("web",num_web);
        startActivity(intent);
    }

    public void onOptionsItemSelected(View item){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        finish();
    }




    public void check_found(String uri_book, String file_name) {
        File apkStorage = new File(
                Environment.getExternalStorageDirectory()+ "/download/" + file_name + ".pdf");
        if (!apkStorage.exists()) {
            // public_number_pdf=num_book;
            download(uri_book, file_name);
        } else {
            open_pdf(apkStorage);
        }
    }
    public void open_pdf(File file) {

        Uri path = Uri.fromFile(file );
        Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
        pdfIntent.setDataAndType(path , "application/pdf");
        pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try
        {
            startActivity(pdfIntent ); }
        catch (ActivityNotFoundException e)
        {
            Toast.makeText(getApplicationContext(),"No Application available to viewPDF",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void download(final String uri_select, final String pdf_name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Support.this);
        builder.setMessage("Are you download file")
                .setIcon(R.drawable.down_all)
                .setTitle("download file")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        check_net = isconnectiontointenet();
                        if (!check_net)
                        {
                            Toast.makeText(getApplicationContext(),"No internet available ", Toast.LENGTH_LONG).show();
                        }
                        else{
                            DownloadManager.Request request = new
                                    DownloadManager.Request(Uri.parse(uri_select));
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,pdf_name+".pdf");
                            manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                            downloadID= manager.enqueue(request);
                            download_id++;

                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {


                    }
                })
                .show();
        context = this;

    }
    public boolean isconnectiontointenet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                return true;
            }
        }
        return  false;
    }
}
