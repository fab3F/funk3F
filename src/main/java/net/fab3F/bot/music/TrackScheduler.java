package net.fab3F.bot.music;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrackScheduler {

    public final long guildId;
    private final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    public final MusicHandler musicHandler;
    private final ConcurrentLinkedDeque<Track> queue = new ConcurrentLinkedDeque<>();
    private final List<Track> lastPlayedTracks = new ArrayList<>();
    private boolean isRepeat = false;
    private int volume;
    public boolean isAutoplay;
    private final AtomicBoolean isStarting = new AtomicBoolean(false);
    private Track currentTrack = null;

    public TrackScheduler(long guildId, MusicHandler mH) {
        this.musicHandler = mH;
        this.guildId = guildId;
        try{
            this.volume = Integer.parseInt(mH.getConfigWorker().getServerConfig(Long.toString(this.guildId)).get("default_volume"));
        } catch (NumberFormatException ex){
            this.volume = 35;
            logger.error("30: Volume-Konfiguration inkorrekt für: {}", this.guildId);
        }
        this.isAutoplay = Boolean.parseBoolean(mH.getConfigWorker().getServerConfig(String.valueOf(this.guildId)).get("default_autoplay"));

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
        TrackInfo info = track.getInfo();
        String msg = "Jetzt spielt: **`" + info.getTitle() + "`** von **`" + info.getAuthor() + "`** [" + MusicHandler.calcDuration((int)info.getLength()) + "]";
        TextChannel channel = musicHandler.resolveTextChannel(this.guildId, track.getUserData(CustomTrackData.class).channelId());
        if (channel != null) {
            channel.sendMessage(msg).queue();
        }
    }

    public void onTrackEnd(Track lastTrack, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
        this.currentTrack = null;
        this.isStarting.set(false);

        if(!endReason.getMayStartNext())
            return;

        if(isRepeat) {
            this.queue.addFirst(lastTrack.makeClone());
        }

        this.processQueue();

        if(this.queue.isEmpty() && this.isAutoplay){
            this.startAutoplay(-1);
        }
    }

    public void skip(){
        this.startTrack(this.queue.poll(), true);
        this.isRepeat = false;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void changeRepeat() {
        isRepeat = !isRepeat;
    }

    public void setVolume(int volume){
        this.volume = Math.min(Math.max(volume, 0), 100);
        this.musicHandler.getLink(guildId).ifPresent(
                (link) -> link.createOrUpdatePlayer()
                        .setVolume(this.volume)
                        .subscribe()
        );
    }

    public int getVolume(){
        return volume;
    }

    public void startAutoplay(long channelId){
        // check if song is playing
        if(this.currentTrack != null || this.isStarting.get() || !this.queue.isEmpty()){
            return;
        }

        if(!lastPlayedTracks.isEmpty()){
            Track last = lastPlayedTracks.get(lastPlayedTracks.size()-1);
            if (last.getInfo().getUri() != null) {
                this.musicHandler.autoplayLoader.loadYtRecommendation(this.guildId, last.getUserData(CustomTrackData.class).channelId(), last.getInfo().getUri());
            }
        } else if (channelId != -1){
            this.musicHandler.autoplayLoader.loadTop30(this.guildId, channelId);
        }
    }

    public void stopAutoplay(){
        this.isAutoplay = false;
        this.clearAutoplay();
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
        this.musicHandler.getLink(guildId).ifPresentOrElse(
                (link) -> link.createOrUpdatePlayer()
                        .setTrack(track)
                        .setVolume(this.volume)
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