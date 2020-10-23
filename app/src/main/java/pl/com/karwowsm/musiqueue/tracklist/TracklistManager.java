package pl.com.karwowsm.musiqueue.tracklist;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.SimpleExoPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import pl.com.karwowsm.musiqueue.Constants;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.controller.RoomTracklistController;
import pl.com.karwowsm.musiqueue.api.controller.SpotifyController;
import pl.com.karwowsm.musiqueue.api.controller.YouTubeController;
import pl.com.karwowsm.musiqueue.api.dto.RoomTrack;
import pl.com.karwowsm.musiqueue.api.dto.Track;
import pl.com.karwowsm.musiqueue.api.dto.UserAccount;
import pl.com.karwowsm.musiqueue.api.dto.soundcloud.SoundCloudTrack;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyAlbum;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyContent;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyPlaylistTrack;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyTrack;
import pl.com.karwowsm.musiqueue.api.dto.spotify.SpotifyTrackSimplified;
import pl.com.karwowsm.musiqueue.api.dto.youtube.YouTubeContent;
import pl.com.karwowsm.musiqueue.api.request.RoomTrackCreateRequest;
import pl.com.karwowsm.musiqueue.api.ws.MessagingService;
import pl.com.karwowsm.musiqueue.tracklist.player.Player;
import pl.com.karwowsm.musiqueue.ui.activity.LoginActivity;
import pl.com.karwowsm.musiqueue.ui.adapter.RoomTrackListViewAdapter;
import pl.com.karwowsm.musiqueue.ui.view.CurrentTrackView;
import pl.com.karwowsm.musiqueue.util.ExoPlayerCallback;
import pl.com.karwowsm.musiqueue.util.FileUtils;

public class TracklistManager {

    private final Context context;
    private final UserAccount me;
    private final UUID roomId;
    @Getter
    private final CurrentTrackView currentTrackView;
    private final MessagingService messagingService;
    private Tracklist tracklist;
    private RoomTrackListViewAdapter roomTrackListViewAdapter;
    @Setter
    private Runnable onYouTubeLoggingInRequisition;
    @Setter
    private Runnable onSpotifyLoggingInRequisition;

    private Toast toast;

    public TracklistManager(Context context, UserAccount me, UUID roomId,
                            CurrentTrackView currentTrackView) {
        this.context = context;
        this.me = me;
        this.roomId = roomId;
        this.currentTrackView = currentTrackView;
        messagingService = new MessagingService();
    }

    public void initialize(ListView listView) {
        RoomTracklistController.getTracklist(roomId, roomTracklist -> {
            roomTrackListViewAdapter = new RoomTrackListViewAdapter(
                context,
                R.layout.list_view_item_room_track,
                roomTracklist.getTracklist());
            listView.setAdapter(roomTrackListViewAdapter);
            tracklist = new Tracklist(roomTracklist);
            if (tracklist.getCurrentTrack() != null) {
                playCurrentTrack(roomTracklist.getStartedPlayingAt());
                listView.setSelection(tracklist.getCurrentTrack().getIndex());
            }
            checkIfRequiredPlayersAreInitialized(tracklist.getNotPlayedYet());
            listView.setOnItemClickListener((adapterView, view, pos, l) -> {
                if (pos < tracklist.getPlayedCount()) {
                    showToast(R.string.on_played_click);
                    return;
                }
                if (isItMine(tracklist.getCurrentTrack())) {
                    for (int i = tracklist.getPlayedCount(); i < pos; i++) {
                        if (!isItMine(roomTrackListViewAdapter.getItem(i))) {
                            showToast(R.string.refusal_to_play);
                            return;
                        }
                    }
                    requestPlaying(roomTrackListViewAdapter.getItem(pos));
                } else {
                    showToast(R.string.refusal_to_play);
                }
            });
            messagingService.initRoomTracksSubscription(roomId, message -> {
                switch (message.getType()) {
                    case ADDED:
                        tracklist.add(message.getTrack());
                        checkIfRequiredPlayerIsInitialized(message.getTrack());
                        break;
                    case DELETED:
                        tracklist.delete(message.getTrack());
                        break;
                    case PLAYED:
                        tracklist.updateCurrentTrack(message.getTrack(), message.getTimestamp());
                        checkIfRequiredPlayerIsInitialized(message.getTrack());
                        afterPlay();
                        break;
                }
                roomTrackListViewAdapter.notifyDataSetChanged();
            });
        });
    }

