package com.example.sis;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sis.Network.APIEndPoint;
import com.example.sis.Network.NetworkUtils;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile extends AppCompatActivity {
    private SharedPreferences pref;
    private NetworkUtils networkUtils;
    private User user;

    private TextView full_name_ar;
    private TextView full_name_en;
    private TextView program_name_ar;
    private TextView program_name_en;
    private EditText email;
    private TextView civilID;
    private EditText mobile;
    private TextView adresse;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("loading");
        progressDialog.setMessage("getting data from server please wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(getResources().getDrawable(R.drawable.logo));
        progressDialog.setCancelable(false);

        pref = getSharedPreferences("user_details",MODE_PRIVATE);
        user = new User(pref.getString("UserLogin", null ));

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


        full_name_ar = findViewById(R.id.full_name_ar);
        full_name_en = findViewById(R.id.full_name_en);
        program_name_ar = findViewById(R.id.program_name_ar);
        program_name_en = findViewById(R.id.program_name_en);
        email = findViewById(R.id.email);
        civilID = findViewById(R.id.civilID);
        mobile = findViewById(R.id.mobile);
        adresse = findViewById(R.id.adresse);

        full_name_ar.setText(user.getStdArName());
        full_name_en.setText(user.getStdEnName());
        program_name_ar.setText(user.getStdTrack());
        program_name_en.setText(user.getStdTrack());
        email.setText(user.getStdUniversityEmail());
        civilID.setText(user.getStdCivilID());
        mobile.setText("0000000000");
        adresse.setText("Address");
    }

    public void onOptionsItemSelected(View item){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        finish();
    }

    public void update(View item){
        networkUtils = new NetworkUtils();
        APIEndPoint apiEndPoint = networkUtils.getApiEndPoint();
        Call<ResponseBody> call = apiEndPoint.Editeprofile("application/json", user.getStdID(), email.getText().toString(), mobile.getText().toString(), adresse.getText().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String msg = "please try again ...";
                if (response.code() == 200) {
                    msg = "Done ...";
                    user.StdUniversityEmail = email.getText().toString();
                    /*
                    user.mobile = mobile.getText().toString();
                    user.adresse = adresse.getText().toString();
                    */
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("UserLogin",user.toString());
                    editor.apply();

                }
                Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("onFailure", "MSG " + t.getMessage());
                Toast.makeText(getApplicationContext(),"please try again ...", Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void change_pass(View view){
        startActivity(new Intent(this,changePassword.class));
        onPause();
    }
}
