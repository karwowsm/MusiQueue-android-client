package pl.com.karwowsm.musiqueue.api.controller;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.android.volley.Request;
import com.android.volley.Response;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import lombok.CustomLog;
import pl.com.karwowsm.musiqueue.BuildConfig;
import pl.com.karwowsm.musiqueue.MusiQueueApplication;
import pl.com.karwowsm.musiqueue.api.JSONSerializer;
import pl.com.karwowsm.musiqueue.api.dto.RoomTracklist;
import pl.com.karwowsm.musiqueue.api.error.ErrorResponse;
import pl.com.karwowsm.musiqueue.api.request.RoomTrackCreateRequest;
import pl.com.karwowsm.musiqueue.util.FileUtils;

public class RoomTracklistController extends BaseController {

    private static final String BASE_PATH = "/rooms/%s/tracklist";

    public static void getTracklist(final UUID id, final int offset,
                                    Response.Listener<RoomTracklist> listener,
                                    ErrorResponse.Listener errorResponseListener) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("offset", String.valueOf(offset));
        addToRequestQueue(Request.Method.GET, String.format(BASE_PATH, id), queryParams, null,
            RoomTracklist.class, listener, errorResponseListener);
    }

    public static void uploadTrack(final UUID id, final Uri fileUri,
                                   ProgressDialog progressDialog, ErrorResponse.Listener errorResponseListener,
                                   int fileSize) {

        new TrackUploadTask(String.format(BASE_PATH, id), fileUri, fileSize, getToken(),
            progressDialog, errorResponseListener).execute();
    }

    public static void addTrack(final UUID id, final RoomTrackCreateRequest request) {
        addToRequestQueue(Request.Method.PATCH, String.format(BASE_PATH, id), request);
    }

    public static void addTrack(final UUID id, final UUID trackId) {
        addToRequestQueue(Request.Method.PATCH, String.format(BASE_PATH, id) + "/" + trackId);
    }

    public static void deleteTrack(final UUID id, final UUID roomTrackId) {
        addToRequestQueue(Request.Method.DELETE, String.format(BASE_PATH, id) + "/" + roomTrackId);
    }

    public static void playNext(final UUID id) {
        addToRequestQueue(Request.Method.PATCH, String.format(BASE_PATH, id) + "/play/next");
    }

    public static void playTrack(final UUID id, final UUID roomTrackId) {
        addToRequestQueue(Request.Method.PATCH, String.format(BASE_PATH + "/play/%s", id, roomTrackId));
    }

    @CustomLog
    public static class TrackUploadTask {

        private static final String TWO_HYPHENS = "--";
        private static final String LINE_END = "\r\n";
        private final String BOUNDARY = "------------------------" + System.currentTimeMillis();
        private final String TAIL = LINE_END + TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END;

        private static final int BUFFER_SIZE = 1024;

        private final Executor executor = Executors.newSingleThreadExecutor();
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final String url;
        private final Uri fileUri;
        private final int fileSize;
        private final String token;
        private final ProgressDialog progressDialog;
        private final ErrorResponse.Listener errorResponseListener;

        public TrackUploadTask(String path, Uri fileUri, int fileSize,
                               String token, ProgressDialog progressDialog,
                               ErrorResponse.Listener errorResponseListener) {
            url = BuildConfig.BASE_URL + path;
            this.fileUri = fileUri;
            this.fileSize = fileSize;
            this.token = token;
            this.progressDialog = progressDialog;
            this.errorResponseListener = errorResponseListener;
        }

        public void execute() {
            onPreExecute();
            executor.execute(() -> {
                Pair<Integer, String> result = doInBackground();
                handler.post(() -> onPostExecute(result));
            });
        }

        private void onPreExecute() {
            progressDialog.show();
        }

        private Pair<Integer, String> doInBackground() {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection httpURLConnection = null;
            try {
                InputStream inputStream = FileUtils.openInputStream(MusiQueueApplication.getInstance().getApplicationContext(), fileUri);
                String fileName = FileUtils.getFileName(MusiQueueApplication.getInstance().getApplicationContext(), fileUri);
                httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
                httpURLConnection.setRequestMethod("PATCH");
                httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
                httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                httpURLConnection.setDoOutput(true);

                String stringData = TWO_HYPHENS + BOUNDARY + LINE_END +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" +
                    LINE_END + LINE_END;

                int requestLength = stringData.length() + fileSize + TAIL.length();
                httpURLConnection.setRequestProperty("Content-length", String.valueOf(requestLength));
                httpURLConnection.setFixedLengthStreamingMode(requestLength);
                httpURLConnection.connect();

                DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                dataOutputStream.writeBytes(stringData);
                dataOutputStream.flush();

                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                byte[] buf = new byte[BUFFER_SIZE];
                int bytesRead;
                int progress = 0;
                while ((bytesRead = bufferedInputStream.read(buf)) != -1) {
                    dataOutputStream.write(buf, 0, bytesRead);
                    dataOutputStream.flush();
                    progress += bytesRead;
                    onProgressUpdate(progress);
                }
                bufferedInputStream.close();

                dataOutputStream.writeBytes(TAIL);
                dataOutputStream.flush();
                dataOutputStream.close();

                InputStream responseInputStream = httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_CREATED
                    ? httpURLConnection.getInputStream()
                    : httpURLConnection.getErrorStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseInputStream));
                String line;
                StringBuilder result = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }
                bufferedReader.close();
                responseInputStream.close();

                return new Pair<>(httpURLConnection.getResponseCode(), result.toString());
            } catch (Exception e) {
                log.e("Exception thrown during uploading track", e);
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return null;
        }

        private void onProgressUpdate(int progress) {
            progressDialog.setProgress(progress);
        }

        private void onPostExecute(Pair<Integer, String> httpResponse) {
            if (httpResponse != null) {
                if (httpResponse.first != HttpURLConnection.HTTP_CREATED) {
                    log.w(httpResponse.second);
                    ErrorResponse response = JSONSerializer.fromJson(httpResponse.second, ErrorResponse.class);
                    onErrorResponse(response);
                    errorResponseListener.onResponse(response);
                }
            } else {
                errorResponseListener.onResponse(null);
            }
            progressDialog.dismiss();
        }
    }
}
