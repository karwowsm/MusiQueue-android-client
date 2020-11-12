package pl.com.karwowsm.musiqueue.tracklist;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import pl.com.karwowsm.musiqueue.api.dto.RoomTrack;
import pl.com.karwowsm.musiqueue.api.dto.RoomTracklist;

class Tracklist {

    private final List<RoomTrack> tracks;
    @Getter
    private RoomTrack currentTrack;

    Tracklist(RoomTracklist roomTracklist) {
        tracks = roomTracklist.getTracklist();
        if (roomTracklist.getCurrentTrackId() != null) {
            updateCurrentTrack(roomTracklist.getCurrentTrackId());
        }
    }

    void add(RoomTrack track) {
        track.setIsPlayed(false);
        tracks.stream()
            .filter(it -> it.getIndex() >= track.getIndex())
            .forEach(RoomTrack::incrementIndex);
        int offset = currentTrack != null ? tracks.indexOf(currentTrack) - currentTrack.getIndex() : 0;
        tracks.add(track.getIndex() + offset, track);
    }

    void delete(RoomTrack roomTrack) {
        tracks.stream()
            .filter(it -> it.getIndex() > roomTrack.getIndex())
            .forEach(RoomTrack::decrementIndex);
        tracks.remove(roomTrack);
    }

    void updateCurrentTrack(RoomTrack roomTrack) {
        updateCurrentTrack(roomTrack.getId());
    }

    RoomTrack getTrackOnPosition(int position) {
        return position <= tracks.size() ? tracks.get(position) : null;
    }

    List<RoomTrack> getQueuedTracks() {
        return tracks.subList(getPlayedCount(), tracks.size());
    }

    int getPlayedCount() {
        return (currentTrack != null && currentTrack.getIsPlayed() ? 1 : 0) + getCurrentTrackIndex();
    }

    int getCurrentTrackIndex() {
        return currentTrack != null ? tracks.indexOf(currentTrack) : 0;
    }

    void reset(RoomTracklist roomTracklist) {
        tracks.clear();
        tracks.addAll(roomTracklist.getTracklist());
        if (roomTracklist.getCurrentTrackId() != null) {
            boolean wasPlayed = currentTrack != null ? currentTrack.getIsPlayed() : false;
            updateCurrentTrack(roomTracklist.getCurrentTrackId());
            currentTrack.setIsPlayed(wasPlayed);
        }
    }

    private void updateCurrentTrack(UUID roomTrackId) {
        currentTrack = tracks.stream()
            .filter(it -> it.getId().equals(roomTrackId))
            .findFirst().get();
        for (RoomTrack track : tracks) {
            track.setIsPlayed(track.getIndex() < currentTrack.getIndex());
        }
    }
}
