package com.example.sis.studentComplaint;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.sis.Network.APIEndPoint;
import com.example.sis.Network.NetworkUtils;
import com.example.sis.R;
import com.example.sis.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class InsertNewComplaint extends AppCompatActivity {
    SharedPreferences pref;
    private NetworkUtils networkUtils;
    private User user;
    private ArrayList<NewComplaintStatuses> statuses;
    private Spinner spinner_statuses;
    private EditText name,comment;
    private String SelectedStatusId="";
    private TextView InsertComplaintMsg;
    private int CHOOSE_FILE_REQUESTCODE=200;
    private Uri uri_file;
    private ProgressDialog progressDialog;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_new_complaint);
        networkUtils=new NetworkUtils();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("loading");
        progressDialog.setMessage("getting data from server please wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(getResources().getDrawable(R.drawable.logo));
        progressDialog.setCancelable(false);

        // get user data
        pref = getSharedPreferences("user_details",MODE_PRIVATE);
        user = new User(pref.getString("UserLogin", null));
        // set custom actionbar
        ActionBar actionBar = this.getSupportActionBar();
        String classTitle = Objects.requireNonNull(actionBar.getTitle()).toString();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.custom_action_bar);
        View view = actionBar.getCustomView();
        TextView activity_title = view.findViewById(R.id.activity_title);
        activity_title.setText(classTitle);
        TextView user_id_name = view.findViewById(R.id.user_id_name);
        user_id_name.setText( user.getStdCode() + "  |  " + user.getStdArName());
        spinner_statuses=findViewById(R.id.insertComplaintSpinner);
        statuses=new ArrayList<>();
        getComplaintStatuses();
        name=findViewById(R.id.InsertComplaintStatusNameEdit);
        comment=findViewById(R.id.InsertComplaintComment);
        InsertComplaintMsg=findViewById(R.id.InsertComplaintMsg);

        spinner_statuses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectedStatusId = statuses.get(position).getId();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @EverythingIsNonNull
    void getComplaintStatuses(){
        progressDialog.show();
        APIEndPoint apiEndPoint=networkUtils.getApiEndPoint();
        Call<ResponseBody> call=apiEndPoint.GetStudentNewComplaintStatuses("application/json", user.getToken());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    assert response.body() != null;
                    Document document = Jsoup.parse(response.body().string());
                    Element p = document.select("body").first();
                    // to extract JSON FROM HTML
                    JSONObject json = new JSONObject(p.text());
                    String respValue=json.getString("RespVal");
                    String res=json.getString("RespMsg");
                    if(Integer.valueOf(respValue) != 0){
                        JSONArray NewComplaintStatuses=json.getJSONArray("NewComplaintStatuses");
                        Gson gson = new Gson();
                        // Get the type of User class ..
                        Type collectionType = new TypeToken<NewComplaintStatuses>() {}.getType();
                        ArrayList<String> statusesSpinner=new ArrayList<>();
                        for (int i=0;i<NewComplaintStatuses.length();i++){
                            NewComplaintStatuses statuse;
                            statuse=gson.fromJson(NewComplaintStatuses.getJSONObject(i).toString(), collectionType);
                            statuses.add(statuse);
                            statusesSpinner.add(statuse.getComplaintStatus());
                        }
                        spinner_statuses.setAdapter(new ArrayAdapter<String>(InsertNewComplaint.this, android.R.layout.simple_spinner_dropdown_item, statusesSpinner));

                    } else {
                        // error message
                        Toast.makeText(getApplicationContext(),res,Toast.LENGTH_LONG).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    @EverythingIsNonNull
    @SuppressLint("SetTextI18n")
    public void submit(View view) {
        progressDialog.show();
        CheckBox checkBox=findViewById(R.id.InsertComplaintCheckBox);
        if(checkBox.isChecked()){
            APIEndPoint apiEndPoint=networkUtils.getApiEndPoint();
            Call<ResponseBody> call=apiEndPoint.InsertStudentComplaint("application/json", user.getToken(),SelectedStatusId,name.getText().toString(),comment.getText().toString());
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try{
                        assert response.body() != null;
                        Document document = Jsoup.parse(response.body().string());
                        Element p = document.select("body").first();
                        // to extract JSON FROM HTML
                        JSONObject json = new JSONObject(p.text());
                        String respValue=json.getString("RespVal");
                        String res=json.getString("RespMsg");
                        if(Integer.valueOf(respValue) != 0){
                            InsertComplaintMsg.setVisibility(View.VISIBLE);
                            InsertComplaintMsg.setText(res);
                            InsertComplaintMsg.setTextColor(getResources().getColor(R.color.colorGreen));
                            sendToMail(uri_file);
                        } else {
                            InsertComplaintMsg.setVisibility(View.VISIBLE);
                            InsertComplaintMsg.setText(res);
                            InsertComplaintMsg.setTextColor(getResources().getColor(R.color.colorRed));
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }else {
            InsertComplaintMsg.setVisibility(View.VISIBLE);
            InsertComplaintMsg.setText("Please check the confirmation box before submit the complaint");
            InsertComplaintMsg.setTextColor(getResources().getColor(R.color.colorRed));
        }

    }

    public void ChooseFile(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        Intent fileIntent = Intent.createChooser(intent, "File");
        if (fileIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(fileIntent,CHOOSE_FILE_REQUESTCODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CHOOSE_FILE_REQUESTCODE) {
            assert data != null;
            uri_file= data.getData();
            try {
                assert uri_file != null;
                InputStream input_File = getContentResolver().openInputStream(uri_file);
            } catch (FileNotFoundException f) {
                Log.d("file not found:", Objects.requireNonNull(f.getMessage()));
            }
        }
    }

    void sendToMail(Uri uri_file){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // set the type to 'email'
        emailIntent.setType("vnd.android.cursor.dir/email");
        String[] to = {"Sally1sall1@gmail.com"};
        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
        // the attachment
        emailIntent .putExtra(Intent.EXTRA_STREAM, uri_file);
        // the mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Complaint attachment");
        startActivity(Intent.createChooser(emailIntent , "Send email..."));
    }

    class NewComplaintStatuses{
        private String Id;
        private String ComplaintStatus;
        String getId() {
            return Id;
        }
        void setId(String id) {
            Id = id;
        }
        String getComplaintStatus() {
            return ComplaintStatus;
        }
        void setComplaintStatus(String complaintStatus) {
            ComplaintStatus = complaintStatus;
        }
    }

    // go back to choose activity
    public void onOptionsItemSelected(View v){
        super.onBackPressed();
    }
}
