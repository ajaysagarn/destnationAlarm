package com.meteoriteapps.android.destinationalarm;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

@Dao
public interface DestDAO {

    @Insert
    public void addDest(Destinations dest);

    @Query("SELECT Dname FROM Destinations")
    public String[] loadAllDestins();

    @Query(" delete from Destinations where Dname = :dname ")
    public void Deletedestn(String dname);

    @Query("Select dlat from Destinations where Dname = :dname")
    public double GetLat(String dname);

    @Query("Select dlong from Destinations where Dname = :dname")
    public double GetLng(String dname);






}
