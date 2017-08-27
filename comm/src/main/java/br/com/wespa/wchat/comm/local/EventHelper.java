package br.com.wespa.wchat.comm.local;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by dsouza on 24/08/17.
 */

public class EventHelper extends BroadcastReceiver {

    private final HashMap<String, Method> mMap;
    private final IntentFilter mFilter;
    private final Object mListener;
    private Context mContext;

    public EventHelper(Context c, Object listener) {
        mFilter = new IntentFilter();
        mMap = new HashMap<>();
        mListener = listener;
        mContext = c;
    }

    public EventHelper map(String action, String methodName) {
        try {
            Method method = mListener.getClass().getDeclaredMethod(methodName, Bundle.class);
            method.setAccessible(true);
            mFilter.addAction(action);
            mMap.put(action, method);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return this;
    }

    public EventHelper register() {
        mContext.registerReceiver(this, mFilter);
        return this;
    }

    public EventHelper unregister() {
        mContext.unregisterReceiver(this);
        return this;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String key = intent.getAction();

            if (!mMap.containsKey(key))
                return;

            mMap.get(key).invoke(mListener, intent.getExtras());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public LocalEvent newEvent(String action) {
        return new LocalEvent(mContext, action);
    }

}

