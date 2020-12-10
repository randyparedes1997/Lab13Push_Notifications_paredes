package com.example.lab13pushnotifications.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.lab13pushnotifications.R;
import com.example.lab13pushnotifications.activities.MainActivity;
import com.example.lab13pushnotifications.app.Config;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.net.IDN;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    final private String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.e("TOKEN","Mi token es: " + s);
        saveToken(s);
    }

    private void saveToken(String s) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("token");
        reference.child("123456789").setValue(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String from = remoteMessage.getFrom();
        Log.e(TAG,"Mensaje recibido de: " + from);
        if (remoteMessage == null) return;
        if (remoteMessage.getData().size() > 0){
            String titulo = remoteMessage.getData().get(Config.TITLE);
            String detalle = remoteMessage.getData().get(Config.DETAIL);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                mayorQueOreo(titulo,detalle);
            }
        }
    }

    private void mayorQueOreo(String titulo, String detalle) {
        final Uri alarm = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"+getPackageName()+"raw/tono1");
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,Config.CHANNEL_ID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(Config.CHANNEL_ID,Config.CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
        }

        builder.setAutoCancel(true)
               .setWhen(System.currentTimeMillis())
               .setSound(alarm)
               .setContentIntent(touchNotification())
               .setContentInfo("Nuevo usuario")
               .setContentText(detalle)
               .setContentTitle(titulo)
               .setSmallIcon(R.drawable.ic_notification);

        Random random = new Random();
        int idNotify = random.nextInt(80000);
        manager.notify(idNotify,builder.build());
    }

    public PendingIntent touchNotification(){
        Intent intent= new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this,0,intent,0);
    }

}
