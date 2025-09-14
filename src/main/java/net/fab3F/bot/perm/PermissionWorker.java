package net.fab3F.bot.perm;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.fab3F.Main;
import net.fab3F.customTools.SyIO;

import java.util.Set;

public class PermissionWorker {

    public String hasPermission(Member member, PermissionGroup permissionGroup) {
        Set<Permission> requiredPermissions = PermissionConfig.PERMISSIONS_MAP.get(permissionGroup);
        if (requiredPermissions == null) {
            Main.logger.error("Error 12: requiredPermissions is null for this botPermission: " + permissionGroup.name());
            return "_FALSE_Error 12: Die erforderlichen Berechtigungen existieren nicht. Bitte versuche es zu einem späteren Zeitpunkt erneut.";
        }

        StringBuilder sb = new StringBuilder();
        for (Permission permission : requiredPermissions.toArray(new Permission[0])) {
            if (!member.hasPermission(permission)) {
                sb.append(permission).append(", ");
            }
        }
        return sb.toString().isEmpty() ? "_TRUE_" : "_FALSE_" + SyIO.replaceLast(sb.toString(), ", ", "");
    }

}
