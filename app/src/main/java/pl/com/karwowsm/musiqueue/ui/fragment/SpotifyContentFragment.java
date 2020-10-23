package pl.com.karwowsm.musiqueue.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import pl.com.karwowsm.musiqueue.AppController;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.controller.SpotifyController;
import pl.com.karwowsm.musiqueue.api.dto.spotify.Page;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyContent;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyTrack;
import pl.com.karwowsm.musiqueue.ui.view.CustomNetworkImageView;

public class SpotifyContentFragment extends Fragment {

    @Setter
    private static SpotifyDialogFragment.Listener listener;

    private SearchView searchView;
    private List<SpotifyContent> items;
    @Getter
    private Type contentType;
    private Adapter adapter;
    private SpotifyContent artist;

    public static SpotifyContentFragment createInstance(Type contentType) {
        SpotifyContentFragment fragment = new SpotifyContentFragment();
        fragment.items = new ArrayList<>();
        fragment.contentType = contentType;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_spotify_content, container, false);
        searchView = rootView.findViewById(R.id.query_sv);
        if (contentType != Type.ARTIST_AND_ALBUMS) {
            searchView.setOnClickListener(v -> searchView.onActionViewExpanded());
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (!newText.isEmpty()) {
                        searchContent(newText);
                    } else {
                        getContent();
                    }
                    return false;
                }
            });
        } else {
            searchView.setVisibility(View.GONE);
        }
        ListView listView = rootView.findViewById(R.id.spotify_content_lv);
        int layoutResource = contentType != Type.TRACKS
            ? R.layout.list_view_item_spotify_content
            : R.layout.list_view_item_external_track;
        adapter = new Adapter(getContext(), layoutResource, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((adapterView, view, pos, l) -> {
            if (contentType == Type.ARTISTS) {
                artist = items.get(pos);
                changeContentType(Type.ARTIST_AND_ALBUMS);
            } else {
                listener.onFinishSpotifyDialog(items.get(pos));
            }
        });
        if (items.isEmpty()) {
            getContent();
        }
        return rootView;
    }

    public void changeContentType(Type contentType) {
        this.contentType = contentType;
        if (contentType == Type.ARTIST_AND_ALBUMS) {
            searchView.setVisibility(View.GONE);
            getContent();
        } else {
            searchView.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(searchView.getQuery())) {
                getContent();
            } else {
                searchContent(searchView.getQuery().toString());
            }
        }
    }

    private void getContent() {
        adapter.clear();
        switch (contentType) {
            case ARTISTS:
                SpotifyController.getMyTopArtists(artistsPage -> adapter.addAll(artistsPage.getItems()));
                break;
            case ARTIST_AND_ALBUMS:
                adapter.add(artist);
                SpotifyController.getArtistAlbums(artist.getId(), albumsPage -> adapter.addAll(albumsPage.getItems()));
                break;
            case PLAYLISTS:
                SpotifyController.getMyPlaylists(playlistPage -> adapter.addAll(playlistPage.getItems()));
                break;
            case TRACKS:
                SpotifyController.getMyTopTracks(tracksPage -> adapter.addAll(tracksPage.getItems()));
                break;
        }
    }

    private void searchContent(String query) {
        Response.Listener<Page<? extends SpotifyContent>> listener = resultPage -> {
            adapter.clear();
            adapter.addAll(resultPage.getItems());
        };
        switch (contentType) {
            case TRACKS:
                SpotifyController.search(query, SpotifyContent.Type.TRACK, listener);
                break;
            case ARTISTS:
                SpotifyController.search(query, SpotifyContent.Type.ARTIST, listener);
                break;
            case PLAYLISTS:
                SpotifyController.search(query, SpotifyContent.Type.PLAYLIST, listener);
                break;
        }
    }

    public enum Type {
        TRACKS, ARTISTS, ARTIST_AND_ALBUMS, PLAYLISTS
    }

    private class Adapter extends ArrayAdapter<SpotifyContent> {

        private final int layoutResource;

        Adapter(@NonNull Context context, @LayoutRes int layoutResource, @NonNull List<SpotifyContent> objects) {
            super(context, layoutResource, objects);
            this.layoutResource = layoutResource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(layoutResource, parent, false);
            }
            SpotifyContent spotifyContent = getItem(position);
            if (spotifyContent == null) {
                return convertView;
            }
            int imageViewIdResource = contentType != Type.TRACKS ? R.id.spotify_content_img : R.id.track_img;
            int nameTextViewIdResource = contentType != Type.TRACKS ? R.id.name_tv : R.id.title_tv;
            CustomNetworkImageView imageView = convertView.findViewById(imageViewIdResource);
            TextView nameTextView = convertView.findViewById(nameTextViewIdResource);
            TextView descriptionTextView = convertView.findViewById(R.id.description_tv);

            imageView.setDefaultImageResId(R.drawable.ic_spotify);
            imageView.setImageUrl(spotifyContent.getImageUrl(), AppController.getInstance().getImageLoader());
            nameTextView.setText(spotifyContent.getName());
            if (contentType == Type.TRACKS) {
                TextView artistTextView = convertView.findViewById(R.id.artist_tv);
                artistTextView.setText(((SpotifyTrack) spotifyContent).getArtistsNames());
            } else if (contentType == Type.ARTIST_AND_ALBUMS && spotifyContent.getType().equals(SpotifyContent.Type.ARTIST)) {
                descriptionTextView.setText(R.string.top_tracks);
            } else {
                descriptionTextView.setText(null);
            }
            return convertView;
        }

        @Override
        public void add(@Nullable SpotifyContent object) {
            super.add(object);
        }
    }
}
