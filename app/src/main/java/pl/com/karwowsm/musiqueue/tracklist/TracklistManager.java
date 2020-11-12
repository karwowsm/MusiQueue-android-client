package pl.com.karwowsm.musiqueue.tracklist;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.google.android.exoplayer2.SimpleExoPlayer;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
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
import pl.com.karwowsm.musiqueue.service.RoomService;
import pl.com.karwowsm.musiqueue.tracklist.player.Player;
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
    private Runnable youTubePlayerInitializer;
    @Setter
    private Runnable onSpotifyLoggingInRequisition;
    @Setter
    private Runnable spotifyPlayerInitializer;

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
            if (getCurrentTrack() != null) {
                play(roomTracklist.getStartedPlayingAt());
                listView.setSelection(getCurrentTrack().getIndex());
            }
            requestLoggingInToSpotifyIfNeeded(tracklist.getQueuedTracks());
            listView.setOnItemClickListener((adapterView, view, pos, l) -> {
                if (pos < tracklist.getPlayedCount()) {
                    showToast(R.string.on_played_click);
                    return;
                }
                if (isItMine(getCurrentTrack())) {
                    for (int i = tracklist.getPlayedCount(); i < pos; i++) {
                        if (!isItMine(roomTrackListViewAdapter.getItem(i))) {
                            showToast(R.string.refusal_to_play);
                            return;
                        }
                    }
                    RoomTracklistController.playTrack(roomId, roomTrackListViewAdapter.getItem(pos).getId());
                } else {
                    showToast(R.string.refusal_to_play);
                }
            });
            messagingService.initRoomTracksSubscription(roomId, message -> {
                switch (message.getType()) {
                    case ADDED:
                        tracklist.add(message.getTrack());
                        requestLoggingInToSpotifyIfNeeded(message.getTrack());
                        break;
                    case DELETED:
                        tracklist.delete(message.getTrack());
                        break;
                    case PLAYED:
                        tracklist.updateCurrentTrack(message.getTrack());
                        play(message.getTimestamp());
                        break;
                }
                roomTrackListViewAdapter.notifyDataSetChanged();
            });
        });
    }

    public void addFileContent(Uri uri) throws IOException {
        int fileSize = (int) FileUtils.getFileSize(context, uri);
        Resources resources = context.getResources();
        int uploadFileSizeLimit = resources.getInteger(R.integer.uploadFileSizeLimit);
        if (fileSize > uploadFileSizeLimit) {
            showToast(R.string.file_too_large, uploadFileSizeLimit / 1024 / 1024);
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getResources().getString(R.string.uploading));
        progressDialog.setCancelable(false);
        progressDialog.setMax(fileSize);

        RoomTracklistController.uploadTrack(roomId, uri, progressDialog, error -> {
            if (error != null) {
                showToast(error.getMessage());
            } else {
                showToast(R.string.uploading_track_failed);
            }
        }, fileSize);
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
        getCurrentTrack().setIsPlayed(true);
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
        int ret = getCurrentTrack() != null && isItMine(getCurrentTrack()) ? 1 : 0;
        List<RoomTrack> queuedTracks = tracklist.getQueuedTracks();
        return ret + queuedTracks.stream().filter(this::isItMine).count();
    }

    public void playBySourceIfNeeded(Track.Source source) {
        RoomTrack currentTrack = getCurrentTrack();
        if (currentTrack != null && currentTrack.getTrack().getSource().equals(source)) {
            play(Player.getStartedPlayingAt());
        }
    }

    public void finish() {
        messagingService.unsubscribe();
        Player.stop();
        Player.releaseExoPlayer();
        Player.releaseYouTubePlayer();
        Player.setSpotifyPlayer(null);
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

    private void play(Instant startedPlayingAt) {
        RoomTrack currentTrack = getCurrentTrack();
        if (Player.prepare(currentTrack, startedPlayingAt)) {
            initRequiredPlayerIfNeeded();
            Player.play(currentTrack);
        }
        if (Player.isPlaying()) {
            RoomService.updateNotification(context, currentTrack.getTrack());
        } else {
            currentTrack.setIsPlayed(true);
            requestPlayingNext();
        }
        currentTrackView.update(currentTrack.getTrack());
    }

    private void requestLoggingInToSpotifyIfNeeded(RoomTrack track) {
        requestLoggingInToSpotifyIfNeeded(Collections.singleton(track));
    }

    private void requestLoggingInToSpotifyIfNeeded(Collection<RoomTrack> tracks) {
        if (!SpotifyController.isInitialized() && tracks.stream().anyMatch(RoomTrack::isSpotifyContent)) {
            onSpotifyLoggingInRequisition.run();
        }
    }

    private void initRequiredPlayerIfNeeded() {
        RoomTrack currentTrack = getCurrentTrack();
        if (!Player.isExoPlayerSet() && (currentTrack.isUploadedContent() || currentTrack.isSoundCloudContent())) {
            SimpleExoPlayer exoPlayer = new SimpleExoPlayer.Builder(context).build();
            exoPlayer.addListener(new ExoPlayerCallback(this::requestPlayingNext));
            exoPlayer.setVolume(Player.isMuted() ? 0 : 1);
            Player.setExoPlayer(exoPlayer);
        }
        if (!Player.isYouTubePlayerSet() && currentTrack.isYouTubeContent()) {
            youTubePlayerInitializer.run();
        }
        if (!Player.isSpotifyPlayerSet() && currentTrack.isSpotifyContent()) {
            if (!SpotifyController.isInitialized()) {
                onSpotifyLoggingInRequisition.run();
            } else {
                spotifyPlayerInitializer.run();
            }
        }
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
