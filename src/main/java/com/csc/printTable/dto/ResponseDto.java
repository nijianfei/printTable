package com.csc.printTable.dto;


public class ResponseDto {
    private String invokeCls = "70";
    private String ngMessage;

    public ResponseDto() {
    }

    public ResponseDto(String invokeCls, String ngMessage) {

        this.invokeCls = invokeCls;
        this.ngMessage = ngMessage;
    }


    public String getInvokeCls() {
        return invokeCls;
    }

    public void setInvokeCls(String invokeCls) {
        this.invokeCls = invokeCls;
    }

    public String getNgMessage() {
        return ngMessage;
    }

    public void setNgMessage(String ngMessage) {
        this.ngMessage = ngMessage;
    }

    public static ResponseDto success() {
        return new ResponseDto( "70", null);
    }

    public static ResponseDto fail(String message) {
        return new ResponseDto( "90", message);
    }
}
