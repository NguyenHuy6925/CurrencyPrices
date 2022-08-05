package com.example.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.currencyprices.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class PriceAdapter extends BaseAdapter {

    Activity context;
    int resource;
    ArrayList<String> rateList;
    ArrayList<String> dateList;

    public PriceAdapter (Activity context,int resource,ArrayList<String> rateList,ArrayList<String> dateList) {
        this.context=context;
        this.resource=resource;
        this.rateList=rateList;
        this.dateList=dateList;
    }

    @Override
    public int getCount() {
        return rateList.size();
    }

    @Override
    public Object getItem(int i) {
        return rateList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(rateList.get(i) != null) {
            LayoutInflater inflater = this.context.getLayoutInflater();
            View customView=inflater.inflate(this.resource,null);
            TextView txtRate = customView.findViewById(R.id.txtRate);
            TextView txtDate = customView.findViewById(R.id.txtDate);

            /*if(rateList.get(i) == null) {
                rateList.add(i,"");
                dateList.add(i,"");
            }*/

            txtRate.setText(rateList.get(i));
            txtDate.setText(dateList.get(i));

            Animation leftInAnimation = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.item_left_in);
            leftInAnimation.setStartOffset(i+300L);
            txtRate.startAnimation(leftInAnimation);

            Animation rightInAnimation = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.item_right_in);
            rightInAnimation.setStartOffset(i+300L);
            txtDate.startAnimation(rightInAnimation);
            return customView;
        }
        else {
            return view;
        }
    }
}
