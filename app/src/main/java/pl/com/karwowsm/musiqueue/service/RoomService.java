package pl.com.karwowsm.musiqueue.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import lombok.CustomLog;
import pl.com.karwowsm.musiqueue.Constants;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.dto.Track;
import pl.com.karwowsm.musiqueue.ui.activity.MainActivity;

@CustomLog
public class RoomService extends Service {

    private static final int NOTIFICATION_ID = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String roomName = intent.getStringExtra("room");
        Notification notification = buildNotification(getApplicationContext())
            .setContentTitle(roomName)
            .build();
        startForeground(NOTIFICATION_ID, notification);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    public static void updateNotification(Context context, Track track) {
        Notification notification = buildNotification(context)
            .setContentTitle(track.getTitle())
            .setContentText(track.getArtist())
            .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private static NotificationCompat.Builder buildNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        return new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ico)
            .setOngoing(true)
            .setContentIntent(pendingIntent);
    }
}
