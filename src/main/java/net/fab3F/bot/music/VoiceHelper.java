package net.fab3F.bot.music;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class VoiceHelper {

    public static void stopMusicInGuild(Guild g) {
        // check if there is überhaupt music running in this guild
        try {
            //Bot.instance.getPM().stopGuildMusicManager(g.getId());
        } catch (NullPointerException ignored){}
    }

    public static boolean joinHelper(Member member, Member bot, JDA jda){
        VoiceChannel memberChannel = inVoiceChannel(member);
        if(memberChannel == null){
            return false;
        }
        VoiceChannel botChannel = inVoiceChannel(bot);
        if(botChannel != null){ // Bot is in voice channel
            return memberChannel.getId().equals(botChannel.getId()); // return false if voice channels are not equal
        } else {
            jda.getDirectAudioController().connect(memberChannel);
            return true;
        }
    }

    public static VoiceChannel inVoiceChannel(Member member){
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        if(memberVoiceState == null || memberVoiceState.getChannel() == null)
            return null;
        return memberVoiceState.getChannel().asVoiceChannel();
    }

}
