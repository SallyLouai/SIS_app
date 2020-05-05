package com.example.sis;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class changePassword extends AppCompatActivity {
    private NetworkUtils networkUtils;
    private EditText oldPassword, newPassword,ConfirmNewPassword;
    private TextView Message;
    SharedPreferences pref;
    private User user;
    private ProgressDialog progressDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("loading");
        progressDialog.setMessage("getting data from server please wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(getResources().getDrawable(R.drawable.logo));
        progressDialog.setCancelable(false);

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

                networkUtils =new NetworkUtils();
                oldPassword=findViewById(R.id.old_pass);
                newPassword=findViewById(R.id.new_pass);
                ConfirmNewPassword=findViewById(R.id.con_new_pass);
                Message=findViewById(R.id.message_change);

    }

    @EverythingIsNonNull
    @SuppressLint("SetTextI18n")
    public void submit(View view){
        progressDialog.show();
    APIEndPoint apiEndPoint=networkUtils.getApiEndPoint();
        if (!oldPassword.getText().toString().isEmpty() && !newPassword.getText().toString().isEmpty() && !ConfirmNewPassword.getText().toString().isEmpty()){
            if (user.getPassword().equals(oldPassword.getText().toString())){
                if(newPassword.getText().toString().equals(ConfirmNewPassword.getText().toString())){
                    if(checkNewPassword(newPassword.getText().toString())){
                        changeUserPassword(newPassword.getText().toString());
                        Call<ResponseBody> call = apiEndPoint.changePassword("application/json",user.getToken(), oldPassword.getText().toString(), newPassword.getText().toString());
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    assert response.body() != null;
                                    Document doc = Jsoup.parse(response.body().string());
                                    // Store server response into an element object (unprocessed text)...
                                    Element p = doc.select("body").first();
                                    // to extract JSON FROM HTML
                                    JSONObject json = new JSONObject(p.text());
                                    String errMsg=json.getString("RespMsg");
                                    Message.setText(errMsg);
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.d("onFailure", "MSG " + t.getMessage());
                                Message.setText( "Failed : " + t.getMessage());
                            }
                        });
                    }
                }else{
                    Message.setText("new password fields are not matched");
                }
            }else {
                Message.setText("Old password is wrong !");
            }
        }else{
            Message.setText("Please fill the required Fields !");
        }
        progressDialog.dismiss();
    }
    @SuppressLint("SetTextI18n")
    private boolean checkNewPassword(String password){
        int defaultMinimumLength = 8;
        Pattern numberPattern = Pattern.compile("[0-9]");
        Pattern lowerCasePattern = Pattern.compile("[a-z]");
        Pattern upperCasePattern = Pattern.compile("[A-Z]");
        Pattern othersPattern = Pattern.compile("[^0-9a-zA-Z]");

         Matcher numberMtch = numberPattern.matcher("");
         Matcher lowerCaseMtch = lowerCasePattern.matcher("");
         Matcher upperCaseMtch = upperCasePattern.matcher("");
         Matcher othersMtch = othersPattern.matcher("");
        if(password.length()>=defaultMinimumLength){
            if (numberMtch.reset(password).find()){
                if (lowerCaseMtch.reset(password).find()){
                    if (upperCaseMtch.reset(password).find()){
                        if(othersMtch.reset(password).find()){
                            return true;
                        }else{
                            Message.setText("Your must use special characters!");
                            return false; }
                    }else{
                        Message.setText("Your must use Upper case letters!");
                        return false; }
                }else {
                    Message.setText("Your must use Lower case letters!");
                    return false; }
            }else{
                Message.setText("Your must use numbers!");
                return false;}
        }else{
         Message.setText("Your password is too short");
            return false; }
    }
    private void changeUserPassword(String newPassword){
        SharedPreferences pref= getSharedPreferences("user_details",MODE_PRIVATE);
        User user=new User(pref.getString("UserLogin", null ));
        user.setPassword(newPassword);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("UserLogin",user.toString());
        editor.apply();
        Intent intent= new Intent(getApplicationContext(),Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.finish();
    }

    public void onOptionsItemSelected(View item){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        finish();
    }
}
