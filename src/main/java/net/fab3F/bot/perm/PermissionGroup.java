package net.fab3F.bot.perm;

public enum PermissionGroup {
    // User Permissions
    VOICE_NORMAL("Grundlegende Sprach-Berechtigungen (z.B. Im Sprachkanal reden)"),
    VOICE_ADVANCED("Weiterführende Sprach-Berechtigungen (z.B. Mitglieder in Sprachkanal verschieben)"),
    TEXT_NORMAL("Grundlegende Text-Berechtigungen (z.B. Nachrichten senden)"),
    TEXT_ADVANCED("Weiterführende Text-Berechtigungen (z.B. Nachrichten verwalten)"),
    ADMIN("Administrator-Berechtigungen"),

    // Bot Permissions
    BOT_VOICE(""),
    BOT_TEXT(""),
    BOT_ADMIN("");

    private final String description;

    PermissionGroup(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}