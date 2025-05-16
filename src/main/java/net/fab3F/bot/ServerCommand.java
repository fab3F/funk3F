package net.fab3F.bot;

public interface ServerCommand {

    default String cmdName(){
        return "{cmdName}";
    }

    boolean peformCommand(net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent event);
    boolean isOnlyForServer();
    net.fab3F.bot.perm.PermissionGroup getUserPermission();

    net.fab3F.bot.perm.PermissionGroup getBotPermission();

    default String getFurtherUsage(){
        return null;
    }

    default String getDescription() {return cmdName() + "-Befehl";}

    default Option[] getOptions(){
        return null;
    }


    class Option{
        public net.dv8tion.jda.api.interactions.commands.OptionType type;
        public String name;
        public String description;
        public boolean required;
        public Option(net.dv8tion.jda.api.interactions.commands.OptionType type, String name, String description, boolean required){
            this.type = type;
            this.name = name;
            this.description = description;
            this.required = required;
        }
    }


}

