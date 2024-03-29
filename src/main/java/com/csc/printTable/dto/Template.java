package com.csc.printTable.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Template {

    /**
     * 页边距（毫米）
     */
    private int margin;

    /**
     * 页面宽度（毫米）
     */
    private int pageWidth;
    /**
     * 页面高度（毫米）
     */
    private int pageHeight;

    /**
     * 模板宽度（毫米）
     */
    private int templateWidth;
    /**
     * 模板高度（毫米）
     */
    private int templateHeight;


    /**
     * 人员姓名
     */
    private Block userName;
    /**
     * 部门名称
     */
    private Block deptName;

    /**
     * 部门名称
     */
    private Block deptName2;

    private Block topSingle;
    private Block topDouble;
    private Block bottomSingle;

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public void setPageWidth(int pageWidth) {
        this.pageWidth = pageWidth;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public void setPageHeight(int pageHeight) {
        this.pageHeight = pageHeight;
    }

    public int getTemplateWidth() {
        return templateWidth;
    }

    public void setTemplateWidth(int templateWidth) {
        this.templateWidth = templateWidth;
    }

    public int getTemplateHeight() {
        return templateHeight;
    }

    public void setTemplateHeight(int templateHeight) {
        this.templateHeight = templateHeight;
    }

    public Block getUserName() {
        return userName;
    }

    public void setUserName(Block userName) {
        this.userName = userName;
    }

    public Block getDeptName() {
        return deptName;
    }

    public void setDeptName(Block deptName) {
        this.deptName = deptName;
    }

    public Block getDeptName2() {
        return deptName2;
    }

    public void setDeptName2(Block deptName2) {
        this.deptName2 = deptName2;
    }

    public Block getTopSingle() {
        return topSingle;
    }

    public void setTopSingle(Block topSingle) {
        this.topSingle = topSingle;
    }

    public Block getTopDouble() {
        return topDouble;
    }

    public void setTopDouble(Block topDouble) {
        this.topDouble = topDouble;
    }

    public Block getBottomSingle() {
        return bottomSingle;
    }

    public void setBottomSingle(Block bottomSingle) {
        this.bottomSingle = bottomSingle;
    }

    public double getPageWidthPoint(){
        if (this.pageWidth != 0) {
            return BigDecimal.valueOf(this.pageWidth).divide(BigDecimal.valueOf(4.1667),2, RoundingMode.HALF_UP).doubleValue();
        }
        return BigDecimal.valueOf(0).doubleValue();
    }

    public double getPageHeightPoint(){
        if (this.pageHeight != 0) {
            return BigDecimal.valueOf(this.pageHeight).divide(BigDecimal.valueOf(4.1667),2, RoundingMode.HALF_UP).doubleValue();
        }
        return BigDecimal.valueOf(0).doubleValue();
    }
}
