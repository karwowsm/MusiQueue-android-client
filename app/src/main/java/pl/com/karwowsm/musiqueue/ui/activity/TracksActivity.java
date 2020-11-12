package pl.com.karwowsm.musiqueue.ui.activity;

import android.os.Bundle;
import android.widget.ListView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.controller.BaseController;
import pl.com.karwowsm.musiqueue.api.controller.TrackController;
import pl.com.karwowsm.musiqueue.api.dto.Track;
import pl.com.karwowsm.musiqueue.api.dto.UserAccount;
import pl.com.karwowsm.musiqueue.ui.adapter.TrackListViewAdapter;

public class TracksActivity extends NavigationViewActivity {

    private List<Track> tracks;
    private TrackListViewAdapter trackListViewAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private int pageSize;
    private boolean lastPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);
        Bundle bundle = getIntent().getExtras();
        me = (UserAccount) bundle.get("me");
        ListView listView = findViewById(R.id.tracks_lv);
        tracks = new ArrayList<>();
        trackListViewAdapter = new TrackListViewAdapter(this, R.layout.list_view_item_room, tracks);
        listView.setAdapter(trackListViewAdapter);
        listView.setOnScrollListener((OnListViewScrollEnd) () -> {
            if (!lastPage) {
                getTracksPage(pageSize + BaseController.PAGE_SIZE);
            }
        });
        swipeRefresh = findViewById(R.id.swipe_refresh_layout);
        setTitle(R.string.tracks);

        initNavigationView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getTracks();
        swipeRefresh.setOnRefreshListener(this::getTracks);
    }

    private void getTracks() {
        getTracksPage(BaseController.PAGE_SIZE);
    }

    private void getTracksPage(int size) {
        TrackController.findTrack(size, trackPage -> {
            tracks.clear();
            tracks.addAll(trackPage.getContent());
            trackListViewAdapter.notifyDataSetChanged();
            swipeRefresh.setRefreshing(false);
            pageSize = trackPage.getSize();
            lastPage = trackPage.isLast();
        }, error -> swipeRefresh.setRefreshing(false));
    }
}
