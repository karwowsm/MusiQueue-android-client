package pl.com.karwowsm.musiqueue.api.controller;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

import pl.com.karwowsm.musiqueue.api.dto.Page;
import pl.com.karwowsm.musiqueue.api.dto.Track;
import pl.com.karwowsm.musiqueue.api.error.ErrorResponse;

public class TrackController extends BaseController {

    private static final String BASE_PATH = "/tracks";

    public static void findTrack(Response.Listener<Page<Track>> listener,
                                 ErrorResponse.Listener errorResponseListener) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("sort", Track.Fields.queuedNumber + ",DESC");
        addToRequestQueue(Request.Method.GET, BASE_PATH, queryParams, null,
            new TypeToken<Page<Track>>() {
            }, listener, errorResponseListener);
    }
}
