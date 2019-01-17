package com.aigushou.entity;

/**
 * 一块数据
 *
 * @author jiezhang
 */
public class Area {

    public Area() {
    }

    public Area(Node rateNode, Node timeNode) {
        this.rateNode = rateNode;
        this.timeNode = timeNode;
    }

    /**
     * 收益率区域
     */
    private Node rateNode;

    /**
     * 时间区域
     */
    private Node timeNode;

    public Node getRateNode() {
        return rateNode;
    }

    public void setRateNode(Node rateNode) {
        this.rateNode = rateNode;
    }

    public Node getTimeNode() {
        return timeNode;
    }

    public void setTimeNode(Node timeNode) {
        this.timeNode = timeNode;
    }

    @Override
    public String toString() {
        return "Area{" +
                "rateNode=" + rateNode +
                ", timeNode=" + timeNode +
                '}';
    }
}
