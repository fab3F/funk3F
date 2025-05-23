package net.fab3F.customTools;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ConfigWorker {
    private Logger logger = null;
    private final String configPath;
    private final String sep = SyIO.sep;
    public ConfigWorker(String configPath){
        this.configPath = configPath;
    }

    public void setLogger(Logger logger){
        this.logger = logger;
    }

    public ServerConfig getServerConfig(String guildId){
        if(guildId == null || guildId.isBlank()){
            logger.error("Guild ID was not set when trying to get server config. ID: " + guildId);
            return null;
        }
        File file = this.getConfigForServer(guildId);
        if(file == null){
            return null;
        }
        YAMLMapper mapper = new YAMLMapper();
        try {
            return mapper.readValue(file, ServerConfig.class);
        } catch (IOException e) {
            logger.error("Error while reading server config: " + guildId + "\n" + e.getMessage());
            return null;
        }
    }
    public boolean writeServerConfig(String guildId, ServerConfig config){
        if(guildId == null || guildId.isBlank() || config == null){
            logger.error("Guild ID or Config was not set when trying to get server config. ID: " + guildId);
            return false;
        }
        File file = this.getConfigForServer(guildId);
        if(file == null){
            return false;
        }
        YAMLMapper mapper = new YAMLMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, config);
            logger.debug("Wrote server config for: " + guildId);
            return true;
        } catch (IOException e) {
            logger.error("Error while writing server config: " + guildId + "\n" + e.getMessage());
            return false;
        }
    }
    public BotConfig getBotConfig(){
        File file = new File(this.configPath + this.sep + "config.yml");
        YAMLMapper mapper = new YAMLMapper();
        try {
            return mapper.readValue(file, BotConfig.class);
        } catch (IOException e) {
            SyIO.println("[LOGGER-NOT-INITIALIZED] Error while reading bot config. Make sure the config file is correct. Path: " + this.configPath + "config.yml\n" + e.getMessage());
            return null;
        }
    }

    // Copy template if new server
    private File getConfigForServer(String guildId){
        Path path = Paths.get(this.configPath, "server", guildId + ".yml");
        if(Files.exists(path))
            return path.toFile();
        try {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Path template = Paths.get(this.configPath, "server", "template.yml");
            Files.copy(template, path, StandardCopyOption.REPLACE_EXISTING);
            return path.toFile();
        }catch (IOException e){
            logger.error("Cant create config file for server: " + guildId + "\n" + e.getMessage());
            return null;
        }
    }

    // Config Classes
    public static class ServerConfig {
        public ServerConfig(){}  // Jackson will throw Exception without this
        private String defaultautoplaysong;
        private String defaultvolume;
        private String volumenormalization;
        public String getDefaultautoplaysong() {
            return defaultautoplaysong;
        }
        public String getDefaultvolume() {
            return defaultvolume;
        }
        public String isVolumenormalization() {
            return volumenormalization;
        }
        public void setDefaultautoplaysong(String defaultautoplaysong) {
            this.defaultautoplaysong = defaultautoplaysong.trim();
        }
        public void setDefaultvolume(String defaultvolume) {
            this.defaultvolume = defaultvolume.trim();
        }
        public void setVolumenormalization(String volumenormalization) {
            this.volumenormalization = volumenormalization.trim();
        }
    }
    public static class BotConfig {
        public BotConfig() {}
        private String token;
        private String logPath;
        private String logMode;
        private String serverConfigPath;
        private List<String> adminIds;
        private List<String> activity;
        private String autoPlayerName;
        private String lastFmKey;
        private String lastFmSecret;
        private String publicYtApiUrl;
        private String publicYtApiKey;
        private String safeTrim(String value) {
            if (value == null) {
                SyIO.println("[CONFIG-ERROR] Error while reading bot config. Make sure the config file is correct.");
                System.exit(0);
                return null;
            }
            return value.trim();
        }
        public String getToken() {
            return safeTrim(token);
        }
        public String getLogPath() {
            return safeTrim(logPath);
        }
        public String getLogMode() {
            return safeTrim(logMode);
        }
        public String getServerConfigPath() {
            return safeTrim(serverConfigPath);
        }
        public List<String> getAdminIds() {
            return adminIds;
        }
        public List<String> getActivity() {
            return activity;
        }
        public String getAutoPlayerName() {
            return safeTrim(autoPlayerName);
        }
        public String getLastFmKey() {
            return safeTrim(lastFmKey);
        }
        public String getLastFmSecret() {
            return safeTrim(lastFmSecret);
        }
        public String getPublicYtApiUrl() {
            return safeTrim(publicYtApiUrl);
        }
        public String getPublicYtApiKey() {
            return safeTrim(publicYtApiKey);
        }
    }

}