    public void addFileContent(Uri uri) throws IOException {
        int fileSize = FileUtils.getFileSize(context, uri);
        Resources resources = context.getResources();
        int uploadFileSizeLimit = resources.getInteger(R.integer.uploadFileSizeLimit);
        if (fileSize > uploadFileSizeLimit) {
            showToast(R.string.file_too_large, uploadFileSizeLimit / 1024 / 1024);
            return;
        }
        String fileName = FileUtils.getFileName(context, uri);
        InputStream inputStream = FileUtils.openInputStream(context, uri);

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getResources().getString(R.string.uploading));
        progressDialog.setCancelable(false);
        progressDialog.setMax(fileSize);

        RoomTracklistController.uploadTrack(roomId, inputStream, progressDialog, error -> {
            if (error != null) {
                showToast(error.getMessage());
            } else {
                showToast(R.string.uploading_track_failed);
            }
        }, fileName, fileSize);
    }

    public void addYouTubeContent(YouTubeContent youTubeContent) {
        YouTubeController.getVideo(youTubeContent.getId().getVideoId(), youTubeVideoPage -> {
            RoomTrackCreateRequest request = RoomTrackCreateRequest.of(youTubeVideoPage.getItems().get(0));
            RoomTracklistController.addTrack(roomId, request);
        });
    }

    public void addSpotifyContent(SpotifyContent spotifyContent) {
        switch (spotifyContent.getType()) {
            case ARTIST:
                SpotifyController.getArtistTopTracks(spotifyContent.getId(),
                    trackList -> addSpotifyTracks(trackList.getTracks()));
                break;
            case ALBUM:
                SpotifyController.getAlbumTracks(spotifyContent.getId(),
                    tracksPage -> addSpotifyTracks(tracksPage.getItems(), (SpotifyAlbum) spotifyContent));
                break;
            case PLAYLIST:
                SpotifyController.getPlaylistTracks(spotifyContent.getId(),
                    playlistTracks -> addSpotifyPlaylistTracks(playlistTracks.getItems()));
                break;
            case TRACK:
                SpotifyTrack track = (SpotifyTrack) spotifyContent;
                if (track.getIs_playable() != null && !track.getIs_playable()) {
                    showToast(context.getResources().getQuantityString(R.plurals.unavailable_tracks_chosen, 1));
                } else {
                    RoomTracklistController.addTrack(roomId, RoomTrackCreateRequest.of(track));
                }
                break;
            default:
                break;
        }
    }

    public void addSoundCloudTrack(SoundCloudTrack soundCloudTrack) {
        RoomTracklistController.addTrack(roomId, RoomTrackCreateRequest.of(soundCloudTrack));
    }

    public void addTrack(Track track) {
        RoomTracklistController.addTrack(roomId, track.getId());
    }

    public void remove(RoomTrack roomTrack) {
        RoomTracklistController.deleteTrack(roomId, roomTrack.getId());
    }

    public void requestPlayingNext() {
        tracklist.getCurrentTrack().setIsPlayed(true);
        roomTrackListViewAdapter.notifyDataSetChanged();
        Player.stop();
        if (!tracklist.getQueuedTracks().isEmpty()) {
            RoomTracklistController.playNext(roomId);
        }
    }

    public RoomTrack getCurrentTrack() {
        return tracklist.getCurrentTrack();
    }

    public RoomTrack getTrackOnPosition(int position) {
        return tracklist.getTrackOnPosition(position);
    }

    public boolean toggleMuted() {
        if (Player.isMuted()) {
            Player.unMute();
        } else {
            Player.mute();
        }
        return Player.isMuted();
    }

    public long getUserQueuedTracksCount() {
        int ret = tracklist.getCurrentTrack() != null && isItMine(tracklist.getCurrentTrack()) ? 1 : 0;
        List<RoomTrack> queuedTracks = tracklist.getQueuedTracks();
        return ret + queuedTracks.stream().filter(this::isItMine).count();
    }

    public void playBySourceIfNeeded(Track.Source... sources) {
        RoomTrack currentTrack = tracklist.getCurrentTrack();
        if (currentTrack != null && Arrays.stream(sources).anyMatch(source -> currentTrack.getTrack().getSource().equals(source))) {
            playCurrentTrack(Player.getStartedPlayingAt());
        }
    }

    public void finish() {
        messagingService.unsubscribe();
        Player.stop();
        Player.releaseExoPlayer();
        Player.setYouTubePlayer(null);
        currentTrackView.finish();
    }

    private void addSpotifyPlaylistTracks(List<SpotifyPlaylistTrack> playlistTracks) {
        addSpotifyTracks(playlistTracks.stream()
            .map(SpotifyPlaylistTrack::getTrack)
            .collect(Collectors.toList()));
    }

    private void addSpotifyTracks(List<SpotifyTrack> tracks) {
        List<SpotifyTrack> playable = getPlayableSpotifyTracks(tracks);
        for (int i = 0; i < playable.size(); i++) {
            RoomTracklistController.addTrack(roomId, RoomTrackCreateRequest.of(playable.get(i)));
        }
    }

    private void addSpotifyTracks(List<SpotifyTrackSimplified> tracks, SpotifyAlbum album) {
        List<SpotifyTrackSimplified> playable = getPlayableSpotifyTracks(tracks);
        for (int i = 0; i < playable.size(); i++) {
            RoomTracklistController.addTrack(roomId, RoomTrackCreateRequest.of(playable.get(i), album));
        }
    }

    private <T extends SpotifyTrackSimplified> List<T> getPlayableSpotifyTracks(List<T> tracks) {
        List<T> playable = tracks.stream().filter(T::getIs_playable).collect(Collectors.toList());
        if (playable.size() == 0) {
            showToast(R.string.unavailable_tracks_chosen);
        } else if (playable.size() != tracks.size()) {
            showToast(context.getResources().getQuantityString(R.plurals.unavailable_tracks_chosen,
                tracks.size() - playable.size(), tracks.size() - playable.size()));
        }
        return playable;
    }

    private boolean isItMine(RoomTrack roomTrack) {
        return roomTrack.getOwner().getId().equals(me.getId());
    }

    private void checkIfRequiredPlayerIsInitialized(RoomTrack track) {
        checkIfRequiredPlayersAreInitialized(Collections.singleton(track));
    }

    private void checkIfRequiredPlayersAreInitialized(Collection<RoomTrack> tracks) {
        if (!Player.isExoPlayerSet() && tracks.stream().anyMatch(roomTrack -> roomTrack.isUploadedContent() || roomTrack.isSoundCloudContent())) {
            SimpleExoPlayer exoPlayer = new SimpleExoPlayer.Builder(context).build();
            exoPlayer.addListener(new ExoPlayerCallback(this::requestPlayingNext));
            Player.setExoPlayer(exoPlayer);
            playBySourceIfNeeded(Track.Source.UPLOADED, Track.Source.SOUNDCLOUD);
        }
        if (!Player.isYouTubePlayerSet() && tracks.stream().anyMatch(RoomTrack::isYouTubeContent)) {
            onYouTubeLoggingInRequisition.run();
        }
        if (!Player.isSpotifyPlayerSet() && tracks.stream().anyMatch(RoomTrack::isSpotifyContent)) {
            onSpotifyLoggingInRequisition.run();
        }
    }

    private void requestPlaying(RoomTrack roomTrack) {
        RoomTracklistController.playTrack(roomId, roomTrack.getId());
    }

    private void playCurrentTrack(Instant startedPlayingAt) {
        tracklist.playCurrentTrack(startedPlayingAt);
        afterPlay();
    }

    private void afterPlay() {
        if (Player.isPlaying()) {
            updateNotification();
        } else {
            requestPlayingNext();
        }
        currentTrackView.update(tracklist.getCurrentTrack().getTrack());
    }

    private void updateNotification() {
        RoomTrack roomTrack = tracklist.getCurrentTrack();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(roomTrack.getTrack().getTitle())
            .setContentText(roomTrack.getTrack().getArtist())
            .setSmallIcon(R.drawable.ico)
            .setOngoing(true)
            .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Constants.NOTIFICATION_ID, notificationBuilder.build());
    }

    void showToast(@StringRes int id, Object... formatArgs) {
        showToast(context.getResources().getString(id, formatArgs));
    }

    private void showToast(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }
}
