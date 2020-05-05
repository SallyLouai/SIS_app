package com.example.sis;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.sis.Network.APIEndPoint;
import com.example.sis.Network.NetworkUtils;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanTabFragment1 extends Fragment {
    private NetworkUtils networkUtils;
    public User user;

    public View view;
    public LayoutInflater linflater;
    private String html;
    private ProgressDialog progressDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       // super.onCreateView(inflater, container, savedInstanceState);
        linflater = inflater;
        View v = inflater.inflate(R.layout.plan_tab_fragment_1, container, false);
        view = v;
        html="";
        networkUtils = new NetworkUtils();
        APIEndPoint apiEndPoint = networkUtils.getApiEndPoint();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("loading");
        progressDialog.setMessage("getting data from server please wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIcon(getResources().getDrawable(R.drawable.logo));
        progressDialog.setCancelable(false);
        progressDialog.show();
        Call<ResponseBody> call = apiEndPoint.studentPlanGroup("application/json", user.getToken());
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
                    String respValue=json.getString("RespVal");
                    String res=json.getString("RespMsg");
                    if(Integer.valueOf(respValue) != 0){
                        JSONArray Groups=json.getJSONArray("Groups");
                        ArrayList<plan_item> items = new ArrayList<>();
                        // to put the names of the columns ...
                        items.add(0,new plan_item("",0,0,0 ));
                        for (int i=0;i<Groups.length();i++){
                            JSONObject jsonObject = Groups.getJSONObject(i);
                            items.add(new plan_item(jsonObject.getString("GroupName"),
                                    Integer.valueOf(jsonObject.getString("Total")),
                                    Integer.valueOf(jsonObject.getString("Completed")),
                                    Integer.valueOf(jsonObject.getString("Remaining"))));
                        }
                        final MyCustomAdapter myadpter = new MyCustomAdapter(items);
                        myadpter.notifyDataSetChanged();

                        ListView plan_lstview = view.findViewById(R.id.plan_lstview);
                        plan_lstview.setAdapter(myadpter);

                    }else{
                        Snackbar.make(view, res, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("onFailure", "MSG --- " + e.getMessage());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("onFailure", "MSG " + t.getMessage());
            }
        });

        return v;
    }




    class plan_item{
         String label;
         int H_total;
         int H_completed;
         int H_remaining;

         plan_item(String l, int t, int c, int r){
            label = l;
            H_total = t;
            H_completed = c;
            H_remaining = r;
        }
    }


    class MyCustomAdapter extends BaseAdapter {
        ArrayList<plan_item> Items ;
        MyCustomAdapter(ArrayList<plan_item> Items) {
            this.Items = Items;
        }
        @Override
        public int getCount() {
            return Items.size();
        }

        @Override
        public String getItem(int position) {
            return Items.get(position).label;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint({"ViewHolder", "InflateParams", "SetTextI18n"})
        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            //LayoutInflater linflater = (LayoutInflater) view.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = convertView;

            v = linflater.inflate(R.layout.plan_item, null);
            Context context = v.getContext();

            TextView t  =    v.findViewById(R.id.plan_label);
            TextView th =   v.findViewById(R.id.total_hours);
            TextView ch =   v.findViewById(R.id.completed_hours);
            TextView rh =   v.findViewById(R.id.remaining_hours);

            if( i == 0 || i == getCount() - 1 ){
                LinearLayout wrap_fees_item = (LinearLayout) v.findViewById(R.id.wrap_plan_item);
                wrap_fees_item.setBackgroundColor( getResources().getColor(R.color.colorPrimary) );
                wrap_fees_item.setPadding(5,5,5,5);
                t.setTextColor( getResources().getColor(R.color.colorWhite) );
                t.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                th.setTextColor( getResources().getColor(R.color.colorWhite) );
                ch.setTextColor( getResources().getColor(R.color.colorWhite) );
                rh.setTextColor( getResources().getColor(R.color.colorWhite) );

                if( i == getCount() - 1 ){
                    t.setText( Items.get(i).label );
                    th.setText( Items.get(i).H_total + "" );
                    ch.setText( Items.get(i).H_completed + "" );
                    rh.setText( Items.get(i).H_remaining + "" );
                }

                return  v;
            }

            t.setText( Items.get(i).label );
            th.setText( Items.get(i).H_total + "" );
            ch.setText( Items.get(i).H_completed + "" );
            rh.setText( Items.get(i).H_remaining + "" );

            return v;
        }
    }
}