package com.example.currencyprices;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.models.Countries;
import com.example.models.Currencies;
import com.example.models.ItemsNotify;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "data.db";
    private static final String MAIN_TABLE_NAME = "dataTable";
    private static final String SAVE_TABLE_NAME = "dataTableSave";
    private Context context;

    private String mainTableQuery = "CREATE TABLE " + MAIN_TABLE_NAME + " (code TEXT, flag TEXT, rate1 DOUBLE, rate2 DOUBLE, rate3 DOUBLE, rate4 DOUBLE, rate5 DOUBLE, "+
            "date1 TEXT, date2 TEXT, date3 TEXT, date4 TEXT, date5 TEXT)";
    private String saveTableQuery = "CREATE TABLE " + SAVE_TABLE_NAME + " (codeleft TEXT, coderight TEXT, rateleft DOUBLE, rateright DOUBLE, rate TEXT, higher INT)";


    public DatabaseHandler(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, version);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(mainTableQuery);
        sqLiteDatabase.execSQL(saveTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    public void updateDatabase (ArrayList<Currencies> currenciesList) {
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * from "+MAIN_TABLE_NAME,null);
        if(cursor.getCount()>0) {
            cursor.moveToFirst();
            String date = cursor.getString(7);
            if(date==null) {
                date = "1";
            }
            if(!(currenciesList.get(0).getDate().equals(date))) {
                cursor.close();
                Cursor cursor1 = database.rawQuery("SELECT * from "+MAIN_TABLE_NAME,null);
                cursor1.moveToFirst();

                for(int i=0;i<currenciesList.size();i++) {
                    String date1 = cursor1.getString(7);
                    String date2 = cursor1.getString(8);
                    String date3 = cursor1.getString(9);
                    String date4 = cursor1.getString(10);
                    Double rate1 = cursor1.getDouble(2);
                    Double rate2 = cursor1.getDouble(3);
                    Double rate3 = cursor1.getDouble(4);
                    Double rate4 = cursor1.getDouble(5);
                    String newDate1 = currenciesList.get(i).getDate();
                    Double newRate1 = currenciesList.get(i).getRate();
                    //Toast.makeText(context, newRate1+"", Toast.LENGTH_SHORT).show();

                    ContentValues values = new ContentValues();
                    values.put("date2",date1);
                    values.put("date3",date2);
                    values.put("date4",date3);
                    values.put("date5",date4);
                    values.put("rate2",rate1);
                    values.put("rate3",rate2);
                    values.put("rate4",rate3);
                    values.put("rate5",rate4);
                    String code = currenciesList.get(i).getCode();
                    database.update(MAIN_TABLE_NAME,values,"code=?",new String[]{code});

                    ContentValues values1 = new ContentValues();
                    values1.put("rate1",String.valueOf(newRate1));
                    values1.put("date1",newDate1);
                    database.update(MAIN_TABLE_NAME,values1,"code=?",new String[]{code});
                    //Toast.makeText(context, newRate1+"", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(context, cursor1.getString(2)+"", Toast.LENGTH_SHORT).show();
                    cursor1.moveToNext();
                }
                cursor1.close();
            }
            database.close();
        }
        else {
            for(int i=0;i<currenciesList.size();i++){
                database.execSQL("INSERT INTO "+MAIN_TABLE_NAME+" (code, rate1, date1) VALUES (?,?,?)", new String[]
                        {currenciesList.get(i).getCode(),currenciesList.get(i).getRate()+"",currenciesList.get(i).getDate()});
            }
        }
    }

    public ArrayList<ItemsNotify> listToNotification() {
        ArrayList<ItemsNotify> list = new ArrayList<>();
        SQLiteDatabase dataBase = getWritableDatabase();
        Cursor saveCursor = dataBase.rawQuery("select * from "+SAVE_TABLE_NAME,null);
        if (saveCursor.getCount()>0) {
            while (saveCursor.moveToNext()){
                String leftCode = saveCursor.getString(0);
                String rightCode = saveCursor.getString(1);
                Double rate = saveCursor.getDouble(4);
                int higher = saveCursor.getInt(5);

                Cursor leftCursor = dataBase.rawQuery("select * from "+MAIN_TABLE_NAME+" where code = ?",new String[]{leftCode});
                Cursor rightCursor = dataBase.rawQuery("select * from "+MAIN_TABLE_NAME+" where code = ?",new String[]{rightCode});
                leftCursor.moveToFirst();
                rightCursor.moveToFirst();
                Double rateLeft = leftCursor.getDouble(2);
                Double rateRight = rightCursor.getDouble(2);
                Double rateToCheck = rateRight/rateLeft;

                if(higher==1){
                    if(rateToCheck>=rate){
                        ItemsNotify itemsNotify = new ItemsNotify(leftCode,rightCode,String.valueOf(rate));
                        list.add(itemsNotify);
                    }
                }
                else if(higher==0) {
                    if(rateToCheck<=rate) {
                        ItemsNotify itemsNotify = new ItemsNotify(leftCode,rightCode,String.valueOf(rate));
                        list.add(itemsNotify);
                    }
                }
                leftCursor.close();
                rightCursor.close();
            }
            if(list.size()>0){
                Toast.makeText(context, list.get(0).getRate()+"", Toast.LENGTH_SHORT).show();
                for(int i=0;i<list.size();i++) {
                    int kq=dataBase.delete(SAVE_TABLE_NAME,"codeleft =? and coderight =? and rate =?",new String[]{list.get(i).getCodeLeft(),list.get(i).getCodeRight(), list.get(i).getRate()});
                    if (kq>0){
                        Toast.makeText(context, "1 item deleted", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        return list;
    }

    public void updateSaveTable (String codeLeft, String codeRight, Double rate, int higher) {
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery("select * from "+SAVE_TABLE_NAME,null);
        if(cursor.getCount()>0){
            boolean priceExist = false;
            while (cursor.moveToNext()){
                if(cursor.getString(0).equals(codeLeft) && cursor.getString(1).equals(codeRight) && cursor.getString(4).equals(String.valueOf(rate))){
                    Toast.makeText(context, "This price is already in the data base, please set another price", Toast.LENGTH_SHORT).show();
                    priceExist = true;
                    break;
                }
            }
            if(priceExist==false) {
                ContentValues values = new ContentValues();
                values.put("codeLeft",codeLeft);
                values.put("codeRight",codeRight);
                values.put("rate",rate);
                values.put("higher",higher);
                Long kq=database.insert(SAVE_TABLE_NAME,null,values);
                if(kq>0){
                    Toast.makeText(context.getApplicationContext(), "price set successfully "+rate, Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(context.getApplicationContext(), "price set unsuccessfully", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            ContentValues values = new ContentValues();
            values.put("codeLeft",codeLeft);
            values.put("codeRight",codeRight);
            values.put("rate",rate);
            values.put("higher",higher);
            Long kq=database.insert(SAVE_TABLE_NAME,null,values);
            if(kq>0) {
                Toast.makeText(context.getApplicationContext(), "price set successfully "+rate, Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(context.getApplicationContext(), "price set unsuccessfully", Toast.LENGTH_SHORT).show();
        }
    }

    public ArrayList<String> list (String codeLeft,String codeRight){
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursorLeft = database.rawQuery("select * from "+MAIN_TABLE_NAME+" where code = ?",new String[]{codeLeft});
        cursorLeft.moveToNext();

        Double rateLeft1 = cursorLeft.getDouble(2);
        Double rateLeft2 = cursorLeft.getDouble(3);
        Double rateLeft3 = cursorLeft.getDouble(4);
        Double rateLeft4 = cursorLeft.getDouble(5);
        Double rateLeft5 = cursorLeft.getDouble(6);
        cursorLeft.close();

        Cursor cursorRight = database.rawQuery("select * from "+MAIN_TABLE_NAME+" where code = ?",new String[]{codeRight});
        cursorRight.moveToNext();

        Double rateRight1 = cursorRight.getDouble(2);
        Double rateRight2 = cursorRight.getDouble(3);
        Double rateRight3 = cursorRight.getDouble(4);
        Double rateRight4 = cursorRight.getDouble(5);
        Double rateRight5 = cursorRight.getDouble(6);

        String date1 = cursorRight.getString(7);
        String date2 = cursorRight.getString(8);
        String date3 = cursorRight.getString(9);
        String date4 = cursorRight.getString(10);
        String date5 = cursorRight.getString(11);
        cursorRight.close();

        String digitAfterComma = "%.5f";
        String rate1String = String.format(digitAfterComma,rateRight1/rateLeft1).replaceAll(",",".");
        String rate2String = String.format(digitAfterComma,rateRight2/rateLeft2).replaceAll(",",".");
        String rate3String = String.format(digitAfterComma,rateRight3/rateLeft3).replaceAll(",",".");
        String rate4String = String.format(digitAfterComma,rateRight4/rateLeft4).replaceAll(",",".");
        String rate5String = String.format(digitAfterComma,rateRight5/rateLeft5).replaceAll(",",".");

        Double rate1 = Double.parseDouble(rate1String);
        Double rate2 = Double.parseDouble(rate2String);
        Double rate3 = Double.parseDouble(rate3String);
        Double rate4 = Double.parseDouble(rate4String);
        Double rate5 = Double.parseDouble(rate5String);

        list.add(String.valueOf(rate1));
        if(rate2.isNaN()){
            list.add("");
        }
        else {
            list.add(String.valueOf(rate2));
        }
        if(rate3.isNaN()) {
            list.add("");
        }
        else {
            list.add(String.valueOf(rate3));
        }
        if(rate4.isNaN()) {
            list.add("");
        }
        else {
            list.add(String.valueOf(rate4));
        }
        if(rate5.isNaN()){
            list.add("");
        }
        else {
            list.add(String.valueOf(rate5));
        }

        list.add(date1);
        list.add(date2);
        list.add(date3);
        list.add(date4);
        list.add(date5);

        return list;
    }
}
