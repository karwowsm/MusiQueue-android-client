package pl.com.karwowsm.musiqueue.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import lombok.CustomLog;
import pl.com.karwowsm.musiqueue.Constants;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.ui.activity.LoginActivity;

@CustomLog
public class RoomService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String roomName = intent.getStringExtra("room");
        Intent launcherIntent = new Intent(getApplicationContext(), LoginActivity.class);
        launcherIntent.setAction(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, launcherIntent, 0);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(roomName)
            .setSmallIcon(R.drawable.ico)
            .setOngoing(true)
            .setContentIntent(pendingIntent);
        startForeground(Constants.NOTIFICATION_ID, notificationBuilder.build());
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }
}
