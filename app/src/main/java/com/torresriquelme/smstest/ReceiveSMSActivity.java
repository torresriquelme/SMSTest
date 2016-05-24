package com.torresriquelme.smstest;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReceiveSMSActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private static ReceiveSMSActivity inst;
    ArrayList<String> smsMessageList = new ArrayList<String>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;
    TextView fechaUltimo, lugarUltimo, tipoUltimo;
    Spinner alarmSelection;
    BaseDatosAlarmas db = new BaseDatosAlarmas(this);
    private static final String TAG = "SMSTest";

    public static ReceiveSMSActivity instance(){
        return inst;
    }

    @Override
    protected void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_sms);

        smsListView = (ListView) findViewById(R.id.SMSList);
        fechaUltimo = (TextView) findViewById(R.id.tv_fechaAlarma);
        lugarUltimo = (TextView) findViewById(R.id.tv_lugarAlarma);
        tipoUltimo = (TextView) findViewById(R.id.tv_tipoAlarma);
        alarmSelection = (Spinner) findViewById(R.id.sp_alarmSelection);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.spinner_alarmCode_selection, android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alarmSelection.setAdapter(spinnerAdapter);
        alarmSelection.setOnItemSelectedListener(this);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessageList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(this);
        //If DataBase have no data, prossess all sms, else, prossess sms until the first DB register
        if(db.getLastAlarm().getSmsId() == -1){
            populateDbFirstTime();
        }else{
            updateDb();
        }
    }

    private void populateDbFirstTime() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        int timeMillis = smsInboxCursor.getColumnIndex("date");
        int indexId = smsInboxCursor.getColumnIndex("_id");
        Date fechaHoraActual = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        if(indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();
        //setContentView(R.layout.actualizando_db);
        ArrayList<String> listadoCodigoAlarmas = new ArrayList<String>();
        String[] codigoAlarmas = getResources().getStringArray(R.array.codigo_alarmas_occidente);
        for(int i = 0; i < codigoAlarmas.length; i++){
            listadoCodigoAlarmas.add(codigoAlarmas[i]);
        }
        do{
            if(smsInboxCursor.getString(indexAddress).equals("2277")){
                long fechaAlarma = Long.parseLong(smsInboxCursor.getString(timeMillis));
                Date fechaEvento = new Date(fechaAlarma);
                //long timeDiference = ((fechaHoraActual.getTime() - fechaEvento.getTime()) / 1000) / 60;
                String cuerpoDelSms = smsInboxCursor.getString(indexBody);
                //String alarmaEvento = tipoAlarma(cuerpoDelSms);
                String mtso = lugar(cuerpoDelSms);
                //Date dateDifference = new Date(timeDiference);

                Alarma alarma = infoAlarma(cuerpoDelSms, fechaAlarma, Integer.parseInt(smsInboxCursor.getString(indexId)), listadoCodigoAlarmas);


                /*String str = smsInboxCursor.getString(indexAddress) + " at " + format.format(fechaEvento) + "\n ---- \n" + smsInboxCursor.getString(indexBody) + "\n ---------- \n" +
                        indexAddress + " - " + timeMillis + " - " + indexBody + "\n" + "Alarma: " + alarma.getDescripcion() + "\n" + "Tiempo de este evento (min): " + alarma.getFecha()
                        + "\nSitio de la alarma: " + alarma.getLugar() + "\nEstatus de la alarma: " + (alarma.getActiva()==1?"\nActiva":"Cancelada") + "\nId del SMS: "
                        + smsInboxCursor.getString(indexId) + "\n-----------\n";
                arrayAdapter.add(str);*/
                if(alarma.getCodigo() != null){
                    listadoCodigoAlarmas.remove(listadoCodigoAlarmas.indexOf(alarma.getCodigo()));
                    db.guardarAlarma(alarma.getSmsId(), alarma.getCodigo(), alarma.getLugar(), alarma.getDescripcion(), alarma.getFecha(), alarma.getActiva());
                    /*String str = smsInboxCursor.getString(indexAddress) + " at " + format.format(fechaEvento) + "\n ---- \n" + smsInboxCursor.getString(indexBody) + "\n ---------- \n" +
                            indexAddress + " - " + timeMillis + " - " + indexBody + "\n" + "Alarma: " + alarma.getDescripcion() + "\n" + "Tiempo de este evento (min): " + alarma.getFecha()
                            + "\nSitio de la alarma: " + alarma.getLugar() + "\nEstatus de la alarma: " + (alarma.getActiva()==1?"\nActiva":"Cancelada") + "\nId del SMS: "
                            + smsInboxCursor.getString(indexId) + "\n-----------\n";*/
                    String str = alarma.getDescripcion() + " en " + alarma.getLugar() + " con fecha " + format.format(alarma.getFecha());
                    arrayAdapter.add(str);
                }
            }
            /*
            Date fechaEvento = new Date(Long.parseLong(smsInboxCursor.getString(timeMillis)));
            long timeDiference = ((fechaHoraActual.getTime() - fechaEvento.getTime()) / 1000) / 60;
            String cuerpoDelSms = smsInboxCursor.getString(indexBody);
            String alarmaEvento = tipoAlarma(cuerpoDelSms);
            String mtso = lugar(cuerpoDelSms);
            Date dateDifference = new Date(timeDiference);

            Alarma alarma = infoAlarma(cuerpoDelSms, timeDiference);
            if(alarma.getDescripcion()!= null) Log.i(TAG, "indexAddress:  " + smsInboxCursor.getString(indexAddress));
            //if(alarmaEvento != null){
                //if(mtso.equals("MTSO_SC")){
                    String str = smsInboxCursor.getString(indexAddress) + " at " + format.format(fechaEvento) + "\n ---- \n" + smsInboxCursor.getString(indexBody) + "\n ---------- \n" +
                            indexAddress + " - " + timeMillis + " - " + indexBody + "\n" + "Alarma: " + alarma.getDescripcion() + "\n" + "Tiempo de este evento (min): " + alarma.getTiempo()
                            + "\nSitio de la alarma: " + alarma.getLugar() + "\nEstatus de la alarma: " + (alarma.isActiva()?"\nActiva":"Cancelada") + "\n-----------\n";
                    arrayAdapter.add(str);
                //}
            //}*/
        }while(smsInboxCursor.moveToNext());
        //setContentView(R.layout.activity_receive_sms);
    }

    public void updateDb(){
        //get the most recent alarm in db

        //get the alarms from sms until the last alarm in db

    }

    public Alarma infoAlarma(String data, long time, int id, ArrayList<String> codigoAlarmas){
        Alarma alarma = new Alarma(0, null, null, null, 0, 0);
        //String[] codigoAlarmas = getResources().getStringArray(R.array.codigo_alarmas_occidente);
        String[] descripcionAlarmas = getResources().getStringArray(R.array.descripcion_alarmas_occidente);
        for(int i = 0; i < codigoAlarmas.size(); i++){
            if(data.indexOf(codigoAlarmas.get(i)) != -1){
                alarma.setCodigo(codigoAlarmas.get(i));
                alarma.setDescripcion(descripcionAlarmas[i]);
            }
        }
        String[] mtsos = getResources().getStringArray(R.array.mtso);
        for(String mtso : mtsos){
            if(data.indexOf(mtso) != -1) alarma.setLugar(mtso);
        }
        alarma.setFecha(time);
        if(data.indexOf("RESET") == -1) alarma.setActiva(1);
        alarma.setSmsId(id);
        return alarma;
    }

    /*public String tipoAlarma(String data){
        String[] listadoAlarmas = getResources().getStringArray(R.array.alarmas_maracaibo);
        for(String alarma : listadoAlarmas){
            if(data.indexOf(alarma) != -1) return alarma;
        }
        return null;
    }*/

    public String lugar(String data){
        String[] mtsos = getResources().getStringArray(R.array.mtso);
        for(String mtso : mtsos){
            if(data.indexOf(mtso) != -1) return mtso;
        }
        return null;
    }

    public void updateList (final String smsMessage){
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try{
            String[] smsMessages = smsMessageList.get(position).split("\n");
            String address = smsMessages[0];
            String smsMessage = "";

            for(int i = 1; i < smsMessages.length; i++){
                smsMessage += smsMessages[i];
            }

            String smsMessageStr = address +"\n";
            smsMessageStr += smsMessage;
            Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void goToCompose(View v){
        Intent intent = new Intent(ReceiveSMSActivity.this, SendSMSActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.sp_alarmSelection:
                Alarma alarma = db.getLastAlarmByCode(parent.getSelectedItem().toString(), "MTSO_MCBO");
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                fechaUltimo.setText(format.format(new Date(alarma.getFecha())));
                lugarUltimo.setText(alarma.getLugar());
                tipoUltimo.setText(alarma.getCodigo());
                Toast.makeText(this, "Alarm Selected: " + parent.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
