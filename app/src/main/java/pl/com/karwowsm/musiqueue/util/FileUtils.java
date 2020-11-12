package pl.com.karwowsm.musiqueue.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import lombok.CustomLog;
import lombok.experimental.UtilityClass;

@CustomLog
@UtilityClass
public final class FileUtils {

    public static long getFileSize(Context context, Uri uri) throws IOException {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
            }
        }
        FileDescriptor fileDescriptor = context.getContentResolver()
            .openFileDescriptor(uri, "r")
            .getFileDescriptor();
        try (FileInputStream fileInputStream = new FileInputStream(fileDescriptor)) {
            return fileInputStream.getChannel().size();
        }
    }

    public static String getFileName(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        String result = uri.getPath();
        int cut = result.lastIndexOf('/');
        if (cut != -1) {
            result = result.substring(cut + 1);
        }
        return result;
    }

    public static InputStream openInputStream(Context context, Uri uri) throws FileNotFoundException {
        return context.getContentResolver().openInputStream(uri);
    }
}
