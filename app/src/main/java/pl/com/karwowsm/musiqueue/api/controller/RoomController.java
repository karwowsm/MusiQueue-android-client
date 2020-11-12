package pl.com.karwowsm.musiqueue.api.controller;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import pl.com.karwowsm.musiqueue.api.dto.Page;
import pl.com.karwowsm.musiqueue.api.dto.Room;
import pl.com.karwowsm.musiqueue.api.error.ErrorResponse;
import pl.com.karwowsm.musiqueue.api.request.RoomCreateRequest;
import pl.com.karwowsm.musiqueue.api.request.RoomUpdateRequest;

public class RoomController extends BaseController {

    private static final String BASE_PATH = "/rooms";

    public static void findRoom(final int pageSize, Response.Listener<Page<Room>> listener,
                                ErrorResponse.Listener errorResponseListener) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("size", String.valueOf(pageSize));
        queryParams.put("sort", Room.Fields.startedPlayingAt + ",DESC");
        addToRequestQueue(Request.Method.GET, BASE_PATH, queryParams, null,
            new TypeToken<Page<Room>>() {
            }, listener, errorResponseListener);
    }

    public static void createRoom(final RoomCreateRequest request,
                                  Response.Listener<Room> listener,
                                  ErrorResponse.Listener errorResponseListener) {
        addToRequestQueue(Request.Method.POST, BASE_PATH, request,
            Room.class, listener, errorResponseListener);
    }

    public static void updateRoom(final UUID id, final RoomUpdateRequest request,
                                  Response.Listener<Room> listener,
                                  ErrorResponse.Listener errorResponseListener) {
        addToRequestQueue(Request.Method.PUT, BASE_PATH + "/" + id, request,
            Room.class, listener, errorResponseListener);
    }

    public static void deleteRoom(final UUID id, Runnable onResponse) {
        addToRequestQueue(Request.Method.DELETE, BASE_PATH + "/" + id, null,
            onResponse);
    }
}
