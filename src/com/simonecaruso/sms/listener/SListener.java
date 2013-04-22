package com.simonecaruso.sms.listener;

import com.simonecaruso.sms.R;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;

public final class SListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("TAGG", "Intent ricevuta");
        Bundle bundle = intent.getExtras();
        SharedPreferences conf = PreferenceManager.getDefaultSharedPreferences(context);
        StringBuilder body = new StringBuilder();
        
        Log.d("TAGG", "OK: " + conf.getBoolean("ison", false) + " String: " + conf.getString("matchstring", "asd"));
        //notify(context, "prrr",Uri.parse(conf.getString("sound", "")));
		 
        if (!conf.getBoolean("ison", false) || bundle == null){
        	return;
        }
        try{
    		Object messages[] = (Object[]) bundle.get("pdus");
    		SmsMessage smsMessage[] = new SmsMessage[messages.length];
    		SmsMessage firstMessage;
    		ContentValues values = new ContentValues();
    		
    		firstMessage = SmsMessage.createFromPdu((byte[]) messages[0]);
  			values.put("person",  firstMessage.getEmailFrom());
			values.put("subject",  firstMessage.getPseudoSubject());
    		
			if(! firstMessage.getOriginatingAddress().equals(conf.getString("matchstring", ""))){
				Log.d("TAGG", "Match NON trovato: " + firstMessage.getOriginatingAddress());
				return;
			}
			Log.d("TAGG", "Match trovato: " + firstMessage.getOriginatingAddress());
			abortBroadcast();
			
			if(conf.getString("changeto", "").equals(""))
				values.put("address", firstMessage.getOriginatingAddress());
			else{
				Log.d("TAGG", "Address changed");
				values.put("address", conf.getString("changeto", ""));
			}
			
    		for (int n = 0; n < messages.length; n++) {
    			smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
    			Log.d("TAGG", "Originating address " + smsMessage[n].getOriginatingAddress());
    			Log.d("TAGG", "Message body " + smsMessage[n].getDisplayMessageBody());
    			Log.d("TAGG", "Pseudo Subject: " + smsMessage[n].getPseudoSubject());
    			body.append(smsMessage[n].getDisplayMessageBody());
    		}
    		values.put("body", body.toString());
    		context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
			notify(context, body.toString(), Uri.parse(conf.getString("sound", "")));
        }catch(Exception e){
        	Log.d("Exception caught ",e.getMessage());
        }
	}
	
	private void notify(Context context, String msg, Uri ring){
		PendingIntent intent = PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
				.setDefaults(0)
				.setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("SMS caught")
		        .setContentText(msg)
		        .setAutoCancel(true)
		        .setSound(ring)
		        .setOnlyAlertOnce(true)
		        .setContentIntent(intent)
		        .setWhen(System.currentTimeMillis());
		
        NotificationManager mNotificationManager =
			    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
		mNotificationManager.notify(0, mBuilder.build());
	}

}
