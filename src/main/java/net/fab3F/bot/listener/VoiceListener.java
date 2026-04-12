package net.fab3F.bot.listener;

import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.fab3F.bot.music.MusicHandler;
import net.fab3F.bot.music.VoiceHelper;

public class VoiceListener extends ListenerAdapter {

    private final MusicHandler musicHandler;

    public VoiceListener(MusicHandler mH){
        this.musicHandler = mH;
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent e) {
        VoiceChannel vc = VoiceHelper.inVoiceChannel(e.getGuild().getSelfMember());
        // if bot is not in voice channel
        if(vc == null) {
            this.musicHandler.stopGuild(e.getGuild());
        // if bot is in voice channel
        } else {
            // if all members are bots -> stop music
            if (vc.getMembers().stream().allMatch(m -> m.getUser().isBot())) {
                this.musicHandler.stopGuild(e.getGuild());
            }
        }
    }

}
