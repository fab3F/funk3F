package net.fab3F.bot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.fab3F.ConfigWorker;
import net.fab3F.bot.ServerMusicCommand;
import net.fab3F.bot.music.GuildMusicHandler;
import net.fab3F.bot.music.MusicHandler;
import net.fab3F.bot.music.VoiceHelper;
import net.fab3F.bot.perm.PermissionGroup;
import org.slf4j.LoggerFactory;

public class SkipMusicCmd extends ServerMusicCommand {
    public SkipMusicCmd(MusicHandler musicHandler) {
        super(musicHandler);
    }

    @Override
    public boolean performCommand(SlashCommandInteractionEvent e) {
        // check vc
        if(!VoiceHelper.joinHelper(e.getMember(), e.getGuild().getSelfMember(), e.getJDA(), false)){
            return false;
        }

        GuildMusicHandler handler = this.musicHandler.getGuildMusicHandlerIfPresent(e.getGuild().getIdLong());
        if(handler == null) {
            LoggerFactory.getLogger(ConfigWorker.class).info("Skip-Command cancelled, Bot was in Channel but there was no GuildMusicHandler assigned!");
            e.reply("Es ist ein interner Fehler aufgetreten.").setEphemeral(true).queue();
            return true;
        }

        if(handler.getScheduler().skip()){
            e.reply("Aktueller Song wurde übersprungen.").queue();
            return true;
        }
        return false;
    }

    @Override
    public PermissionGroup getUserPermission() {
        return PermissionGroup.VOICE_ADVANCED;
    }

    @Override
    public String getFurtherUsage() {
        return "Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.\n" +
                "Es muss gerade ein Song abgespielt werden.";
    }

    @Override
    public String getDescription() {
        return "Überspringt den aktuellen Song";
    }

}
