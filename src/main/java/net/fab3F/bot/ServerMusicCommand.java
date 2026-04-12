package net.fab3F.bot;

public abstract class ServerMusicCommand implements ServerCommand {

    protected final net.fab3F.bot.music.MusicHandler musicHandler;

    public ServerMusicCommand(net.fab3F.bot.music.MusicHandler musicHandler) {
        this.musicHandler = musicHandler;
    }

    // Override normal cmds
    @Override
    public boolean isOnlyForServer(){
        return true;
    }

    @Override
    public net.fab3F.bot.perm.PermissionGroup getBotPermission(){
        return net.fab3F.bot.perm.PermissionGroup.BOT_VOICE;
    }

    @Override
    public String getDescription() {
        return cmdName() + "-Musikbefehl";
    }
}