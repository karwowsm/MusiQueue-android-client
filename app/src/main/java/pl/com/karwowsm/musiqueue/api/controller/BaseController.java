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
import pl.com.karwowsm.musiqueue.api.TokenHolder;
import pl.com.karwowsm.musiqueue.api.error.ErrorResponse;

@CustomLog
public abstract class BaseController extends TokenHolder {

    @Setter
    private static ErrorResponse.Listener baseErrorResponseListener;

    static void addToRequestQueue(int method, String path) {
        addToRequestQueue(method, path, null);
    }

    static <ReqT> void addToRequestQueue(int method, String path, ReqT jsonRequest) {
        addToRequestQueue(method, path, jsonRequest, null);
    }

    static <ReqT> void addToRequestQueue(int method, String path, ReqT jsonRequest,
                                         Runnable onResponse) {
        Response.Listener<Void> listener = onResponse != null ? response -> onResponse.run() : null;
        addToRequestQueue(method, path, jsonRequest, Void.class, listener, null);
    }

    static <ResT> void addToRequestQueue(int method, String path,
                                         Class<ResT> responseClass,
                                         Response.Listener<ResT> listener) {
        addToRequestQueue(method, path, responseClass, listener, null);
    }

    static <ResT> void addToRequestQueue(int method, String path,
                                         Class<ResT> responseClass,
                                         Response.Listener<ResT> listener,
                                         ErrorResponse.Listener errorResponseListener) {
        addToRequestQueue(method, path, null, responseClass, listener, errorResponseListener);
    }

    static <ReqT, ResT> void addToRequestQueue(int method, String path, ReqT jsonRequest,
                                               Class<ResT> responseClass,
                                               Response.Listener<ResT> listener,
                                               ErrorResponse.Listener errorResponseListener) {
        addToRequestQueue(method, path, jsonRequest, TypeToken.get(responseClass), listener, errorResponseListener);
    }

    static <ReqT, ResT> void addToRequestQueue(int method, String path, ReqT jsonRequest,
                                               TypeToken<ResT> responseTypeToken,
                                               Response.Listener<ResT> listener,
                                               ErrorResponse.Listener errorResponseListener) {
        addToRequestQueue(method, path, new HashMap<>(), jsonRequest, responseTypeToken, listener, errorResponseListener);
    }

    static <ReqT, ResT> void addToRequestQueue(int method, String path, Map<String, String> queryParams,
                                               ReqT jsonRequest, TypeToken<ResT> responseTypeToken,
                                               Response.Listener<ResT> listener,
                                               ErrorResponse.Listener errorResponseListener) {

        Uri.Builder uriBuilder = Uri.parse(BuildConfig.BASE_URL + path).buildUpon();
        for (Map.Entry<String, String> param : queryParams.entrySet()) {
            uriBuilder.appendQueryParameter(param.getKey(), param.getValue());
        }

        Request<ReqT, ResT> request = new Request<>(getToken(), method, uriBuilder.build().toString(),
            jsonRequest, responseTypeToken, listener, error -> {
            ErrorResponse response = null;
            if (error.networkResponse != null) {
                String data = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                log.w(data);
                response = JSONSerializer.fromJson(data, ErrorResponse.class);
            } else {
                log.e(error.toString());
            }
            onErrorResponse(response);
            if (errorResponseListener != null) {
                errorResponseListener.onResponse(response);
            }
        });

        MusiQueueApplication.getInstance().addToRequestQueue(request);
    }

    static void onErrorResponse(ErrorResponse errorResponse) {
        if (baseErrorResponseListener != null) {
            baseErrorResponseListener.onResponse(errorResponse);
        }
    }
}
