package net.fab3F.bot.commands.music;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.fab3F.bot.ServerMusicCommand;
import net.fab3F.bot.music.MusicHandler;
import net.fab3F.bot.music.TrackScheduler;
import net.fab3F.bot.music.VoiceHelper;
import net.fab3F.bot.perm.PermissionGroup;

public class AutoplayMusicCmd extends ServerMusicCommand {


    public AutoplayMusicCmd(MusicHandler musicHandler) {
        super(musicHandler);
    }

    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(e.getGuild() == null){
            return false;
        }

        Guild g = e.getGuild();

        // Join VC (or not)
        if(!VoiceHelper.joinHelper(e.getMember(), g.getSelfMember(), e.getJDA(), true)){
            return false;
        }
        TrackScheduler scheduler = musicHandler.getOrCreateGuildMusicHandler(g.getIdLong()).getScheduler();

        if(scheduler.isAutoplay()){
            scheduler.stopAutoplay();
            e.reply("Autoplay wurde deaktiviert.").queue();
            return true;
        }

        scheduler.setAutoplay(true);
        scheduler.startAutoplay(e.getChannel().asTextChannel().getIdLong());
        e.reply("Autoplay wurde aktiviert. Nachdem die Wiedergabeliste abgespielt wurde, werden empfohlene Songs abgespielt.").queue();
        return true;

    }

    @Override
    public PermissionGroup getUserPermission() {
        return PermissionGroup.VOICE_ADVANCED;
    }

    @Override
    public String getFurtherUsage() {
        return "Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden, falls der Bot bereits in einem Sprachkanal ist.\n" +
                "Es können YouTube-Link, Spotify-Link sowie beliebige Suchbegriffe verwendet werden.";
    }

    @Override
    public String getDescription() {
        return "Spiele automatisch weitere Songs ab, sobald die Wiedergabeliste leer ist oder höre aktuelle Charts!";
    }
}
