package pl.com.karwowsm.musiqueue.api.ws;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lombok.CustomLog;
import pl.com.karwowsm.musiqueue.BuildConfig;
import pl.com.karwowsm.musiqueue.api.JSONSerializer;
import pl.com.karwowsm.musiqueue.api.TokenHolder;
import pl.com.karwowsm.musiqueue.api.ws.dto.Event;
import pl.com.karwowsm.musiqueue.api.ws.dto.RoomEvent;
import pl.com.karwowsm.musiqueue.api.ws.dto.RoomMemberEvent;
import pl.com.karwowsm.musiqueue.api.ws.dto.RoomTrackEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

@CustomLog
public class MessagingService extends TokenHolder {

    private static final String BASE_PATH = "/ws/websocket";

    private static StompClient stompClient;
    private static Disposable lifecycleSubscription;
    private static Runnable onOpenedConnection;
    private static Runnable onClosedConnection;

    private final CompositeDisposable subscriptions = new CompositeDisposable();

    public static void connect(Runnable onOpened, Runnable onClosed) {
        onOpenedConnection = onOpened;
        onClosedConnection = onClosed;
        if (stompClient == null || !stompClient.isConnected()) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + getToken());
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, BuildConfig.BASE_URL + BASE_PATH, headers)
                .withClientHeartbeat(25000);
            lifecycleSubscription = stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            log.d("Stomp connection opened");
                            if (onOpenedConnection != null) {
                                onOpenedConnection.run();
                            }
                            break;
                        case ERROR:
                            log.e("Stomp connection error", lifecycleEvent.getException());
                            break;
                        case CLOSED:
                            log.d("Stomp connection closed");
                            if (onClosedConnection != null) {
                                onClosedConnection.run();
                                lifecycleSubscription.dispose();
                            }
                            break;
                        case FAILED_SERVER_HEARTBEAT:
                            log.w("Stomp failed server heartbeat");
                            break;
                    }
                });

            stompClient.connect();
        }
    }

    public static void disconnect() {
        if (stompClient.isConnected()) {
            stompClient.disconnect();
            lifecycleSubscription.dispose();
        }
        onOpenedConnection = null;
        onClosedConnection = null;
    }

    public void initRoomSubscription(UUID roomId, Consumer<RoomEvent> consumer) {
        initSubscription(String.format("/topic/rooms/%s", roomId), consumer, RoomEvent.class);
    }

    public void initRoomMembersSubscription(UUID roomId, Consumer<RoomMemberEvent> consumer) {
        initSubscription(String.format("/topic/rooms/%s/members", roomId), consumer, RoomMemberEvent.class);
    }

    public void initRoomTracksSubscription(UUID roomId, Consumer<RoomTrackEvent> consumer) {
        initSubscription(String.format("/topic/rooms/%s/tracklist", roomId), consumer, RoomTrackEvent.class);
    }

    private <T extends Event<?>> void initSubscription(String path, Consumer<T> consumer, Class<T> messageClass) {
        subscriptions.add(stompClient.topic(path)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(msg -> {
                log.v("Received message: " + msg.getPayload());
                consumer.accept(JSONSerializer.fromJson(msg.getPayload(), messageClass));
            }, throwable -> log.e("Error on subscribe topic", throwable)));
    }

    public void unsubscribe() {
        subscriptions.dispose();
    }
}
