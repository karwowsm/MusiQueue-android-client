package pl.com.karwowsm.musiqueue.api.controller;

import android.net.Uri;

import com.android.volley.Response;
import com.google.gson.reflect.TypeToken;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import lombok.CustomLog;
import lombok.Setter;
import pl.com.karwowsm.musiqueue.BuildConfig;
import pl.com.karwowsm.musiqueue.MusiQueueApplication;
import pl.com.karwowsm.musiqueue.api.JSONSerializer;
import pl.com.karwowsm.musiqueue.api.dto.youtube.Page;
import pl.com.karwowsm.musiqueue.api.dto.youtube.YouTubeContent;
import pl.com.karwowsm.musiqueue.api.dto.youtube.YouTubeVideo;
import pl.com.karwowsm.musiqueue.api.error.YouTubeErrorResponse;
import pl.com.karwowsm.musiqueue.util.SignatureUtils;

@CustomLog
public class YouTubeController {

    private static final Uri BASE_URL = Uri.parse("https://content.googleapis.com/youtube/v3");

    private static final Map<String, String> headers = new HashMap<>();

    @Setter
    private static YouTubeErrorResponse.Listener errorResponseListener;

    public static void init() {
        String packageName = MusiQueueApplication.getInstance().getPackageName();
        String signature = SignatureUtils.getSignature(MusiQueueApplication.getInstance().getPackageManager(), packageName);
        headers.put("X-Android-Package", packageName);
        headers.put("X-Android-Cert", signature);
    }

    public static void search(final String query,
                              Response.Listener<Page<YouTubeContent>> listener) {
        Uri.Builder uriBuilder = BASE_URL.buildUpon().appendPath("search")
            .appendQueryParameter("q", query)
            .appendQueryParameter("type", "video")
            .appendQueryParameter("part", "snippet")
            .appendQueryParameter("safeSearch", "none")
            .appendQueryParameter("topicId", "/m/04rlf")
            .appendQueryParameter("videoSyndicated", "true")
            .appendQueryParameter("prettyPrint", "false")
            .appendQueryParameter("maxResults", "50");

        addToRequestQueue(uriBuilder, new TypeToken<Page<YouTubeContent>>() {
        }, listener);
    }

    public static void getVideo(final String id,
                                Response.Listener<Page<YouTubeVideo>> listener) {
        Uri.Builder uriBuilder = BASE_URL.buildUpon().appendPath("videos")
            .appendQueryParameter("id", id)
            .appendQueryParameter("part", "snippet")
            .appendQueryParameter("part", "contentDetails")
            .appendQueryParameter("prettyPrint", "false");

        addToRequestQueue(uriBuilder, new TypeToken<Page<YouTubeVideo>>() {
        }, listener);
    }

    private static <ReqT, ResT> void addToRequestQueue(Uri.Builder uriBuilder,
                                                       TypeToken<ResT> responseTypeToken,
                                                       Response.Listener<ResT> listener) {
        String url = uriBuilder
            .appendQueryParameter("key", BuildConfig.YOUTUBE_API_KEY)
            .build().toString();

        Request<ReqT, ResT> request = new Request<>(headers, Request.Method.GET, url, null,
            responseTypeToken, listener, error -> {
            YouTubeErrorResponse response = null;
            if (error.networkResponse != null) {
                String data = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                log.w(data);
                response = JSONSerializer.fromJson(data, YouTubeErrorResponse.class);
            } else {
                log.e(error.toString());
            }
            if (errorResponseListener != null) {
                errorResponseListener.onResponse(response);
            }
        });

        MusiQueueApplication.getInstance().addToRequestQueue(request);
    }
}
