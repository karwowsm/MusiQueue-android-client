package pl.com.karwowsm.musiqueue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import pl.com.karwowsm.musiqueue.AppController;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.dto.Track;
import pl.com.karwowsm.musiqueue.ui.util.TrackUtils;
import pl.com.karwowsm.musiqueue.ui.view.CustomNetworkImageView;

public class TrackListViewAdapter extends ArrayAdapter<Track> {

    public TrackListViewAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Track> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_view_item_track, parent, false);
        }
        Track track = getItem(position);
        if (track == null) {
            return convertView;
        }
        CustomNetworkImageView trackImageView = convertView.findViewById(R.id.track_img);
        TextView titleTextView = convertView.findViewById(R.id.title_tv);
        TextView artistTextView = convertView.findViewById(R.id.artist_tv);
        TextView queuedNumberTextView = convertView.findViewById(R.id.queued_number_tv);

        trackImageView.setDefaultImageResId(TrackUtils.getImageResId(track.getSource()));
        trackImageView.setImageUrl(track.getImageUrl(), AppController.getInstance().getImageLoader());
        titleTextView.setText(track.getTitle());
        artistTextView.setText(track.getArtist());
        queuedNumberTextView.setText(String.valueOf(track.getQueuedNumber()));
        return convertView;
    }
}
