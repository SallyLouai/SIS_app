package com.example.sis.studentComplaint.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.sis.R;
import com.example.sis.studentComplaint.adapter.item.ComplaintItem;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ComplaintAdapter extends ArrayAdapter<ComplaintItem> {
    public ComplaintAdapter(@NonNull Context context, ArrayList<ComplaintItem> items) {
        super(context, R.layout.activity_complaint_item ,items);
    }

    public class Holder{
        TextView ComplaintDate;
        TextView ComplaintType;
        TextView StudentComments;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ComplaintItem item=getItem(position);
        Holder VHolder;
        if(convertView == null){
            VHolder = new Holder();
            LayoutInflater ItemInflater=LayoutInflater.from(getContext());
            convertView=ItemInflater.inflate(R.layout.activity_complaint_item,parent,false);
            VHolder.ComplaintDate=convertView.findViewById(R.id.ComplaintDate);
            VHolder.ComplaintType=convertView.findViewById(R.id.ComplaintType);
            VHolder.StudentComments=convertView.findViewById(R.id.StudentComments);
            convertView.setTag(VHolder);
        }else {
            VHolder=(Holder) convertView.getTag();
        }
        assert item != null;
        VHolder.ComplaintDate.setText(item.getAddDate());
        VHolder.ComplaintType.setText(item.getComplaintType());
        VHolder.StudentComments.setText(item.getStudentComments());
        return convertView;
    }
}
