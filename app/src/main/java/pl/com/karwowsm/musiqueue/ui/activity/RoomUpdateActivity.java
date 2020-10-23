package pl.com.karwowsm.musiqueue.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.controller.RoomController;
import pl.com.karwowsm.musiqueue.api.dto.Room;
import pl.com.karwowsm.musiqueue.api.request.RoomUpdateRequest;

public class RoomUpdateActivity extends AbstractActivity {

    private Room room;
    private EditText nameEditText;
    private EditText userQueuedTracksLimitEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_create_or_update);
        Bundle bundle = getIntent().getExtras();
        room = (Room) bundle.get("room");
        nameEditText = findViewById(R.id.name_et);
        userQueuedTracksLimitEditText = findViewById(R.id.user_queued_tracks_limit_et);
        nameEditText.setText(room.getName());
        if (room.getUserQueuedTracksLimit() != null) {
            userQueuedTracksLimitEditText.setText(String.valueOf(room.getUserQueuedTracksLimit()));
        }
        Button submitBtn = findViewById(R.id.submit_btn);
        submitBtn.setText(R.string.update_room);
        setBarsColor(R.color.colorAccent);
    }

    public void onSubmit(View view) {
        String name = nameEditText.getText().toString().trim();
        String userQueuedTracksLimit = userQueuedTracksLimitEditText.getText().toString();
        if (name.isEmpty()) {
            showToast(R.string.blank_room_name);
        } else {
            updateRoom(RoomUpdateRequest.builder()
                .name(name)
                .userQueuedTracksLimit(userQueuedTracksLimit.isEmpty() ? null : Integer.valueOf(userQueuedTracksLimit))
                .build());
        }
    }

    private void updateRoom(final RoomUpdateRequest request) {
        showProgressDialog(R.string.please_wait);
        RoomController.updateRoom(room.getId(), request,
            updatedRoom -> {
                hideProgressDialog();
                finish();
            },
            error -> hideProgressDialog());
    }
}
