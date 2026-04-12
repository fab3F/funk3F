package net.fab3F.bot.music;

import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrackScheduler {

    private final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    private final GuildMusicHandler handler;

    private final ConcurrentLinkedDeque<Track> queue = new ConcurrentLinkedDeque<>();
    private final List<Track> lastPlayedTracks = new ArrayList<>();
    private boolean isRepeat = false;
    private boolean isAutoplay;

    private final AtomicBoolean isStarting = new AtomicBoolean(false);
    private Track currentTrack = null;

    public TrackScheduler(GuildMusicHandler handler, boolean isAutoplay) {
        this.handler = handler;
        this.isAutoplay = isAutoplay;
    }

    public Guild getGuild(){
        return this.handler.getGuild();
    }

    public synchronized void enqueue(Track track, boolean playAsFirst) {
        CustomTrackData data = track.getUserData(CustomTrackData.class);
        boolean isAutoplayTrack = (data != null && data.isAutoplay());

        if (playAsFirst) {
            this.queue.addFirst(track);
        } else {
            if (!isAutoplayTrack) {
                this.clearAutoplay();
            }
            this.queue.offer(track);
        }
        this.processQueue();
    }

    public synchronized void enqueuePlaylist(List<Track> tracks) {
        if(tracks.isEmpty()){
            return;
        }
        if(!(tracks.get(0).getUserData(CustomTrackData.class) != null && tracks.get(0).getUserData(CustomTrackData.class).isAutoplay())){
            this.clearAutoplay();
        }
        this.queue.addAll(tracks);
        this.processQueue();
    }


    public void onTrackStart(Track track) {
        this.currentTrack = track;
        this.isStarting.set(false);
        this.lastPlayedTracks.add(track);
    }

    public void onTrackEnd(Track lastTrack, boolean mayStartNext) {
        this.currentTrack = null;
        this.isStarting.set(false);

        if(!mayStartNext)
            return;

        if(isRepeat) {
            this.queue.addFirst(lastTrack.makeClone());
        }

        this.processQueue();

        if(this.queue.isEmpty() && this.isAutoplay){
            this.startAutoplay(-1);
        }
    }

    public boolean skip(){
        if(this.currentTrack == null)
            return false;

        this.startTrack(this.queue.poll(), true);
        this.isRepeat = false;
        return true;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void changeRepeat() {
        isRepeat = !isRepeat;
    }

    public void startAutoplay(long channelId){
        // check if song is playing
        if(this.currentTrack != null || this.isStarting.get() || !this.queue.isEmpty()){
            return;
        }

        if(!lastPlayedTracks.isEmpty()){
            Track last = lastPlayedTracks.get(lastPlayedTracks.size()-1);
            if (last.getInfo().getUri() != null) {
                AutoplayLoader.getLoader().loadYtRecommendation(this.getGuild().getIdLong(), last.getUserData(CustomTrackData.class).channelId(), last.getInfo().getUri());
            }
        } else if (channelId != -1){
            AutoplayLoader.getLoader().loadTop30(this.getGuild().getIdLong(), channelId);
        }
    }

    public void stopAutoplay(){
        this.isAutoplay = false;
        this.clearAutoplay();
    }

    public boolean isAutoplay(){
        return this.isAutoplay;
    }

    public void setAutoplay(boolean autoplay){
        this.isAutoplay = autoplay;
    }

    private void clearAutoplay(){
        queue.removeIf(track -> {
            CustomTrackData data = track.getUserData(CustomTrackData.class);
            return data != null && data.isAutoplay();
        });
    }

    private synchronized void processQueue() {
        // Already starting
        if (this.isStarting.get() || this.currentTrack != null) return;

        if (!this.queue.isEmpty()) {
            this.startTrack(this.queue.poll(), false);
        }
    }

    private void startTrack(Track track, boolean skipping) {
        if(track == null && !skipping){
            return;
        }

        // Check if track is autoplay and was played before
        if(track != null && !this.lastPlayedTracks.isEmpty()
                && this.lastPlayedTracks.stream().map(track1 -> track1.getInfo().getTitle()).toList().contains(track.getInfo().getTitle())
                && track.getUserData(CustomTrackData.class).isAutoplay()){
            logger.debug("Skipped Autoplay duplicate: {}", track.getInfo().getTitle());
            this.startTrack(this.queue.poll(), skipping);
            return;
        }

        this.isStarting.set(true);

        // Set the new track
        this.handler.getLink().ifPresentOrElse(
                (link) -> link.createOrUpdatePlayer()
                        .setTrack(track)
                        .setVolume(this.handler.getVolume())
                        .subscribe(
                                player -> {}, // success (onNext)
                                error -> {
                                    this.isStarting.set(false); // remove lock
                                    this.processQueue(); // try next song
                                }
                        ),
                () -> this.isStarting.set(false) // remove lock id link doesnt exist
        );
    }
}