package com.example.sis.studentComplaint;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.sis.Network.APIEndPoint;
import com.example.sis.Network.NetworkUtils;
import com.example.sis.R;
import com.example.sis.User;
import com.example.sis.studentComplaint.adapter.ComplaintAdapter;
import com.example.sis.studentComplaint.adapter.item.ComplaintItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class Complaint extends AppCompatActivity {
    SharedPreferences pref;
    private NetworkUtils networkUtils;
    private User user;
    private ArrayList<ComplaintItem> complaintItems;
    private ListView complaintListView;
    private ProgressDialog progressDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);
        // progressDialog
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
        networkUtils=new NetworkUtils();
        complaintItems=new ArrayList<>();
        complaintListView=findViewById(R.id.complaint_listView);
        getStudentComplaints();

        complaintListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent complaintView=new Intent(Complaint.this, ComplaintView.class);
                complaintView.putExtra("complaint",complaintItems.get(position).toString());
                startActivity(complaintView);
            }
        });

    }

    @EverythingIsNonNull
    void getStudentComplaints(){
        progressDialog.show();
        APIEndPoint apiEndPoint=networkUtils.getApiEndPoint();
        Call<ResponseBody> call=apiEndPoint.GetStudentComplaints("application/json", user.getToken());
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
                        JSONArray Complaints=json.getJSONArray("Complaints");
                        Gson gson = new Gson();
                        // Get the type of User class ..
                        Type collectionType = new TypeToken<ComplaintItem>() {}.getType();
                        for (int i=0;i<Complaints.length();i++){
                            ComplaintItem complaintItem;
                            complaintItem=gson.fromJson(Complaints.getJSONObject(i).toString(), collectionType);
                            complaintItems.add(complaintItem);
                        }
                        ComplaintAdapter adapter=new ComplaintAdapter(getApplicationContext(),complaintItems);
                        complaintListView.setAdapter(adapter);
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

    // go back to choose activity
    public void onOptionsItemSelected(View v){
        super.onBackPressed();
    }

    public void insert_complaint(View view) {
        startActivity(new Intent(this,InsertNewComplaint.class));
    }
}