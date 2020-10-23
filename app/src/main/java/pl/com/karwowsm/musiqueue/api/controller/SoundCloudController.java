package pl.com.karwowsm.musiqueue.api.controller;

import android.net.Uri;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.reflect.TypeToken;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.CustomLog;
import lombok.Setter;
import pl.com.karwowsm.musiqueue.AppController;
import pl.com.karwowsm.musiqueue.api.dto.soundcloud.Page;
import pl.com.karwowsm.musiqueue.api.dto.soundcloud.SoundCloudTrack;
import pl.com.karwowsm.musiqueue.api.dto.soundcloud.SoundCloudTrackUrl;

@CustomLog
public class SoundCloudController {

    private static final Uri BASE_URL = Uri.parse("https://api-v2.soundcloud.com");

    private static String CLIENT_ID;

    @Setter
    private static Response.ErrorListener errorListener;

    public static void getClientId() {
        StringRequest request = new StringRequest("https://a-v2.sndcdn.com/assets/2-fb73347f-3.js", response -> {
            Matcher matcher = Pattern.compile("[\\s\\S]*client_id=([0-9a-zA-z]{32})[\\s\\S]*")
                .matcher(response);
            if (matcher.matches()) {
                CLIENT_ID = matcher.group(1);
            }
        },
            error -> {
                log.e(error.toString());
                errorListener.onErrorResponse(error);
            });

        AppController.getInstance().addToRequestQueue(request);
    }

    public static void searchTracks(final String query,
                                    Response.Listener<Page<SoundCloudTrack>> listener) {
        Uri.Builder uriBuilder = BASE_URL.buildUpon().appendPath("search").appendPath("tracks")
            .appendQueryParameter("q", query)
            .appendQueryParameter("limit", "50");

        addToRequestQueue(uriBuilder,
            new TypeToken<Page<SoundCloudTrack>>() {
            }, listener);
    }

    public static void generateSoundCloudTrackUrl(String url,
                                                  Response.Listener<SoundCloudTrackUrl> listener) {
        addToRequestQueue(Uri.parse(url).buildUpon(),
            TypeToken.get(SoundCloudTrackUrl.class), listener);
    }

    private static <ReqT, ResT> void addToRequestQueue(Uri.Builder uriBuilder,
                                                       TypeToken<ResT> responseTypeToken,
                                                       Response.Listener<ResT> listener) {
        String url = uriBuilder
            .appendQueryParameter("client_id", CLIENT_ID)
            .build().toString();

        Request<ReqT, ResT> request = new Request<>(Request.Method.GET, url, null,
            responseTypeToken, listener, error -> {
            log.e(error.toString());
            errorListener.onErrorResponse(error);
        });

        AppController.getInstance().addToRequestQueue(request);
    }
}
