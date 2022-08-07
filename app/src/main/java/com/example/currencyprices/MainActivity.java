package com.example.currencyprices;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.animation.ObjectAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adapters.PopUpAdapter;
import com.example.adapters.PriceAdapter;
import com.example.broadcastreciever.mBroadCast;
import com.example.fragments.AboutFragment;
import com.example.models.Countries;
import com.example.models.Currencies;
import com.example.service.fetchDataService;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    PopupWindow popupWindow;
    public static FragmentTransaction fragmentTransaction;
    public static FragmentManager fragmentManager;
    private IntentFilter intentFilter;
    public static ArrayList<Currencies> currenciesList;
    ArrayList<Countries> countriesList;
    ArrayList<String> itemsToShowList, rateList,dateList;
    TextView txtCountryNameLeft, txtCountryNameRight, codeLeft, codeRight;
    Button btnNotification;
    ImageView imgLeft, imgRight;
    ListView lv;
    EditText searchBox;
    ListView popUpListView;
    TabHost tabHost;
    public static DatabaseHandler dataBase;
    public static PriceAdapter priceAdapter;
    PopUpAdapter mPopUpAdapter;
    String channelId1 = "id1";
    NotificationCompat.Builder builder;
    BroadcastReceiver receiver;
    ActionBarDrawerToggle drawerToggle;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    ConstraintLayout mainLoyout, currencyLayout;
    public static Fragment aboutFragment;
    FrameLayout frameLayout_above;
    public static boolean aboutFragmentShowing=false;
    Intent serviceIntent;
    ProgressDialog progressDialog;
    View topLine;
    TextView txtTap, devideLine;
    boolean animationEnded = false;
    TextView tab1Title;

    Long textViewTime, layoutTime, tabHostTime, itemTime;
    Long textViewOffsetTime,layoutOffsetTime,tabHostOffsetTime,itemOffsetTime;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addControls(); //findviewbyid cho các biến
        setInitalData(); //gán giá trị ban đầu cho các biến
        creatNotificationChannel();

        progressDialog.show();
        FetchCurrenciesData fetchCurrenciesData = new FetchCurrenciesData(); //lấy thông tin về currency từ link json: code, rate, date
        fetchCurrenciesData.execute();
        FetchCountriesData fetchCountriesData = new FetchCountriesData(); //lấy thông tin về country từ link json: country name, flag
        fetchCountriesData.execute();

        stopService(serviceIntent); //service tải dữ liệu (chạy ngầm), 1h update 1 lần
        startForegroundService(serviceIntent);
        registerReceiver(receiver,intentFilter);
        addEvents();
    }

    private void addEvents() {
        //tạo sự kiện nhấn vào image để tải thông tin từ sqlte
        imgLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showPopUpMenu(imgLeft,codeLeft,txtCountryNameLeft);
                setPopUpWindow(currenciesList,imgLeft,codeLeft,txtCountryNameLeft);
                popupWindow.showAsDropDown(view,200,0);

            }
        });
        imgRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showPopUpMenu(imgRight,codeRight,txtCountryNameRight);
                setPopUpWindow(currenciesList,imgRight, codeRight, txtCountryNameRight);
                popupWindow.showAsDropDown(view,-200,0);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawers();
                if(item.getItemId()==R.id.aboutItem && aboutFragmentShowing==false) {
                    //setup và chạy fragment khi nhấn vào nút "about" trong slide menu
                    frameLayout_above.bringToFront();
                   fragmentManager = getSupportFragmentManager();
                   fragmentTransaction = fragmentManager.beginTransaction();
                   fragmentTransaction.setCustomAnimations(R.anim.slide_in,R.anim.slide_out,R.anim.pop_slide_in,R.anim.pop_slide_out);
                   fragmentTransaction.replace(R.id.fragment_container_above,aboutFragment);
                   fragmentTransaction.addToBackStack(null);
                   fragmentTransaction.commit();
                   aboutFragmentShowing=true;
                }
                else if(item.getItemId()==R.id.exitItem && aboutFragmentShowing==false){
                    finish();
                }
                return true;
            }
        });

    }

    private void addControls() {

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Fetching data, please wait...");
        progressDialog.setCancelable(false);

        serviceIntent = new Intent(MainActivity.this, fetchDataService.class);
        frameLayout_above = findViewById(R.id.fragment_container_above);
        currencyLayout = findViewById(R.id.currencyLayout);
        mainLoyout = findViewById(R.id.mainLayout);
        aboutFragment = new AboutFragment();

        navigationView = findViewById(R.id.navView);
        drawerLayout = findViewById(R.id.rootLayout);
        drawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        devideLine = findViewById(R.id.txtDividLine);
        txtTap = findViewById(R.id.txtTap);
        intentFilter = new IntentFilter();
        intentFilter.addAction("SEND_ITEMS_FOR_NOTIFY_ACTION");
        receiver = new mBroadCast();
        lv = findViewById(R.id.lv);
        dataBase = new DatabaseHandler(this,"data",null,1);
        currenciesList = new ArrayList<>();
        countriesList = new ArrayList<>();
        //btnNotification = findViewById(R.id.btnNotification);

        tab1Title = findViewById(R.id.tab1Title);
        txtCountryNameLeft = findViewById(R.id.txtCountryNameLeft);
        txtCountryNameRight = findViewById(R.id.txtCountryNameRight);
        codeLeft = findViewById(R.id.txtCurrencyCodeLeft);
        codeRight = findViewById(R.id.txtCurrencyCodeRight);
        imgLeft = findViewById(R.id.imgLeft);
        imgRight = findViewById(R.id.imgRight);
        tabHost = findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tab1 = tabHost.newTabSpec("t1");
        tab1.setContent(R.id.lv);
        tab1.setIndicator("latest rates");
        tabHost.addTab(tab1);

        topLine = findViewById(R.id.topLine);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    //gán giá trị ban đầu cho các biến
    public void setInitalData() {
        currencyLayout.bringToFront();
        tabHost.setAlpha(0);
        tabHost.setClickable(false);
        devideLine.setAlpha(0);
        //btnNotification.setAlpha(0);
        //btnNotification.setClickable(false);
        topLine.setVisibility(View.INVISIBLE);

        //animation durations
        textViewTime=1000L;
        layoutTime = 1000L;
        tabHostTime = 1000L;
        itemTime = 500L;

        //animation offset
        layoutOffsetTime = textViewTime;
        tabHostOffsetTime = layoutOffsetTime+layoutTime;
        itemTime = tabHostOffsetTime+tabHostTime;
    }

    //tạo animation cho các view trên main activity
    public void startAnimations() {
        //kiểm tra cả hai thông tin textview code bên trái và code bên phải đã có thông tin chưa, nếu có thì bắt đầu chạy animation
        if(!codeLeft.getText().equals("") && !codeRight.getText().equals("")) {
            txtTap.animate().setDuration(textViewTime);
            txtTap.animate().alpha(0);

            currencyLayout.animate().setDuration(layoutTime).setStartDelay(layoutOffsetTime).translationY(-(currencyLayout.getTop())+topLine.getTop());

            devideLine.animate().setDuration(500).setStartDelay(tabHostOffsetTime).alpha(1);

            tabHost.animate().setDuration(tabHostTime);
            tabHost.animate().setStartDelay(tabHostOffsetTime);
            tabHost.animate().alpha(1.f);
            tabHost.setClickable(true);
            tabHost.animate().withEndAction(new Runnable() { //khí tabhost hiện ra thì mới tải dữ liệu từ sqlite lên listview
                @Override
                public void run() {
                    showItems();
                }
            });

            //btnNotification.animate().setDuration(1000).setStartDelay(tabHostOffsetTime).alpha(1);
            //btnNotification.setClickable(true);
            animationEnded = true;
        }
    }

    private void showPopUpMenu(ImageView imageView, TextView txtCode, TextView txtCountry) {
          PopupMenu popupMenu = new PopupMenu(MainActivity.this,imageView);
                for(int i=0;i<currenciesList.size();i++){
                    popupMenu.getMenu().add(currenciesList.get(i).getCode());
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        txtCode.setText(menuItem.getTitle());
                        for(int i =0;i<countriesList.size();i++){
                            if(countriesList.get(i).getCurrency().getCode().equals(menuItem.getTitle())){
                                txtCountry.setText(countriesList.get(i).getName());
                                byte[] data = Base64.decode(countriesList.get(i).getFlag(),Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                                imageView.setImageBitmap(bitmap);
                                showItems();
                                break;
                            }
                        }
                        return true;
                    }
                });
                popupMenu.show();
    }

    //popup window khi nhấn vào imagview, hiển thị thông tin code, country name, flag
    private void setPopUpWindow(ArrayList<Currencies> currenciesList,ImageView imageView, TextView txtCode, TextView txtCountry) {
        if(currenciesList.size()>0) {
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.popup_window,null);
            popUpListView = view.findViewById(R.id.popUpListView);
            searchBox = view.findViewById(R.id.searchBox);
            mPopUpAdapter = new PopUpAdapter(MainActivity.this,currenciesList);
            popUpListView.setAdapter(mPopUpAdapter);
            popupWindow = new PopupWindow(view,300,500,true);
            popupWindow.setFocusable(true);
            popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);

            //tìm kiếm code
            searchBox.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    mPopUpAdapter.getFilter().filter(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            popUpListView.setOnItemClickListener((adapterView, view1, position, l) -> {
                String code = (String) popUpListView.getAdapter().getItem(position);
                txtCode.setText(code);
                for(int i =0;i<countriesList.size();i++){
                    if(countriesList.get(i).getCurrency().getCode().equals(code)){
                        txtCountry.setText(countriesList.get(i).getName());
                        byte[] data = Base64.decode(countriesList.get(i).getFlag(),Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                        imageView.setImageBitmap(bitmap);
                        if(animationEnded){
                            showItems();
                        }
                        else {
                            startAnimations();
                        }
                        break;
                    }
                }

                //vì link json thiếu dữ liệu của "usd" và "eur" nên bổ sung
                if(txtCode.getText().equals("USD")){
                    txtCountry.setText("United States");
                    byte[] data = Base64.decode("iVBORw0KGgoAAAANSUhEUgAAAB4AAAAUCAYAAACaq43EAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyRpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNiAoTWFjaW50b3NoKSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDpERTc5MkI3RjE3OEExMUUyQTcxNDlDNEFCRkNENzc2NiIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDpERTc5MkI4MDE3OEExMUUyQTcxNDlDNEFCRkNENzc2NiI+IDx4bXBNTTpEZXJpdmVkRnJvbSBzdFJlZjppbnN0YW5jZUlEPSJ4bXAuaWlkOkEyMTE0RjIyMTc4QTExRTJBNzE0OUM0QUJGQ0Q3NzY2IiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOkRFNzkyQjdFMTc4QTExRTJBNzE0OUM0QUJGQ0Q3NzY2Ii8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+60cYSwAAAyhJREFUeNrElN1PU3cYxz/tOQUBD/aNymtbUAq2IOiUWXmZA40Iy2BzcW53y7JlyZLtZuE/8NaY7Gbe7WJbdJnTDOdQCbLKrERUotgCSodQ7AsFpK28yKT7rfsL2gv7JCcn+eV3zpPv5/l+H9X2xp65SqtJGfr1Fg3vNPD02SIhfwRniwP3pdvsOVxPaCHGs7+DOA/VJs8crXXEs3P48OfTfMIcU+SRaqlMzm8SNut2VuefIxvyydZIxFbWyX35iviLNZRiPZJaxdLyCkoiQUyc6cwFTPvC9FRkcbJMy7JaTrmxHIuvxaZm5xW7+Jl3NkKRaRt5OVlMjvuoqa9gwr9AgS4PvTYP78hjdtVVEAw9J+Kdxv7Td+hL8tGTeslGg8Jeexk3/riLs62O+cU441NBDjbZGbg+SlNbPYvRF9zzzHCoycFA/yhvCtRqnZbr5a1YEjGm5S2po1ZXfRHVaCTlWLODq24v1eWFGPVbuXH5Dh3vORm88xhziR5zoZ5rl9y0dx/ggS/EzGSQs5Ua3s39h7CUlbri0mKdUGzmijBXqzBXYH4Z931fsmlf7zBvd+wjIigMDI/TcbyRvt+GOSgUZ62uU3S2h8IdRgrTQK1S2T6PyhpZ+aB9LxcF2hpbCUUF27hy4S+Of/wWfUMeykuNVIin9/xNuj9qYWR8juknIc5szNC1voA/DdSypayAhlor57/vp/NEC7OBRfpveek+0cwvP/7JsfedhEWcLg8+pOtkMxfOuTjc5WSrSc+S6ymSQYtGyk5dsVT9/4zbhZmu3Z5IztggXOwSZjvSuZ+hUR9mEan/KAz+PkJb5z7GngSYdXu46T9Ho3EL6ZSKnZ9Fax0W5aFrDNuB6mROA6El7BYTnns+bPt3srK2gV+QcIjIPRLzrxL3ZkLLfB0c40udRCAd1EfFNioxaSG+Sl2NmchSnCKjwh6HBWlzk/rd1uTyMOTn8MbuctRiieyqLKbKbqXs4gSvQmFephOnRCIRFW+F11yyp/3TtD/eSKjYTM4rjcZh110yUZlDPfnVqcwovkppRhRnDrX/2x+UjKDuJXcuE4r/FWAAjBMttNdoYOEAAAAASUVORK5CYII=",Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                    imageView.setImageBitmap(bitmap);
                }
                if (txtCode.getText().equals("EUR")){
                    txtCountry.setText("European Union");
                    byte[] data = Base64.decode("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",Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                    imageView.setImageBitmap(bitmap);
                }
                popupWindow.dismiss();
            });
        }
      else {
            Toast.makeText(MainActivity.this, "currencylist empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDataAfterClickingItemPopUpWindow(ImageView imageView, TextView txtCode, TextView txtCountry) {
        popUpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                txtCode.setText((String) popUpListView.getItemAtPosition(position));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //sự kiện mở slide menu bên trái
        if(drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        //mở notification dialog khi nhấn nút chuông bên phải
        else if(item.getItemId()==R.id.option_notificaiton) {
            Double rateLeft=0.0;
            Double rateRight=0.0;
            for(int i=0;i<currenciesList.size();i++){
                if(currenciesList.get(i).getCode().equals(codeLeft.getText().toString())) {
                    rateLeft = currenciesList.get(i).getRate();
                }
                if(currenciesList.get(i).getCode().equals(codeRight.getText().toString())) {
                    rateRight = currenciesList.get(i).getRate();
                }
            }
            NotificationDialog notificationDialog = new NotificationDialog(MainActivity.this,codeLeft.getText().toString(),codeRight.getText().toString(),
                    rateLeft,rateRight);
            notificationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            notificationDialog.setCanceledOnTouchOutside(false);
            notificationDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    //tải dữ liệu lên listview từ sqlite
    private void showItems() {
        rateList = new ArrayList<>();
        dateList = new ArrayList<>();
        if(!codeLeft.getText().equals("") && !codeRight.getText().equals("")){
            //Toast.makeText(MainActivity.this, "show Items", Toast.LENGTH_SHORT).show();
            itemsToShowList = new ArrayList<>();
            itemsToShowList = dataBase.list(codeLeft.getText().toString(),codeRight.getText().toString());
            for(int i=0;i<itemsToShowList.size()/2;i++) {
                rateList.add(itemsToShowList.get(i));
                dateList.add(itemsToShowList.get(i+5));
            }
        }
        if(rateList.size()>0 & dateList.size()>0){
            priceAdapter = new PriceAdapter(MainActivity.this,R.layout.item,rateList,dateList);
            lv.setAdapter(priceAdapter);
        }
    }

    //lưu dữ liệu vào database
    private void addItemsToDatabase() {
        if(currenciesList.size()>0){
            //Toast.makeText(MainActivity.this, "run addItem", Toast.LENGTH_SHORT).show();
            dataBase.updateDatabase(currenciesList);
            /*if(dataBase!=null){
                Toast.makeText(MainActivity.this, "items added", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(MainActivity.this, "not ok", Toast.LENGTH_SHORT).show();
            }*/
        }
        else{
            Toast.makeText(MainActivity.this, "not run", Toast.LENGTH_SHORT).show();
        }
    }

    private void creatNotificationChannel() {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId1,name,importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public class FetchCurrenciesData extends AsyncTask<Void, Void, ArrayList<Currencies>> {
        ArrayList<String> listString = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.codes)));
        @Override
        protected ArrayList<Currencies> doInBackground(Void... voids) {
            ArrayList<Currencies> currencyList = new ArrayList<>();
            try {
                URL url1 = new URL("https://www.floatrates.com/daily/usd.json");
                HttpURLConnection connection1 = (HttpURLConnection) url1.openConnection();
                InputStreamReader inputStreamReader1 = new InputStreamReader(connection1.getInputStream(), "UTF-8");
                BufferedReader bufferedReader1 = new BufferedReader(inputStreamReader1);
                String line1 = bufferedReader1.readLine();
                JSONObject rootJsonObject = new JSONObject(line1);
                    for (int i = 0; i < listString.size(); i++) {
                            if(rootJsonObject.has(listString.get(i))){
                                JSONObject object = (JSONObject) rootJsonObject.getJSONObject(listString.get(i));
                                Currencies currency = new Currencies();
                                currency.setCode(object.getString("code"));
                                currency.setRate(object.getDouble("rate"));
                                currency.setInverseRate(object.getDouble("inverseRate"));
                                currency.setDate(object.getString("date"));
                                currencyList.add(currency);
                            }
                }
                currencyList.add(new Currencies("USD",1.0,currencyList.get(0).getDate(),1.0));
            } catch (Exception ex) {
                ex.toString();
            }
            return currencyList;
        }

        @Override
        protected void onPostExecute(ArrayList<Currencies> currencies) {
            super.onPostExecute(currencies);
            currenciesList.addAll(currencies);
            currenciesList.get(currenciesList.size()-1).setRate(1.0);
            //Toast.makeText(MainActivity.this, ""+currenciesList.get(currenciesList.size()-1).getCode(), Toast.LENGTH_SHORT).show();
            addItemsToDatabase();
            dismissProgressDialog();
        }
    }

    public class FetchCountriesData extends AsyncTask<Void, Void, ArrayList<Countries>> {
        @Override
        protected ArrayList<Countries> doInBackground(Void... voids) {
            ArrayList<Countries> countryList = new ArrayList<>();
            try {
                URL url = new URL("https://gist.githubusercontent.com/CodeTheInternet/9312404/raw/4987ca07c4032bb6262a65794f428b350e1d86a1/gistfile1.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream(), "UTF-8");
                /*InputStream inputStream = getApplicationContext().getResources().openRawResource(R.raw.countriesdatas);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);*/
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = bufferedReader.readLine();
                JSONArray jsonArray = new JSONArray(line);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        Countries country = new Countries();
                        country.setName(object.getString("name"));
                        JSONObject currencyObject = object.getJSONObject("currency");
                        country.setCurrency(new Currencies(currencyObject.getString("code")));
                        country.setFlag(object.getString("flag"));
                        countryList.add(country);
                    }
            } catch (Exception ex) {
                ex.toString();
            }
            return countryList;
        }

        @Override
        protected void onPostExecute(ArrayList<Countries> countries) {
            super.onPostExecute(countries);
            countriesList.addAll(countries);
            dismissProgressDialog();
        }
    }

    //progress dialog sẽ dismiss khi toàn bộ dữ liệu đã được tải về
    private void dismissProgressDialog() {
        if(currenciesList.size()>0 && countriesList.size()>0){
            progressDialog.dismiss();
        }
    }
}


