package com.example.sis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

public class SupportTabFragment2 extends Fragment {

    WebView adviser_web;
    ProgressBar advise_bar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.support_tab_fragment_2, container, false);
        adviser_web=(WebView)rootView.findViewById(R.id.web_adviser);
        advise_bar=(ProgressBar) rootView.findViewById(R.id.adviser_bar);
        advise_bar.setMax(100);
        adviser_web.setWebViewClient(new mybrowser());
        adviser_web.getSettings().setDisplayZoomControls(true);
        adviser_web.getSettings().setUseWideViewPort(true);
        adviser_web.getSettings().setBuiltInZoomControls(true);
        adviser_web.setInitialScale(1);
        adviser_web.getSettings().setLoadsImagesAutomatically(true);
        adviser_web.getSettings().setJavaScriptEnabled(true);
        adviser_web.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        adviser_web.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                advise_bar.setProgress(newProgress);
            }
        });
        adviser_web.loadUrl("https://sisonline.arabou.edu.kw/ksaeng/forms/StudentAdviser.aspx");
        return rootView;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private class mybrowser extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //uri_common = url;
            view.loadUrl(url);
            return true;
        }
    }
}