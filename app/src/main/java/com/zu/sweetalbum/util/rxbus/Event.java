package com.zu.sweetalbum.util.rxbus;

import java.util.HashMap;

/**
 * Created by zu on 17-6-12.
 */

public class Event {

    public Object content = null;

    public String action = null;

    private HashMap<String, Object> extras = new HashMap<>();


    public Event(String action, Object content) {
        this.content = content;
        this.action = action;
    }

    public void putExtra(String key, Object value)
    {
        extras.put(key, value);
    }

    public Object getExtra(String key)
    {
        return extras.get(key);
    }





}
