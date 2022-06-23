package com.example.chitchat.Tools;

import java.util.Comparator;

public class SortMessages implements Comparator<Value> {
    @Override
    public int compare(Value value1, Value value2) {
        return value1.getDateCreated().compareTo(value2.getDateCreated());
    }
}
