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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReceiveSMSActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static ReceiveSMSActivity inst;
    ArrayList<String> smsMessageList = new ArrayList<String>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;
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
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessageList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(this);
        refreshSmsInbox();
    }

    private void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        int timeMillis = smsInboxCursor.getColumnIndex("date");
        Date fechaHoraActual = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        //String dateText = format.format(date);
        if(indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();
        do{
            if(smsInboxCursor.getString(indexAddress).equals("2277")){
                Date fechaEvento = new Date(Long.parseLong(smsInboxCursor.getString(timeMillis)));
                long timeDiference = ((fechaHoraActual.getTime() - fechaEvento.getTime()) / 1000) / 60;
                String cuerpoDelSms = smsInboxCursor.getString(indexBody);
                String alarmaEvento = tipoAlarma(cuerpoDelSms);
                String mtso = lugar(cuerpoDelSms);
                Date dateDifference = new Date(timeDiference);

                Alarma alarma = infoAlarma(cuerpoDelSms, timeDiference);
                Log.i(TAG, "Id: " + smsInboxCursor.getString(0) + " || Thread_id: " + smsInboxCursor.getString(1));

                String str = smsInboxCursor.getString(indexAddress) + " at " + format.format(fechaEvento) + "\n ---- \n" + smsInboxCursor.getString(indexBody) + "\n ---------- \n" +
                        indexAddress + " - " + timeMillis + " - " + indexBody + "\n" + "Alarma: " + alarma.getDescripcion() + "\n" + "Tiempo de este evento (min): " + alarma.getTiempo()
                        + "\nSitio de la alarma: " + alarma.getLugar() + "\nEstatus de la alarma: " + (alarma.isActiva()?"\nActiva":"Cancelada") + "\nId del SMS: "
                        + smsInboxCursor.getString(0) + "\n-----------\n";
                arrayAdapter.add(str);

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
    }

    public Alarma infoAlarma(String data, long time){
        Alarma alarma = new Alarma(null, null, 0, false);
        String[] listadoAlarmas = getResources().getStringArray(R.array.alarmas_maracaibo);
        for(String descripcion : listadoAlarmas){
            if(data.indexOf(descripcion) != -1) alarma.setDescripcion(descripcion);
        }
        String[] mtsos = getResources().getStringArray(R.array.mtso);
        for(String mtso : mtsos){
            if(data.indexOf(mtso) != -1) alarma.setLugar(mtso);
        }
        alarma.setTiempo(time);
        if(data.indexOf("RESET") == -1) alarma.setActiva(true);
        return alarma;
    }

    public String tipoAlarma(String data){
        String[] listadoAlarmas = getResources().getStringArray(R.array.alarmas_maracaibo);
        for(String alarma : listadoAlarmas){
            if(data.indexOf(alarma) != -1) return alarma;
        }
        return null;
    }

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

}
