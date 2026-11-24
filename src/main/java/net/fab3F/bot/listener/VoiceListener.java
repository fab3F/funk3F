package net.fab3F.bot.listener;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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
        Guild guild = e.getGuild();
        Member self = guild.getSelfMember();

        // Bot left channel (disconnected/kicked)
        if (e.getMember().getIdLong() == self.getIdLong()) {
            if (e.getChannelJoined() == null) {
                musicHandler.stopGuild(guild);
            }
            return;
        }

        // User left channel (check if only bots are left)
        VoiceChannel botChannel = VoiceHelper.inVoiceChannel(self);
        if (botChannel != null
                && e.getChannelLeft() != null
                && e.getChannelLeft().getIdLong() == botChannel.getIdLong()
                && botChannel.getMembers().stream().allMatch(m -> m.getUser().isBot())) {
            musicHandler.stopGuild(guild);
        }
    }
}
