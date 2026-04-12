package net.fab3F.bot.music;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class VoiceHelper {

    // Returns true if in same voice channel or if bot joined the channel (only does that if doJoin is true)
    public static boolean joinHelper(Member member, Member bot, JDA jda, boolean doJoin){
        VoiceChannel memberChannel = inVoiceChannel(member);
        if(memberChannel == null){
            return false;
        }
        VoiceChannel botChannel = inVoiceChannel(bot);
        if(botChannel != null){ // Bot is in voice channel
            return memberChannel.getId().equals(botChannel.getId()); // return false if voice channels are not equal
        } else {
            if(doJoin){
                jda.getDirectAudioController().connect(memberChannel);
                return true;
            } else {
                return false;
            }

        }
    }

    public static VoiceChannel inVoiceChannel(Member member){
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        if(memberVoiceState == null || memberVoiceState.getChannel() == null)
            return null;
        return memberVoiceState.getChannel().asVoiceChannel();
    }

}
