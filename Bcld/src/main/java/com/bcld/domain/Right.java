package com.bcld.domain;

import java.io.Serializable;

public class Right implements Serializable {

    private static final long serialVersionUID = 2518266570447768164L;

    private String id;

    private String name;

    private String code;

    private String rightGroupId;

    private String description;

    private Long orderid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRightGroupId() {
        return rightGroupId;
    }

    public void setRightGroupId(String rightGroupId) {
        this.rightGroupId = rightGroupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }
}