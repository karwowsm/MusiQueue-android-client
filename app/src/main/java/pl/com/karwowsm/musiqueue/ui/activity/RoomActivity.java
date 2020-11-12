package pl.com.karwowsm.musiqueue.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerAndroidXFragment;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Spotify;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import lombok.CustomLog;
import pl.com.karwowsm.musiqueue.BuildConfig;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.controller.RoomMembersController;
import pl.com.karwowsm.musiqueue.api.controller.SoundCloudController;
import pl.com.karwowsm.musiqueue.api.controller.SpotifyController;
import pl.com.karwowsm.musiqueue.api.controller.YouTubeController;
import pl.com.karwowsm.musiqueue.api.dto.Room;
import pl.com.karwowsm.musiqueue.api.dto.RoomTrack;
import pl.com.karwowsm.musiqueue.api.dto.Track;
import pl.com.karwowsm.musiqueue.api.dto.UserAccount;
import pl.com.karwowsm.musiqueue.api.ws.MessagingService;
import pl.com.karwowsm.musiqueue.service.RoomService;
import pl.com.karwowsm.musiqueue.tracklist.TracklistManager;
import pl.com.karwowsm.musiqueue.tracklist.player.Player;
import pl.com.karwowsm.musiqueue.ui.fragment.SoundCloudDialogFragment;
import pl.com.karwowsm.musiqueue.ui.fragment.SpotifyDialogFragment;
import pl.com.karwowsm.musiqueue.ui.fragment.YouTubeDialogFragment;
import pl.com.karwowsm.musiqueue.ui.menu.FloatingActionButtonMenu;
import pl.com.karwowsm.musiqueue.ui.view.CurrentTrackView;
import pl.com.karwowsm.musiqueue.util.SpotifyCallback;
import pl.com.karwowsm.musiqueue.util.SpotifyUtils;
import pl.com.karwowsm.musiqueue.util.YouTubePlayerCallback;

@CustomLog
public class RoomActivity extends NavigationViewActivity {

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 0;
    private static final int AUDIO_FILE_CONTENT_REQUEST_CODE = 1;
    private static final int YOUTUBE_RECOVERY_REQUEST_CODE = 2;
    private static final int SPOTIFY_LOGIN_REQUEST_CODE = 3;

    private static final int CONTEXT_MENU_DELETE_ITEM_ID = 0;
    private static final int CONTEXT_MENU_QUEUE_AGAIN_ITEM_ID = 1;

    private Room room;
    private TracklistManager tracklistManager;
    private MessagingService messagingService;
    private FloatingActionButtonMenu floatingActionButtonMenu;
    private boolean intendedToAddTrack;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        Bundle bundle = getIntent().getExtras();
        me = (UserAccount) bundle.get("me");
        room = (Room) bundle.get("room");
        setTitle(room.getName());
        CurrentTrackView currentTrackView = new CurrentTrackView(findViewById(R.id.current_track_view_layout));
        ListView listView = findViewById(R.id.room_tracks_lv);
        registerForContextMenu(listView);
        registerForContextMenu(findViewById(R.id.current_track_img));

