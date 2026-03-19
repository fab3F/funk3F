package net.fab3F.bot.perm;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.fab3F.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


public class PermissionWorker {
    private final Logger logger = LoggerFactory.getLogger(PermissionWorker.class);

    public String hasPermission(Member member, PermissionGroup permissionGroup) {
        Set<Permission> requiredPermissions = PermissionConfig.PERMISSIONS_MAP.get(permissionGroup);
        if (requiredPermissions == null) {
            logger.error("11: requiredPermissions is null for this botPermission: {}", permissionGroup.name());
            return "_FALSE_Error 11: Die erforderlichen Berechtigungen existieren nicht. Bitte versuche es zu einem späteren Zeitpunkt erneut.";
        }

        StringBuilder sb = new StringBuilder();
        for (Permission permission : requiredPermissions.toArray(new Permission[0])) {
            if (!member.hasPermission(permission)) {
                sb.append(permission).append(", ");
            }
        }
        return sb.toString().isEmpty() ? "_TRUE_" : "_FALSE_" + Main.replaceLast(sb.toString(), ", ", "");
    }

}
