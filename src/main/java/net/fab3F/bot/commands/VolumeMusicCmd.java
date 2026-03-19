package net.fab3F.bot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.fab3F.bot.ServerCommand;
import net.fab3F.bot.music.MusicHandler;
import net.fab3F.bot.music.TrackScheduler;
import net.fab3F.bot.music.VoiceHelper;
import net.fab3F.bot.perm.PermissionGroup;

public class VolumeMusicCmd implements ServerCommand {
    private final MusicHandler musicHandler;

    public VolumeMusicCmd(MusicHandler mH){
        this.musicHandler = mH;
    }

    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {

        if(!VoiceHelper.joinHelper(e.getMember(), e.getGuild().getSelfMember(), e.getJDA(), false)){
            return false;
        }

        TrackScheduler scheduler = musicHandler.getOrCreateTrackScheduler(e.getGuild().getIdLong());

        if(e.getOption("value") == null){
            e.reply("Die aktuelle Lautstärke beträgt " + scheduler.getVolume() + "%.").queue();
            return true;
        }

        int newVolume = Math.min(Math.max(e.getOption("value").getAsInt(), 0), 100);

        scheduler.setVolume(newVolume);

        e.reply("Die Lautstärke wurde zu " + newVolume + "% geändert. Maximalwert: 100%. Falls die Lautstärkenormalisierung verwendet wird, ändert sich die Lautstärke beim nächsten Song wieder.").queue();
        return true;
    }

    @Override
    public boolean isOnlyForServer() {
        return true;
    }

    @Override
    public PermissionGroup getUserPermission() {
        return PermissionGroup.VOICE_NORMAL;
    }

    @Override
    public PermissionGroup getBotPermission() {
        return PermissionGroup.BOT_VOICE;
    }

    @Override
    public String getFurtherUsage() {
        return "Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.\n" +
                "Der Wert muss zwischen 0 und 100 liegen.";
    }

    @Override
    public String getDescription() {
        return "Ändere oder erhalte allgemeine Lautstärke des Bots";
    }

    @Override
    public Option[] getOptions() {
        return new Option[]{new Option(OptionType.INTEGER, "value", "Wert zwischen 0 und 100 (Prozent)", false)};
    }
}
