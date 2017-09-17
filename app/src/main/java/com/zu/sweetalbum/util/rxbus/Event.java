package com.zu.sweetalbum.util.rxbus;

/**
 * Created by zu on 17-6-12.
 */

public class Event {

    public Object content = null;

    public String action = null;


    public Event(String action, Object content) {
        this.content = content;
        this.action = action;
    }





}
