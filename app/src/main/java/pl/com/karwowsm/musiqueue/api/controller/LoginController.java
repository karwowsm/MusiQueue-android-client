package pl.com.karwowsm.musiqueue.api.controller;

import com.android.volley.Request;
import com.android.volley.Response;

import pl.com.karwowsm.musiqueue.api.dto.Token;
import pl.com.karwowsm.musiqueue.api.error.ErrorResponse;
import pl.com.karwowsm.musiqueue.api.request.TokenCreateRequest;

public class LoginController extends BaseController {

    private static final String BASE_PATH = "/login";

    public static void login(final TokenCreateRequest request,
                             Response.Listener<Token> listener,
                             ErrorResponse.Listener errorResponseListener) {
        addToRequestQueue(Request.Method.POST, BASE_PATH, request,
            Token.class, listener, errorResponseListener);
    }
}
