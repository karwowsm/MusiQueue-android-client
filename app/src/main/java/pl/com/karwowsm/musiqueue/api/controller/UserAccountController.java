package pl.com.karwowsm.musiqueue.api.controller;

import com.android.volley.Request;
import com.android.volley.Response;

import pl.com.karwowsm.musiqueue.api.dto.UserAccount;
import pl.com.karwowsm.musiqueue.api.error.ErrorResponse;
import pl.com.karwowsm.musiqueue.api.request.UserAccountCreateRequest;

public class UserAccountController extends BaseController {

    private static final String BASE_PATH = "/users";

    public static void register(final UserAccountCreateRequest request,
                                Response.Listener<UserAccount> listener,
                                ErrorResponse.Listener errorResponseListener) {
        addToRequestQueue(Request.Method.POST, BASE_PATH, request,
            UserAccount.class, listener, errorResponseListener);
    }

    public static void getMe(Response.Listener<UserAccount> listener) {
        addToRequestQueue(Request.Method.GET, BASE_PATH + "/me",
            UserAccount.class, listener);
    }
}
