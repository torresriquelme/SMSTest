package com.torresriquelme.smstest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by E04083 on 18/05/2016.
 */
public class BaseDatosAlarmas extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "alarmas.db";
    private static final String TABLE_ALARMS_SMS = "alarmasSms";
    private static final String FIELD_SMSNUM = "smsId";  //Número de registro de SMS en el dispositivo
    private static final String FIELD_CODIGO = "codigo"; //Código de 4 dígitos que identifica el tipo de alarma en la BSC
    private static final String FIELD_LUGAR = "lugar";  //Core donde se produce la alarma
    private static final String FIELD_DESCRIPCION = "descripcion";  //Texto descriptivo de la alarma
    private static final String FIELD_DATE = "date";  //Dato tipo long que tiene la información de la fecha y hora de la alarma
    private static final String FIELD_ACTIVA = "activa"; //Variable boolean para indicar el set o reset de la alarma.
    private static final int DATABASE_VERSION = 1;
    private final static String TAG = "SMSTest";

    public BaseDatosAlarmas(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ALARMS_SMS + "(_id integger PRIMARY KEY, " + FIELD_SMSNUM + " INTEGER, " + FIELD_CODIGO + " TEXT, " +
                FIELD_LUGAR + " TEXT, " + FIELD_DESCRIPCION + " TEXT, " + FIELD_DATE + " INTEGER, " + FIELD_ACTIVA + " INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void guardarAlarma(int smsId, String codigo, String lugar, String descripcion, long fecha, int activa){
        Log.i(TAG, "Datos enviados a encuentraAlarmaId()  smsId=" + smsId + " codigo=" + codigo + " lugar=" + lugar + " fecha=" + fecha);
        long id = encuentraAlarmaId(smsId, codigo, lugar, fecha);
        Log.i(TAG, "Resultado de encuentraAlarmaId() " + id + " smsId=" + smsId + " codigo=" + codigo + " lugar=" + lugar + " fecha=" + fecha);
        if (id > 0){
            //updateAlarma(smsId, codigo, lugar, fecha) //En caso de que la alarma ya se encuentre registrada se hace un update ??
        }else{
            addAlarma(smsId, codigo, lugar, descripcion, fecha, activa);
            Log.i(TAG, "Alarma insertada en Base de Datos");
        }
    }

    private long addAlarma(int smsId, String codigo, String lugar, String descripcion, long fecha, int activa){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FIELD_SMSNUM, smsId);
        values.put(FIELD_CODIGO, codigo);
        values.put(FIELD_LUGAR, lugar);
        values.put(FIELD_DESCRIPCION, descripcion);
        values.put(FIELD_DATE, fecha);
        values.put(FIELD_ACTIVA, activa);
        return db.insert(TABLE_ALARMS_SMS, null, values);
    }

    private long encuentraAlarmaId(int smsId, String codigo, String lugar, long fecha) {
        long returnValue = -1;
        String[] args = new String[]{String.valueOf(smsId), codigo, lugar, String.valueOf(fecha)};
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ALARMS_SMS + " WHERE smsId = ? AND codigo = ? AND lugar = ? AND date =?", args);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            returnValue = cursor.getInt(0);
        }
        return returnValue;
    }

    public Alarma getLastAlarmByCode (String codigo, String lugar){
        Alarma alarma = new Alarma(-1, null, null,null, 0, 0);
        String colName = "MAX(" + FIELD_DATE + ")";
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        String[] args = new String[]{codigo, lugar};
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + colName + "," + FIELD_CODIGO + "," + FIELD_LUGAR + "," + FIELD_SMSNUM + "," + FIELD_DATE + " FROM " + TABLE_ALARMS_SMS +  " WHERE codigo = ? AND lugar = ?", args);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            alarma.setFecha(cursor.getLong(cursor.getColumnIndex(FIELD_DATE)));
            alarma.setSmsId(cursor.getInt(cursor.getColumnIndex(FIELD_SMSNUM)));
            alarma.setCodigo(cursor.getString(cursor.getColumnIndex(FIELD_CODIGO)));
            alarma.setLugar(cursor.getString(cursor.getColumnIndex(FIELD_LUGAR)));
        }
        return alarma;
    }

    public Alarma getLastAlarm(){
        Alarma alarma = new Alarma();
        String colName = "MAX(" + FIELD_DATE + ")";
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + colName + "," + FIELD_CODIGO + "," + FIELD_LUGAR + "," + FIELD_SMSNUM + "," + FIELD_DATE + " FROM " + TABLE_ALARMS_SMS, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            alarma.setFecha(cursor.getLong(cursor.getColumnIndex(FIELD_DATE)));
            alarma.setSmsId(cursor.getInt(cursor.getColumnIndex(FIELD_SMSNUM)));
            alarma.setCodigo(cursor.getString(cursor.getColumnIndex(FIELD_CODIGO)));
            alarma.setLugar(cursor.getString(cursor.getColumnIndex(FIELD_LUGAR)));
        }
        return alarma;
    }

}
