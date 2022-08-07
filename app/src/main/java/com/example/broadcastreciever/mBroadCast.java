package com.example.broadcastreciever;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.currencyprices.MainActivity;
import com.example.currencyprices.R;
import com.example.models.ItemsNotify;
import com.example.service.fetchDataService;

import java.util.ArrayList;
import java.util.Collections;

public class mBroadCast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "intent recieved", Toast.LENGTH_SHORT).show();

        //tạo notification nếu có dữ liệu
        if(intent.getAction().equals("SEND_ITEMS_FOR_NOTIFY_ACTION")) {
            ArrayList<ItemsNotify> itemList = (ArrayList<ItemsNotify>) intent.getSerializableExtra("itemlist");
            if(itemList.size()==0){
                Toast.makeText(context, "itemList empty", Toast.LENGTH_SHORT).show();
            }
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                CharSequence name = "name";
                String description = "description";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel("notificationId",name,importance);
                channel.setDescription(description);
                notificationManager.createNotificationChannel(channel);
            }
            for(int i=0;i<itemList.size();i++){
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"notificationId").
                        setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(itemList.get(i).getCodeLeft()+"/"+itemList.get(i).getCodeRight())
                        .setContentText(itemList.get(i).getRate().toString())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                notificationManager.notify(1,builder.build());
            }
        }

        if(intent.getAction().equals("SHUT_DOWN_ACTION")) {
            context.startService(new Intent(context, fetchDataService.class));
        }

        if(intent.getAction().equals("notthing")){
            Toast.makeText(context, "recieved sussecfully  ", Toast.LENGTH_SHORT).show();
        }
    }
}
