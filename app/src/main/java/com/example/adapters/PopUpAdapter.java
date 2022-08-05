package com.example.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.currencyprices.R;
import com.example.models.Countries;
import com.example.models.Currencies;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class PopUpAdapter extends BaseAdapter implements Filterable{

    Activity context;
    ArrayList<Currencies> currenciesList;
    ArrayList<String> originalCodeList;
    ArrayList<String> displayCodeList;

    public PopUpAdapter(Activity context,ArrayList<Currencies> currenciesList){
        this.context=context;
        this.currenciesList = currenciesList;
        originalCodeList = new ArrayList<>();
        displayCodeList = new ArrayList<>();
        if(this.currenciesList.size()==0) {
            Toast.makeText(context, "currency list empty", Toast.LENGTH_SHORT).show();
        }
        else{
            for(int i =0;i<currenciesList.size();i++){
                originalCodeList.add(this.currenciesList.get(i).getCode());
                displayCodeList.add(this.currenciesList.get(i).getCode());
            }
        }

    }

    @Override
    public int getCount() {
        return displayCodeList.size();
    }

    @Override
    public Object getItem(int i) {
        return displayCodeList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public class ViewHolder{
        LinearLayout container;
        TextView txtPopUpItem;
    }

    private String returnCode(String code){
        return code;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = this.context.getLayoutInflater();
        ViewHolder viewHolder =null;
        if(view ==null){
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.item_popup,null);
            viewHolder.container = view.findViewById(R.id.llContainer);
            viewHolder.txtPopUpItem = view.findViewById(R.id.txtPopupItem);
            view.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.txtPopUpItem.setText(displayCodeList.get(i));
        return view;
    }


    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                ArrayList<String> displayList = new ArrayList<>();
                if(charSequence == null || charSequence.length()==0){
                    filterResults.count = currenciesList.size();
                    filterResults.values = originalCodeList;
                }
                else {
                    charSequence = charSequence.toString().toLowerCase();
                    for(int i=0;i<originalCodeList.size();i++) {
                        String data = originalCodeList.get(i);
                        if(data.toLowerCase().startsWith(charSequence.toString())) {
                            displayList.add(originalCodeList.get(i));
                        }
                    }
                    filterResults.count = displayList.size();
                    filterResults.values = displayList;
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                displayCodeList = (ArrayList<String>) filterResults.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }
}
