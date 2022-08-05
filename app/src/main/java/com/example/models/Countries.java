package com.example.models;

public class Countries {
    private String name;
    private Currencies currency;
    private String flag;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Currencies getCurrency() {
        return currency;
    }

    public void setCurrency(Currencies currency) {
        this.currency = currency;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public Countries(String name, Currencies currency, String flag) {
        this.name = name;
        this.currency = currency;
        this.flag = flag;
    }

    public Countries() {
    }
}
