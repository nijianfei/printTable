package com.csc.printTable.dto;

import java.awt.*;

public class Block {
    private Integer x;

    private Integer y;
    /**
     * 字体
     */
    private String font;
    /**
     * 字体大小
     */
    private Integer size;
    //加粗
    private Integer style;
    /**
     * 字体间距
     */
    private Float space;

    /**
     * 颜色
     */
    private String color;

    private Integer width;
    private Integer height;

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getStyle() {
        return style;
    }

    public void setStyle(Integer style) {
        this.style = style;
    }

    public Float getSpace() {
        return space;
    }

    public void setSpace(Float space) {
        this.space = space;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Color getColorBean(){
        String[] split = color.split(",");
        return new Color(parseInt(split[0]), parseInt(split[1]), parseInt(split[2]));
    }
    private Integer parseInt(String str){
        return Integer.parseInt(str.trim());
    }
    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

}
