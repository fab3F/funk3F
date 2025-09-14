package net.fab3F.bot.music;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public record CustomTrackData(String url, String user, TextChannel channel) {
}
