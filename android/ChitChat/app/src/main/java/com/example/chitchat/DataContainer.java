package com.example.chitchat;

import android.provider.ContactsContract;

import com.example.chitchat.UserNode.Android_User_Node;

public class DataContainer {
    private static DataContainer instance = null;

    private Android_User_Node androidUserNode;

    public void setAndroidUserNode(Android_User_Node androidUserNode) {
        this.androidUserNode = androidUserNode;
    }

    private DataContainer(){};

    public static DataContainer getInstance(){
        return instance != null ? instance: (instance = new DataContainer());
    }

    public Android_User_Node getAndroidUserNode() {
        return androidUserNode;
    }
}
