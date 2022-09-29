package com.bcld.domain.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TreeNode implements Serializable {

    private static final long serialVersionUID = 742991312368802746L;

    private String id;
    private TreeNode parent;
    private String name;
    private String description;
    private Long orderid;

    /**
     * 下边是规则用到的参数
     */
    private Integer level;
    private String plan;

    private List<TreeNode> children = new ArrayList<TreeNode>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        if (null != this.getParent()) {
            this.getParent().removeChild(this);
        }
        this.parent = parent;
        this.getParent().addChild(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public void addChild(TreeNode treeNode) {
        if (null != treeNode.getParent()) {
            treeNode.getParent().removeChild(treeNode);
        }
        treeNode.parent = this;
        this.getChildren().add(treeNode);
    }

    public void removeChild(TreeNode treeNode) {
        treeNode.parent = null;
        this.getChildren().remove(treeNode);
    }

    /*
     * 下边是规则用到的参数
     */
    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

}
