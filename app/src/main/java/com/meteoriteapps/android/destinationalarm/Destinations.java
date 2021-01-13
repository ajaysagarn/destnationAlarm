package com.meteoriteapps.android.destinationalarm;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Destinations {


    @NonNull
    @PrimaryKey
    private String Dname;
    @ColumnInfo(name = "dlat")
    private double dlat;
    @ColumnInfo(name = "dlong")
    private double dlong;


    Destinations() {
        Dname = "";

    }

    @NonNull
    public String getDname() {
        return Dname;
    }

    public void setDname(@NonNull String dname) {
        Dname = dname;
    }

    public double getDlat() {
        return dlat;
    }

    public void setDlat(double dlat) {
        this.dlat = dlat;
    }

    public double getDlong() {
        return dlong;
    }

    public void setDlong(double dlong) {
        this.dlong = dlong;
    }


}

