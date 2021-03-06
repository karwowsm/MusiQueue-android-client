package pl.com.karwowsm.musiqueue.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pl.com.karwowsm.musiqueue.Constants;
import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.TokenHolder;
import pl.com.karwowsm.musiqueue.api.controller.BaseController;
import pl.com.karwowsm.musiqueue.api.controller.RoomController;
import pl.com.karwowsm.musiqueue.api.controller.RoomMembersController;
import pl.com.karwowsm.musiqueue.api.controller.UserAccountController;
import pl.com.karwowsm.musiqueue.api.dto.Room;
import pl.com.karwowsm.musiqueue.ui.adapter.RoomListViewAdapter;

public class MainActivity extends NavigationViewActivity {

    private List<Room> rooms;
    private RoomListViewAdapter roomListViewAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private int pageSize;
    private boolean lastPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = findViewById(R.id.rooms_lv);
        registerForContextMenu(listView);
        rooms = new ArrayList<>();
        roomListViewAdapter = new RoomListViewAdapter(this, R.layout.list_view_item_room, rooms);
        listView.setAdapter(roomListViewAdapter);
        listView.setOnItemClickListener((adapterView, view, pos, l) -> joinRoom(rooms.get(pos).getId()));
        listView.setOnScrollListener((OnListViewScrollEnd) () -> {
            if (!lastPage) {
                getRoomsPage(pageSize + BaseController.PAGE_SIZE);
            }
        });
        swipeRefresh = findViewById(R.id.swipe_refresh_layout);
        setTitle(R.string.rooms);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, RoomCreateActivity.class);
            intent.putExtra("me", me);
            startActivity(intent);
        });

        BaseController.setBaseErrorResponseListener(error -> {
            if (error != null && error.getStatus().equals(HttpURLConnection.HTTP_UNAUTHORIZED)) {
                logout();
            }
            showToast(error != null
                ? error.getMessage()
                : getString(R.string.server_error));
        });
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (me != null) {
            getRooms();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        int pos = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
        Room room = roomListViewAdapter.getItem(pos);
        menu.setHeaderTitle(room.getName());
        if (room.getHost().equals(me)) {
            menu.add(Menu.NONE, 0, Menu.NONE, R.string.update)
                .setOnMenuItemClickListener(item -> {
                    Intent intent = new Intent(this, RoomUpdateActivity.class);
                    intent.putExtra("room", room);
                    startActivity(intent);
                    return false;
                });
            menu.add(Menu.NONE, 1, Menu.NONE, R.string.delete)
                .setOnMenuItemClickListener(item -> {
                    RoomController.deleteRoom(room.getId(), this::getRooms);
                    return false;
                });
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    private void init() {
        String token = getSharedPreferences(Constants.AUTH_PREFS_NAME, Context.MODE_PRIVATE)
            .getString(Constants.PREF_TOKEN, null);
        if (token != null) {
            TokenHolder.setToken(token);
            getMe();
        } else {
            logout();
        }
    }

    private void getMe() {
        UserAccountController.getMe(me -> {
            this.me = me;
            initNavigationView();
            showToast(R.string.hello, me.getUsername());
            getRooms();
            swipeRefresh.setRefreshing(false);
            swipeRefresh.setOnRefreshListener(this::getRooms);
        }, error -> {
            swipeRefresh.setRefreshing(false);
            swipeRefresh.setOnRefreshListener(this::getMe);
        });
    }

    private void getRooms() {
        getRoomsPage(BaseController.PAGE_SIZE);
    }

    private void getRoomsPage(int size) {
        RoomController.findRoom(size, roomPage -> {
            rooms.clear();
            rooms.addAll(roomPage.getContent());
            roomListViewAdapter.notifyDataSetChanged();
            swipeRefresh.setRefreshing(false);
            pageSize = roomPage.getSize();
            lastPage = roomPage.isLast();
        }, error -> swipeRefresh.setRefreshing(false));
    }

    private void joinRoom(final UUID roomId) {
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage(getString(R.string.please_wait));
        final Runnable showDialog = pDialog::show;
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(showDialog, 100);

        RoomMembersController.joinRoom(roomId, room -> {
            handler.removeCallbacks(showDialog);
            pDialog.dismiss();
            Intent intent = new Intent(this, RoomActivity.class);
            intent.putExtra("me", me);
            intent.putExtra("room", room);
            startActivity(intent);
        }, error -> {
            handler.removeCallbacks(showDialog);
            pDialog.dismiss();
        });
    }
}
