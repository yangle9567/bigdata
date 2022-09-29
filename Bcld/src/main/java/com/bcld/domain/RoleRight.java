package com.bcld.domain;

import java.io.Serializable;

public class RoleRight implements Serializable {

    private static final long serialVersionUID = -2789991677894636739L;

    private String id;

    private String roleId;

    private String rightId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRightId() {
        return rightId;
    }

    public void setRightId(String rightId) {
        this.rightId = rightId;
    }
}