package pl.com.karwowsm.musiqueue.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import pl.com.karwowsm.musiqueue.R;
import pl.com.karwowsm.musiqueue.api.controller.RoomController;
import pl.com.karwowsm.musiqueue.api.controller.RoomMembersController;
import pl.com.karwowsm.musiqueue.api.dto.UserAccount;
import pl.com.karwowsm.musiqueue.api.request.RoomCreateRequest;

public class RoomCreateActivity extends AbstractActivity {

    private UserAccount me;
    private EditText nameEditText;
    private EditText userQueuedTracksLimitEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_create_or_update);
        Bundle bundle = getIntent().getExtras();
        me = (UserAccount) bundle.get("me");
        nameEditText = findViewById(R.id.name_et);
        userQueuedTracksLimitEditText = findViewById(R.id.user_queued_tracks_limit_et);
        Button submitBtn = findViewById(R.id.submit_btn);
        submitBtn.setText(R.string.create_room);
        setBarsColor(R.color.colorAccent);
    }

    public void onSubmit(View view) {
        String name = nameEditText.getText().toString().trim();
        String userQueuedTracksLimit = userQueuedTracksLimitEditText.getText().toString();
        if (name.isEmpty()) {
            showToast(R.string.blank_room_name);
        } else {
            createRoom(RoomCreateRequest.builder()
                .name(name)
                .userQueuedTracksLimit(userQueuedTracksLimit.isEmpty() ? null : Integer.valueOf(userQueuedTracksLimit))
                .build());
        }
    }

    private void createRoom(final RoomCreateRequest request) {
        showProgressDialog(R.string.please_wait);

        RoomController.createRoom(request,
            createdRoom -> RoomMembersController.joinRoom(createdRoom.getId(),
                room -> {
                    hideProgressDialog();
                    Intent intent = new Intent(this, RoomActivity.class);
                    intent.putExtra("me", me);
                    intent.putExtra("room", room);
                    startActivity(intent);
                    finish();
                },
                error -> hideProgressDialog()),
            error -> hideProgressDialog());
    }
}
