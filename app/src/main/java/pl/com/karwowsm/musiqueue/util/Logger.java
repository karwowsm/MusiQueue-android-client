package pl.com.karwowsm.musiqueue.util;

import android.util.Log;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import pl.com.karwowsm.musiqueue.BuildConfig;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Logger {

    private final String tag;

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz.getSimpleName());
    }

    public void v(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, msg);
        }
    }

    public void d(String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }

    public void w(String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, msg);
        }
    }

    public void e(String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg);
        }
    }

    public void e(String msg, Throwable tr) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg, tr);
        }
    }
}
