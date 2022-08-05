package com.example.models;

import java.io.Serializable;

public class ItemsNotify implements Serializable {
    private String codeLeft;
    private String codeRight;
    private String rate;

    public ItemsNotify(String codeLeft, String codeRight, String rate) {
        this.codeLeft = codeLeft;
        this.codeRight = codeRight;
        this.rate = rate;
    }

    public ItemsNotify() {
    }

    public String getCodeLeft() {
        return codeLeft;
    }

    public void setCodeLeft(String codeLeft) {
        this.codeLeft = codeLeft;
    }

    public String getCodeRight() {
        return codeRight;
    }

    public void setCodeRight(String codeRight) {
        this.codeRight = codeRight;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}
