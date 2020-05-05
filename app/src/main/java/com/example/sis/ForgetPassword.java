package com.example.sis;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sis.Network.APIEndPoint;
import com.example.sis.Network.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgetPassword extends AppCompatActivity {
    private NetworkUtils networkUtils;
    private EditText STD_ID, STD_Civil;
    private TextView Message;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        networkUtils = new NetworkUtils();
        STD_ID=findViewById(R.id.STD_ID);
        STD_Civil =findViewById(R.id.Civil_ID);
        Message=findViewById(R.id.message);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("loading");
        progressDialog.setMessage("getting data from server please wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(getResources().getDrawable(R.drawable.logo));
        progressDialog.setCancelable(false);

    }

    @SuppressLint("SetTextI18n")
    public void resetPassword(View view){
        progressDialog.show();
        APIEndPoint apiEndPoint = networkUtils.getApiEndPoint();
        if (!STD_ID.getText().toString().isEmpty() && !STD_Civil.getText().toString().isEmpty()){
        Call<ResponseBody> call = apiEndPoint.forgetPassword("application/json", STD_ID.getText().toString(), STD_Civil.getText().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try{
                        assert response.body() != null;
                        Document doc = Jsoup.parse(response.body().string());
                        // Store server response into an element object (unprocessed text)...
                        Element p = doc.select("body").first();
                        // to extract JSON FROM HTML
                        JSONObject json = new JSONObject(p.text());
                        String errMsg=json.getString("RespMsg");
                        Message.setText(errMsg);
                    }catch (IOException e) {
                        e.printStackTrace();
                        Message.setText( "Failed : " + e.getMessage());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("onFailure", "MSG " + t.getMessage());
                Message.setText( "Failed : " + t.getMessage());
            }
        });
        } else{
            Message.setText("Please fill the required Fields !");
        }
        progressDialog.dismiss();
    }
}