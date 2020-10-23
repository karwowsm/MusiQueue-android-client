package pl.com.karwowsm.musiqueue.api.controller;

import com.android.volley.Request;
import com.android.volley.Response;

import java.util.UUID;

import pl.com.karwowsm.musiqueue.api.dto.Room;
import pl.com.karwowsm.musiqueue.api.error.ErrorResponse;

public class RoomMembersController extends BaseController {

    private static final String BASE_PATH = "/rooms/%s/members";

    public static void joinRoom(final UUID id, Response.Listener<Room> listener,
                                ErrorResponse.Listener errorResponseListener) {
        addToRequestQueue(Request.Method.PATCH, String.format(BASE_PATH, id) + "/me", null,
            Room.class, listener, errorResponseListener);
    }

    public static void leaveRoom(final UUID id, Response.Listener<Room> listener) {
        addToRequestQueue(Request.Method.DELETE, String.format(BASE_PATH, id) + "/me",
            Room.class, listener);
    }
}
