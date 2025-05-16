package net.fab3F.bot.perm;

import net.dv8tion.jda.api.Permission;

import java.util.*;

public class PermissionConfig {

    public static final Map<PermissionGroup, Set<Permission>> PERMISSIONS_MAP;
    private static final Permission[] standardPermissions = {
            Permission.VIEW_CHANNEL,
            Permission.MESSAGE_MANAGE,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_HISTORY,
            Permission.MESSAGE_ADD_REACTION,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_MENTION_EVERYONE,
            Permission.MESSAGE_EMBED_LINKS,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_EXT_STICKER,
            Permission.MESSAGE_SEND_IN_THREADS
    };

    static {
        PERMISSIONS_MAP = new EnumMap<>(PermissionGroup.class);
        set(PermissionGroup.VOICE_NORMAL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK);
        set(PermissionGroup.VOICE_ADVANCED, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_MOVE_OTHERS);
        set(PermissionGroup.TEXT_NORMAL, Permission.MESSAGE_SEND);
        set(PermissionGroup.TEXT_ADVANCED, Permission.MESSAGE_MANAGE);
        set(PermissionGroup.ADMIN, Permission.ADMINISTRATOR);

        set(PermissionGroup.BOT_TEXT, standardPermissionsPlus());
        set(PermissionGroup.BOT_VOICE, standardPermissionsPlus(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK));
        set(PermissionGroup.BOT_ADMIN, Permission.ADMINISTRATOR);
    }

    private static void set(PermissionGroup botPermission, Permission... permissions) {
        PERMISSIONS_MAP.put(botPermission, EnumSet.copyOf(Arrays.asList(permissions)));
    }
    private static Permission[] standardPermissionsPlus(Permission... permissions) {
        Permission[] result = new Permission[permissions.length + standardPermissions.length];
        System.arraycopy(permissions, 0, result, 0, permissions.length);
        System.arraycopy(standardPermissions, 0, result, permissions.length, standardPermissions.length);
        return result;
    }


}
