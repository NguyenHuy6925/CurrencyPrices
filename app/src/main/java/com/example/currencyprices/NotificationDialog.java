package com.example.currencyprices;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class NotificationDialog extends Dialog implements View.OnClickListener {
    public Activity c;
    public Dialog d;
    public Button btnOk, btnCancel;
    public EditText txtSetPrice;
    public TextView txtCurrentRate;
    private String codeLeft,codeRight;
    private Double rateLeft, rateRight, rate, rateTextView;
    private LinearLayout rootLayout;

    public NotificationDialog(@NonNull Activity context,String codeLeft, String codeRight, Double rateLeft, Double rateRight) {
        super(context);
        this.c = context;
        this.codeLeft = codeLeft;
        this.codeRight=codeRight;
        this.rateLeft=rateLeft;
        this.rateRight = rateRight;
        this.rate = rateRight/rateLeft;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        rootLayout = findViewById(R.id.notifiction_rootLayout);
        setContentView(R.layout.notification_dialog);
        btnOk = findViewById(R.id.btnOk);
        btnCancel = findViewById(R.id.btnCancel);
        txtSetPrice = findViewById(R.id.txtSetPrice);
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        txtCurrentRate = findViewById(R.id.txtCurrentRate);
        txtCurrentRate.setText(String.format("%.5f",rateRight/rateLeft));
    }

    @Override
    public void onClick(View view) {

            switch (view.getId()){
                case R.id.btnOk: {
                    if (!txtSetPrice.getText().toString().equals("")) {
                        rateTextView = Double.parseDouble(txtSetPrice.getText().toString());
                        if (rate > rateTextView) {
                            MainActivity.dataBase.updateSaveTable(codeLeft, codeRight, rateTextView, 0);
                        } else {
                            MainActivity.dataBase.updateSaveTable(codeLeft, codeRight, rateTextView, 1);
                        }
                    }
                    else {
                        Toast.makeText(c, "Please input some thing in the textbox", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case R.id.btnCancel: {
                    dismiss();
                }
                default:
            }
        }

    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, @Nullable Menu menu, int deviceId) {
        super.onProvideKeyboardShortcuts(data, menu, deviceId);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
