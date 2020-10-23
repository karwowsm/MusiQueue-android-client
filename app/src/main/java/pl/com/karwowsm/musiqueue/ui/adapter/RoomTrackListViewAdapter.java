package pl.com.karwowsm.musiqueue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import pl.com.karwowsm.musiqueue.AppController;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.dto.RoomTrack;
import pl.com.karwowsm.musiqueue.api.dto.Track;
import pl.com.karwowsm.musiqueue.ui.util.TrackUtils;
import pl.com.karwowsm.musiqueue.ui.view.CustomNetworkImageView;

public class RoomTrackListViewAdapter extends ArrayAdapter<RoomTrack> {

    public RoomTrackListViewAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<RoomTrack> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_view_item_room_track, parent, false);
        }
        RoomTrack roomTrack = getItem(position);
        if (roomTrack == null) {
            return convertView;
        }
        CustomNetworkImageView trackImageView = convertView.findViewById(R.id.track_img);
        TextView titleTextView = convertView.findViewById(R.id.title_tv);
        TextView artistTextView = convertView.findViewById(R.id.artist_tv);
        TextView usernameTextView = convertView.findViewById(R.id.username_tv);
        ImageView kebabMenuIcon = convertView.findViewById(R.id.kebab_menu_ic);

        Track track = roomTrack.getTrack();
        trackImageView.setDefaultImageResId(TrackUtils.getImageResId(track.getSource()));
        trackImageView.setImageUrl(track.getImageUrl(), AppController.getInstance().getImageLoader());
        titleTextView.setText(track.getTitle());
        artistTextView.setText(track.getArtist());
        usernameTextView.setText(roomTrack.getOwner().getUsername());
        kebabMenuIcon.setOnClickListener(parent::showContextMenuForChild);

        if (roomTrack.getIsPlayed()) {
            convertView.setBackgroundColor(convertView.getResources().getColor(R.color.listViewItem));
            convertView.setAlpha(0.5f);
        } else {
            convertView.setBackgroundColor(convertView.getResources().getColor(R.color.listViewItemHighlighted));
            convertView.setAlpha(1);
        }
        return convertView;
    }
}
