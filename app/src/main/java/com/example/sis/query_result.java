package com.example.sis;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class query_result extends AppCompatActivity {

    private SharedPreferences pref;
    private User user;

    WebView browser;
    LinearLayout lin_noconect;
    String uri_common;
    ProgressBar prog_web;
    boolean check_net;
    int select_web;

    EditText txt_uri;
    String std_guid_study_uri="http://www.open.ac.uk/cicp/main/";
    String uni_reg_std_byl_uri="http://arabou.edu.kw/index.php?option=com_k2&view=item&layout=item&id=98&Itemid=176&lang=en";
    String appeals_system_uri="https://sisonline.arabou.edu.kw/ksaeng/forms/NewStudentSupport.aspx";
    String compliant_sys_uri="https://sisonline.arabou.edu.kw/ksaeng/forms/NewStudentSupport2.aspx";
    String inq_sys_uri="https://sisonline.arabou.edu.kw/ksaeng/forms/NewStudentSupport6.aspx";
    String dis_dys_support_uri="https://sisonline.arabou.edu.kw/ksaeng/forms/NewStudentSupport3.aspx";
    String exam_slip="https://sisonline.arabou.edu.kw/ksaeng/forms/NewStudentSupport5.aspx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_result);
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


        lin_noconect = (LinearLayout) findViewById(R.id.lin_no_conect);
        txt_uri=(EditText)findViewById(R.id.txt_uri);
        prog_web = (ProgressBar) findViewById(R.id.progress_web);
        browser = (WebView) findViewById(R.id.web_query);
        browser.setWebViewClient(new mybrowser());
        browser.getSettings().setDisplayZoomControls(true);
        browser.getSettings().setUseWideViewPort(true);
        browser.getSettings().setBuiltInZoomControls(true);
        browser.setInitialScale(1);
        browser.getSettings().setLoadsImagesAutomatically(true);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        prog_web.setMax(100);
        browser.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                prog_web.setProgress(newProgress);
            }
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                // change_uri(uri_common);
            }
            @Override
            public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
                super.onReceivedTouchIconUrl(view, url, precomposed);
            }
        });
        Intent data = getIntent();
        select_web = data.getExtras().getInt("web");

        //check connect
        check_net = isconnectiontointenet();
        if (! check_net) {
            lin_noconect.setVisibility(View.VISIBLE);
            prog_web.setVisibility(View.GONE);
        } else {
            lin_noconect.setVisibility(View.GONE);
            prog_web.setVisibility(View.VISIBLE);
            if (select_web == 1) {
                browser.loadUrl(std_guid_study_uri);
                txt_uri.setText(std_guid_study_uri);
            } else if (select_web == 2) {
                browser.loadUrl(uni_reg_std_byl_uri);
                txt_uri.setText(uni_reg_std_byl_uri);
            }
            else if (select_web == 3) {
                browser.loadUrl(appeals_system_uri);
                txt_uri.setText(appeals_system_uri);
            }
            else if (select_web == 4) {
                browser.loadUrl(compliant_sys_uri);
                txt_uri.setText(compliant_sys_uri);
            }
            else if (select_web == 5) {
                browser.loadUrl(inq_sys_uri);
                txt_uri.setText(inq_sys_uri);
            }
            else if (select_web == 6) {
                browser.loadUrl(dis_dys_support_uri);
                txt_uri.setText(dis_dys_support_uri);
            }
            else if (select_web == 7) {
                browser.loadUrl(exam_slip);
                txt_uri.setText(exam_slip);
            }
        }


    }
    private class mybrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            uri_common = url;
            view.loadUrl(url);
            txt_uri.setText(url);
            return true;
        }
    }

    public void btn_back(View view) {
        browser.goBack();
    }
    public void ubdate(View view) {
        check_net = isconnectiontointenet();
        if (! check_net) {
            lin_noconect.setVisibility(View.VISIBLE);
            browser.setVisibility(View.GONE);
        } else {
            lin_noconect.setVisibility(View.GONE);
            browser.setVisibility(View.VISIBLE);
            browser.loadUrl(uri_common);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onOptionsItemSelected(View v){
        Intent myIntent = new Intent(getApplicationContext(), Support.class);
        startActivityForResult(myIntent, 0);
        finish();
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
