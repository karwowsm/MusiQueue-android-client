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
import java.util.Optional;

import pl.com.karwowsm.musiqueue.AppController;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.dto.Room;
import pl.com.karwowsm.musiqueue.api.dto.RoomTrack;
import pl.com.karwowsm.musiqueue.api.dto.Track;
import pl.com.karwowsm.musiqueue.ui.util.TrackUtils;
import pl.com.karwowsm.musiqueue.ui.view.CustomNetworkImageView;

public class RoomListViewAdapter extends ArrayAdapter<Room> {

    public RoomListViewAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Room> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_view_item_room, parent, false);
        }
        Room room = getItem(position);
        if (room == null) {
            return convertView;
        }
        CustomNetworkImageView currentTrackImageView = convertView.findViewById(R.id.current_track_img);
        TextView nameTextView = convertView.findViewById(R.id.name_tv);
        TextView hostTextView = convertView.findViewById(R.id.host_tv);
        TextView membersCountTextView = convertView.findViewById(R.id.members_count_tv);

        Track currentTrack = Optional.ofNullable(room.getCurrentTrack()).map(RoomTrack::getTrack).orElse(null);
        if (currentTrack == null) {
            currentTrackImageView.setDefaultImageResId(R.drawable.ico);
            currentTrackImageView.setImageUrl(null, AppController.getInstance().getImageLoader());
        } else {
            currentTrackImageView.setDefaultImageResId(TrackUtils.getImageResId(currentTrack.getSource()));
            currentTrackImageView.setImageUrl(currentTrack.getImageUrl(), AppController.getInstance().getImageLoader());
        }
        nameTextView.setText(room.getName());
        hostTextView.setText(room.getHost().getUsername());
        membersCountTextView.setText(String.valueOf(room.getMembersCount()));

        if (room.getPlaying()) {
            convertView.setBackgroundColor(convertView.getResources().getColor(R.color.listViewItemHighlighted));
            convertView.setAlpha(1);
        } else {
            convertView.setBackgroundColor(convertView.getResources().getColor(R.color.listViewItem));
            convertView.setAlpha(0.5f);
        }
        return convertView;
    }
}
