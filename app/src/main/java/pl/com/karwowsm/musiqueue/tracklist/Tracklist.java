package pl.com.karwowsm.musiqueue.tracklist;

import android.util.Pair;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.Getter;
import pl.com.karwowsm.musiqueue.api.dto.RoomTrack;
import pl.com.karwowsm.musiqueue.api.dto.RoomTracklist;
import pl.com.karwowsm.musiqueue.tracklist.player.Player;

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

    void updateCurrentTrack(RoomTrack roomTrack, Instant startedPlayingAt) {
        RoomTrack currentTrack = tracks.stream()
            .filter(it -> it.getId().equals(roomTrack.getId()))
            .findFirst().get();
        setCurrentTrack(currentTrack);
        playCurrentTrack(startedPlayingAt);
    }

    void playCurrentTrack(Instant startedPlayingAt) {
        Player.play(currentTrack, startedPlayingAt);
        if (!Player.isPlaying()) {
            currentTrack.setIsPlayed(true);
        }
    }

    RoomTrack getTrackOnPosition(int position) {
        return position > tracks.size() ? null : tracks.get(position);
    }

    List<RoomTrack> getQueuedTracks() {
        return tracks.subList(getPlayedCount(), tracks.size());
    }

    List<RoomTrack> getNotPlayedYet() {
        if (currentTrack != null && !currentTrack.getIsPlayed()) {
            return Stream.concat(Stream.of(currentTrack), getQueuedTracks().stream())
                .collect(Collectors.toList());
        } else {
            return getQueuedTracks();
        }
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
