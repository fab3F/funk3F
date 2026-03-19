package net.fab3F.bot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.fab3F.bot.ServerCommand;
import net.fab3F.bot.perm.PermissionGroup;
import net.fab3F.ConfigWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Date;
import java.util.List;

public class ConfigCmd implements ServerCommand {
    private final Logger logger = LoggerFactory.getLogger(ConfigCmd.class);
    private final ConfigWorker cw;
    public ConfigCmd(ConfigWorker cw){
        this.cw = cw;
    }

    @Override
    public String cmdName() {
        return "config";
    }

    @Override
    public boolean peformCommand(SlashCommandInteractionEvent event) {

        event.deferReply().queue();
        List<OptionMapping> options = event.getOptions();
        String id = event.getGuild().getId();

        if(!options.isEmpty()){
            ConfigWorker.ServerConfig serverConfig = cw.getServerConfig(id);
            ConfigWorker.ServerConfig templateConfig = null;
            if(serverConfig == null){
                logger.error("25: Fehler beim Einlesen der Server Config während /config für: '{}'", id);
                event.getHook().sendMessage("Fehler beim Einlesen der Server Config").queue();
                return true;
            }

            for (OptionMapping optionMapping : options) {
                String optionName = optionMapping.getName();
                String optionValue = optionMapping.getAsString();

                if (optionValue.isBlank()) {
                    handleOptionError(optionName, optionValue, id, event);
                    return true;
                }

                if ("-1".equals(optionValue)) {
                    if (templateConfig == null) {
                        templateConfig = cw.getServerConfig("template");
                    }
                    optionValue = templateConfig.get(optionName);
                }

                if (!serverConfig.set(optionName, optionValue)) {
                    handleOptionError(optionName, optionValue, id, event);
                    return true;
                }
            }

            if(!cw.writeServerConfig(id, serverConfig)){
                logger.error("26: Fehler beim Schreiben der Konfiguration für: {}", id);
                event.getHook().sendMessage("Beim Schreiben der neuen Konfiguration ist ein Fehler aufgetreten.").queue();
                return true;
            }

        }

        event.getHook().sendMessageEmbeds(printCurrent(id)).queue();
        return true;
    }

    private void handleOptionError(String optionName, String optionValue, String serverId, SlashCommandInteractionEvent event) {
        String msg = String.format("Fehler beim Ändern von '%s' zu '%s' für den Server '%s'", optionName, optionValue, serverId);
        logger.debug(msg);
        event.getHook().sendMessage("Fehler beim Ändern folgender Einstellung (Es wurde nichts geändert): " + optionName).queue();
        event.getChannel().sendMessageEmbeds(printCurrent(serverId)).queue();
    }

    private MessageEmbed printCurrent(String guildId){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Konfiguration");
        eb.setDescription("Dies sind die aktuellen Einstellungen des Bots für diesen Server:");
        eb.setColor(Color.CYAN);
        ConfigWorker.ServerConfig serverConfig = cw.getServerConfig(guildId);
        for(Option option : this.getOptions()){
            eb.addField(option.name, serverConfig.get(option.name), false);
        }
        eb.setFooter("Befehl '/config'");
        eb.setTimestamp(new Date().toInstant());
        return eb.build();
    }

    @Override
    public boolean isOnlyForServer() {
        return true;
    }

    @Override
    public PermissionGroup getUserPermission() {
        return PermissionGroup.ADMIN;
    }

    @Override
    public PermissionGroup getBotPermission() {
        return PermissionGroup.BOT_TEXT;
    }

    @Override
    public String getFurtherUsage() {
        return """
                Verwende diesen Befehl nur, wenn du weißt, was du tust!
                Ändere die Konfiguration des Bots wie folgt:
                1) Wähle mindestens eine Option zum Ändern aus.
                2) Gib einen neuen Wert für die ausgewählte Option ein.
                3) Gib den Wert '-1' ein, um den Standard wiederherzustellen.""";
    }

    @Override
    public String getDescription() {
        return "Ändere die Einstellungen des Bots. Verwende diesen Befehl nur, wenn du weißt, was du tust!";
    }

    @Override
    public Option[] getOptions() {
        return new Option[]{
                new Option(OptionType.BOOLEAN, "default_autoplay", "Autoplay standardmäßig ein- oder ausgeschaltet", false),
                new Option(OptionType.STRING, "default_autoplay_song", "Standardmäßger Song für Autoplay", false),
                new Option(OptionType.INTEGER, "default_volume", "Standardmäßige Lautstärke des Bots (Wert zwischen 0 und 100, Empfohlen: 50)", false),
                new Option(OptionType.BOOLEAN, "volume_normalization", "Automatische Normalisierung der Lautstärke", false)
        };
    }
}
