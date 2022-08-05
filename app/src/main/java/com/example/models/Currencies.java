package com.example.models;

public class Currencies {

private String code;
private double rate;
private String date;
private double inverseRate;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getInverseRate() {
        return inverseRate;
    }

    public void setInverseRate(double inverseRate) {
        this.inverseRate = inverseRate;
    }

    public Currencies(String code, double rate, String date, double inverseRate) {
        this.code = code;
        this.rate = rate;
        this.date = date;
        this.inverseRate = inverseRate;
    }

    public Currencies(String code) {
        this.code = code;
    }

    public Currencies() {
    }
}
