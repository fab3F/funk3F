package net.fab3F;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigWorker {
    private Logger logger = null;
    private final String configPath;
    private final String sep = java.io.File.separator;
    private BotConfig current = null;

    public ConfigWorker(String configPath){
        this.configPath = configPath;
    }

    public void setLogger(){
        this.logger = LoggerFactory.getLogger(ConfigWorker.class);
    }

    public ServerConfig getServerConfig(String guildId){
        if(guildId == null || guildId.isBlank()){
            logger.error("20: Guild ID was not set when trying to get server config. ID: {}", guildId);
            return null;
        }
        Path file = this.getConfigForServer(guildId);
        if(file == null){
            logger.error("26: File not found. ID: {}", guildId);
            return null;
        }
        YAMLMapper mapper = new YAMLMapper();
        try {
            return mapper.readValue(file, ServerConfig.class);
        } catch (JacksonException e) {
            logger.error("21: Error while reading server config: {}\n{}", guildId, e.getMessage());
            return null;
        }
    }
    public boolean writeServerConfig(String guildId, ServerConfig config){
        if(guildId == null || guildId.isBlank() || config == null){
            logger.error("22: Guild ID or Config was not set when trying to get server config. ID: {}", guildId);
            return false;
        }
        Path file = this.getConfigForServer(guildId);
        if(file == null){
            return false;
        }
        YAMLMapper mapper = new YAMLMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, config);
            logger.debug("Wrote server config for: {}", guildId);
            return true;
        } catch (JacksonException e) {
            logger.error("23: Error while writing server config: {}\n{}", guildId, e.getMessage());
            return false;
        }
    }

    public BotConfig getBotConfig(){
        return current;
    }

    public BotConfig readBotConfig() throws IllegalArgumentException{
        Path file = Paths.get(this.configPath + this.sep + "config.yml");
        YAMLMapper mapper = new YAMLMapper();
        try {
            BotConfig config = mapper.readValue(file, BotConfig.class);
            config.validate();
            this.current = config;
            return config;
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Error while reading bot config. Make sure the config file is correct. Path: " + this.configPath + "\\config.yml\n" + e.getMessage());
        }
    }

    // Copy template if new server
    private Path getConfigForServer(String guildId){
        Path path = Paths.get(this.configPath, "server", guildId + ".yml");
        if(Files.exists(path))
            return path;
        try {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Path template = Paths.get(this.configPath, "server", "template.yml");
            Files.copy(template, path, StandardCopyOption.REPLACE_EXISTING);
            return path;
        }catch (IOException e){
            logger.error("24: Cant create config file for server: {}\n{}", guildId, e.getMessage());
            return null;
        }
    }


    // Config Classes
    public static class ServerConfig {
        public ServerConfig() {}
        private final Map<String, String> settings = new HashMap<>();
        @JsonAnyGetter
        public Map<String, String> getSettings() {
            return settings;
        }

        @JsonAnySetter
        public void setSetting(String key, String value) {
            if (value != null) {
                this.settings.put(key, value.trim());
            }
        }

        public String get(String key) {
            return settings.get(key);
        }

        public boolean set(String key, String value) {
            if (value == null) return false;

            // check for valid values?

            settings.put(key, value.trim());
            return true;
        }
    }

    public static class BotConfig {
        public BotConfig(){}

        private String token;
        private String lastFmBase;
        private String lastFmKey;
        private String lastFmSecret;
        private String logMode;
        private String autoplayerName;
        private String logPath;
        private String serverConfigPath;
        private String publicYtApiUrl;
        private String publicYtApiKey;
        private String volumeScale;
        private String lavalinkAddress;
        private String lavalinkPort;
        private String lavalinkPassword;

        // Default values
        private final List<String> adminIds = List.of("502823513443401730");
        private final List<String> activity = Arrays.asList("watching", "fab3F Homepage");


        public void validate(){

            // required
            this.token = require(this.token, "token");
            this.lavalinkAddress = require(this.lavalinkAddress, "lavalinkAddress");
            this.lavalinkPort = require(this.lavalinkPort, "lavalinkPort");
            this.lavalinkPassword = require(this.lavalinkPassword, "lavalinkPassword");

            // optional (with defaults)
            this.logMode = fallback(this.logMode, "INFO");
            this.logPath = fallback(this.logPath, "logs");
            this.serverConfigPath = fallback(this.serverConfigPath, "server");
            this.autoplayerName = fallback(this.autoplayerName, "Premium Autoplayer");
            this.volumeScale = fallback(this.volumeScale, "0.1");
            try {
                Double.parseDouble(this.volumeScale);
            } catch (NumberFormatException e) {
                System.out.println("[CONFIG-WARNING] Using default value for volumeScale: 0.1");
                this.volumeScale = "0.1";
            }
            this.publicYtApiUrl = fallback(this.publicYtApiUrl, "https://www.youtube.com/youtubei/v1/player?key=");
            this.publicYtApiKey = fallback(this.publicYtApiKey, "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8");

            this.lastFmBase = fallback(this.lastFmBase, "http://ws.audioscrobbler.com/2.0/");
            this.lastFmKey = fallback(this.lastFmKey, "0");
            this.lastFmSecret = fallback(this.lastFmSecret, "0");
        }

        // required
        private String require(String value, String fieldName) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Critical Configuration-Error: Required field '" + fieldName + "' missing or empty!");
            }
            return value.trim();
        }

        // optional
        private String fallback(String value, String defaultValue) {
            if (value == null || value.isBlank()) {
                System.out.println("[CONFIG-WARNING] Using default value: " + defaultValue);
                return defaultValue;
            }
            return value.trim();
        }

        // getters
        public String getToken() { return token; }
        public String getLastFmBase() { return lastFmBase; }
        public String getLastFmKey() { return lastFmKey; }
        public String getLastFmSecret() { return lastFmSecret; }
        public String getLogMode() { return logMode; }
        public List<String> getAdminIds() { return adminIds; }
        public List<String> getActivity() { return activity; }
        public String getAutoplayerName() { return autoplayerName; }
        public String getLogPath() { return logPath; }
        public String getServerConfigPath() { return serverConfigPath; }
        public String getPublicYtApiUrl() { return publicYtApiUrl; }
        public String getPublicYtApiKey() { return publicYtApiKey; }
        public String getVolumeScale() { return volumeScale; }
        public String getLavalinkAddress() { return lavalinkAddress; }
        public String getLavalinkPort() { return lavalinkPort; }
        public String getLavalinkPassword() { return lavalinkPassword; }
    }



}



