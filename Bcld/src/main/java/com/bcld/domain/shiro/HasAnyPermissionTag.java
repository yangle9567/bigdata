package com.bcld.domain.shiro;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.tags.PermissionTag;

public class HasAnyPermissionTag extends PermissionTag {

    private static final String PERMISSION_NAMES_DELIMETER = ",";
    
    @Override
    protected boolean showTagBody(String permissionNames) {
        boolean hasAnyPermission = false;

        Subject subject = getSubject();

        if (subject != null) {

            for (String permission : permissionNames.split(PERMISSION_NAMES_DELIMETER)) {

                if (isPermitted(permission.trim())) {
                    hasAnyPermission = true;
                    break;
                }

            }

        }

        return hasAnyPermission;
    }

}
