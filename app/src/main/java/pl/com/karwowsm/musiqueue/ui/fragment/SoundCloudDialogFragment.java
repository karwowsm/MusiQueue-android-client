package pl.com.karwowsm.musiqueue.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import java.io.Serializable;

import pl.com.karwowsm.musiqueue.AppController;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.controller.SoundCloudController;
import pl.com.karwowsm.musiqueue.api.dto.soundcloud.SoundCloudTrack;
import pl.com.karwowsm.musiqueue.ui.view.CustomNetworkImageView;

public class SoundCloudDialogFragment extends DialogFragment {

    private Adapter adapter;
    private Listener listener;

    @Override
    public void setArguments(Bundle args) {
        listener = (Listener) args.get(Listener.BUNDLE_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_fragment_search, container, false);
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setTitle(R.string.soundCloud);
        toolbar.setBackgroundColor(getResources().getColor(R.color.soundCloud));
        toolbar.setNavigationOnClickListener(v -> dismiss());
        ListView listView = rootView.findViewById(R.id.external_tracks_lv);
        adapter = new Adapter(getContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            SoundCloudTrack item = adapter.getItem(position);
            listener.onFinish(item);
        });
        SearchView searchView = rootView.findViewById(R.id.query_sv);
        searchView.setOnClickListener(v -> searchView.onActionViewExpanded());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    searchTracks(newText);
                }
                return false;
            }
        });
        return rootView;
    }

    private void searchTracks(String query) {
        SoundCloudController.searchTracks(query, soundCloudTrackPage -> {
            adapter.clear();
            adapter.addAll(soundCloudTrackPage.getCollection());
        });
    }

    private static class Adapter extends ArrayAdapter<SoundCloudTrack> {

        Adapter(@NonNull Context context) {
            super(context, R.layout.list_view_item_external_track);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_view_item_external_track, parent, false);
            }
            SoundCloudTrack soundCloudTrack = getItem(position);
            if (soundCloudTrack == null) {
                return convertView;
            }
            CustomNetworkImageView trackImageView = convertView.findViewById(R.id.track_img);
            TextView titleTextView = convertView.findViewById(R.id.title_tv);
            TextView artistTextView = convertView.findViewById(R.id.artist_tv);

            trackImageView.setDefaultImageResId(R.drawable.ic_soundcloud);
            trackImageView.setImageUrl(soundCloudTrack.getArtwork_url(), AppController.getInstance().getImageLoader());
            titleTextView.setText(soundCloudTrack.getTitle());
            artistTextView.setText(soundCloudTrack.getUser().getUsername());
            return convertView;
        }

        @Override
        public void add(@Nullable SoundCloudTrack object) {
            super.add(object);
        }
    }

    public interface Listener extends Serializable {

        String BUNDLE_KEY = "SOUNDCLOUD_DIALOG_LISTENER";

        void onFinish(SoundCloudTrack soundCloudTrack);
    }
}
