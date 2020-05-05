package com.example.sis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar ;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sis.studentComplaint.Complaint;


public class MainActivity extends AppCompatActivity {

    SharedPreferences pref;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getSharedPreferences("user_details",MODE_PRIVATE);
        User user = new User(pref.getString("UserLogin", null));

        ActionBar actionBar = this.getSupportActionBar();
        String classTitle = actionBar.getTitle().toString();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.custom_action_bar);
        View view = actionBar.getCustomView();
        TextView activity_title = view.findViewById(R.id.activity_title);
        activity_title.setText(classTitle);
        TextView user_id_name = view.findViewById(R.id.user_id_name);
        user_id_name.setText(user.getStdCode() + "  |  " + user.getStdArName());
        ImageView home=view.findViewById(R.id.to_home);
        home.setImageDrawable(getDrawable(R.drawable.logo));
        home.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    public void registration(View view){
        Intent intent = new Intent(this, Regisration.class);
        startActivity(intent);
    }
    public void profile(View view){
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }

    public void grade(View view){
        Intent intent = new Intent(this, Grades.class);
        startActivity(intent);
    }

    public void fees(View view){
        Intent intent = new Intent(this, Fees.class);
        startActivity(intent);
    }

    public void support(View view){
        Intent intent = new Intent(this, Support.class);
        startActivity(intent);
    }

    public void plan(View view){
        Intent intent = new Intent(this, Plan.class);
        startActivity(intent);
    }

    public void titmetable(View view){
        Intent intent = new Intent(this, Timetable.class);
        startActivity(intent);
    }

    public void logout(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Are you want sure ?")
                .setIcon(R.drawable.logo)
                .setTitle("Logout")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        logout();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .show();
    }
    public void logout(){
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
        editor.commit();
        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    public void onOptionsItemSelected(View item){
    }

    public void timeTable(View view) {
        Intent timeTable=new Intent(this,StudentTimeTable.class);
        startActivity(timeTable);
    }

    public void change_pass(View view) {
        startActivity(new Intent(MainActivity.this,changePassword.class));
    }

    public void complaint_btn(View view) {
        startActivity(new Intent(MainActivity.this, Complaint.class));
    }

}
