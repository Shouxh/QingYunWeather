package com.shouxh.weatherMain;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ProvinceHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="CitiesDataBase";
    private final String TABLE_NAME = "CITIES";
    private final String COL_CITY = "City_Name";
    private final String CREATE_TABLE="CREATE TABLE "+TABLE_NAME+"( "
            +COL_CITY+" text not null );";
    private Context context;

    public ProvinceHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String DROP_TABLE = "DROP TABLE IF EXISTS "+TABLE_NAME;
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    public boolean isEmpty(){
        final String empty = "select * from "+TABLE_NAME;
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(empty,null);
        if(cursor.getCount()!=0){
            cursor.close();
            return false;
        }else {
            cursor.close();
            return true;
        }
    }

    public void initData(List<String> CitiesList){
        final String queryExists = "select * from sqlite_master where name="+"\""+TABLE_NAME+"\"";
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery(queryExists,null);
        if(cursor.getCount()==0){
            database.execSQL(CREATE_TABLE);
        }
        if(isEmpty()){
            ContentValues[] values = new ContentValues[CitiesList.size()];
            for(int i=0;i<values.length;i++){
                values[i]=new ContentValues();
                values[i].put(COL_CITY,CitiesList.get(i));
            }
            for (ContentValues value:values){
                database.insert(TABLE_NAME,null,value);
            }
            database.close();
        }
        cursor.close();
    }

    /**
     *
     * 当用户搜索的城市获取天气预报成功后，调用此方法将成功的城市加入城市列表中
     * */
    public void addUserSearchCity(String cityName){
        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_CITY,cityName);
        database.insert(TABLE_NAME,null,contentValues);
        database.close();
    }

    /**
     * 撤销添加自定义的操作
     *
     * */
    public void UndoAddUserSearchCity(String cityName){
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            return;
        }
        SQLiteDatabase database = getWritableDatabase();
        database.delete(TABLE_NAME,"City_Name = ?",new String[]{cityName});
        database.close();
    }

    public ArrayList<String> getAllInformation(){
        SQLiteDatabase database = getReadableDatabase();
        ArrayList<String> cityList = new ArrayList<>();
        Cursor cursor = database.query(TABLE_NAME,new String[]{COL_CITY},null,
                null,null, null,null);
        while(cursor.moveToNext()){
            String Cname = cursor.getString(cursor.getColumnIndex(COL_CITY));
            cityList.add(Cname);
        }
        cursor.close();
        database.close();
        return cityList;
    }
}
