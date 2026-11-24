package net.fab3F.bot.music;

import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrackScheduler {

    private enum State { IDLE, STARTING, PLAYING, CLOSED }

    private static final long START_TIMEOUT_SECONDS = 20;
    private static final long AUTOPLAY_LOAD_WINDOW_MS = 15_000;
    private static final int PLAYED_HISTORY_SIZE = 200;

    private static final ScheduledExecutorService WATCHDOG = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "funk3F-track-scheduler-watchdog");
        t.setDaemon(true);
        return t;
    });

    private final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    private final GuildMusicHandler handler;

    // State
    private final Object lock = new Object();
    private final Deque<Track> queue = new ArrayDeque<>();
    private final Deque<String> playedTitles = new ArrayDeque<>();
    private State state = State.IDLE;
    private Track current = null;          // current Track
    private Track lastTrack = null;        // last Track for autoplay suggestions
    private long generation = 0;           // counts every start up
    private boolean repeat = false;
    private boolean autoplay;
    private long autoplayRequestedAt = 0;  // 0 = no autoplay request

    public TrackScheduler(GuildMusicHandler handler, boolean isAutoplay) {
        this.handler = handler;
        this.autoplay = isAutoplay;
    }

    public Guild getGuild() {
        return this.handler.getGuild();
    }

    public void enqueue(Track track, boolean playAsFirst) {
        synchronized (lock) {
            if (state == State.CLOSED) {
                return;
            }
            if (isAutoplayTrack(track)) {
                if (!autoplay) {
                    return;             // if Autoplay was disabled while loading
                }
                autoplayRequestedAt = 0;
            } else {
                removeAutoplayTracks();
            }
            if (playAsFirst){
                queue.addFirst(track);
            } else {
                queue.addLast(track);
            }
            advanceIfIdle();
        }
    }

    public void enqueuePlaylist(List<Track> tracks) {
        if (tracks.isEmpty()) {
            return;
        }
        synchronized (lock) {
            if (state == State.CLOSED) {
                return;
            }
            if (isAutoplayTrack(tracks.get(0))) {
                if (!autoplay) {
                    return;
                }
                autoplayRequestedAt = 0;
            } else {
                removeAutoplayTracks();
            }
            queue.addAll(tracks);
            advanceIfIdle();
            // autoplay mix with only duplicates
            if (state == State.IDLE && isAutoplayTrack(tracks.get(0))) {
                giveUpAutoplay(tracks.get(0));
            }
        }
    }

    public boolean skip() {
        Runnable after;
        synchronized (lock) {
            if (state == State.CLOSED || state == State.IDLE) {
                return false;
            }
            repeat = false;
            Track next = pollNextPlayable();
            if (next != null) {
                startTrack(next);
                after = null;
            } else {
                stopPlayer();
                state = State.IDLE;
                current = null;
                generation++;
                after = prepareAutoplay(-1);
            }
        }
        runAsync(after);
        return true;
    }

    public boolean onTrackStart(Track track) {
        synchronized (lock) {
            if (state == State.CLOSED) {
                return false;
            }
            if (state != State.STARTING || current == null || notSameTrack(current, track)) {
                logger.debug("Ignoriere TrackStart, passt nicht zum aktuellen Start: {}", track.getInfo().getTitle());
                return false;
            }
            state = State.PLAYING;
            lastTrack = current;
            playedTitles.addLast(current.getInfo().getTitle());
            while (playedTitles.size() > PLAYED_HISTORY_SIZE) {
                playedTitles.removeFirst();
            }
            return true;
        }
    }

    public void onTrackEnd(Track ended, String reason, boolean mayStartNext) {
        Runnable after;
        synchronized (lock) {
            if (state == State.CLOSED) {
                return;
            }

            // Player removed on node
            if ("CLEANUP".equals(reason)) {
                state = State.IDLE;
                current = null;
                generation++;
                return;
            }
            if (!mayStartNext) {
                return;
            }

            if (current == null || notSameTrack(current, ended)) {
                logger.debug("Ignoriere TrackEnd (veraltet): {}", ended.getInfo().getTitle());
                return;
            }

            Track finished = current;
            state = State.IDLE;
            current = null;
            generation++;

            if (repeat && "FINISHED".equals(reason)) {
                Track copy = finished.makeClone();
                copy.setUserData(finished.getUserData(CustomTrackData.class));
                queue.addFirst(copy);
            }
            after = afterTrackFinished();
        }
        runAsync(after);
    }

    public void close() {
        synchronized (lock) {
            state = State.CLOSED;
            queue.clear();
            current = null;
            generation++;
        }
    }

    public void startAutoplay(long channelId) {
        Runnable load;
        synchronized (lock) {
            load = prepareAutoplay(channelId);
        }
        runAsync(load);
    }

    public void stopAutoplay() {
        synchronized (lock) {
            autoplay = false;
            autoplayRequestedAt = 0;
            removeAutoplayTracks();
        }
    }

    public void setAutoplay(boolean value) {
        synchronized (lock) {
            this.autoplay = value;
        }
    }

    public boolean isAutoplay() {
        synchronized (lock) {
            return autoplay;
        }
    }

    public boolean isRepeat() {
        synchronized (lock) {
            return repeat;
        }
    }

    public boolean changeRepeat() {
        synchronized (lock) {
            repeat = !repeat;
            return repeat;
        }
    }

    public List<Track> getQueueSnapshot() {
        synchronized (lock) {
            return new ArrayList<>(queue);
        }
    }

    public Track getCurrentTrack() {
        synchronized (lock) {
            return current;
        }
    }

    private void advanceIfIdle() {
        if (state != State.IDLE) {
            return;
        }
        Track next = pollNextPlayable();
        if (next != null) {
            startTrack(next);
        }
    }

    private Runnable afterTrackFinished() {
        advanceIfIdle();
        if (state == State.IDLE) {
            return prepareAutoplay(-1);
        }
        return null;
    }

    // send track to lavalink
    private void startTrack(Track track) {
        final long gen = ++generation;
        state = State.STARTING;
        current = track;

        var link = handler.getLink();
        if (link.isEmpty()) {
            logger.warn("Kein Lavalink-Link vorhanden, Track wird verworfen: {}", track.getInfo().getTitle());
            state = State.IDLE;
            current = null;
            return;
        }

        link.get().createOrUpdatePlayer()
                .setTrack(track)
                .setVolume(handler.getVolume())
                .subscribe(
                        player -> {},
                        error -> onStartFailed(gen, error)
                );

        WATCHDOG.schedule(() -> onStartTimeout(gen), START_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private void onStartFailed(long gen, Throwable error) {
        Runnable after;
        synchronized (lock) {
            if (state == State.CLOSED || gen != generation || state != State.STARTING) {
                return;
            }
            logger.warn("Track konnte nicht gestartet werden: {}", error.getMessage());
            state = State.IDLE;
            current = null;
            generation++;
            after = afterTrackFinished();
        }
        runAsync(after);
    }

    private void onStartTimeout(long gen) {
        Runnable after;
        synchronized (lock) {
            if (state == State.CLOSED || gen != generation || state != State.STARTING) {
                return;
            }
            logger.warn("Track-Start hat nach {}s kein TrackStart-Event geliefert, setze Zustand zurück.", START_TIMEOUT_SECONDS);
            stopPlayer();
            state = State.IDLE;
            current = null;
            generation++;
            after = afterTrackFinished();
        }
        runAsync(after);
    }

    private void stopPlayer() {
        handler.getLink().ifPresent(link -> link.createOrUpdatePlayer().stopTrack().subscribe());
    }

    private Track pollNextPlayable() {
        Track t;
        while ((t = queue.pollFirst()) != null) {
            if (isAutoplayTrack(t) && playedTitles.contains(t.getInfo().getTitle())) {
                logger.debug("Autoplay-Duplikat übersprungen: {}", t.getInfo().getTitle());
                continue;
            }
            return t;
        }
        return null;
    }

    private Runnable prepareAutoplay(long channelId) {
        if (state == State.CLOSED || !autoplay || state != State.IDLE || !queue.isEmpty()) {
            return null;
        }
        if (System.currentTimeMillis() - autoplayRequestedAt < AUTOPLAY_LOAD_WINDOW_MS) {
            return null;
        }

        final long guildId = getGuild().getIdLong();

        if (lastTrack != null && lastTrack.getInfo().getUri() != null) {
            CustomTrackData data = lastTrack.getUserData(CustomTrackData.class);
            final long targetChannel = data != null ? data.channelId() : channelId;
            final String uri = lastTrack.getInfo().getUri();
            autoplayRequestedAt = System.currentTimeMillis();
            return () -> AutoplayLoader.getLoader().loadYtRecommendation(guildId, targetChannel, uri);
        }
        if (channelId != -1) {
            autoplayRequestedAt = System.currentTimeMillis();
            return () -> AutoplayLoader.getLoader().loadTop30(guildId, channelId);
        }
        return null;
    }

    private void removeAutoplayTracks() {
        queue.removeIf(this::isAutoplayTrack);
    }

    private void giveUpAutoplay(Track source) {
        autoplay = false;
        autoplayRequestedAt = 0;
        CustomTrackData data = source.getUserData(CustomTrackData.class);
        TextChannel ch = data != null ? getGuild().getChannelById(TextChannel.class, data.channelId()) : null;
        if (ch != null) {
            ch.sendMessage("Alle empfohlenen Songs wurden schon gespielt. Autoplay wurde deaktiviert.").queue();
        }
    }

    private boolean isAutoplayTrack(Track track) {
        CustomTrackData data = track.getUserData(CustomTrackData.class);
        return data != null && data.isAutoplay();
    }

    private boolean notSameTrack(Track a, Track b) {
        return !a.getInfo().getIdentifier().equals(b.getInfo().getIdentifier())
                || !a.getInfo().getSourceName().equals(b.getInfo().getSourceName());
    }

    private void runAsync(Runnable r) {
        if (r != null) {
            CompletableFuture.runAsync(r).exceptionally(ex -> {
                logger.error("Error when loading new (Autoplay):", ex);
                return null;
            });
        }
    }
}