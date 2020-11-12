package pl.com.karwowsm.musiqueue;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.StrictMode;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import lombok.CustomLog;
import pl.com.karwowsm.musiqueue.util.LruBitmapCache;

@CustomLog
public class MusiQueueApplication extends Application {

    private static MusiQueueApplication instance;

    private RequestQueue requestQueue;
    private ImageLoader imageLoader;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .permitDiskWrites()
                .permitDiskReads()
                .penaltyLog()
                .penaltyDeath()
                .penaltyFlashScreen()
                .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        }
    }

    public static synchronized MusiQueueApplication getInstance() {
        return instance;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(buildRequestTag(req));
        getRequestQueue().add(req);
        log.v("Sent request: " + req.getTag());
    }

    public ImageLoader getImageLoader() {
        if (imageLoader == null) {
            imageLoader = new ImageLoader(getRequestQueue(), new LruBitmapCache());
        }
        return imageLoader;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            getString(R.string.current_room_notification_channel),
            NotificationManager.IMPORTANCE_LOW);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return requestQueue;
    }

    private <T> String buildRequestTag(Request<T> req) {
        return getRequestMethod(req.getMethod()) + " " + req.getUrl();
    }

    private String getRequestMethod(int method) {
        switch (method) {
            case Request.Method.DEPRECATED_GET_OR_POST:
                return "DEPRECATED_GET_OR_POST";
            case Request.Method.GET:
                return "GET";
            case Request.Method.POST:
                return "POST";
            case Request.Method.PUT:
                return "PUT";
            case Request.Method.DELETE:
                return "DELETE";
            case Request.Method.HEAD:
                return "HEAD";
            case Request.Method.OPTIONS:
                return "OPTIONS";
            case Request.Method.TRACE:
                return "TRACE";
            case Request.Method.PATCH:
                return "PATCH";
            default:
                throw new IllegalStateException("Unknown request method.");
        }
    }
}
