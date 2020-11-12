package pl.com.karwowsm.musiqueue.api.controller;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import lombok.CustomLog;
import pl.com.karwowsm.musiqueue.api.JSONSerializer;

@CustomLog
final class Request<ReqT, ResT> extends JsonRequest<ResT> {

    private final TypeToken<ResT> responseTypeToken;
    private final Map<String, String> headers;

    public Request(int method, String url, ReqT request, TypeToken<ResT> responseTypeToken,
                   Response.Listener<ResT> listener, Response.ErrorListener errorListener) {
        this(new HashMap<>(), method, url, request, responseTypeToken, listener, errorListener);
    }

    public Request(String token, int method, String url, ReqT request, TypeToken<ResT> responseTypeToken,
                   Response.Listener<ResT> listener, Response.ErrorListener errorListener) {
        this(new HashMap<>(), method, url, request, responseTypeToken, listener, errorListener);
        if (token != null) {
            this.headers.put("Authorization", "Bearer " + token);
        }
    }

    public Request(Map<String, String> headers, int method, String url, ReqT request, TypeToken<ResT> responseTypeToken,
                   Response.Listener<ResT> listener, Response.ErrorListener errorListener) {
        super(method, url, (request == null) ? null : JSONSerializer.toJson(request), listener, errorListener);
        this.headers = headers;
        this.responseTypeToken = responseTypeToken;
    }

    @Override
    protected Response<ResT> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            log.v("Got response: " + response.statusCode + " " + jsonString);
            return Response.success(JSONSerializer.fromJson(jsonString, responseTypeToken.getType()),
                HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }
}
