package com.meteoriteapps.android.destinationalarm;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Destinations {


    @NonNull @PrimaryKey
    private String Dname;
    @ColumnInfo(name="dlat")
    private double dlat;
    @ColumnInfo(name="dlong")
    private double dlong;



    Destinations(){
        Dname="";

    }

    @NonNull
    public String getDname() {
        return Dname;
    }

    public double getDlat() {
        return dlat;
    }

    public double getDlong() {
        return dlong;
    }

    public void setDname(@NonNull String dname) {
        Dname = dname;
    }

    public void setDlat(double dlat) {
        this.dlat = dlat;
    }

    public void setDlong(double dlong) {
        this.dlong = dlong;
    }


}

