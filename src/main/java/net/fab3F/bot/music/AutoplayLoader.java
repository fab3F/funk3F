package net.fab3F.bot.music;


import net.fab3F.ConfigWorker;
import net.fab3F.Main;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AutoplayLoader {

    private final String userAgent;
    private final String base;
    private final String lastFmKey;
    private final MusicHandler musicHandler;

    public AutoplayLoader(MusicHandler musicHandler){
        this.userAgent = "Music Bot/" + Main.VERSION + " funk3F";
        ConfigWorker.BotConfig botConfig = musicHandler.getConfigWorker().getBotConfig();
        this.base = botConfig.getLastFmBase();
        this.lastFmKey = botConfig.getLastFmKey();
        this.musicHandler = musicHandler;
    }

    public void loadYtRecommendation(long guildId, long channelId, String lastUrl){
        String vId = lastUrl.replaceAll("^(?:https?://)?(?:www\\.)?(?:youtube\\.com/.*v=|youtu\\.be/)([a-zA-Z0-9_-]{11}).*$", "$1");
        this.musicHandler.addAutoplayContent("https://www.youtube.com/watch?v=" + vId + "&list=RD" + vId, guildId, channelId);
    }

    public void loadTop30(long guildId, long channelId){
        String searchQuery = String.format("method=chart.gettoptracks&api_key=%s&format=json&limit=30", lastFmKey);
        String searchUrl = base + "?" + searchQuery;

        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonResponse;
        try {
            jsonResponse = getJsonResponse(searchUrl, mapper);
        } catch (Exception e) {
            LoggerFactory.getLogger(AutoplayLoader.class).error("43: Exception lastFM: " + e.getMessage());
            return;
        }
        JsonNode trackArray = jsonResponse.get("tracks").get("track");
        for (JsonNode track : trackArray) {
            String name = track.get("name").asText();
            String artist = track.get("artist").get("name").asText();
            this.musicHandler.addAutoplayContent(artist + " - " + name, guildId, channelId);
        }
    }

    private JsonNode getJsonResponse(String urlString, ObjectMapper mapper) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", this.userAgent);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        conn.disconnect();

        return mapper.readTree(content.toString());
    }


}
