package com.example.chitchat;

public class GlobalVariables {
    private static GlobalVariables globalVariables = null;
    private String username;
    private String ip_address;
    private String port_number;
    private GlobalVariables(){}

    public static synchronized GlobalVariables getInstance(){
        if(null == globalVariables) {
            globalVariables = new GlobalVariables();
        }
        return globalVariables;
    }

    public static GlobalVariables getGlobalVariables() {
        return globalVariables;
    }

    public static void setGlobalVariables(GlobalVariables globalVariables) {
        GlobalVariables.globalVariables = globalVariables;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public String getPort_number() {
        return port_number;
    }

    public void setPort_number(String port_number) {
        this.port_number = port_number;
    }
}
