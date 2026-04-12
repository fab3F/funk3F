package net.fab3F.bot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.fab3F.bot.ServerMusicCommand;
import net.fab3F.bot.music.MusicHandler;
import net.fab3F.bot.music.VoiceHelper;
import net.fab3F.bot.perm.PermissionGroup;

public class SkipMusicCmd extends ServerMusicCommand {
    public SkipMusicCmd(MusicHandler musicHandler) {
        super(musicHandler);
    }

    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        // check vc
        if(!VoiceHelper.joinHelper(e.getMember(), e.getGuild().getSelfMember(), e.getJDA(), true)){
            return false;
        }

        if(this.musicHandler.getOrCreateGuildMusicHandler(e.getGuild().getIdLong()).getScheduler().skip()){
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
