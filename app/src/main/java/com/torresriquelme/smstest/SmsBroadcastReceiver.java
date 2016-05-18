package com.torresriquelme.smstest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by E04083 on 11/05/2016.
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();

        if(intentExtras != null){
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";

            for(int i = 0; i < sms.length; i++){
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                String smsBody = smsMessage.getMessageBody().toString();
                String address = smsMessage.getOriginatingAddress();
                long timeMillis = smsMessage.getTimestampMillis();

                Date date = new Date(timeMillis);
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
                String dateText = format.format(date);

                smsMessageStr += address + " at " + "\t" + dateText + "\n";
                smsMessageStr += "--------------" + "\n";
                smsMessageStr += smsBody + "\n";
                smsMessageStr += "--------------" + "\n";
            }

            Toast.makeText(context, smsMessageStr, Toast.LENGTH_SHORT).show();

            ReceiveSMSActivity inst = ReceiveSMSActivity.instance().instance();
            if(inst != null){
                inst.updateList(smsMessageStr);
            }

        }
    }
}
