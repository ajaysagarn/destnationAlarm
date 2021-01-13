package com.meteoriteapps.android.destinationalarm;

import android.arch.persistence.room.RoomDatabase;

@android.arch.persistence.room.Database(entities = {Destinations.class}, version = 1, exportSchema = false)
public abstract class Database extends RoomDatabase {

    public abstract DestDAO myDao();

}
