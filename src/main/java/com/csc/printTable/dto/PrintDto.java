package com.csc.printTable.dto;

import org.apache.commons.lang3.StringUtils;

public class PrintDto {
    private String userName;
    private String deptName;

    private String deptName2;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDeptName2() {
        return deptName2;
    }

    public String getDeptNameTemp() {
        return StringUtils.isNotBlank(deptName2) ? deptName + "  " + deptName2 : deptName;
    }

    public void setDeptName2(String deptName2) {
        this.deptName2 = deptName2;
    }

}