        tracklistManager = new TracklistManager(this, me, room.getId(), currentTrackView);
        tracklistManager.setYouTubePlayerInitializer(this::initYouTubePlayer);
        tracklistManager.setOnSpotifyLoggingInRequisition(
            () -> Snackbar.make(findViewById(R.id.activity_room_layout), R.string.not_logged_in_spotify, Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.log_in), view -> SpotifyUtils.login(this, SPOTIFY_LOGIN_REQUEST_CODE))
                .show()
        );
        tracklistManager.setSpotifyPlayerInitializer(this::initSpotifyPlayer);

        MessagingService.connect(() -> tracklistManager.initialize(findViewById(R.id.swipe_refresh_layout), listView),
            () -> {
                showToast(R.string.connection_error);
                leaveRoom();
            });
        messagingService = new MessagingService();
        messagingService.initRoomSubscription(room.getId(), message -> {
            room = message.getRoom();
            setTitle(room.getName());
            switch (message.getType()) {
                case UPDATED:
                    showToast(R.string.room_was_updated, room.getHost().getUsername());
                    break;
                case DELETED:
                    showToast(R.string.room_was_deleted, room.getName(), room.getHost().getUsername());
                    leaveRoom();
                    break;
            }
        });
        messagingService.initRoomMembersSubscription(room.getId(), message -> {
            switch (message.getType()) {
                case JOINED:
                    if (!message.getUserAccount().getId().equals(me.getId())) {
                        showToast(R.string.room_member_joined, message.getUserAccount().getUsername());
                    }
                    break;
                case LEFT:
                    if (!message.getUserAccount().getId().equals(me.getId())) {
                        showToast(R.string.room_member_left, message.getUserAccount().getUsername());
                    }
                    break;
            }
        });

        YouTubeController.setErrorResponseListener(error -> showToast(error != null
            ? error.toString()
            : getString(R.string.youtube_error_response)));
        YouTubeController.init();
        SpotifyController.setErrorResponseListener(error -> showToast(error != null
            ? error.toString()
            : getString(R.string.spotify_error_response)));
        SoundCloudController.setErrorListener(error -> showToast(getString(R.string.soundcloud_error_response)));
        SoundCloudController.getClientId();

        initFloatingActionButtonMenu();

        ImageButton muteButton = findViewById(R.id.mute_btn);
        muteButton.setImageResource(Player.isMuted()
            ? R.drawable.ic_volume_off
            : R.drawable.ic_volume_on);
        muteButton.setOnClickListener(view -> {
            ImageView iV = (ImageView) view;
            if (tracklistManager.toggleMuted()) {
                iV.setImageResource(R.drawable.ic_volume_off);
            } else {
                iV.setImageResource(R.drawable.ic_volume_on);
            }
        });

        initNavigationView();

        Intent intent = new Intent(this, RoomService.class);
        intent.putExtra("room", room.getName());
        startService(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        RoomTrack roomTrack = null;
        if (v instanceof ListView) {
            int pos = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
            roomTrack = (RoomTrack) ((ListView) v).getAdapter().getItem(pos);
        } else if (v instanceof ImageView) {
            roomTrack = tracklistManager.getCurrentTrack();
            if (roomTrack == null) {
                return;
            }
        }
        menu.setHeaderTitle(roomTrack.getTrack().getTitle());
        if (roomTrack.getOwner().getId().equals(me.getId()) && !roomTrack.getIsPlayed()) {
            menu.add(Menu.NONE, CONTEXT_MENU_DELETE_ITEM_ID, Menu.NONE, R.string.delete);
        }
        menu.add(Menu.NONE, CONTEXT_MENU_QUEUE_AGAIN_ITEM_ID, Menu.NONE, R.string.queue_again);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        RoomTrack roomTrack;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info != null) {
            roomTrack = tracklistManager.getTrackOnPosition(info.position);
        } else {
            roomTrack = tracklistManager.getCurrentTrack();
        }
        if (item.getItemId() == CONTEXT_MENU_DELETE_ITEM_ID) {
            tracklistManager.remove(roomTrack);
        } else if (item.getItemId() == CONTEXT_MENU_QUEUE_AGAIN_ITEM_ID) {
            tracklistManager.addTrack(roomTrack.getTrack());
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.app_bar_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.log_in_to_spotify_menu_item) {
            SpotifyUtils.login(this, SPOTIFY_LOGIN_REQUEST_CODE);
        } else if (id == R.id.settings_menu_item) {
            showToast(R.string.not_developed_yet);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (Arrays.stream(grantResults).allMatch(it -> it == PackageManager.PERMISSION_GRANTED)) {
                requestAudioFileContent();
            } else {
                showToast(R.string.permission_not_granted, getString(R.string.app_name));
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (tracklistManager.getCurrentTrackView().isFullscreen()) {
            Player.exitFullscreen();
        } else {
            RoomMembersController.leaveRoom(room.getId(), super::onBackPressed);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == AUDIO_FILE_CONTENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                tracklistManager.addFileContent(intent.getData());
            } catch (IOException e) {
                log.e("Exception thrown during file processing", e);
                showToast(R.string.adding_file_content_failed);
            }
        }
        if (requestCode == YOUTUBE_RECOVERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            initYouTubePlayer();
        }
        if (requestCode == SPOTIFY_LOGIN_REQUEST_CODE) {
            AuthenticationResponse authResponse = AuthenticationClient.getResponse(resultCode, intent);
            if (authResponse.getType() == AuthenticationResponse.Type.TOKEN) {
                SpotifyController.init(authResponse);
                if (tracklistManager.getCurrentTrack() != null && tracklistManager.getCurrentTrack().isSpotifyContent()) {
                    initSpotifyPlayer();
                }
                if (intendedToAddTrack) {
                    intendedToAddTrack = false;
                    showSpotifyDialog();
                }
            } else {
                log.d("SpotifyAuthResponse: " + authResponse.getError());
                String msg = getString(R.string.spotify_error_authenticating);
                Snackbar.make(findViewById(R.id.activity_room_layout), msg, Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.try_again), view -> SpotifyUtils.login(this, SPOTIFY_LOGIN_REQUEST_CODE))
                    .show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        tracklistManager.finish();
        messagingService.unsubscribe();
        MessagingService.disconnect();
        YouTubeController.setErrorResponseListener(null);
        Spotify.destroyPlayer(this);
        SpotifyController.setErrorResponseListener(null);
        SoundCloudController.setErrorListener(null);
        Intent serviceIntent = new Intent(this, RoomService.class);
        stopService(serviceIntent);
        super.onDestroy();
    }

    private void initFloatingActionButtonMenu() {
        FloatingActionButton menuToggleBtn = findViewById(R.id.fab);
        FloatingActionButton fileBtn = findViewById(R.id.file_fab);
        FloatingActionButton youTubeBtn = findViewById(R.id.youtube_fab);
        FloatingActionButton spotifyBtn = findViewById(R.id.spotify_fab);
        FloatingActionButton soundCloudBtn = findViewById(R.id.soundcloud_fab);

        menuToggleBtn.setOnClickListener(view -> {
            if (!floatingActionButtonMenu.isShown()) {
                if (room.getUserQueuedTracksLimit() == null
                    || tracklistManager.getUserQueuedTracksCount() < room.getUserQueuedTracksLimit()) {
                    floatingActionButtonMenu.show();
                } else {
                    showToast(R.string.refusal_to_add, room.getUserQueuedTracksLimit());
                }
            } else {
                floatingActionButtonMenu.close();
            }
        });

        fileBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!requestPermissionsIfNeeded(STORAGE_PERMISSION_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    requestAudioFileContent();
                }
            } else {
                requestAudioFileContent();
            }
            floatingActionButtonMenu.close();
        });

        youTubeBtn.setOnClickListener(view -> showYouTubeDialog());

        spotifyBtn.setOnClickListener(view -> {
            if (SpotifyController.isInitialized() && !SpotifyController.isTokenExpired()) {
                showSpotifyDialog();
            } else {
                if (SpotifyController.isTokenExpired()) {
                    showToast(R.string.spotify_access_token_expired);
                }
                intendedToAddTrack = true;
                SpotifyUtils.login(this, SPOTIFY_LOGIN_REQUEST_CODE);
            }
        });

        soundCloudBtn.setOnClickListener(view -> showSoundCloudDialog());

        Resources resources = getResources();
        floatingActionButtonMenu = new FloatingActionButtonMenu(fileBtn, youTubeBtn, spotifyBtn, soundCloudBtn,
            resources.getDimension(R.dimen.fab_menu_single_margin));
    }

    private void initYouTubePlayer() {
        YouTubePlayerAndroidXFragment playerFragment = (YouTubePlayerAndroidXFragment) getSupportFragmentManager()
            .findFragmentById(R.id.youtube_fragment);
        playerFragment.initialize(BuildConfig.YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                YouTubePlayerCallback playerCallback = new YouTubePlayerCallback(new YouTubePlayerCallback.Listener() {

                    @Override
                    public void onLoaded() {
                        Player.setYouTubePlayerPaused(false);
                        tracklistManager.getCurrentTrackView().showVideoBox();
                    }

                    @Override
                    public void onPlaying() {
                        Player.seekYouTubePlayerPositionIfNeeded();
                        player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                        tracklistManager.getCurrentTrackView().showProgressBar();
                        tracklistManager.getCurrentTrackView().restart();
                    }

                    @Override
                    public void onPaused() {
                        Player.setYouTubePlayerPaused(true);
                        player.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
                        tracklistManager.getCurrentTrackView().hideProgressBar();
                    }

                    @Override
                    public void onVideoEnded() {
                        tracklistManager.getCurrentTrackView().hideVideoBox();
                        tracklistManager.requestPlayingNext();
                    }
                });
                player.setPlaybackEventListener(playerCallback);
                player.setPlayerStateChangeListener(playerCallback);
                player.setOnFullscreenListener(isFullscreen -> {
                    tracklistManager.getCurrentTrackView().setFullscreen(isFullscreen);
                    View decorView = getWindow().getDecorView();
                    if (isFullscreen) {
                        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
                    } else {
                        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }

                    List<View> otherViews = Arrays.asList(findViewById(R.id.toolbar),
                        findViewById(R.id.room_tracks_lv),
                        findViewById(R.id.fab),
                        findViewById(R.id.file_fab),
                        findViewById(R.id.youtube_fab),
                        findViewById(R.id.spotify_fab),
                        findViewById(R.id.soundcloud_fab));

                    for (View it : otherViews) {
                        it.setVisibility(isFullscreen ? View.GONE : View.VISIBLE);
                    }
                });
                player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                Player.setYouTubePlayer(player);
                tracklistManager.playBySourceIfNeeded(Track.Source.YOUTUBE);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
                if (error.isUserRecoverableError()) {
                    error.getErrorDialog(RoomActivity.this, YOUTUBE_RECOVERY_REQUEST_CODE).show();
                } else {
                    showToast(R.string.youtube_player_init_error, error.toString());
                }
            }
        });
    }

    private void initSpotifyPlayer() {
        if (SpotifyController.isInitialized() && !SpotifyController.isTokenExpired()) {
            SpotifyUtils.initPlayer(this, new SpotifyCallback(new SpotifyCallback.Listener() {
                @Override
                public void onLoggedIn() {
                    tracklistManager.playBySourceIfNeeded(Track.Source.SPOTIFY);
                }

                @Override
                public void onPlaybackError() {
                    showToast(R.string.spotify_playback_error);
                }
            }), () -> runOnUiThread(tracklistManager::requestPlayingNext));
        } else {
            if (SpotifyController.isTokenExpired()) {
                showToast(R.string.spotify_access_token_expired);
            }
            SpotifyUtils.login(this, SPOTIFY_LOGIN_REQUEST_CODE);
        }
    }

    private void requestAudioFileContent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent
            .setType("audio/*")
            .addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, AUDIO_FILE_CONTENT_REQUEST_CODE);
    }

    private void showYouTubeDialog() {
        final YouTubeDialogFragment youTubeDialogFragment = new YouTubeDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(YouTubeDialogFragment.Listener.BUNDLE_KEY,
            (YouTubeDialogFragment.Listener) youTubeContent -> {
                tracklistManager.addYouTubeContent(youTubeContent);
                youTubeDialogFragment.dismiss();
                floatingActionButtonMenu.close();
            });
        showDialog(youTubeDialogFragment, bundle);
    }

    private void showSpotifyDialog() {
        final SpotifyDialogFragment spotifyDialogFragment = new SpotifyDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(SpotifyDialogFragment.Listener.BUNDLE_KEY,
            (SpotifyDialogFragment.Listener) spotifyContent -> {
                tracklistManager.addSpotifyContent(spotifyContent);
                spotifyDialogFragment.dismiss();
                floatingActionButtonMenu.close();
            });
        showDialog(spotifyDialogFragment, bundle);
    }

    private void showSoundCloudDialog() {
        final SoundCloudDialogFragment soundcloudDialogFragment = new SoundCloudDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(SoundCloudDialogFragment.Listener.BUNDLE_KEY,
            (SoundCloudDialogFragment.Listener) soundCloudTrack -> {
                tracklistManager.addSoundCloudTrack(soundCloudTrack);
                soundcloudDialogFragment.dismiss();
                floatingActionButtonMenu.close();
            });
        showDialog(soundcloudDialogFragment, bundle);
    }

    private void showDialog(DialogFragment dialogFragment, Bundle bundle) {
        dialogFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        dialogFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        dialogFragment.show(fragmentManager, dialogFragment.getClass().getSimpleName());
    }

    private void leaveRoom() {
        RoomMembersController.leaveRoom(room.getId(), this::finish);
    }
}
