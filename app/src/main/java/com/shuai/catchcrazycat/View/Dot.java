package com.shuai.catchcrazycat.View;


/**
 * 点的抽象
 */

public class Dot {

    private int x, y;//坐标位置
    private STATUS status;//定义三种状态,建议用枚举

    public enum STATUS {STATUS_OFF, STATUS_IN, STATUS_ON}//定义三种状态,建议用枚举

    public Dot(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

}
