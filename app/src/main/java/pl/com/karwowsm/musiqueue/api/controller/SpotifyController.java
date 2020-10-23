package pl.com.karwowsm.musiqueue.api.controller;

import android.net.Uri;

import com.android.volley.Response;
import com.google.gson.reflect.TypeToken;

import java.nio.charset.StandardCharsets;

import lombok.CustomLog;
import lombok.Setter;
import pl.com.karwowsm.musiqueue.AppController;
import pl.com.karwowsm.musiqueue.api.JSONSerializer;
import pl.com.karwowsm.musiqueue.api.dto.spotify.Page;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyAlbum;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyArtist;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyContent;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyPlaylist;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyPlaylistTrack;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifySearchResult;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyTrack;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyTrackList;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyTrackSimplified;
import pl.com.karwowsm.musiqueue.api.error.SpotifyErrorResponse;
import pl.com.karwowsm.musiqueue.util.SpotifyCallback;

@CustomLog
public class SpotifyController {

    private static final Uri BASE_URL = Uri.parse("https://api.spotify.com/v1");

    @Setter
    private static SpotifyErrorResponse.Listener errorResponseListener;

    public static void getMyTopTracks(Response.Listener<Page<SpotifyTrack>> listener) {
        String url = BASE_URL.buildUpon().appendPath("me").appendPath("top").appendPath("tracks")
            .appendQueryParameter("time_range", "medium_term")
            .appendQueryParameter("limit", "50")
            .build().toString();

        addToRequestQueue(url, new TypeToken<Page<SpotifyTrack>>() {
        }, listener);
    }

    public static void search(final String query, SpotifyContent.Type type, Response.Listener<Page<? extends SpotifyContent>> listener) {
        String url = BASE_URL.buildUpon().appendPath("search")
            .appendQueryParameter("q", query)
            .appendQueryParameter("type", type.name().toLowerCase())
            .appendQueryParameter("market", "from_token")
            .appendQueryParameter("limit", "50")
            .build().toString();

        addToRequestQueue(url, SpotifySearchResult.class,
            result -> {
                switch (type) {
                    case TRACK:
                        listener.onResponse(result.getTracks());
                        break;
                    case ARTIST:
                        listener.onResponse(result.getArtists());
                        break;
                    case ALBUM:
                        listener.onResponse(result.getAlbums());
                        break;
                    case PLAYLIST:
                        listener.onResponse(result.getPlaylists());
                        break;
                }
            });
    }

    public static void getMyTopArtists(Response.Listener<Page<SpotifyArtist>> listener) {
        String url = BASE_URL.buildUpon().appendPath("me").appendPath("top").appendPath("artists")
            .appendQueryParameter("time_range", "medium_term")
            .appendQueryParameter("limit", "50")
            .build().toString();

        addToRequestQueue(url, new TypeToken<Page<SpotifyArtist>>() {
        }, listener);
    }

    public static void getArtistAlbums(String id, Response.Listener<Page<SpotifyAlbum>> listener) {
        String url = BASE_URL.buildUpon().appendPath("artists").appendPath(id).appendPath("albums")
            .appendQueryParameter("include_groups", "album")
            .appendQueryParameter("limit", "50")
            .build().toString();

        addToRequestQueue(url, new TypeToken<Page<SpotifyAlbum>>() {
        }, listener);
    }

    public static void getMyPlaylists(Response.Listener<Page<SpotifyPlaylist>> listener) {
        String url = BASE_URL.buildUpon().appendPath("me").appendPath("playlists")
            .appendQueryParameter("limit", "50")
            .build().toString();

        addToRequestQueue(url, new TypeToken<Page<SpotifyPlaylist>>() {
        }, listener);
    }

    public static void getArtistTopTracks(String id, Response.Listener<SpotifyTrackList> listener) {
        String url = BASE_URL.buildUpon().appendPath("artists").appendPath(id).appendPath("top-tracks")
            .appendQueryParameter("country", "from_token")
            .build().toString();

        addToRequestQueue(url, SpotifyTrackList.class, listener);
    }

    public static void getAlbumTracks(String id, Response.Listener<Page<SpotifyTrackSimplified>> listener) {
        String url = BASE_URL.buildUpon().appendPath("albums").appendPath(id).appendPath("tracks")
            .appendQueryParameter("market", "from_token")
            .appendQueryParameter("limit", "50")
            .build().toString();

        addToRequestQueue(url, new TypeToken<Page<SpotifyTrackSimplified>>() {
        }, listener);
    }

    public static void getPlaylistTracks(String id, Response.Listener<Page<SpotifyPlaylistTrack>> listener) {
        String url = BASE_URL.buildUpon().appendPath("playlists").appendPath(id).appendPath("tracks")
            .appendQueryParameter("market", "from_token")
            .build().toString();

        addToRequestQueue(url, new TypeToken<Page<SpotifyPlaylistTrack>>() {
        }, listener);
    }

    private static <ReqT, ResT> void addToRequestQueue(String url,
                                                       Class<ResT> responseClass,
                                                       Response.Listener<ResT> listener) {
        addToRequestQueue(url, TypeToken.get(responseClass), listener);
    }

    private static <ReqT, ResT> void addToRequestQueue(String url,
                                                       TypeToken<ResT> responseTypeToken,
                                                       Response.Listener<ResT> listener) {
        Request<ReqT, ResT> request = new Request<>(getToken(), Request.Method.GET, url, null,
            responseTypeToken, listener, error -> {
            SpotifyErrorResponse response = null;
            if (error.networkResponse != null) {
                String data = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                log.w(data);
                response = JSONSerializer.fromJson(data, SpotifyErrorResponse.class);
            } else {
                log.e(error.toString());
            }
            if (errorResponseListener != null) {
                errorResponseListener.onResponse(response);
            }
        });

        AppController.getInstance().addToRequestQueue(request);
    }

    private static String getToken() {
        return SpotifyCallback.getInstance().getToken();
    }
}
