package com.example.sis;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sis.Network.APIEndPoint;
import com.example.sis.Network.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.lang.reflect.Type;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Login extends AppCompatActivity {
    private SharedPreferences pref;
    private SharedPreferences prefs;
    public static final String PREFS_NAME = "rememberMe";
    public static final String PREF_USERNAME = "username";
    public static final String PREF_PASSWORD = "password";
    private static final String PREF_CHECK = "checked";

    User user;
    private TextView errorText;
    private EditText username;
    private EditText password;
    private NetworkUtils networkUtils;
    private CheckBox remember;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("loading");
        progressDialog.setMessage("getting data from server please wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(getResources().getDrawable(R.drawable.logo));
        progressDialog.setCancelable(false);

        remember=findViewById(R.id.remember);
        networkUtils = new NetworkUtils();
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        errorText = findViewById(R.id.error_text);

        // if the user is already logged in don't ask for username and password.
        pref = getSharedPreferences("user_details",MODE_PRIVATE);
        if( pref.contains("UserLogin") ){
            openMain();
        }
        // to remember the user :)
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.contains(PREF_USERNAME) && prefs.contains(PREF_PASSWORD)){
            username.setText(prefs.getString(PREF_USERNAME,""));
            password.setText(prefs.getString(PREF_PASSWORD,""));
            remember.setChecked(prefs.getBoolean(PREF_CHECK,false));
        }
    }

    @SuppressLint("SetTextI18n")
    public void submit(View view) {
        progressDialog.show();
        errorText.setText( "" );
        // ensure that the username and password are not null..
        if (!username.getText().toString().isEmpty() && !password.getText().toString().isEmpty()){
        APIEndPoint apiEndPoint = networkUtils.getApiEndPoint();
        // send request to PHP file to check username and password using apiEndPoint login method..
        Call<ResponseBody> call = apiEndPoint.login("application/json", username.getText().toString(), password.getText().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                        assert response.body() != null;
                        Document doc = Jsoup.parse(response.body().string());
                        // Store server response into an element object (unprocessed text)...
                        Element p = doc.select("body").first();
                        // to extract JSON FROM HTML
                        JSONObject json = new JSONObject(p.text());
                        // check if the response is for successful login
                        if (json.toString().contains("Token")) {
                            Gson gson = new Gson();
                            // Get the type of User class ..
                            Type collectionType = new TypeToken<User>() {}.getType();
                            // put user data in User object
                            // get data from json object and the Type of the User class
                            user = gson.fromJson(json.toString(), collectionType);
                            user.setPassword(password.getText().toString());
                            // put user credentials in shared preferences (to use them in auto-login)
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("UserLogin",user.toString());
                            editor.apply();
                            rememberme();
                            // open main page
                            openMain();
                        } else {
                            // show invalid credentials message
                            user = null;
                            // Store the response in a variable :
                            String errMsg=json.getString("RespMsg");
                            errorText.setText(errMsg);
                        }
                } catch (IOException e) {
                    e.printStackTrace();
                    errorText.setText( "Failed : " + e.getMessage());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("onFailure", "MSG " + t.getMessage());
                errorText.setText( "Failed : " + t.getMessage());
            }
        });
        }else{
            errorText.setText("Please Fill the required fields !");
        }
        progressDialog.dismiss();
    }

    public void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    public void forget(View view){
        startActivity(new Intent(this,ForgetPassword.class));
        onPause();
    }
    private void rememberme(){
        //  remember the user when the remember me checkbox is clicked
        // when the user clicked on remember me
        boolean checked = remember.isChecked();
        SharedPreferences.Editor edit=  prefs.edit();
        if (checked){
            edit.putString(PREF_USERNAME, username.getText().toString());
            edit.putString(PREF_PASSWORD, password.getText().toString());
            edit.putBoolean(PREF_CHECK,checked);
            edit.apply();
        }
        else {
            edit.clear().apply();
        }
    }

}