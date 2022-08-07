package com.example.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.broadcastreciever.mBroadCast;
import com.example.currencyprices.DatabaseHandler;
import com.example.currencyprices.MainActivity;
import com.example.currencyprices.R;
import com.example.models.Currencies;
import com.example.models.ItemsNotify;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class fetchDataService extends Service implements Serializable {

    ArrayList<Currencies> currenciesList;
    ArrayList<String> listString;
    ArrayList<ItemsNotify> listItemtoNotify;
    public static DatabaseHandler dataBase;
    private String SEND_ITEMS_FOR_NOTIFY_ACTION = "SEND_ITEMS_FOR_NOTIFY_ACTION";
    private String SHUT_DOWN_ACTION = "SHUT_DOWN_ACTION";
    Handler handler;
    MediaPlayer mediaPlayer;
    int backgroundNotifactionID = 1;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        dataBase = new DatabaseHandler(this,"data",null,1);
        listString = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.codes)));
        currenciesList = new ArrayList<>();
        listItemtoNotify = new ArrayList<>();
        handler = new Handler();

        //tạo một forground notification luôn chạy trên status bar
        Intent notificationIntent = new Intent(this,MainActivity.class);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this,"forgroundchannel")
                .setContentText("Currency Prices")
                .setContentText("This app is running in background")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setTicker("ticker");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID = 1;
        NotificationChannel channel = new NotificationChannel("forgroundchannel","for",NotificationManager.IMPORTANCE_NONE);
        channel.setDescription("This is a notification");
        notificationManager.createNotificationChannel(channel);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);

        startForeground(1337,notification.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         super.onStartCommand(intent, flags, startId);
        //Toast.makeText(this, "service started", Toast.LENGTH_SHORT).show();
         handler.post(runnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //Toast.makeText(this, "service stopped", Toast.LENGTH_SHORT).show();
        Intent restartIntent = new Intent(SHUT_DOWN_ACTION);
        sendBroadcast(restartIntent);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //lấy dữ liệu mới mỗi giờ 1 lần, kiểm tra và gửi dữ liệu đến reciever (nếu có)
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(fetchDataService.this, "service running", Toast.LENGTH_SHORT).show();
            ArrayList<Currencies> currencyList = new ArrayList<>();
            try {
                URL url1 = new URL("https://www.floatrates.com/daily/usd.json");
                HttpURLConnection connection1 = (HttpURLConnection) url1.openConnection();
                InputStreamReader inputStreamReader1 = new InputStreamReader(connection1.getInputStream(), "UTF-8");
                BufferedReader bufferedReader1 = new BufferedReader(inputStreamReader1);
                String line1 = bufferedReader1.readLine();
                JSONObject rootJsonObject = new JSONObject(line1);
                for (int i = 0; i < rootJsonObject.length(); i++) {
                    if(rootJsonObject.has(listString.get(i))){
                        JSONObject object = (JSONObject) rootJsonObject.getJSONObject(listString.get(i));
                        Currencies currency = new Currencies();
                        currency.setCode(object.getString("code"));
                        currency.setRate(object.getDouble("rate"));
                        currency.setInverseRate(object.getDouble("inverseRate"));
                        currency.setDate(object.getString("date"));
                        currencyList.add(currency);
                    }
                    else {
                        continue;
                    }
                }
                currencyList.add(new Currencies("USD"));
                dataBase.updateDatabase(currencyList);
            } catch (Exception ex) {
                ex.toString();
            }
            listItemtoNotify.clear();
            listItemtoNotify = dataBase.listToNotification();
            if(listItemtoNotify.size()>0) {
                //Toast.makeText(fetchDataService.this, "service running", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(fetchDataService.this, mBroadCast.class);
                intent1.setAction(SEND_ITEMS_FOR_NOTIFY_ACTION);
                intent1.putExtra("itemlist", listItemtoNotify);
                sendBroadcast(intent1);
            }
            else {
                Intent intent1 = new Intent(fetchDataService.this, mBroadCast.class);
                intent1.setAction("notthing");
                sendBroadcast(intent1);
            }
            handler.postDelayed(this,3600000);
        }
    };
}
