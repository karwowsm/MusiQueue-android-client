package pl.com.karwowsm.musiqueue.tracklist;

import android.util.Pair;

import java.util.List;
import java.util.stream.IntStream;

import lombok.Getter;
import pl.com.karwowsm.musiqueue.api.dto.RoomTrack;
import pl.com.karwowsm.musiqueue.api.dto.RoomTracklist;

class Tracklist {

    private final List<RoomTrack> tracks;
    @Getter
    private RoomTrack currentTrack;

    Tracklist(RoomTracklist roomTracklist) {
        this.tracks = roomTracklist.getTracklist();
        if (roomTracklist.getCurrentTrackId() != null) {
            RoomTrack currentTrack = tracks.stream()
                .filter(it -> it.getId().equals(roomTracklist.getCurrentTrackId()))
                .findFirst().get();
            currentTrack.setIsPlayed(false);
            setCurrentTrack(currentTrack);
        }
    }

    void add(RoomTrack track) {
        track.setIsPlayed(false);
        IntStream.range(0, tracks.size())
            .mapToObj(index -> Pair.create(tracks.get(index), index))
            .filter(pair -> pair.second >= pair.first.getIndex() && pair.second >= track.getIndex())
            .map(pair -> pair.first)
            .forEach(RoomTrack::incrementIndex);
        tracks.add(track.getIndex(), track);
    }

    void delete(RoomTrack roomTrack) {
        tracks.stream()
            .filter(it -> it.getIndex() > roomTrack.getIndex())
            .forEach(RoomTrack::decrementIndex);
        tracks.remove(roomTrack);
    }

    void updateCurrentTrack(RoomTrack roomTrack) {
        RoomTrack currentTrack = tracks.stream()
            .filter(it -> it.getId().equals(roomTrack.getId()))
            .findFirst().get();
        setCurrentTrack(currentTrack);
    }

    RoomTrack getTrackOnPosition(int position) {
        return position > tracks.size() ? null : tracks.get(position);
    }

    List<RoomTrack> getQueuedTracks() {
        return tracks.subList(getPlayedCount(), tracks.size());
    }

    int getPlayedCount() {
        return (int) tracks.stream().filter(RoomTrack::getIsPlayed).count();
    }

    private void setCurrentTrack(RoomTrack roomTrack) {
        currentTrack = roomTrack;
        for (RoomTrack track : tracks) {
            track.setIsPlayed(track.getIndex() < currentTrack.getIndex());
        }
    }
}
